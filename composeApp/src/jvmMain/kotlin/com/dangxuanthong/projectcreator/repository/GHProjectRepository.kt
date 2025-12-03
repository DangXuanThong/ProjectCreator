package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.api.GitHubApiService
import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.forEachLine
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.visitFileTree
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import org.koin.core.annotation.Single

@Single
class GHProjectRepository(private val apiService: GitHubApiService) : ProjectRepository {
    override suspend fun saveProject(path: String): Result<DownloadInfo> =
        apiService.downloadProject(File(path))

    override suspend fun verifyProject(path: String): Result<Unit> {
        val root = File(path)
        check(root.exists() && root.isDirectory) { "Project folder does not exist" }

        val shaTree = apiService.getShaForProject()
        if (shaTree is Result.Error) return Result.Error(shaTree.exception)

        val items = (shaTree as Result.Success).data.tree.filter { it.type == "blob" }
        items.forEach {
            val file = File(root, it.path)
            if (!file.exists()) return Result.Error(
                FileNotFoundException("Missing file ${file.absolutePath}")
            )
            val hash = getHashValue(file)
            if (hash is Result.Error) return Result.Error(hash.exception)
            if ((hash as Result.Success).data != it.sha) return Result.Error(
                Exception("Checksum failed for file ${file.name}")
            )
        }
        return Result.Success(Unit)
    }

    override suspend fun getHashValue(file: File): Result<String> = try {
        with(file) {
            val header = "blob ${length()}\u0000".toByteArray(Charsets.UTF_8)
            val md = MessageDigest.getInstance("SHA-1")
            md.update(header)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            inputStream().use { stream ->
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            val hashBytes = md.digest()
            hashBytes.joinToString("") { "%02x".format(it) }
                .let { Result.Success(it) }
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun renameProject(path: String, newName: String): Result<Unit> = try {
        val root = File(path)
        // Change project name in gradle settings file
        val file = File(root, "settings.gradle.kts")
        file.mapLine {
            if (!it.contains("rootProject.name")) it
            else it.replaceAfter("= ", "\"$newName\"")
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.Error(e)
    }

    override suspend fun renamePackage(path: Path, newPackage: String) = runCatchIOExceptions {
        val oldPath = path.resolve("app/src/main/kotlin/com/dangxuanthong/sampleapp")
        val newPath = path.resolve("app/src/main/kotlin/${newPackage.replace(".", "/")}")
        // Create new directory
        newPath.createDirectories()
        // Move files to new package
        oldPath.forEachDirectoryEntry { it.moveTo(newPath / it.name) }
        // Delete old package
        var oldDir = oldPath
        while (oldDir.listDirectoryEntries().isEmpty()) {
            oldDir.deleteExisting()
            oldDir = oldDir.parent
        }
        // Change package of .kt files
        newPath.visitFileTree {
            onVisitFile { file, _ ->
                if (file.extension != "kt") return@onVisitFile FileVisitResult.CONTINUE
                file.mapLine {
                    if (it.startsWith("package")) "package $newPackage"
                    else it
                }
                FileVisitResult.CONTINUE
            }
        }

        Result.Success(Unit)
    }

    private suspend inline fun <T> runCatchIOExceptions(
        crossinline block: suspend () -> Result<T>
    ) = try {
        withContext(Dispatchers.IO) { block() }
    } catch (e: IOException) {
        Result.Error(e)
    }

    private inline fun File.mapLine(
        crossinline transform: (String) -> String
    ) {
        val tempFile = File.createTempFile(nameWithoutExtension, null)
        tempFile.bufferedWriter().use { writer ->
            this.forEachLine {
                writer.write(transform(it))
                writer.newLine()
            }
        }
        Files.move(tempFile.toPath(), this.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private inline fun Path.mapLine(transform: (String) -> String) {
        createTempFile(nameWithoutExtension).run {
            bufferedWriter().use { writer ->
                this@mapLine.forEachLine {
                    writer.write(transform(it))
                    writer.newLine()
                }
            }
            moveTo(this@mapLine, overwrite = true)
        }
    }
}

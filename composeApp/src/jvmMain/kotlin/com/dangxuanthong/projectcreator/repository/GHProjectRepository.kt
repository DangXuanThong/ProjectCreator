package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.api.GitHubApiService
import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.forEachLine
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.visitFileTree
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
import org.koin.core.annotation.Single

@Single
class GHProjectRepository(private val apiService: GitHubApiService) : ProjectRepository {
    override suspend fun saveProject(path: Path): Result<DownloadInfo> =
        apiService.downloadProject(path)

    override suspend fun verifyProject(path: Path): Result<Unit> {
        check(path.exists() && path.isDirectory()) { "Project folder does not exist" }

        val shaTree = apiService.getShaForProject()
        if (shaTree is Result.Error) return Result.Error(shaTree.exception)

        val items = (shaTree as Result.Success).data.tree.filter { it.type == "blob" }
        items.forEach {
            val file = path / it.path
            if (file.notExists()) return Result.Error(
                FileNotFoundException("Missing file ${file.absolutePathString()}")
            )
            val hash = getHashValue(file)
            if (hash is Result.Error) return Result.Error(hash.exception)
            if ((hash as Result.Success).data != it.sha) return Result.Error(
                Exception("Checksum failed for file ${file.name}")
            )
        }
        return Result.Success(Unit)
    }

    override suspend fun getHashValue(file: Path): Result<String> = try {
        with(file) {
            val header = "blob ${fileSize()}\u0000".toByteArray(Charsets.UTF_8)
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
        if (e is CancellationException) throw e
        Result.Error(e)
    }

    override suspend fun renameProject(path: Path, newName: String): Result<Unit> = try {
        // Change project name in gradle settings file
        path.resolve("settings.gradle.kts").mapLine {
            if (!it.contains("rootProject.name")) it
            else it.replaceAfter("= ", "\"$newName\"")
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.Error(e)
    }

    override suspend fun renamePackage(path: Path, newPackage: String) = runCatchIOExceptions {
        moveSourceSet(path, newPackage, "main")
        moveSourceSet(path, newPackage, "test")
        moveSourceSet(path, newPackage, "androidTest")
        // Change android.namespace and applicationId in build.gradle.kts
        path.resolve("app/build.gradle.kts").mapLine {
            if (it.contains("namespace")) it.replaceAfter("\"", "$newPackage\"")
            else if (it.contains("applicationId")) it.replaceAfter("\"", "$newPackage\"")
            else it
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

    private fun moveSourceSet(path: Path, newPackage: String, sourceSet: String) {
        val oldPath = path.resolve("app/src/$sourceSet/kotlin/com/dangxuanthong/sampleapp")
        val newPath = path.resolve("app/src/$sourceSet/kotlin/${newPackage.replace(".", "/")}")
        // Create new directory
        newPath.createDirectories()
        // Move files to new package
        oldPath.forEachDirectoryEntry { it.moveTo(newPath / it.name) }
        // Delete old packages
        var oldDir = oldPath
        while (oldDir.listDirectoryEntries().isEmpty()) {
            oldDir.deleteExisting()
            oldDir = oldDir.parent
        }
        // Change package of *.kt files
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

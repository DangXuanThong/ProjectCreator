package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.api.GitHubApiService
import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.FileNotFoundException
import org.koin.core.annotation.Factory

@Factory
class GHOnlineProjectRepository(private val apiService: GitHubApiService) :
    OnlineProjectRepository {

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
}

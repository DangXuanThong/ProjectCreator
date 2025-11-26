package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.api.GitHubApiService
import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.io.File
import java.security.MessageDigest
import kotlinx.io.files.FileNotFoundException
import org.koin.core.annotation.Single

@Single
class GHProjectRepository(private val apiService: GitHubApiService) : ProjectRepository {
    override suspend fun saveProject(path: String): Result<DownloadInfo> =
        apiService.downloadProject(File(path))

    override suspend fun verifyProject(path: String): Result<Unit> {
        val root = File(path)
        require(root.exists() && root.isDirectory) { "Project folder does not exist" }

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
}

package com.dangxuanthong.projectcreator.api

import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import com.dangxuanthong.projectcreator.model.ShaTree
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.BufferedInputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.div
import kotlin.io.path.outputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GitHubApiService(@Named("GitHub") private val client: HttpClient) {
    suspend fun downloadProject(
        path: Path,
        progress: ((Float?) -> Unit)? = null
    ): Result<DownloadInfo> = runCatchNetworkExceptions {
        path.createDirectories()
        client.prepareGet(TEMPLATE_URL) {
            accept(ContentType.parse("application/vnd.github+json"))
            if (progress != null) onDownload { current, total ->
                progress.invoke(total?.let { current.toFloat() / it })
            }
        }.execute { response ->
            // Get a ByteReadChannel, streaming as it's downloaded
            val channel: ByteReadChannel = response.bodyAsChannel()
            // Convert to InputStream so we can use ZipInputStream
            val inputStream = channel.toInputStream()

            ZipInputStream(BufferedInputStream(inputStream)).use { zip ->
                var totalBytes = 0L
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    val rawName = entry.name
                    // Skip the outer directory: remove the first path component
                    val normalizedName = rawName.substringAfter('/')
                    // If normalizedName is empty (e.g. the root folder itself), skip it
                    if (normalizedName.isNotEmpty()) {
                        totalBytes += entry.compressedSize
                        val outFile = path / normalizedName

                        if (entry.isDirectory) outFile.createDirectory()
                        else {
                            outFile.parent?.createDirectories()
                            outFile.outputStream().use { fos -> zip.copyTo(fos) }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
                Result.Success(DownloadInfo(totalBytes, path.absolutePathString()))
            }
        }
    }

    suspend fun getShaForProject(): Result<ShaTree> = runCatchNetworkExceptions {
        val shaTree = client.get(SHA_TREE_URL) {
            accept(ContentType.parse("application/vnd.github+json"))
            parameter("recursive", 1)
        }.body<ShaTree>()
        Result.Success(shaTree)
    }

    private suspend inline fun <T> runCatchNetworkExceptions(
        crossinline block: suspend () -> Result<T>
    ): Result<T> = try {
        withContext(Dispatchers.IO) { block() }
    } catch (_: UnresolvedAddressException) {
        Result.Error(Exception("Cannot connect to GitHub"))
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.Error(e)
    }

    companion object {
        private const val TEMPLATE_URL =
            "https://api.github.com/repos/dangxuanthong/projectcreator/zipball/sample-android"
        private const val SHA_TREE_URL =
            "https://api.github.com/repos/dangxuanthong/projectcreator/git/trees/sample-android"
    }
}

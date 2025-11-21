package com.dangxuanthong.projectcreator.repository

import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class ProjectApiService(private val client: HttpClient) {
    suspend fun downloadProject(
        path: File,
        progress: ((Float?) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        if (!path.exists()) path.mkdirs()
        client.prepareGet(
            "https://api.github.com/repos/dangxuanthong/projectcreator/zipball/sample-android"
        ) {
            headers {
                append("Accept", "application/vnd.github+json")
            }
            if (progress != null) onDownload { current, total ->
                progress.invoke(total?.let { current.toFloat() / it })
            }
        }.execute { response ->
            // Get a ByteReadChannel, streaming as it's downloaded
            val channel: ByteReadChannel = response.bodyAsChannel()
            // Convert to InputStream so we can use ZipInputStream
            val inputStream = channel.toInputStream()

            ZipInputStream(BufferedInputStream(inputStream)).use { zip ->
                var entry: ZipEntry? = zip.nextEntry

                while (entry != null) {
                    val rawName = entry.name
                    // Skip the outer directory: remove the first path component
                    val normalizedName = rawName.substringAfter('/', rawName)

                    // If normalizedName is empty (e.g. the root folder itself), skip it
                    if (normalizedName.isNotEmpty()) {
                        val outFile = File(path, normalizedName)

                        if (entry.isDirectory) outFile.mkdirs()
                        else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { fos -> zip.copyTo(fos) }
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
    }
}

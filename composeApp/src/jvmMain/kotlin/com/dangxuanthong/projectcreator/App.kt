package com.dangxuanthong.projectcreator

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dangxuanthong.projectcreator.ui.theme.ProjectCreatorTheme
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            val dotenv = dotenv { directory = "../" }
            val client = HttpClient(CIO) {
                install(Logging)
            }
            val file = File("C:/Users/Admin/Downloads/Sample.zip")

            client.prepareGet(
                "https://api.github.com/repos/dangxuanthong/projectcreator/zipball/sample-android"
            ) {
                headers {
                    append("Accept", "application/vnd.github+json")
                    bearerAuth(dotenv["GH_PAT"])
                }
                onDownload { bytesSentTotal, contentLength ->
                    println("Received $bytesSentTotal bytes from $contentLength")
                }
            }.execute { httpResponse ->
                val channel: ByteReadChannel = httpResponse.body()
                channel.copyAndClose(file.writeChannel())
                println("A file saved to ${file.path}")
            }
        }
    }

    ProjectCreatorTheme {
        Surface {
            Box(
                Modifier.fillMaxSize().wrapContentSize().scrollable(
                    rememberScrollableState { it },
                    Orientation.Vertical
                )
            ) {
                Text(data)
            }
        }
    }
}

@Serializable
data class Response(val content: String)

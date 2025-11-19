package com.dangxuanthong.projectcreator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Project Creator"
    ) {
        App()
    }
}

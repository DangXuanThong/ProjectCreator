package com.dangxuanthong.projectcreator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dangxuanthong.projectcreator.di.AppModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() {
    startKoin {
        modules(AppModule().module)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Project Creator"
        ) {
            App()
        }
    }
}

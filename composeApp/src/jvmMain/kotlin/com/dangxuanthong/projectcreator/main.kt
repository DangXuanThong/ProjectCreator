package com.dangxuanthong.projectcreator

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dangxuanthong.projectcreator.di.AppModule
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import projectcreator.composeapp.generated.resources.Res
import projectcreator.composeapp.generated.resources.compose_multiplatform

fun main() {
    startKoin {
        modules(AppModule().module)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Project Creator",
            icon = painterResource(Res.drawable.compose_multiplatform)
        ) {
            val dialogSettings = FileKitDialogSettings(this.window)
            App(dialogSettings)
        }
    }
}

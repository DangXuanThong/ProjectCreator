package com.dangxuanthong.projectcreator.ui.screen

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AndroidProjectScreen(
    modifier: Modifier = Modifier,
    vm: AndroidProjectViewModel = koinViewModel()
) {
    val filePicker = rememberDirectoryPickerLauncher {
        it?.let { dir ->
            vm.onUpdateProjectPath(dir.absolutePath())
        }
    }

    Column(
        modifier = modifier.scrollable(
            rememberScrollableState { it },
            Orientation.Vertical
        ).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Project name:")
        OutlinedTextField(
            value = vm.projectName,
            onValueChange = vm::onUpdateProjectName,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("My App") },
            singleLine = true
        )

        Text("Project location:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = vm.projectPath,
                onValueChange = vm::onUpdateProjectPath,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                singleLine = true
            )
            TextButton(
                onClick = { filePicker.launch() },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text("Browse")
            }
        }

        Button(
            onClick = { vm.onCreateProject() },
            modifier = Modifier.align(
                Alignment.End
            ).padding(top = 16.dp).pointerHoverIcon(PointerIcon.Hand),
            enabled = vm.isCreateProjectEnable
        ) {
            Text("Create")
        }
    }
}

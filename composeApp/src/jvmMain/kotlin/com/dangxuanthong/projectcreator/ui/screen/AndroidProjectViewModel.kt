package com.dangxuanthong.projectcreator.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangxuanthong.projectcreator.model.Result
import com.dangxuanthong.projectcreator.repository.ProjectRepository
import kotlin.io.path.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AndroidProjectViewModel(private val projectRepository: ProjectRepository) : ViewModel() {
    val uiState: StateFlow<AndroidProjectState>
        field = MutableStateFlow(AndroidProjectState())

    var log by mutableStateOf("")
        private set

    fun onUpdateProjectName(projectName: String) {
        uiState.update { it.copy(projectName = projectName) }
        updatePackageNameBasedOnName()
    }

    fun onUpdateProjectPath(projectPath: String) {
        uiState.update { it.copy(projectPath = projectPath) }
    }

    fun onUpdatePackageName(packageName: String) {
        uiState.update { it.copy(packageName = packageName) }
    }

    fun updatePackageNameBasedOnName() {
        uiState.value.run {
            val normalizedName = projectName.replace(" ", "").lowercase()
            onUpdatePackageName(packageName.replaceAfterLast(".", normalizedName))
        }
    }

    fun onCreateProject() = viewModelScope.launch {
        uiState.update { it.copy(status = Status.Loading) }
        try {
            val name = uiState.value.projectName.takeIf { it.isNotBlank() } ?: "My App"
            val path = "${uiState.value.projectPath}/$name"

            log += "Downloading template...\n"
            val downloadResult = projectRepository.saveProject(path)
            require(downloadResult is Result.Success) {
                (downloadResult as Result.Error).description
            }
            log += "Downloaded ${downloadResult.data.totalBytes} bytes.\n"

            log += "Checking downloaded files\n"
            val checkResult = projectRepository.verifyProject(path)
            require(checkResult is Result.Success) { (checkResult as Result.Error).description }

            log += "Changing project name\n"
            val renameResult = projectRepository.renameProject(path, name)
            require(renameResult is Result.Success) { (renameResult as Result.Error).description }

            log += "Changing package\n"
            val renamePackageResult =
                projectRepository.renamePackage(Path(path), uiState.value.packageName)
            require(renamePackageResult is Result.Success) {
                (renamePackageResult as Result.Error).description
            }

            log += "Created project.\n"
            uiState.update { it.copy(status = Status.Idle) }
        } catch (e: IllegalArgumentException) {
            e.message?.let { log += "$it\n" }
            uiState.update {
                it.copy(status = Status.Error("Failed to create project"))
            }
        }
    }
}

data class AndroidProjectState(
    val projectName: String = "My App",
    val projectPath: String = "",
    val packageName: String = "com.example.myapp",
    val status: Status = Status.Idle
) {
    val isCreateProjectEnable: Boolean
        get() = projectPath.isNotEmpty() && status != Status.Loading
}

sealed interface Status {
    data object Idle : Status
    data object Loading : Status
    data class Error(val message: String) : Status
}

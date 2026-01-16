package com.dangxuanthong.projectcreator.ui.screen.javaweb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangxuanthong.projectcreator.model.Result
import com.dangxuanthong.projectcreator.model.then
import com.dangxuanthong.projectcreator.repository.ProjectConfigRepository
import com.dangxuanthong.projectcreator.repository.ProjectDownloadRepository
import com.dangxuanthong.projectcreator.repository.ProjectType
import java.nio.file.Path
import kotlin.io.path.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class JavaWebViewModel(
    private val downloadRepository: ProjectDownloadRepository,
    @Named("java") private val getProjectConfigRepository: (Path) -> ProjectConfigRepository
) : ViewModel() {
    val uiState: StateFlow<JavaWebProjectState>
        field = MutableStateFlow(JavaWebProjectState())

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
        val name = uiState.value.projectName.takeIf { it.isNotBlank() } ?: "My App"
        val path = Path(uiState.value.projectPath, name)

        log += "Downloading template...\n"
        val configRepository = getProjectConfigRepository(path)
        val result = with(ProjectType.JAVA_WEB) {
            downloadRepository.saveProject(path).then {
                log += "Downloaded ${it.totalBytes} bytes.\n"
                log += "Checking downloaded files\n"
                downloadRepository.verifyProject(path)
            }.then {
                log += "Changing project name\n"
                configRepository.renameProject(name)
            }.then {
                log += "Changing package\n"
                configRepository.renamePackage(uiState.value.packageName)
            }.catch { _, desc ->
                log += desc + "\n"
                uiState.update {
                    it.copy(status = Status.Error("Failed to create project"))
                }
            }
        }

        if (result is Result.Success) {
            log += "Created project.\n"
            uiState.update { it.copy(status = Status.Idle) }
        }
    }
}

data class JavaWebProjectState(
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

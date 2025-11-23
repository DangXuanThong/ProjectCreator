package com.dangxuanthong.projectcreator.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangxuanthong.projectcreator.model.Result
import com.dangxuanthong.projectcreator.repository.ProjectRepository
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
    }

    fun onUpdateProjectPath(projectPath: String) {
        uiState.update { it.copy(projectPath = projectPath) }
    }

    fun onCreateProject() = viewModelScope.launch {
        with(uiState.value) {
            uiState.update { it.copy(status = Status.Loading) }
            val projectName = projectName.takeIf(String::isNotBlank) ?: "My App"
            log += "Downloading template...\n"
            when (val result = projectRepository.saveProject("$projectPath/$projectName")) {
                is Result.Success -> {
                    log += "Downloaded ${result.data.totalBytes} bytes.\n"
                    uiState.update { it.copy(status = Status.Success) }
                }
                is Result.Error -> {
                    log += (result.exception.message ?: "Unknown error") + "\n"
                    uiState.update {
                        it.copy(status = Status.Error("Failed to create project"))
                    }
                }
            }

            log += "Created project.\n"
        }
    }
}

data class AndroidProjectState(
    val projectName: String = "",
    val projectPath: String = "",
    val status: Status = Status.Idle
) {
    val isCreateProjectEnable: Boolean
        get() = projectPath.isNotEmpty() && status != Status.Loading
}

sealed interface Status {
    data object Idle : Status
    data object Loading : Status
    data object Success : Status
    data class Error(val message: String) : Status
}

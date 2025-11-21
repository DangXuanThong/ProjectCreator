package com.dangxuanthong.projectcreator.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangxuanthong.projectcreator.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AndroidProjectViewModel(private val projectRepository: ProjectRepository) : ViewModel() {
    var projectName by mutableStateOf("")
        private set

    var projectPath by mutableStateOf("")
        private set

    val isCreateProjectEnable: Boolean
        get() = projectPath.isNotEmpty()

    fun onUpdateProjectName(projectName: String) {
        this.projectName = projectName
    }

    fun onUpdateProjectPath(projectPath: String) {
        this.projectPath = projectPath
    }

    fun onCreateProject() = viewModelScope.launch(Dispatchers.IO) {
        projectRepository.saveProject(
            "$projectPath/${projectName.takeIf(String::isNotBlank) ?: "My App"}"
        )
    }
}

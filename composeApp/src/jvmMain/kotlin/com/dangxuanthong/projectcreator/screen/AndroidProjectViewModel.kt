package com.dangxuanthong.projectcreator.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangxuanthong.projectcreator.repository.ProjectApiService
import com.dangxuanthong.projectcreator.repository.ProjectRepository
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AndroidProjectViewModel : ViewModel() {
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
        val client = HttpClient(CIO) {
            install(Logging)
            install(Auth) {
                bearer { loadTokens { BearerTokens(dotenv { directory = ".." }["GH_PAT"], null) } }
            }
        }
        val repo = ProjectRepository(ProjectApiService(client))
        repo.saveProject("$projectPath/${projectName.takeIf { it.isNotBlank() } ?: "My App"}")
    }
}

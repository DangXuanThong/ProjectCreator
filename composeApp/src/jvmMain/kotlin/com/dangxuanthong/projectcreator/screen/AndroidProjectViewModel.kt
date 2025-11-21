package com.dangxuanthong.projectcreator.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AndroidProjectViewModel : ViewModel() {
    var projectName by mutableStateOf("")
        private set

    var projectPath by mutableStateOf("")
        private set

    fun onUpdateProjectName(projectName: String) {
        this.projectName = projectName
    }

    fun onUpdateProjectPath(projectPath: String) {
        this.projectPath = projectPath
    }
}

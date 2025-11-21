package com.dangxuanthong.projectcreator.repository

import java.io.File
import org.koin.core.annotation.Single

@Single
class ProjectRepository(private val apiService: ProjectApiService) {
    suspend fun saveProject(path: String) =
        apiService.downloadProject(File(path))
}

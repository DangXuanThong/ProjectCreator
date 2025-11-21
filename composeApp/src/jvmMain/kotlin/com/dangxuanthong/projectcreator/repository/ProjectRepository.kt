package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.api.GithubApiService
import java.io.File
import org.koin.core.annotation.Single

@Single
class ProjectRepository(private val apiService: GithubApiService) {
    suspend fun saveProject(path: String) =
        apiService.downloadProject(File(path))
}

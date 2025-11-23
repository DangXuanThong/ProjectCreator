package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.api.GithubApiService
import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.io.File
import org.koin.core.annotation.Single

@Single
class ProjectRepository(private val apiService: GithubApiService) {
    suspend fun saveProject(path: String): Result<DownloadInfo> =
        apiService.downloadProject(File(path))
}

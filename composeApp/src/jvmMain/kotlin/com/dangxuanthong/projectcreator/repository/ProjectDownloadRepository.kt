package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.nio.file.Path

interface ProjectDownloadRepository {
    context(type: ProjectType)
    suspend fun saveProject(path: Path): Result<DownloadInfo>

    context(type: ProjectType)
    suspend fun verifyProject(path: Path): Result<Unit>

    suspend fun getHashValue(file: Path): Result<String>
}

enum class ProjectType {
    ANDROID,
    MULTIPLATFORM,
    JAVA_WEB
}

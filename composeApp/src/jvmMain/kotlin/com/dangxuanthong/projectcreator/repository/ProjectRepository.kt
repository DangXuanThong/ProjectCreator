package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.nio.file.Path

interface ProjectRepository {
    suspend fun saveProject(path: Path): Result<DownloadInfo>

    suspend fun verifyProject(path: Path): Result<Unit>

    suspend fun getHashValue(file: Path): Result<String>

    suspend fun renameProject(path: Path, newName: String): Result<Unit>

    suspend fun renamePackage(path: Path, newPackage: String): Result<Unit>
}

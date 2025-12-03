package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.model.DownloadInfo
import com.dangxuanthong.projectcreator.model.Result
import java.io.File
import java.nio.file.Path

interface ProjectRepository {
    suspend fun saveProject(path: String): Result<DownloadInfo>

    suspend fun verifyProject(path: String): Result<Unit>

    suspend fun getHashValue(file: File): Result<String>

    suspend fun renameProject(path: String, newName: String): Result<Unit>

    suspend fun renamePackage(path: Path, newPackage: String): Result<Unit>
}

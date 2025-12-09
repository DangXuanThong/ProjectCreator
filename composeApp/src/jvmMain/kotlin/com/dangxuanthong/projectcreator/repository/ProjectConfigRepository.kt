package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.model.Result

interface ProjectConfigRepository {
    suspend fun renameProject(newName: String): Result<Unit>

    suspend fun renamePackage(newPackage: String): Result<Unit>
}

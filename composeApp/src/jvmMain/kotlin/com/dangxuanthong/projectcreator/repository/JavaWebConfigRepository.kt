package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.model.Result
import java.nio.file.Path
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class JavaWebConfigRepository(@InjectedParam private val root: Path) : ProjectConfigRepository {
    override suspend fun renameProject(newName: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun renamePackage(newPackage: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}

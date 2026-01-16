package com.dangxuanthong.projectcreator.repository

import com.dangxuanthong.projectcreator.model.Result
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.forEachLine
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.visitFileTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class AndroidConfigRepository(@InjectedParam private val root: Path) : ProjectConfigRepository {

    override suspend fun renameProject(newName: String) = runCatchIOExceptions {
        with(root) {
            // Change project name in gradle settings file
            resolve("settings.gradle.kts").mapLine {
                if (!it.contains("rootProject.name")) it
                else it.replace("Sample App", newName)
            }
            // Change app name in strings.xml
            resolve("app/src/main/res/values/strings.xml").mapLine {
                if (!it.contains("app_name")) it
                else it.replace("Sample App", newName)
            }

            resolve("app/src/main/res/values/themes.xml").mapLine {
                if (!it.contains("Theme.SampleApp")) it
                else it.replace("SampleApp", newName.replace(" ", ""))
            }
            resolve("app/src/main/AndroidManifest.xml").mapLine {
                if (!it.contains("android:theme")) it
                else it.replace("SampleApp", newName.replace(" ", ""))
            }
        }
        Result.Success(Unit)
    }

    override suspend fun renamePackage(newPackage: String) = runCatchIOExceptions {
        moveSourceSet(newPackage, "main")
        moveSourceSet(newPackage, "test")
        moveSourceSet(newPackage, "androidTest")
        // Change android.namespace and applicationId in build.gradle.kts
        root.resolve("app/build.gradle.kts").mapLine {
            if (it.contains("namespace")) it.replaceAfter("\"", "$newPackage\"")
            else if (it.contains("applicationId")) it.replaceAfter("\"", "$newPackage\"")
            else it
        }
        Result.Success(Unit)
    }

    private suspend inline fun <T> runCatchIOExceptions(
        crossinline block: suspend () -> Result<T>
    ) = try {
        withContext(Dispatchers.IO) { block() }
    } catch (e: IOException) {
        Result.Error(e)
    }

    private inline fun Path.mapLine(transform: (String) -> String) {
        createTempFile(nameWithoutExtension).run {
            bufferedWriter().use { writer ->
                this@mapLine.forEachLine {
                    writer.write(transform(it))
                    writer.newLine()
                }
            }
            moveTo(this@mapLine, overwrite = true)
        }
    }

    private fun moveSourceSet(newPackage: String, sourceSet: String) {
        val oldPath = root.resolve("app/src/$sourceSet/kotlin/com/dangxuanthong/sampleapp")
        val newPath = root.resolve("app/src/$sourceSet/kotlin/${newPackage.replace(".", "/")}")
        // Create new directory
        newPath.createDirectories()
        // Move files to new package
        oldPath.forEachDirectoryEntry { it.moveTo(newPath / it.name) }
        // Delete old packages
        var oldDir = oldPath
        while (oldDir.listDirectoryEntries().isEmpty()) {
            oldDir.deleteExisting()
            oldDir = oldDir.parent
        }
        // Change package of *.kt files
        newPath.visitFileTree {
            onVisitFile { file, _ ->
                if (file.extension != "kt") return@onVisitFile FileVisitResult.CONTINUE
                file.mapLine {
                    if (!it.startsWith("package")) it
                    else "package $newPackage".also {
                        return@onVisitFile FileVisitResult.CONTINUE
                    }
                }
                FileVisitResult.CONTINUE
            }
        }
    }
}

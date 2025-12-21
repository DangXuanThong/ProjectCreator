package com.dangxuanthong.projectcreator.model.buildconfig.toml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TomlBuildTreeTest {
    private val tomlBuildTree = TomlBuildTree()

    @Test
    fun addVersion_addNewVersion_newVersionCreated() {
        assertTrue(tomlBuildTree.addVersion("kotlin", "2.0.0"))
        assertEquals("2.0.0", tomlBuildTree.versionTree["kotlin"]?.version)
    }

    @Test
    fun addVersion_addSameVersion_nothingChange() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        assertFalse(tomlBuildTree.addVersion("kotlin", "1.0.0"))
        assertEquals("2.0.0", tomlBuildTree.versionTree["kotlin"]?.version)
    }

    @Test
    fun updateVersion_updateExistingVersion_versionUpdated() {
        tomlBuildTree.addVersion("kotlin", "1.0.0")
        assertTrue(tomlBuildTree.updateVersion("kotlin", "2.0.0"))
        assertEquals("2.0.0", tomlBuildTree.versionTree["kotlin"]?.version)
    }

    @Test
    fun updateVersion_updateNonExistingVersion_nothingChange() =
        assertFalse(tomlBuildTree.updateVersion("kotlin", "2.0.0"))

    @Test
    fun removeVersion_removeExistingVersion_versionRemoved() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        assertTrue(tomlBuildTree.removeVersion("kotlin"))
        assertNull(tomlBuildTree.versionTree["kotlin"])
    }

    @Test
    fun removeVersion_removeNonExistingVersion_nothingChange() =
        assertFalse(tomlBuildTree.removeVersion("kotlin"))

    @Test
    fun addLibrary_addNewLibraryWithExistingVersion_newLibraryCreated() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        assertTrue(tomlBuildTree.addLibrary("kotlin", "kotlin"))
        tomlBuildTree.libraryTree["kotlin"].let {
            assertNotNull(it)
            assertEquals("kotlin", it.module)
            assertEquals("kotlin", it.version)
        }
    }

    @Test
    fun addLibrary_addNewLibraryWithNonExistingVersion_nothingChange() {
        assertFalse(tomlBuildTree.addLibrary("kotlin", "kotlin"))
        assertNull(tomlBuildTree.libraryTree["kotlin"])
    }

    @Test
    fun addLibrary_addExistingLibrary_nothingChange() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        tomlBuildTree.addLibrary("kotlin", "kotlin")
        assertFalse(tomlBuildTree.addLibrary("kotlin", "kotlin2"))
        assertEquals("kotlin", tomlBuildTree.libraryTree["kotlin"]?.version)
    }

    @Test
    fun removeLibrary_removeExistingLibrary_libraryRemoved() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        tomlBuildTree.addLibrary("kotlin", "kotlin")
        assertNotNull(tomlBuildTree.removeLibrary("kotlin"))
        assertNull(tomlBuildTree.libraryTree["kotlin"])
    }

    @Test
    fun removeLibrary_removeNonExistingLibrary_nothingChange() {
        assertFalse(tomlBuildTree.removeLibrary("kotlin"))
    }

    @Test
    fun addPlugin_addNewPluginWithExistingVersion_newPluginCreated() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        assertTrue(tomlBuildTree.addPlugin("kotlin", "kotlin"))
        tomlBuildTree.pluginTree["kotlin"].let {
            assertNotNull(it)
            assertEquals("kotlin", it.id)
            assertEquals("kotlin", it.version)
        }
    }

    @Test
    fun addPlugin_addNewPluginWithNonExistingVersion_nothingChange() {
        assertFalse(tomlBuildTree.addPlugin("kotlin", "kotlin"))
        assertNull(tomlBuildTree.pluginTree["kotlin"])
    }

    @Test
    fun addPlugin_addExistingPlugin_nothingChange() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        tomlBuildTree.addPlugin("kotlin", "kotlin")
        assertFalse(tomlBuildTree.addPlugin("kotlin", "kotlin2"))
        assertEquals("kotlin", tomlBuildTree.pluginTree["kotlin"]?.version)
    }

    @Test
    fun removePlugin_removeExistingPlugin_pluginRemoved() {
        tomlBuildTree.addVersion("kotlin", "2.0.0")
        tomlBuildTree.addPlugin("kotlin", "kotlin")
        assertNotNull(tomlBuildTree.removePlugin("kotlin"))
        assertNull(tomlBuildTree.pluginTree["kotlin"])
    }

    @Test
    fun removePlugin_removeNonExistingPlugin_nothingChange() {
        assertFalse(tomlBuildTree.removePlugin("kotlin"))
    }
}

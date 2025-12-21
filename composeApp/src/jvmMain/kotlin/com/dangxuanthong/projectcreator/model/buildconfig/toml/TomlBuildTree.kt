package com.dangxuanthong.projectcreator.model.buildconfig.toml

import com.dangxuanthong.projectcreator.model.buildconfig.BuildTree
import java.nio.file.Path
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.contains
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.getOrNull
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.text.endsWith
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.split
import kotlin.text.startsWith

class TomlBuildTree : BuildTree<TomlNode> {
    val versionTree: Map<String, VersionNode>
        field = mutableMapOf<String, VersionNode>()

    val libraryTree: Map<String, LibraryNode>
        field = mutableMapOf<String, LibraryNode>()

    val pluginTree: Map<String, PluginNode>
        field = mutableMapOf<String, PluginNode>()

    override fun parse(path: Path) = path.bufferedReader().use { reader ->
        var mode = -1
        reader.forEachLine { line ->
            if (line.isEmpty()) return@forEachLine
            if (line.startsWith('[') && line.endsWith(']')) {
                when (line) {
                    "[versions]" -> mode = 0
                    "[libraries]" -> mode = 1
                    "[plugins]" -> mode = 2
                }
            } else {
                val words = line.split(" ", "=", ",", "{", "}", "module", "id", "version.ref", "\"")
                    .filter { it.isNotEmpty() }
                when (mode) {
                    0 -> {
                        if (words.size != 2) return@forEachLine
                        versionTree[words[0]] = VersionNode(words[1])
                    }

                    1 -> {
                        if (words.size !in 2..3) return@forEachLine
                        libraryTree[words[0]] = LibraryNode(words[1], words.getOrNull(2))
                    }

                    2 -> {
                        if (words.size != 3) return@forEachLine
                        pluginTree[words[0]] = PluginNode(words[1], words[2])
                    }
                }
            }
        }
    }

    override fun write(path: Path) = path.bufferedWriter().use {
        it.write("[versions]")
        versionTree.forEach { (name, version) -> it.write("$name = \"${version.version}\"\n") }
        it.write("\n[libraries]")
        libraryTree.forEach { (name, library) ->
            if (library.version == null) it.write("$name = { module = ${library.module} }\n")
            else it.write(
                "$name = { module = ${library.module}, version.ref = ${library.version} }\n"
            )
        }
        it.write("\n[plugins]")
        pluginTree.forEach { (name, plugin) ->
            it.write("$name = { id = ${plugin.id}, version.ref = ${plugin.version} }\n")
        }
    }

    fun addVersion(name: String, version: String): Boolean {
        if (name in versionTree) return false
        versionTree[name] = VersionNode(version)
        return true
    }

    fun updateVersion(name: String, version: String): Boolean {
        if (name !in versionTree) return false
        versionTree[name] = VersionNode(version)
        return true
    }

    fun removeVersion(name: String): Boolean = versionTree.remove(name) != null

    fun addLibrary(module: String, version: String): Boolean {
        if (module in libraryTree || version !in versionTree) return false
        libraryTree[module] = LibraryNode(module, version)
        return true
    }

    fun removeLibrary(module: String): Boolean = libraryTree.remove(module) != null

    fun addPlugin(id: String, version: String): Boolean {
        if (id in pluginTree || version !in versionTree) return false
        pluginTree[id] = PluginNode(id, version)
        return true
    }

    fun removePlugin(id: String): Boolean = pluginTree.remove(id) != null
}

package com.dangxuanthong.projectcreator.model.buildconfig

import java.nio.file.Path

interface BuildTree<out T : Node> {
    fun parse(path: Path)
    fun write(path: Path)
}

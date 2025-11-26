package com.dangxuanthong.projectcreator.model

import kotlinx.serialization.Serializable

@Serializable
data class ShaTree(val tree: List<ShaItem>)

@Serializable
data class ShaItem(val path: String, val mode: String, val type: String, val sha: String)

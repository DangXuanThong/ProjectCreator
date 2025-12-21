package com.dangxuanthong.projectcreator.util

operator fun String.times(number: Int): String = StringBuilder().apply {
    repeat(number) { append(this@times) }
}.toString()

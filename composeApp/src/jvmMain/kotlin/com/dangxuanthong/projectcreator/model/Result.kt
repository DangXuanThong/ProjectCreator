package com.dangxuanthong.projectcreator.model

interface Result<T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error<T>(val exception: Exception) : Result<T>
}

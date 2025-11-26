package com.dangxuanthong.projectcreator.model

sealed interface Result<T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error<T>(val exception: Exception) : Result<T> {
        val description: String
            get() = (exception::class.simpleName ?: "Unknown exception") +
                if (exception.message != null) ": ${exception.message}" else ""
    }
}

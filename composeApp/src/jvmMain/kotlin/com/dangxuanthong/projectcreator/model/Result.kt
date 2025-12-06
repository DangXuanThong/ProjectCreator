package com.dangxuanthong.projectcreator.model

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Exception) : Result<Nothing> {
        val description: String
            get() = (exception::class.simpleName ?: "Unknown exception") +
                if (exception.message != null) ": ${exception.message}" else ""
    }

    fun catch(handler: (Exception, String) -> Unit): Result<T> {
        if (this is Error) handler(exception, description)
        return this
    }
}

inline fun <T, R> Result<T>.then(block: (T) -> Result<R>): Result<R> =
    when (this) {
        is Result.Success<T> -> block(data)
        is Result.Error -> this
    }

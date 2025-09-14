package xyz.lilsus.papp.domain.model

sealed class Resource<out T, out E> {
    data class Success<out T>(val data: T) : Resource<T, Nothing>()
    data class Error<out E>(val error: E) : Resource<Nothing, E>()
    object Loading : Resource<Nothing, Nothing>()
}

inline fun <T, E> Resource<T, E>.onSuccess(action: (T) -> Unit): Resource<T, E> {
    if (this is Resource.Success) {
        action(data)
    }
    return this
}

inline fun <T, E> Resource<T, E>.onError(action: (E) -> Unit): Resource<T, E> {
    if (this is Resource.Error) {
        action(error)
    }
    return this
}

inline fun <T, E> Resource<T, E>.onLoading(action: () -> Unit): Resource<T, E> {
    if (this is Resource.Loading) {
        action()
    }
    return this
}

inline fun <T, R, E> Resource<T, E>.map(transform: (T) -> R): Resource<R, E> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> this
        is Resource.Loading -> this
    }
}

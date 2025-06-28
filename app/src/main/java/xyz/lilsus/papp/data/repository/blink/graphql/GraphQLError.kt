package xyz.lilsus.papp.data.repository.blink.graphql

sealed class GraphQLError : Throwable() {
    object EmptyBody : GraphQLError()
    data class HttpError(val code: Int, val body: String?) : GraphQLError()
    data class NetworkError(override val cause: Throwable) : GraphQLError()
}

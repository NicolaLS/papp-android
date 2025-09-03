package xyz.lilsus.papp.data.repository.blink.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("error")
data class ErrorDto(
    val message: String,
)

package xyz.lilsus.papp.data.repository.blink.dto

import kotlinx.serialization.Serializable

@Serializable
data class SatAmountPayload(
    val amount: Long,
    val errors: List<ErrorDto>? = emptyList(),
)

@Serializable
data class LnInvoiceFeeProbeData(
    val lnInvoiceFeeProbe: SatAmountPayload
)

@Serializable
data class LnInvoiceFeeProbeResponse(
    val data: LnInvoiceFeeProbeData
)

package xyz.lilsus.papp.common

sealed class InvalidReason {
}

sealed class Invoice {
    sealed class Invalid : Invoice() {
        object NoAmountBolt11 : Invalid()
        object NotBolt11Invoice : Invalid()
    }

    data class Bolt11(
        val bolt11: Bolt11Invoice,
        val amountSatoshi: Long,
    ) : Invoice()

    companion object {
        fun parse(rawString: String): Invoice {
            if (rawString.isBlank()) {
                return Invalid.NotBolt11Invoice
            }

            // Currently this only parses into bolt11 or nothing.
            val bolt11Invoice =
                Bolt11Invoice.parseOrNull(rawString) ?: return Invalid.NotBolt11Invoice

            val amountSatoshi = bolt11Invoice.amountSatoshiOrNull()
            return if (amountSatoshi == null) {
                Invalid.NoAmountBolt11
            } else {
                Bolt11(
                    bolt11Invoice,
                    amountSatoshi
                )
            }
        }
    }
}

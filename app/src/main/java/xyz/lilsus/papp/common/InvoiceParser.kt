package xyz.lilsus.papp.common

import android.util.Log

// Parse Bitcoin Invoices. Invoices are either BOLT11 Strings or Bitcoin URI's containing a
// BOLT11 String.
class InvoiceParser {
    // TODO: lnurlCallback
    private lateinit var bolt11Callback: (Bolt11Invoice) -> Unit

    fun onBolt11(callback: (Bolt11Invoice) -> Unit): InvoiceParser {
        this.bolt11Callback = callback
        return this
    }

    fun parse(raw: String?) {
        if (raw == null || raw.isBlank()) {
            return
        }

        // TODO: Parse Bitcoin URI's.

        // Assuming raw is bolt11 string for now.
        val invoice = Bolt11Invoice.parseOrNull(raw)
        Log.i("PARSER", "Invoice null: ${invoice == null}")
        Log.i("PARSER", "Invoice : ${invoice?.encodedSafe}")
        invoice?.let { bolt11Callback(it) }
    }
}
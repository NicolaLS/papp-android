package xyz.lilsus.papp.common

import android.util.Log

// Parse Bitcoin Invoices. Invoices are either BOLT11 Strings or Bitcoin URI's containing a
// BOLT11 String, or a LNURL.
class InvoiceParser {
    companion object {
        const val TAG = "INVOICE_PARSER"
    }


    // TODO: lnurlCallback
    private lateinit var bolt11Callback: (Bolt11Invoice) -> Unit

    fun onBolt11(callback: (Bolt11Invoice) -> Unit): InvoiceParser {
        Log.d(TAG, "Attached onBolt11 callback")
        this.bolt11Callback = callback
        return this
    }

    fun parse(raw: String?) {
        if (raw == null || raw.isBlank()) {
            Log.i(TAG, "Raw invoice string is null, returning.")
            return
        }

        // TODO: Parse Bitcoin URI's.

        // Assuming raw is bolt11 string for now.
        val invoice = Bolt11Invoice.parseOrNull(raw)
        Log.i(TAG, "Found BOLT11 Invoice: ${invoice != null}")
        invoice?.let { bolt11Callback(it) }
    }
}
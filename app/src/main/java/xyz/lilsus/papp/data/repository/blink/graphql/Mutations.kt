package xyz.lilsus.papp.data.repository.blink.graphql

object Mutations {
    const val LnInvoicePaymentSend = """
	mutation LnInvoicePaymentSend(${'$'}input: LnInvoicePaymentInput!) {
	    lnInvoicePaymentSend(input: ${'$'}input) {
		errors {
		    message
		    path
		    code
		}
		status
		transaction {
		    createdAt
		    direction
		    id
		    memo
		    settlementAmount
		    settlementCurrency
		    settlementDisplayAmount
		    settlementDisplayCurrency
		    settlementDisplayFee
		    settlementFee
		    status
		}

	    }
	}
	"""

    const val LnInvoiceFeeProbe = """
    mutation lnInvoiceFeeProbe(${'$'}input: LnInvoiceFeeProbeInput!) {
  lnInvoiceFeeProbe(input: ${'$'}input) {
    errors {
      message
    }
    amount
  }
}
"""
}


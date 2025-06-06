# Payment App

Better defaults for smoother payment UX.
- only support lightning invoices
- don't confirm amount below X.
- don't confirm fees

Connect wallets and use this app as a better frontend. Planned Blink and Alby for now.


# TODO:

- Settings page
- Persist and load settings
- Secure API key storage
- OAuth2 (do this later for now manual key input)
- Alby connect
- Error handling
- General style spec.
- Connect LN node ?
- nostr wallet stuff ?


- only attempt to pay ln invoices, no need for actual bolt11 validation
- add bolt11 kotlin library and parse amount:
https://github.com/block/ln-invoice
- bundle qr code pay result bolt11 parsed data in one struct
- show nice checkmark after pay
- show amount from pay result after pay, if n/a fall back on parsed amount
- debounce for the *same* qr scanned so that if req fails or cancel, or even succeeds,
  and user still points phone at same qr it does not immediately try again.
- do **nothing** if its not a ln invoice, or an amountless invoice

# Ideas

- show ephemeral banner / notification if scanned qr is a bitcoin address
    "hey this is an on-chain invoice"
    maybe a merchant accidentially creates a on chain invoice and if the app would just do
    nothing at all the user could think the app has a problem, if the user sees feedback that the app
    does see the qr but its not a ln invoice he can tell the merchant or use another wallet.

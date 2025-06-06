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

- Better UI for main screen

- Error handling

- General style spec.

- Connect LN node ?

- nostr wallet stuff ?


## Details

- disable rotation / enforce potrait mode
- disable dissmissing the bottom sheet before either error or success
- only attempt to pay ln invoices, no need for actual bolt11 validation

# Ideas

- show ephemeral banner / notification if scanned qr is a bitcoin address
    "hey this is an on-chain invoice"
    maybe a merchant accidentially creates a on chain invoice and if the app would just do
    nothing at all the user could think the app has a problem, if the user sees feedback that the app
    does see the qr but its not a ln invoice he can tell the merchant or use another wallet.

# Payment App

Payment App is designed for *Bitcoin Payments* but it is not a wallet.

Use this app to connect to your favorite wallets or your own lightning node as a frontend
designed and optimized for payments with a better user experience.

## Disclaimer

This code is provided **as is**, without any warranties or guarantees.  
I do not take any responsibility for any issues, losses, or damages resulting from its use.

Do not connect any wallet unless:
- You reviewed the source code and built the app yourself.
- You know me personally and trust me

## Connect Blink Wallet

Blink wallet can be connected directly via API Key and Wallet ID or using OAuth2. Currently only
connection via API Key is supported.

1. Open Blink Wallet on your mobile device
2. Open Settings (Hamburger Menu top right)
3. Click on "API Access"
4. Sign in
5. Copy the wallet-id of your **BTC Account**
7. Open *papp* app
8. Open Settings and click on "Add Wallet"
9. Choose Blink Wallet
10. Paste in the wallet-id
11. Go back to blink dashboard (browser) and navigate to "API Keys"
12. Click on "+"
13. Choose expiry and scopes: read, receive, write
14. Click create and copy the API Key starting with `blink_`
15. Go back to the *papp* app and paste in the API Key
16. Click on "Connect"

## License

This project is licensed under the [MIT License](LICENSE).

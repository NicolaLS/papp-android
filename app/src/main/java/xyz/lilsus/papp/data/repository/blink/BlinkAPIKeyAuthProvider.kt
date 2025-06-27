package xyz.lilsus.papp.data.repository.blink

import xyz.lilsus.papp.domain.repository.AuthProvider

// AuthProvider implementation for connected blink wallets using API Keys instead of OAuth2.
// Simply return the stored API key, there is no refresh logic.
class BlinkAPIKeyAuthProvider(private val apiKey: String) : AuthProvider {
    override fun getAuthHeader(): Pair<String, String> {
        return Pair("X-API-KEY", apiKey)
    }
}
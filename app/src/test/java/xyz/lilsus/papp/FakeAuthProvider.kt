package xyz.lilsus.papp

import xyz.lilsus.papp.domain.repository.AuthProvider

class FakeAuthProvider : AuthProvider {
    override fun getAuthHeader(): Pair<String, String> {
        return Pair("X-API-KEY", "KEY")
    }
}
package xyz.lilsus.papp.domain.repository

// Provides authentication for different wallets.
// This is required because different wallets might use different authentication for example
// API Keys (Bearer token), OAuth2 (Bearer token, refreshable) or JWT.
// The wallet client implementations should not implement authentication logic, if they did
// 401 responses would need to be handled differently per client. One client might use API Keys
// and fail on 401, another client might use OAuth2 and try to refresh the token when it gets a 401
// with the AuthProvider all clients can handle 401 the same way and pass it to the user.

// AuthProvider implementations SHOULD NOT observe the auth/config using the config repository but
// rather use the initial config that is passed to them and update its state. The repository should
// only be used to sync. the state updates with the store. If the AuthProviderImpl also observe the
// config it would create a weird cycle.
interface AuthProvider {
    fun getAuthHeader(): Pair<String, String> // e.g. ("Authorization", "Bearer token")
}
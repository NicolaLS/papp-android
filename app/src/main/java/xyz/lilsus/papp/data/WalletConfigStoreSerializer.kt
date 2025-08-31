package xyz.lilsus.papp.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.lilsus.papp.proto.wallet_config.WalletConfigStore
import java.io.InputStream
import java.io.OutputStream

object WalletConfigStoreSerializer : Serializer<WalletConfigStore> {
    override val defaultValue: WalletConfigStore = WalletConfigStore.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): WalletConfigStore =
        withContext(Dispatchers.IO) {
            try {
                input.use {
                    val encryptedBytes = it.readBytes()
                    val decryptedBytes = Crypto.decrypt(encryptedBytes)
                    WalletConfigStore.parseFrom(decryptedBytes)
                }
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }

    override suspend fun writeTo(t: WalletConfigStore, output: OutputStream) =
        withContext(Dispatchers.IO) {
            val plainBytes = t.toByteArray()
            val encryptedBytes = Crypto.encrypt(plainBytes)
            output.use { it.write(encryptedBytes) }
        }

    val Context.walletConfigStore: DataStore<WalletConfigStore> by dataStore(
        fileName = "wallet_config.pb",
        serializer = WalletConfigStoreSerializer
    )
}
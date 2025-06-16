package xyz.lilsus.papp.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.lilsus.papp.proto.wallet_config.WalletConfig
import java.io.InputStream
import java.io.OutputStream

object WalletConfigSerializer : Serializer<WalletConfig> {
    override val defaultValue: WalletConfig = WalletConfig.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): WalletConfig = withContext(Dispatchers.IO) {
        try {
            input.use {
                val encryptedBytes = it.readBytes()
                val decryptedBytes = Crypto.decrypt(encryptedBytes)
                WalletConfig.parseFrom(decryptedBytes)
            }
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: WalletConfig, output: OutputStream) =
        withContext(Dispatchers.IO) {
            val plainBytes = t.toByteArray()
            val encryptedBytes = Crypto.encrypt(plainBytes)
            output.use { it.write(encryptedBytes) }
        }
}
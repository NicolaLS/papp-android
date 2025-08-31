package xyz.lilsus.papp.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import xyz.lilsus.papp.common.DEFAULT_ALWAYS_CONFIRM_PAYMENT
import xyz.lilsus.papp.common.DEFAULT_CONFIRM_ABOVE
import xyz.lilsus.papp.proto.settings.Payments
import xyz.lilsus.papp.proto.settings.SettingsStore
import java.io.InputStream
import java.io.OutputStream


object SettingsStoreSerializer : Serializer<SettingsStore> {
    override val defaultValue: SettingsStore = SettingsStore.newBuilder()
        .setPaymentSettings(
            Payments.newBuilder()
                .setAlwaysConfirmPayment(DEFAULT_ALWAYS_CONFIRM_PAYMENT)
                .setConfirmPaymentAbove(DEFAULT_CONFIRM_ABOVE)
        )
        .build()

    override suspend fun readFrom(input: InputStream): SettingsStore {
        try {
            return SettingsStore.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: SettingsStore,
        output: OutputStream
    ) = t.writeTo(output)

}

val Context.settingsDataStore: DataStore<SettingsStore> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsStoreSerializer
)
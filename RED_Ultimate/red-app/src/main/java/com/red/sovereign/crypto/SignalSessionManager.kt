package com.red.sovereign.crypto

import android.content.Context
import com.red.sovereign.auth.ApiResult
import com.red.sovereign.auth.AuthorizedApiClient
import com.red.sovereign.auth.DeviceKeyManager
import com.red.sovereign.auth.TokenStore
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.ECPublicKey
import org.signal.libsignal.protocol.kem.KEMPublicKey
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.signal.libsignal.protocol.state.PreKeyBundle
import java.security.MessageDigest
import java.util.Base64

class SignalSessionManager(context: Context) {
    private val tokens = TokenStore(context)
    private val keys = DeviceKeyManager(context)
    private val store = PersistentSignalProtocolStore(context, keys)
    private val directory = IdentityDirectoryApi(AuthorizedApiClient(tokens))
    private val decoder = Base64.getDecoder()

    suspend fun encrypt(remoteRedId: String, plaintext: ByteArray): ApiResult<List<EncryptedEnvelope>> {
        require(plaintext.isNotEmpty() && plaintext.size <= 256 * 1024)
        val result = directory.get(remoteRedId)
        if (result is ApiResult.Error) return result
        result as ApiResult.Success
        if (result.value.devices.isEmpty()) return ApiResult.Error(404, "NO_APPROVED_REMOTE_DEVICE")
        val envelopes = result.value.devices.map { device ->
            val remote = SignalProtocolAddress(remoteRedId, device.protocolDeviceId)
            val local = localAddress()
            if (!store.containsSession(remote)) {
                val bundle = PreKeyBundle(
                    device.registrationId, device.protocolDeviceId, PreKeyBundle.NULL_PRE_KEY_ID, null,
                    device.signedPreKeyId, ECPublicKey(decoder.decode(device.signedPreKey)), decoder.decode(device.signedPreKeySignature),
                    IdentityKey(decoder.decode(device.identityKey)), device.kyberPreKeyId,
                    KEMPublicKey(decoder.decode(device.kyberPreKey)), decoder.decode(device.kyberPreKeySignature)
                )
                SessionBuilder(store, remote, local).process(bundle)
            }
            val ciphertext = SessionCipher(store, local, remote).encrypt(plaintext)
            EncryptedEnvelope(device.protocolDeviceId, ciphertext.type, ciphertext.serialize())
        }
        return ApiResult.Success(result.code, envelopes)
    }

    fun decrypt(senderRedId: String, senderDeviceId: Int, ciphertextType: Int, ciphertext: ByteArray): ByteArray {
        val remote = SignalProtocolAddress(senderRedId, senderDeviceId)
        val cipher = SessionCipher(store, localAddress(), remote)
        return when (ciphertextType) {
            CiphertextMessage.PREKEY_TYPE -> cipher.decrypt(PreKeySignalMessage(ciphertext))
            CiphertextMessage.WHISPER_TYPE -> cipher.decrypt(SignalMessage(ciphertext))
            else -> throw IllegalArgumentException("Unsupported ciphertext type")
        }
    }

    fun safetyNumber(remoteRedId: String, remoteIdentityKey: ByteArray): String {
        val local = keys.identityKeyPair().publicKey.serialize()
        val ordered = if (tokens.redId.orEmpty() <= remoteRedId) local + remoteIdentityKey else remoteIdentityKey + local
        val digest = MessageDigest.getInstance("SHA-256").digest(ordered)
        val digits = digest.joinToString("") { ((it.toInt() and 0xff) % 100).toString().padStart(2, '0') }.take(60)
        return digits.chunked(5).joinToString(" ")
    }

    private fun localAddress() = SignalProtocolAddress(requireNotNull(tokens.redId), keys.protocolDeviceId())
}

data class EncryptedEnvelope(val receiverDeviceId: Int, val ciphertextType: Int, val bytes: ByteArray)

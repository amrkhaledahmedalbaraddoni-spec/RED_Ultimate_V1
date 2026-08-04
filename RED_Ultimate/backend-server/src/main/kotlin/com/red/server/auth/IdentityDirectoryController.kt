package com.red.server.auth

import com.red.server.auth.model.DeviceStatus
import com.red.server.auth.repository.UserAccountRepository
import com.red.server.auth.repository.UserDeviceRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Base64

@RestController
@RequestMapping("/api/identity/directory")
class IdentityDirectoryController(
    private val users: UserAccountRepository,
    private val devices: UserDeviceRepository
) {
    @GetMapping("/{redId}")
    fun bundles(@PathVariable redId: String): IdentityDirectoryResponse {
        val user = users.findByRedId(redId) ?: throw NoSuchElementException("RED identity not found")
        val encoder = Base64.getEncoder()
        val bundles = devices.findAllByUserIdAndStatus(user.id, DeviceStatus.APPROVED).map { device ->
            PreKeyBundleResponse(
                deviceId = device.id.toString(),
                registrationId = device.registrationId,
                protocolDeviceId = device.protocolDeviceId,
                signedPreKeyId = device.signedPreKeyId,
                kyberPreKeyId = device.kyberPreKeyId,
                identityKey = encoder.encodeToString(device.identityKey),
                signedPreKey = encoder.encodeToString(device.signedPreKey),
                kyberPreKey = encoder.encodeToString(device.kyberPreKey),
                signedPreKeySignature = encoder.encodeToString(device.signedPreKeySignature),
                kyberPreKeySignature = encoder.encodeToString(device.kyberPreKeySignature),
                identityFingerprint = device.identityFingerprint,
                authorizationCertificate = requireNotNull(device.authorizationCertificate),
                certificateExpiresAt = requireNotNull(device.certificateExpiresAt)
            )
        }
        return IdentityDirectoryResponse(user.redId, bundles)
    }
}

data class IdentityDirectoryResponse(val redId: String, val devices: List<PreKeyBundleResponse>)
data class PreKeyBundleResponse(
    val deviceId: String,
    val registrationId: Int,
    val protocolDeviceId: Int,
    val signedPreKeyId: Int,
    val kyberPreKeyId: Int,
    val identityKey: String,
    val signedPreKey: String,
    val kyberPreKey: String,
    val signedPreKeySignature: String,
    val kyberPreKeySignature: String,
    val identityFingerprint: String,
    val authorizationCertificate: String,
    val certificateExpiresAt: java.time.Instant
)

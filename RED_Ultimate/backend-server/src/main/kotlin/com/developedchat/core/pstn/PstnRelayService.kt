package com.red.core.pstn

import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class PstnRelayService {

    /**
     * يربط التطبيق بجهاز Dumin الفيزيائي
     */
    fun relayToDumin(phoneNumber: String, duminIp: String) {
        try {
            val url = URL("http://$duminIp/api/dial?number=$phoneNumber")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                println("Relaying PSTN Call to Dumin: $responseCode")
            }
        } catch (e: Exception) {
            println("Dumin Relay Failed: ${e.message}")
        }
    }
}

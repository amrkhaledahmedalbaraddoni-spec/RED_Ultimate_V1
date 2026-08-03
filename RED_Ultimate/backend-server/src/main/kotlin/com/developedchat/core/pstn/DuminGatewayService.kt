package com.red.core.pstn

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DuminGatewayService {
    
    // This connects the Server to the PHYSICAL Dumin Device
    private val restTemplate = RestTemplate()

    fun initiatePstnCall(phoneNumber: String, duminIp: String): String {
        // Command the Dumin device via its local API
        val url = "http://$duminIp/api/call/dial?number=$phoneNumber"
        val response = restTemplate.postForEntity(url, null, Map::class.java)
        return response.body?.get("call_id") as String
    }

    fun getSimStatus(duminIp: String): Map<String, Any> {
        val url = "http://$duminIp/api/sim/status"
        return restTemplate.getForObject(url, Map::class.java) ?: emptyMap()
    }
}

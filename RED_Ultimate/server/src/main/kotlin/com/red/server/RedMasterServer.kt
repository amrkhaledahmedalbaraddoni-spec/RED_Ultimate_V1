package com.red.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * RED Master Server
 * 100% Local. Zero Cloud. Zero Telemetry.
 * All rights reserved to RED.
 */
@SpringBootApplication
class RedMasterServer

fun main(args: Array<String>) {
    // Force Disable any external analytics
    System.setProperty("spring.main.banner-mode", "off")
    System.setProperty("logging.level.root", "INFO")
    
    println("----------------------------------------")
    println("🔴 RED Sovereign Server is STARTING")
    println("🔴 Mode: LOCAL ONLY")
    println("🔴 Security: POST-QUANTUM READY")
    println("----------------------------------------")
    
    runApplication<RedMasterServer>(*args)
}

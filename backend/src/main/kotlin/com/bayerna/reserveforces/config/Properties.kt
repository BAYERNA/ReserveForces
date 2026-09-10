package com.bayerna.reserveforces.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(val secret: String, val expirationMinutes: Long)

@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(val allowedOrigins: String)

@ConfigurationProperties(prefix = "app.seed")
data class SeedProperties(val enabled: Boolean)

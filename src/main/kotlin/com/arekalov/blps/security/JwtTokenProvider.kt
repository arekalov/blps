package com.arekalov.blps.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    @Value("\${jwt.expiration}")
    private val jwtExpiration: Long,
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateAccessToken(userId: UUID, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration * 7)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): UUID {
        return UUID.fromString(getClaims(token).subject)
    }

    fun getRoleFromToken(token: String): String {
        return getClaims(token).get("role", String::class.java)
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: JwtException) {
            logger.debug("Invalid JWT token: {}", e.message)
            false
        } catch (e: IllegalArgumentException) {
            logger.debug("JWT token is empty or malformed: {}", e.message)
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}

package com.supernova.utils

import android.util.Patterns
import java.net.URL

object ValidationUtils {


    fun validatePortalUrl(portal: String): Result<String> {
        val trimmedPortal = portal.trim()

        if (trimmedPortal.isEmpty()) {
            return Result.failure(Exception("Portal URL cannot be empty"))
        }

        if (!trimmedPortal.startsWith("http://") && !trimmedPortal.startsWith("https://")) {
            return Result.failure(Exception("Portal URL must start with http:// or https://"))
        }

        val url = URL(trimmedPortal)
        if (url.host.isNullOrEmpty()) {
            return Result.failure(Exception("Invalid portal URL format"))
        } else {
            val normalizedUrl = normalizePortalUrl(trimmedPortal)
            return Result.success(normalizedUrl)
        }
    }


    fun normalizePortalUrl(portal: String): String {
        val normalized = portal.trim()
        return if (normalized.endsWith("/")) normalized.dropLast(1) else normalized
    }

    fun validateUsername(username: String): Result<String> {
        val trimmedUsername = username.trim()

        if (trimmedUsername.isEmpty()) {
             return Result.failure(Exception("Username cannot be empty"))
        }

        if (trimmedUsername.length < 3) {
             return Result.failure(Exception("Username must be at least 3 characters"))
        }

        return Result.success(trimmedUsername)
    }

    fun validatePassword(password: String):  Result<String> {
        val trimmedPassword = password.trim()

        if (trimmedPassword.isEmpty()) {
             return Result.failure(Exception("Password cannot be empty"))
        }

        if (trimmedPassword.length < 3) {
             return Result.failure(Exception("Password must be at least 3 characters"))
        }

        return Result.success(trimmedPassword)
    }

    fun validateProfileName(name: String): Result<String> {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
             return Result.failure(Exception("Profile name cannot be empty"))
        }

        if (trimmedName.length < 2) {
             return Result.failure(Exception("Profile name must be at least 2 characters"))
        }

        if (trimmedName.length > 20) {
             return Result.failure(Exception("Profile name must be less than 20 characters"))
        }

        return Result.success(trimmedName)
    }

    fun validatePin(pin: String): Result<Int> {
        if (pin.length != 4) {
             return Result.failure(Exception("PIN must be exactly 4 digits"))
        }

        if (!pin.all { it.isDigit() }) {
             return Result.failure(Exception("PIN must contain only numbers"))
        }

        return try {
            val pinInt = pin.toInt()
            Result.success(pinInt)
        } catch (e: NumberFormatException) {
            return Result.failure(Exception("Invalid PIN format"))
        }
    }
}
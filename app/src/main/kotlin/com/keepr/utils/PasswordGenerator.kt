package com.keepr.utils

import java.security.SecureRandom

data class PasswordOptions(
    val length: Int = 16,
    val uppercase: Boolean = true,
    val lowercase: Boolean = true,
    val numbers: Boolean = true,
    val symbols: Boolean = true
)

enum class PasswordStrength(val label: String, val score: Int) {
    VERY_WEAK("Very weak", 0),
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    STRONG("Strong", 3),
    VERY_STRONG("Very strong", 4)
}

object PasswordGenerator {

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"

    private val secureRandom = SecureRandom()

    fun generate(options: PasswordOptions): String {
        if (!options.uppercase && !options.lowercase && !options.numbers && !options.symbols) {
            return generate(options.copy(lowercase = true))
        }

        val pool = buildString {
            if (options.uppercase) append(UPPERCASE)
            if (options.lowercase) append(LOWERCASE)
            if (options.numbers) append(NUMBERS)
            if (options.symbols) append(SYMBOLS)
        }

        val guaranteed = mutableListOf<Char>()
        if (options.uppercase) guaranteed.add(UPPERCASE[secureRandom.nextInt(UPPERCASE.length)])
        if (options.lowercase) guaranteed.add(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])
        if (options.numbers) guaranteed.add(NUMBERS[secureRandom.nextInt(NUMBERS.length)])
        if (options.symbols) guaranteed.add(SYMBOLS[secureRandom.nextInt(SYMBOLS.length)])

        val remaining = (options.length - guaranteed.size).coerceAtLeast(0)
        val passwordChars = guaranteed.toMutableList()
        repeat(remaining) {
            passwordChars.add(pool[secureRandom.nextInt(pool.length)])
        }

        return passwordChars.shuffled(secureRandom).joinToString("")
    }

    fun evaluateStrength(password: String): PasswordStrength {
        if (password.length < 6) return PasswordStrength.VERY_WEAK

        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it in SYMBOLS }) score++

        val entropy = calculateEntropy(password)
        if (entropy > 60) score++

        return when {
            score <= 1 -> PasswordStrength.VERY_WEAK
            score <= 2 -> PasswordStrength.WEAK
            score <= 3 -> PasswordStrength.FAIR
            score <= 5 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }

    private fun calculateEntropy(password: String): Double {
        var poolSize = 0
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { it in SYMBOLS }) poolSize += SYMBOLS.length
        if (poolSize == 0) return 0.0
        return password.length * Math.log(poolSize.toDouble()) / Math.log(2.0)
    }
}

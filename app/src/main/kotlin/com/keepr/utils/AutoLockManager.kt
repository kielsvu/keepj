package com.keepr.utils

import com.keepr.data.repository.AutoLockTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoLockManager {

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var backgroundedAt: Long = 0L
    private var currentTimeout: AutoLockTimeout = AutoLockTimeout.ONE_MINUTE

    fun onAppForegrounded() {
        if (backgroundedAt == 0L) return
        val elapsed = System.currentTimeMillis() - backgroundedAt
        backgroundedAt = 0L

        when {
            currentTimeout == AutoLockTimeout.IMMEDIATELY -> lock()
            currentTimeout != AutoLockTimeout.NEVER && elapsed >= currentTimeout.milliseconds -> lock()
        }
    }

    fun onAppBackgrounded() {
        if (_isLocked.value) return
        if (currentTimeout == AutoLockTimeout.IMMEDIATELY) {
            lock()
        } else if (currentTimeout != AutoLockTimeout.NEVER) {
            backgroundedAt = System.currentTimeMillis()
        }
    }

    fun unlock() {
        _isLocked.value = false
        backgroundedAt = 0L
    }

    fun lock() {
        _isLocked.value = true
        backgroundedAt = 0L
    }

    fun updateTimeout(timeout: AutoLockTimeout) {
        currentTimeout = timeout
    }
}

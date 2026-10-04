package com.keepr.ui.screens.generator

import androidx.lifecycle.ViewModel
import com.keepr.utils.PasswordGenerator
import com.keepr.utils.PasswordOptions
import com.keepr.utils.PasswordStrength
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GeneratorState(
    val options: PasswordOptions = PasswordOptions(),
    val generatedPassword: String = "",
    val strength: PasswordStrength = PasswordStrength.VERY_STRONG
)

class GeneratorViewModel : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    init {
        regenerate()
    }

    fun regenerate() {
        val password = PasswordGenerator.generate(_state.value.options)
        _state.update { it.copy(
            generatedPassword = password,
            strength = PasswordGenerator.evaluateStrength(password)
        ) }
    }

    fun onLength(length: Int) = updateAndRegenerate { it.copy(options = it.options.copy(length = length)) }
    fun onUppercase(v: Boolean) = updateAndRegenerate { it.copy(options = it.options.copy(uppercase = v)) }
    fun onLowercase(v: Boolean) = updateAndRegenerate { it.copy(options = it.options.copy(lowercase = v)) }
    fun onNumbers(v: Boolean) = updateAndRegenerate { it.copy(options = it.options.copy(numbers = v)) }
    fun onSymbols(v: Boolean) = updateAndRegenerate { it.copy(options = it.options.copy(symbols = v)) }

    private fun updateAndRegenerate(transform: (GeneratorState) -> GeneratorState) {
        _state.update(transform)
        regenerate()
    }
}

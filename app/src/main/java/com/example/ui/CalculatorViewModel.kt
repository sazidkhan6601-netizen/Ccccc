package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculatorDatabase
import com.example.data.model.CalculationHistory
import com.example.data.repository.HistoryRepository
import com.example.engine.CalculatorEngine
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository: HistoryRepository

    init {
        val database = CalculatorDatabase.getInstance(application)
        historyRepository = HistoryRepository(database.historyDao())
    }

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    private val _isScientificMode = MutableStateFlow(false)
    val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(true)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _isSecondFunction = MutableStateFlow(false)
    val isSecondFunction: StateFlow<Boolean> = _isSecondFunction.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isHistoryOpen = MutableStateFlow(false)
    val isHistoryOpen: StateFlow<Boolean> = _isHistoryOpen.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // History Flow combined with filters
    val historyList: StateFlow<List<CalculationHistory>> = combine(
        historyRepository.allHistory,
        _historySearchQuery,
        _showFavoritesOnly
    ) { list, query, favOnly ->
        list.filter { item ->
            val matchesFav = !favOnly || item.isFavorite
            val matchesQuery = query.isBlank() ||
                    item.expression.contains(query, ignoreCase = true) ||
                    item.result.contains(query, ignoreCase = true)
            matchesFav && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getAngleMode(): CalculatorEngine.AngleMode {
        return if (_isDegreeMode.value) CalculatorEngine.AngleMode.DEGREE else CalculatorEngine.AngleMode.RADIAN
    }

    private fun updatePreview() {
        val expr = _expression.value
        if (expr.isBlank()) {
            _previewResult.value = null
            _errorMessage.value = null
            return
        }
        val preview = CalculatorEngine.evaluatePreview(expr, getAngleMode())
        _previewResult.value = preview
        _errorMessage.value = null
    }

    fun onDigit(digit: String) {
        // If previous action ended in an evaluated state and user enters digit, reset or append
        val current = _expression.value
        _expression.value = current + digit
        updatePreview()
    }

    fun onDecimal() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "0."
        } else {
            // Find the last number token
            val lastTokens = current.split(Regex("[+\\-×÷*/^()%]"))
            val lastToken = lastTokens.lastOrNull() ?: ""
            if (!lastToken.contains(".")) {
                _expression.value = current + "."
            }
        }
        updatePreview()
    }

    fun onOperator(op: String) {
        val current = _expression.value
        if (current.isEmpty()) {
            // If empty but lastResult exists, start with lastResult
            val last = _lastResult.value
            if (last != null && last != "Error") {
                _expression.value = last.replace(",", "") + op
                updatePreview()
                return
            }
            if (op == "−" || op == "-") {
                _expression.value = "−"
            }
            return
        }

        // Replace consecutive trailing operator
        val lastChar = current.last()
        if (lastChar in "+−-×÷*/^") {
            _expression.value = current.dropLast(1) + op
        } else {
            _expression.value = current + op
        }
        updatePreview()
    }

    fun onFunction(fn: String) {
        val current = _expression.value
        // Functions like sin, cos, tan, ln, log, sqrt, cbrt, asin, acos, atan
        _expression.value = current + fn + "("
        updatePreview()
    }

    fun onConstant(c: String) {
        val current = _expression.value
        _expression.value = current + c
        updatePreview()
    }

    fun onParenthesis(p: String) {
        val current = _expression.value
        _expression.value = current + p
        updatePreview()
    }

    fun onNegate() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "−"
            return
        }
        // If starts with negative sign, toggle
        if (current.startsWith("−") || current.startsWith("-")) {
            _expression.value = current.substring(1)
        } else {
            _expression.value = "−" + current
        }
        updatePreview()
    }

    fun onSquare() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^2"
            updatePreview()
        }
    }

    fun onCube() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^3"
            updatePreview()
        }
    }

    fun onPower() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^"
            updatePreview()
        }
    }

    fun onSquareRoot() {
        val current = _expression.value
        _expression.value = current + "√("
        updatePreview()
    }

    fun onCubeRoot() {
        val current = _expression.value
        _expression.value = current + "∛("
        updatePreview()
    }

    fun onFactorial() {
        val current = _expression.value
        if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) {
            _expression.value = "$current!"
            updatePreview()
        }
    }

    fun onPercent() {
        val current = _expression.value
        if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) {
            _expression.value = "$current%"
            updatePreview()
        }
    }

    fun onInverse() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "1/("
        } else {
            _expression.value = "1/($current)"
        }
        updatePreview()
    }

    fun onClear() {
        _expression.value = ""
        _previewResult.value = null
        _errorMessage.value = null
    }

    fun onBackspace() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            // Check if removing a multi-character function like "sin(", "cos(", "tan⁻¹(", "sqrt(", "log("
            val functions = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sin(", "cos(", "tan(", "log(", "ln(", "sqrt(", "cbrt(", "abs(")
            val matchedFn = functions.firstOrNull { current.endsWith(it) }
            if (matchedFn != null) {
                _expression.value = current.dropLast(matchedFn.length)
            } else {
                _expression.value = current.dropLast(1)
            }
            updatePreview()
        }
    }

    fun onCalculate() {
        val expr = _expression.value.trim()
        if (expr.isBlank()) return

        val angleMode = getAngleMode()
        val result = CalculatorEngine.evaluate(expr, angleMode)

        when (result) {
            is CalculatorEngine.EvalResult.Success -> {
                val formatted = result.formattedText
                _lastResult.value = formatted
                _expression.value = formatted.replace(",", "")
                _previewResult.value = null
                _errorMessage.value = null

                // Save to History Database
                viewModelScope.launch {
                    val modeLabel = if (_isDegreeMode.value) "DEG" else "RAD"
                    historyRepository.addHistory(
                        expression = expr,
                        result = formatted,
                        angleMode = modeLabel
                    )
                }
            }
            is CalculatorEngine.EvalResult.Error -> {
                _errorMessage.value = result.message
            }
        }
    }

    fun toggleScientificMode() {
        _isScientificMode.value = !_isScientificMode.value
    }

    fun toggleAngleMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        updatePreview()
    }

    fun toggleSecondFunction() {
        _isSecondFunction.value = !_isSecondFunction.value
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setHistoryOpen(isOpen: Boolean) {
        _isHistoryOpen.value = isOpen
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun onUseHistoryExpression(item: CalculationHistory) {
        _expression.value = item.expression
        _isHistoryOpen.value = false
        updatePreview()
    }

    fun onUseHistoryResult(item: CalculationHistory) {
        val current = _expression.value
        val cleanResult = item.result.replace(",", "")
        _expression.value = current + cleanResult
        _isHistoryOpen.value = false
        updatePreview()
    }

    fun onDeleteHistory(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
        }
    }

    fun onToggleFavorite(id: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(id, currentFavorite)
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    // Memory operations (MC, MR, M+, M-)
    fun onMemoryAdd() {
        val currentResult = _previewResult.value ?: _lastResult.value ?: _expression.value
        val value = currentResult.replace(",", "").toDoubleOrNull()
        if (value != null) {
            _memoryValue.value += value
        }
    }

    fun onMemorySubtract() {
        val currentResult = _previewResult.value ?: _lastResult.value ?: _expression.value
        val value = currentResult.replace(",", "").toDoubleOrNull()
        if (value != null) {
            _memoryValue.value -= value
        }
    }

    fun onMemoryRecall() {
        val mem = _memoryValue.value
        val formatted = CalculatorEngine.formatResult(mem).replace(",", "")
        _expression.value = _expression.value + formatted
        updatePreview()
    }

    fun onMemoryClear() {
        _memoryValue.value = 0.0
    }
}

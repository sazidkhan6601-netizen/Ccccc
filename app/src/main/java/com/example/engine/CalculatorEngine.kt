package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

/**
 * Robust mathematical expression evaluator supporting basic arithmetic,
 * scientific operations, trigonometric functions (DEG/RAD), powers, roots,
 * logarithms, factorials, and real-time live preview.
 */
object CalculatorEngine {

    private const val MAX_FACTORIAL = 170

    enum class AngleMode {
        DEGREE,
        RADIAN
    }

    sealed class EvalResult {
        data class Success(val value: Double, val formattedText: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    /**
     * Evaluates a mathematical expression string.
     */
    fun evaluate(expression: String, angleMode: AngleMode): EvalResult {
        if (expression.isBlank()) {
            return EvalResult.Error("Empty expression")
        }

        try {
            val sanitized = sanitizeExpression(expression)
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) {
                return EvalResult.Error("Invalid expression")
            }

            val rpn = shuntingYard(tokens)
            val value = evaluateRPN(rpn, angleMode)

            if (value.isNaN()) {
                return EvalResult.Error("Not a Number (Undefined)")
            }
            if (value.isInfinite()) {
                return EvalResult.Error("Cannot divide by zero")
            }

            val formatted = formatResult(value)
            return EvalResult.Success(value, formatted)
        } catch (e: ArithmeticException) {
            return EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            return EvalResult.Error(e.message ?: "Invalid syntax")
        } catch (e: Exception) {
            return EvalResult.Error("Error")
        }
    }

    /**
     * Attempts a safe real-time preview evaluation. Returns formatted string or null if incomplete.
     */
    fun evaluatePreview(expression: String, angleMode: AngleMode): String? {
        if (expression.isBlank()) return null
        // Don't show preview for single numbers without operations
        if (expression.toDoubleOrNull() != null) return null

        try {
            // Auto-close unbalanced parentheses for live preview
            var balancedExpr = expression
            val openCount = balancedExpr.count { it == '(' }
            val closeCount = balancedExpr.count { it == ')' }
            if (openCount > closeCount) {
                balancedExpr += ")".repeat(openCount - closeCount)
            }

            // Strip trailing binary operators for live preview
            balancedExpr = balancedExpr.trimEnd { it in "+-×÷*/^" }
            if (balancedExpr.isBlank()) return null

            val result = evaluate(balancedExpr, angleMode)
            return when (result) {
                is EvalResult.Success -> result.formattedText
                is EvalResult.Error -> null
            }
        } catch (_: Exception) {
            return null
        }
    }

    private fun sanitizeExpression(raw: String): String {
        return raw
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("ln", "ln")
            .replace("log", "log")
            .replace(" ", "")
    }

    // Token types
    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: Char, val precedence: Int, val isRightAssociative: Boolean) : Token()
        data class Function(val name: String) : Token()
        object OpenParen : Token()
        object CloseParen : Token()
        object Percent : Token()
        object Factorial : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var prevToken: Token? = null

        while (i < expr.length) {
            val c = expr[i]

            when {
                c.isDigit() || c == '.' -> {
                    // Check for implicit multiplication e.g., )2 or PI2 (handled below)
                    if (prevToken is Token.CloseParen || prevToken is Token.Factorial || prevToken is Token.Percent) {
                        tokens.add(Token.Operator('*', 2, false))
                    }
                    val sb = StringBuilder()
                    var hasDot = false
                    while (i < expr.length && (expr[i].isDigit() || (!hasDot && expr[i] == '.'))) {
                        if (expr[i] == '.') hasDot = true
                        sb.append(expr[i])
                        i++
                    }
                    val num = sb.toString().toDouble()
                    val token = Token.Number(num)
                    tokens.add(token)
                    prevToken = token
                    continue
                }

                c == '+' || c == '-' -> {
                    // Unary vs Binary
                    val isUnary = prevToken == null ||
                            prevToken is Token.OpenParen ||
                            prevToken is Token.Operator

                    if (isUnary) {
                        if (c == '-') {
                            // Unary minus treated as 0 - x or function neg
                            tokens.add(Token.Number(0.0))
                            val token = Token.Operator('-', 1, false)
                            tokens.add(token)
                            prevToken = token
                        }
                        // Unary plus ignored
                    } else {
                        val token = Token.Operator(c, 1, false)
                        tokens.add(token)
                        prevToken = token
                    }
                    i++
                }

                c == '*' || c == '/' -> {
                    val token = Token.Operator(c, 2, false)
                    tokens.add(token)
                    prevToken = token
                    i++
                }

                c == '^' -> {
                    val token = Token.Operator('^', 3, true)
                    tokens.add(token)
                    prevToken = token
                    i++
                }

                c == '%' -> {
                    val token = Token.Percent
                    tokens.add(token)
                    prevToken = token
                    i++
                }

                c == '!' -> {
                    val token = Token.Factorial
                    tokens.add(token)
                    prevToken = token
                    i++
                }

                c == '(' -> {
                    if (prevToken is Token.Number || prevToken is Token.CloseParen || prevToken is Token.Factorial || prevToken is Token.Percent) {
                        tokens.add(Token.Operator('*', 2, false))
                    }
                    val token = Token.OpenParen
                    tokens.add(token)
                    prevToken = token
                    i++
                }

                c == ')' -> {
                    val token = Token.CloseParen
                    tokens.add(token)
                    prevToken = token
                    i++
                }

                c.isLetter() -> {
                    // Implicit multiplication before function/constant e.g., 2sin or 2PI
                    if (prevToken is Token.Number || prevToken is Token.CloseParen || prevToken is Token.Factorial || prevToken is Token.Percent) {
                        tokens.add(Token.Operator('*', 2, false))
                    }

                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    val word = sb.toString()

                    when (word) {
                        "PI" -> {
                            val token = Token.Number(Math.PI)
                            tokens.add(token)
                            prevToken = token
                        }
                        "e", "E" -> {
                            val token = Token.Number(Math.E)
                            tokens.add(token)
                            prevToken = token
                        }
                        else -> {
                            val token = Token.Function(word)
                            tokens.add(token)
                            prevToken = token
                        }
                    }
                    continue
                }

                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Percent -> output.add(token)
                is Token.Factorial -> output.add(token)

                is Token.Function -> stack.addLast(token)

                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Function) {
                            output.add(stack.removeLast())
                        } else if (top is Token.Operator) {
                            val topPrecedence = top.precedence
                            val currPrecedence = token.precedence
                            if ((!token.isRightAssociative && currPrecedence <= topPrecedence) ||
                                (token.isRightAssociative && currPrecedence < topPrecedence)
                            ) {
                                output.add(stack.removeLast())
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }

                is Token.OpenParen -> stack.addLast(token)

                is Token.CloseParen -> {
                    var foundOpen = false
                    while (stack.isNotEmpty()) {
                        val top = stack.removeLast()
                        if (top is Token.OpenParen) {
                            foundOpen = true
                            break
                        } else {
                            output.add(top)
                        }
                    }
                    if (!foundOpen) {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                    if (stack.isNotEmpty() && stack.last() is Token.Function) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top is Token.OpenParen || top is Token.CloseParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRPN(rpn: List<Token>, angleMode: AngleMode): Double {
        val valStack = ArrayDeque<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> valStack.addLast(token.value)

                is Token.Percent -> {
                    if (valStack.isEmpty()) throw IllegalArgumentException("Invalid percent")
                    val a = valStack.removeLast()
                    valStack.addLast(a / 100.0)
                }

                is Token.Factorial -> {
                    if (valStack.isEmpty()) throw IllegalArgumentException("Invalid factorial")
                    val a = valStack.removeLast()
                    valStack.addLast(calculateFactorial(a))
                }

                is Token.Operator -> {
                    if (valStack.size < 2) throw IllegalArgumentException("Invalid syntax")
                    val b = valStack.removeLast()
                    val a = valStack.removeLast()

                    val result = when (token.symbol) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> {
                            if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                            a / b
                        }
                        '^' -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown operator: ${token.symbol}")
                    }
                    valStack.addLast(result)
                }

                is Token.Function -> {
                    if (valStack.isEmpty()) throw IllegalArgumentException("Missing argument for ${token.name}")
                    val a = valStack.removeLast()
                    val result = evaluateFunction(token.name, a, angleMode)
                    valStack.addLast(result)
                }

                else -> {}
            }
        }

        if (valStack.size != 1) {
            throw IllegalArgumentException("Invalid expression evaluation")
        }

        return valStack.removeLast()
    }

    private fun evaluateFunction(name: String, arg: Double, angleMode: AngleMode): Double {
        val rad = if (angleMode == AngleMode.DEGREE) Math.toRadians(arg) else arg

        return when (name.lowercase()) {
            "sin" -> {
                // Round very small precision inaccuracies (e.g. sin(180 deg) = 0)
                val s = sin(rad)
                if (abs(s) < 1e-15) 0.0 else s
            }
            "cos" -> {
                val c = cos(rad)
                if (abs(c) < 1e-15) 0.0 else c
            }
            "tan" -> {
                if (angleMode == AngleMode.DEGREE && abs(arg % 180.0) == 90.0) {
                    throw ArithmeticException("Tangent undefined at 90°")
                }
                val t = tan(rad)
                if (abs(t) < 1e-15) 0.0 else t
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: asin outside [-1, 1]")
                val res = asin(arg)
                if (angleMode == AngleMode.DEGREE) Math.toDegrees(res) else res
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: acos outside [-1, 1]")
                val res = acos(arg)
                if (angleMode == AngleMode.DEGREE) Math.toDegrees(res) else res
            }
            "atan" -> {
                val res = atan(arg)
                if (angleMode == AngleMode.DEGREE) Math.toDegrees(res) else res
            }
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "sqrt" -> {
                if (arg < 0) throw ArithmeticException("Square root of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "ln" -> {
                if (arg <= 0) throw ArithmeticException("Log of non-positive number")
                ln(arg)
            }
            "log" -> {
                if (arg <= 0) throw ArithmeticException("Log of non-positive number")
                log10(arg)
            }
            "log2" -> {
                if (arg <= 0) throw ArithmeticException("Log of non-positive number")
                ln(arg) / ln(2.0)
            }
            "abs" -> abs(arg)
            "exp" -> exp(arg)
            else -> throw IllegalArgumentException("Unknown function: $name")
        }
    }

    private fun calculateFactorial(n: Double): Double {
        if (n < 0 || n != floor(n)) {
            throw ArithmeticException("Factorial only for non-negative integers")
        }
        val intVal = n.toInt()
        if (intVal > MAX_FACTORIAL) {
            throw ArithmeticException("Factorial overflow (>170)")
        }
        var result = 1.0
        for (i in 2..intVal) {
            result *= i
        }
        return result
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value == Double.POSITIVE_INFINITY) return "∞"
        if (value == Double.NEGATIVE_INFINITY) return "-∞"

        // Zero check
        if (abs(value) < 1e-14) return "0"

        val absVal = abs(value)
        // If the number is huge or tiny, use scientific notation
        return if (absVal >= 1e12 || (absVal > 0 && absVal < 1e-6)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            df.format(value).replace("E", "e")
        } else {
            // Check if it is an integer
            if (value == floor(value) && absVal < 1e12) {
                val df = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
                df.format(value.toLong())
            } else {
                val df = DecimalFormat("#,##0.########", DecimalFormatSymbols(Locale.US))
                df.format(value)
            }
        }
    }
}

package com.kazemieh.common.util

import kotlin.math.roundToLong

object CalculatorParser {

    fun evaluate(expression: String): String {
        if (expression.isEmpty()) return "0"
        return try {
            val result = calculate(expression)
            if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    fun calculate(expression: String): Double {
        if (expression.isEmpty()) return 0.0
        
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return 0.0

        // Handle percentage logic: 100 + 10 % -> 100 + (100 * 0.1)
        val processedTokens = mutableListOf<String>()
        for (i in tokens.indices) {
            val token = tokens[i]
            if (token == "%") {
                if (i > 0) {
                    val prevValueStr = processedTokens.removeAt(processedTokens.size - 1)
                    val prevValue = prevValueStr.toDoubleOrNull() ?: 0.0
                    
                    if (processedTokens.size >= 2) {
                        val operator = processedTokens[processedTokens.size - 1]
                        val baseValue = processedTokens[processedTokens.size - 2].toDoubleOrNull() ?: 0.0
                        
                        if (operator == "+" || operator == "-") {
                            // 100 + 10% -> 100 + (100 * 0.1)
                            processedTokens.add((baseValue * (prevValue / 100.0)).toString())
                        } else {
                            // 100 * 10% -> 100 * 0.1
                            processedTokens.add((prevValue / 100.0).toString())
                        }
                    } else {
                        processedTokens.add((prevValue / 100.0).toString())
                    }
                }
            } else {
                processedTokens.add(token)
            }
        }

        // Standard evaluation of processed tokens (MDAS)
        return evalTokens(processedTokens)
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()
        
        for (char in expression.replace("×", "*").replace("÷", "/")) {
            if (char.isDigit() || char == '.') {
                currentNumber.append(char)
            } else {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString())
                    currentNumber = StringBuilder()
                }
                if (!char.isWhitespace()) {
                    tokens.add(char.toString())
                }
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }
        return tokens
    }

    private fun evalTokens(tokens: List<String>): Double {
        if (tokens.isEmpty()) return 0.0
        
        // Handle Multiplication and Division first
        val pass1 = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "*" || token == "/") {
                val left = pass1.removeAt(pass1.size - 1).toDouble()
                val right = tokens[i + 1].toDouble()
                val res = if (token == "*") left * right else left / right
                pass1.add(res.toString())
                i += 2
            } else {
                pass1.add(token)
                i++
            }
        }

        // Handle Addition and Subtraction
        var result = pass1[0].toDouble()
        var j = 1
        while (j < pass1.size) {
            val op = pass1[j]
            val val2 = pass1[j + 1].toDouble()
            if (op == "+") result += val2
            else if (op == "-") result -= val2
            j += 2
        }
        
        return result
    }
}

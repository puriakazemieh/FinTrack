package com.kazemieh.sms_reader

import com.kazemieh.common.model.SmsDraft

interface BankParser {
    val bankName: String
    val senderNumbers: List<String>
    fun parse(sender: String, body: String): SmsDraft?
}

object BankParserRegistry {
    private val parsers = mutableListOf<BankParser>()
    private var fallbackParser: BankParser? = null

    fun register(parser: BankParser) {
        parsers.add(parser)
    }

    fun setFallback(parser: BankParser) {
        fallbackParser = parser
    }

    fun parse(sender: String, body: String): SmsDraft? {
        val specificParser = parsers.firstOrNull { parser ->
            parser.senderNumbers.any { it.equals(sender, ignoreCase = true) }
        }
        
        return specificParser?.parse(sender, body) ?: fallbackParser?.parse(sender, body)
    }

    fun init(defaultParsers: List<BankParser>, fallback: BankParser? = null) {
        parsers.clear()
        parsers.addAll(defaultParsers)
        fallbackParser = fallback
    }
}

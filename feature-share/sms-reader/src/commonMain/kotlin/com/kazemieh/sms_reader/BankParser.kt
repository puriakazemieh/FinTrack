package com.kazemieh.sms_reader

import com.kazemieh.common.model.SmsDraft

interface BankParser {
    val bankName: String
    val senderNumbers: List<String>
    fun parse(sender: String, body: String): SmsDraft?
}

object BankParserRegistry {
    private val parsers = mutableListOf<BankParser>()
    // A broadcast can start the app process before Koin has eagerly created the parser
    // initializer. Keep a safe generic parser from process start so a valid bank SMS is
    // never discarded solely because dependency initialization was late.
    private var fallbackParser: BankParser? = GenericBankParser()

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

    fun getParsers(): List<BankParser> = parsers.toList()

    fun init(defaultParsers: List<BankParser>, fallback: BankParser? = null) {
        parsers.clear()
        parsers.addAll(defaultParsers)
        fallbackParser = fallback
    }
}

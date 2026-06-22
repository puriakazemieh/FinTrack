package com.kazemieh.sms_reader.di

import com.kazemieh.sms_reader.*
import org.koin.dsl.module

val smsReaderModule = module {
    single { BluParser() }
    single { MellatParser() }
    single { SaderatParser() }
    single { PasargadParser() }
    single { SamanParser() }
    single { GenericBankParser() }

    single(createdAtStart = true) {
        SmsReaderInitializer(
            parsers = listOf(
                get<BluParser>(),
                get<MellatParser>(),
                get<SaderatParser>(),
                get<PasargadParser>(),
                get<SamanParser>()
            ),
            fallback = get<GenericBankParser>()
        )
    }
}

class SmsReaderInitializer(parsers: List<BankParser>, fallback: BankParser) {
    init {
        BankParserRegistry.init(parsers, fallback)
    }
}

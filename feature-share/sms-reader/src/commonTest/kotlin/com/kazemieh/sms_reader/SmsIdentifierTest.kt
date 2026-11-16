package com.kazemieh.sms_reader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SmsIdentifierTest {
    @Test fun specificParserAcceptsPersianCardSuffix() {
        BankParserRegistry.init(listOf(BluParser()), GenericBankParser())
        val draft = assertNotNull(BankParserRegistry.parse("blubank",
            "بلوبانک برداشت مبلغ: ۵۰٬۰۰۰ تومان کارت ****۲۲۲۲ موجودی ۹۰۰٬۰۰۰ ریال"))
        assertEquals("2222", draft.sourceIdentifier)
    }
}

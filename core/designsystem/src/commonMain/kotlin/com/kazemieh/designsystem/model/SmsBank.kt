package com.kazemieh.designsystem.model

import com.kazemieh.common.model.BankIdentity

/** Accept existing persisted English/Persian bank names without migrating draft data. */
fun bankForSms(name: String): Bank = BankIdentity.fromName(name)?.let { identity ->
    Bank.entries.firstOrNull { it.name == identity.name }
} ?: Bank.Unknown

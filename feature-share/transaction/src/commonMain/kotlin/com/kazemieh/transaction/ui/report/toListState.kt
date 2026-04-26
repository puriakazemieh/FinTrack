package com.kazemieh.transaction.ui.report

import com.kazemieh.transaction.ui.main.TransactionState

fun TransactionReportState.toListState(): TransactionState {
    return TransactionState(
        items = this.items,
        endReached = this.endReached,
        isRefreshing = this.isRefreshing,
        isAppending = this.isAppending,
        refreshError = this.refreshError,
        appendError = this.appendError,
    )
}
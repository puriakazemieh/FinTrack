package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.formatted
import com.kazemieh.common.model.*
import com.kazemieh.common.toFa
import com.kazemieh.common.toFormattedFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import org.jetbrains.compose.resources.stringResource
import fintrack.core.designsystem.generated.resources.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListByFilterScreen(
    selectedSources: Set<Source>,
    isAllSources: Boolean = true,
    selectedCategories: Set<Category>,
    isAllCategories: Boolean = true,
    selectedTags: Set<Tag>,
    isAllTags: Boolean = true,
    selectedPersons: Set<Person>,
    isAllPersons: Boolean = true,
    selectedTransactionType: TransactionType,
    fromTimestamp: Long? = null,
    toTimestamp: Long? = null,
    enableAnimationChart: Boolean = true,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    LaunchedEffect(
        selectedSources, isAllSources, selectedCategories, isAllCategories,
        selectedTags, isAllTags, selectedPersons, isAllPersons,
        selectedTransactionType, fromTimestamp, toTimestamp
    ) {
        viewModel.onIntent(
            TransactionReportIntent.SetFilters(
                sources = selectedSources,
                isAllSources = isAllSources,
                categories = selectedCategories,
                isAllCategories = isAllCategories,
                tags = selectedTags,
                isAllTags = isAllTags,
                persons = selectedPersons,
                isAllPersons = isAllPersons,
                type = selectedTransactionType,
                fromTimestamp = fromTimestamp,
                toTimestamp = toTimestamp
            )
        )
    }

    val state = viewModel.state.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()

    TransactionListByFilterContent(
        state = state,
        listState = listState,
        enableAnimationChart = enableAnimationChart,
        onDelete = onDelete,
        onEdit = onEdit,
        onLoadMore = { viewModel.onIntent(TransactionReportIntent.LoadNextPage) },
        onRetryRefresh = { viewModel.onIntent(TransactionReportIntent.RetryRefresh) },
        onRetryAppend = { viewModel.onIntent(TransactionReportIntent.RetryAppend) }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListByFilterContent(
    state: TransactionReportState,
    listState: LazyListState,
    enableAnimationChart: Boolean,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    onLoadMore: () -> Unit,
    onRetryRefresh: () -> Unit,
    onRetryAppend: () -> Unit,
) {
    val space = LocalSpacing.current

    LaunchedEffect(state.items.size, state.endReached, state.isAppending, state.isRefreshing) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisible ->
                val shouldLoadMore = lastVisible >= (state.items.size - 5).coerceAtLeast(0)
                if (shouldLoadMore && !state.endReached && !state.isAppending && !state.isRefreshing) {
                    onLoadMore()
                }
            }
    }

    val groupedItems = remember(state.items) {
        state.items.groupBy { it.transaction.date }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = space.mediumSmall)
    ) {
        item {
            SummaryCard()
            Spacer(Modifier.height(14.dp))
            CategoryStrip()
            Spacer(Modifier.height(24.dp))
        }

        if (state.isRefreshing && state.items.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GlassGreen)
                }
            }
        } else if (state.items.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    FintrackBodyMediumText(text = stringResource(Res.string.msg_no_transaction_found), color = GlassText3)
                }
            }
        } else {
            groupedItems.forEach { (date, items) ->
                val totalNet = items.sumOf { 
                    when(it.transaction.type) {
                        TransactionType.INCOME -> it.transaction.amount.toLong()
                        TransactionType.EXPENSE -> -it.transaction.amount.toLong()
                        TransactionType.TRANSFER -> -(it.transaction.amountTransfer.toLong())
                        else -> 0L
                    }
                }

                stickyHeader {
                    DayHeader(date = date, count = items.size, netAmount = totalNet)
                }

                items(items, key = { it.transaction.id }) { item ->
                    TxRow(
                        item = item,
                        onClick = { onEdit(item) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (state.isAppending) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GlassGreen, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: String, count: Int, netAmount: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FintrackBodyLargeText(text = date, fontWeight = FontWeight.Bold, color = GlassText)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                FintrackLabelSmallText(
                    text = count.toLong().toFa() + " " + stringResource(Res.string.unit_transaction),
                    fontSize = 10.sp,
                    color = GlassText3
                )
            }
        }

        FintrackTitleSmallText(
            text = stringResource(
                Res.string.label_amount_with_unit,
                (if (netAmount >= 0) "+" else "") + netAmount.toLong().toFa(),
                stringResource(Res.string.unit_toman_short)
            ),
            fontWeight = FontWeight.W700,
            color = if (netAmount >= 0) GlassGreen else GlassRed
        )
    }
}

@Composable
private fun TxRow(item: TransactionWithRelations, onClick: () -> Unit) {
    val isIncome = item.transaction.type == TransactionType.INCOME
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    val color = when {
        isIncome -> GlassGreen
        isTransfer -> GlassBlue
        else -> GlassRed
    }
    val bgColor = when {
        isIncome -> GlassGreenSoft
        isTransfer -> GlassBlueSoft
        else -> GlassRedSoft
    }

    val icon = com.kazemieh.designsystem.picker.FinTrackIcons.findIcon(item.category.iconId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(GlassColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.small)
                .background(bgColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                painter = org.jetbrains.compose.resources.painterResource(icon.resource),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyMediumText(text = item.category.name, fontWeight = FontWeight.Bold, color = GlassText)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FintrackLabelSmallText(text = item.source.name, color = GlassText3)
                Box(modifier = Modifier.size(3.dp).background(GlassText3, CircleShape))
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val sign = when (item.transaction.type) {
                TransactionType.INCOME -> "+"
                TransactionType.EXPENSE -> "-"
                TransactionType.TRANSFER -> "-"
                else -> ""
            }
            val displayAmount = if (isTransfer) item.transaction.amountTransfer else item.transaction.amount

            FintrackTitleSmallText(
                text = stringResource(
                    Res.string.label_amount_with_unit,
                    "$sign${displayAmount.toLong().toFormattedFa()}",
                    stringResource(Res.string.unit_toman_short)
                ),
                fontWeight = FontWeight.W700,
                color = color
            )
            item.transaction.description?.takeIf { it.isNotEmpty() }?.let {
                FintrackLabelSmallText(text = it, color = GlassText3, maxLines = 1)
            }
        }
    }
}

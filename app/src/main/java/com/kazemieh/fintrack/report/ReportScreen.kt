package com.kazemieh.fintrack.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.CategoryListSelectionBottomSheet
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.financialsource.ui.SourceListSelectionBottomSheet
import com.kazemieh.fintrack.R
import com.kazemieh.transaction.ui.report.TransactionFilterType
import com.kazemieh.transaction.ui.report.TransactionListByFilterScreen
import org.koin.androidx.compose.koinViewModel


@Composable
fun ReportScreen(
    viewModel: ReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column {

            Spacer(modifier = Modifier.height(8.dp))

            ReportTopBar(onIntent = viewModel::onIntent, state = state)

            TransactionListByFilterScreen(
                selectedSources = state.selectedSources,
                selectedCategories = state.selectedCategories,
                selectedTransactionType = state.selectedTransactionType.count
            )

        }


        if (state.isSourceSheetVisible) {
            SourceListSelectionBottomSheet(
                onSourceClick = {
                    viewModel.onIntent(ReportFilterIntent.OnSourcesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportFilterIntent.OnToggleSourceSheet)
                }
            )
        }

        if (state.isCategorySheetVisible) {
            CategoryListSelectionBottomSheet(
                selectedTransactionType = state.selectedTransactionType.count,
                onCategoryClick = {
                    viewModel.onIntent(ReportFilterIntent.OnCategoriesSelected(it))
                },
                onDismiss = {
                    viewModel.onIntent(ReportFilterIntent.OnToggleCategorySheet)
                }
            )
        }


    }
}

@Preview(
    showSystemUi = true, showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ReportTopBarPrev() {
    ReportTopBar({}, ReportFilterState())
}

@Composable
fun ReportTopBar(onIntent: (ReportFilterIntent) -> Unit, state: ReportFilterState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TransactionTypeFilter.entries.forEachIndexed { index, option ->
                        val text = when (option.count) {
                            1 -> {
                                stringResource(com.kazemieh.transaction.R.string.incoming)
                            }

                            2 -> {
                                stringResource(com.kazemieh.transaction.R.string.outcoming)
                            }

                            else -> {
                                stringResource(com.kazemieh.transaction.R.string.all)
                            }
                        }
                        SegmentedButton(
                            selected = state.selectedTransactionType == option,
                            onClick = {
                                onIntent(ReportFilterIntent.OnTransactionTypeSelected(option))
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TransactionFilterType.entries.size
                            ),
                        ) {

                            FintrackBodyMediumText(text = text)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                val sourceTextStyle =
                    if (state.selectedSources.isEmpty()) MaterialTheme.typography.bodyMedium
                    else MaterialTheme.typography.labelSmall
                FintrackOutlinedTextField(
                    value = if (state.selectedSources.size == 1) state.selectedSources.first().second
                    else state.selectedSources.joinToString(", ") { it.second },
                    onClick = { onIntent(ReportFilterIntent.OnToggleSourceSheet) },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    textStyle = sourceTextStyle,
                    modifier = Modifier.weight(1f),
                    label = {
                        val text =
                            if (state.selectedSources.isEmpty()) stringResource(R.string.all_source)
                            else stringResource(R.string.sources, state.selectedSources.size)

                        FintrackBodyMediumText(text = text)

                    }

                )

                val categoryTextStyle =
                    if (state.selectedCategories.isEmpty()) MaterialTheme.typography.bodyMedium
                    else MaterialTheme.typography.labelSmall
                FintrackOutlinedTextField(
                    value = if (state.selectedCategories.size == 1) state.selectedCategories.first().second
                    else state.selectedCategories.joinToString(", ") { it.second },
                    onClick = { onIntent(ReportFilterIntent.OnToggleCategorySheet) },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    textStyle = categoryTextStyle,
                    modifier = Modifier.weight(1f),
                    label = {
                        val text =
                            if (state.selectedCategories.isEmpty()) stringResource(R.string.all_category)
                            else stringResource(R.string.categories, state.selectedCategories.size)

                        FintrackBodyMediumText(text = text)

                    }

                )
            }

            DatePeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onChange = { onIntent(ReportFilterIntent.OnPeriodChanged(it)) }
            )
        }
    }
}


@Composable
fun DatePeriodSelector(selectedPeriod: ReportPeriod, onChange: (ReportPeriod) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* ماه قبل */ }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ماه قبل")
        }

        Text(
            text = when (selectedPeriod) {
                ReportPeriod.ThisMonth -> "این ماه"
                ReportPeriod.LastMonth -> "ماه قبل"
                ReportPeriod.Custom -> "بازه دلخواه"
            },
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = { /* ماه بعد */ }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه بعد")
        }
    }
}


package com.kazemieh.sms_reader.ui

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackInlineFeedback
import com.kazemieh.designsystem.component.localizedInteger
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.domain.usecase.SmsBatchProgress
import fintrack.core.designsystem.generated.resources.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.LocalHideBalance
import com.kazemieh.sms_reader.smsBank
import com.kazemieh.sms_reader.smsDisplayCurrency
import com.kazemieh.sms_reader.smsFormattedAmount
import com.kazemieh.domain.usecase.reviewSmsAmount
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.EntityChip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman_full
import fintrack.core.designsystem.generated.resources.currency_rial_full
import fintrack.core.designsystem.generated.resources.sms_review_amount
import fintrack.core.designsystem.generated.resources.card_suffix_label
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.label_most_used
import fintrack.core.designsystem.generated.resources.notif_action_register
import fintrack.core.designsystem.generated.resources.action_delete_all
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.sms_sheet_sub
import fintrack.core.designsystem.generated.resources.sms_sheet_title
import fintrack.core.designsystem.generated.resources.source
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetectionSheet(
    drafts: List<SmsDraft>,
    categories: List<Category>,
    sources: List<Source>,
    mostUsedCategories: List<Category>,
    mostUsedSources: List<Source>,
    currency: String,
    onQuickRegister: (SmsDraft) -> Unit,
    onEdit: (SmsDraft) -> Unit,
    onDelete: (SmsDraft) -> Unit,
    onDeleteAll: () -> Unit,
    onUpdateDraft: (SmsDraft) -> Unit,
    onDismiss: () -> Unit,
    readyIds: Set<Long>,
    isBusy: Boolean,
    isRegistering: Boolean,
    progress: SmsBatchProgress?,
    errors: Map<Long, UiText>,
    generalError: UiText?,
    deletedCount: Int?,
    onRegisterAll: () -> Unit,
    onDismissSummary: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pickingDraft by remember { mutableStateOf<SmsDraft?>(null) }
    var pickingType by remember { mutableStateOf<PickerType?>(null) }

    ModalBottomSheet(
        onDismissRequest = { if (!isBusy) onDismiss() },
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Keep delete on the physical left and close on the physical right in either language.
                val direction = LocalLayoutDirection.current
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(LocalLayoutDirection provides direction) {
                            OutlinedButton(onClick = onDeleteAll, enabled = drafts.isNotEmpty() && !isBusy,
                                modifier = Modifier.heightIn(min = 48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                FintrackLabelMediumText(stringResource(Res.string.action_delete_all),
                                    color = LocalContentColor.current)
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.Start) {
                                FintrackHeadlineSmallText(stringResource(Res.string.sms_sheet_title),
                                    fontSize = 18.sp, fontWeight = FontWeight.W700,
                                    color = MaterialTheme.colorScheme.onBackground)
                                FintrackLabelSmallText(stringResource(Res.string.sms_sheet_sub),
                                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = onDismiss, enabled = !isBusy) {
                            Icon(Icons.Default.Close, stringResource(Res.string.sms_cancel))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    item(key = "sms_actions") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FintrackBodyMediumText(stringResource(Res.string.sms_ready_count,
                                localizedInteger(drafts.size), localizedInteger(readyIds.size)))
                            FintrackButton(text = stringResource(Res.string.sms_register_all, localizedInteger(readyIds.size)),
                                enabled = readyIds.isNotEmpty() && !isBusy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), onClick = onRegisterAll)
                            if (isRegistering) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                progress?.let { FintrackBodyMediumText(stringResource(Res.string.sms_registration_progress,
                                    localizedInteger(it.processed), localizedInteger(it.total))) }
                            } else if (progress != null) {
                                FintrackInlineFeedback(stringResource(Res.string.sms_registration_summary,
                                    localizedInteger(progress.posted), localizedInteger(progress.incomplete),
                                    localizedInteger(progress.failed), localizedInteger(progress.unavailable)),
                                    isError = progress.failed > 0)
                            }
                            deletedCount?.let { FintrackInlineFeedback(stringResource(Res.string.sms_delete_complete, localizedInteger(it))) }
                            generalError?.let { FintrackInlineFeedback(it.asString(), isError = true) }
                            if (!isBusy && (progress != null || deletedCount != null || generalError != null)) {
                                TextButton(onClick = onDismissSummary) {
                                    FintrackLabelMediumText(stringResource(Res.string.sms_dismiss_summary), color = LocalContentColor.current)
                                }
                            }
                            if (drafts.isEmpty()) FintrackBodyMediumText(stringResource(Res.string.sms_no_pending))
                        }
                    }
                    items(drafts, key = { it.id }) { draft ->
                        SmsDraftItem(
                            draft = draft,
                            categories = categories,
                            sources = sources,
                            mostUsedCategories = mostUsedCategories,
                            mostUsedSources = mostUsedSources,
                            currency = currency,
                            isReady = draft.id in readyIds, enabled = !isBusy, error = errors[draft.id],
                            onQuickRegister = { onQuickRegister(draft) },
                            onEdit = { onEdit(draft) },
                            onDelete = { onDelete(draft) },
                            onUpdate = onUpdateDraft,
                            onPick = { type ->
                                pickingDraft = draft
                                pickingType = type
                            }
                        )
                    }
                    
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }

    if (pickingDraft != null && pickingType != null) {
        if (pickingType == PickerType.Category) {
            CategoryPickerBottomSheet(
                transactionType = pickingDraft!!.type,
                onCategoryClick = { category ->
                    onUpdateDraft(pickingDraft!!.copy(categoryId = category.id))
                    pickingDraft = null
                    pickingType = null
                },
                onDismiss = {
                    pickingDraft = null
                    pickingType = null
                }
            )
        } else {
            SourcePickerBottomSheet(
                onSourceClick = { source ->
                    onUpdateDraft(pickingDraft!!.copy(sourceId = source.id))
                    pickingDraft = null
                    pickingType = null
                },
                onDismiss = {
                    pickingDraft = null
                    pickingType = null
                }
            )
        }
    }
}

enum class PickerType { Category, Source }

@Composable
fun SmsDraftItem(
    draft: SmsDraft,
    categories: List<Category>,
    sources: List<Source>,
    mostUsedCategories: List<Category>,
    mostUsedSources: List<Source>,
    currency: String,
    onQuickRegister: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (SmsDraft) -> Unit,
    onPick: (PickerType) -> Unit,
    isReady: Boolean,
    enabled: Boolean,
    error: UiText?
) {
    val glassColors = LocalGlassColors.current

    val displayCurrency = smsDisplayCurrency(currency)
    val unitLabel = stringResource(if (displayCurrency.code == "IRT")
        Res.string.currency_toman_full else Res.string.currency_rial_full)
    val bank = smsBank(draft.bankName)
    val hideBalance = LocalHideBalance.current
    val amountReview = reviewSmsAmount(draft)
    var textExpanded by rememberSaveable(draft.id) { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(bank.logo),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackLabelMediumText(
                            text = stringResource(bank.title),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        FintrackLabelMediumText(
                            text = if (hideBalance) "••••••" else
                                smsFormattedAmount(draft.amount, displayCurrency, unitLabel),
                            color = if (draft.type == TransactionType.INCOME) GlassGreen else GlassRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackLabelSmallText(
                            text = if (hideBalance) "••••••" else (draft.sourceIdentifier?.let {
                                stringResource(
                                    Res.string.card_suffix_label,
                                    it
                                )
                            } ?: "") + draft.body.take(60),
                            maxLines = 1,
                            color = glassColors.text.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )

                        FintrackLabelSmallText(
                            text = stringResource(Res.string.sms_confidence, localizedInteger(draft.confidence)),
                            color = if (draft.confidence >= 90) GlassGreen else glassColors.text3,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(onClick = onDelete, enabled = enabled, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete),
                        tint = GlassRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!hideBalance) {
                TextButton(onClick = { textExpanded = !textExpanded }) {
                    FintrackLabelMediumText(stringResource(if (textExpanded) Res.string.sms_hide_text else Res.string.sms_show_text),
                        color = LocalContentColor.current)
                }
                if (textExpanded) SelectionContainer {
                    FintrackBodyMediumText(draft.body, modifier = Modifier.fillMaxWidth())
                }
            }
            error?.let { FintrackInlineFeedback(it.asString(), isError = true, announce = false) }
            // Quick Pickers - Matching AddTransaction UI style
            if (amountReview != null) {
                FintrackLabelSmallText(text = stringResource(Res.string.sms_review_amount), color = GlassRed)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category Selection
                val selectedCategory = categories.find { it.id == draft.categoryId }
                FieldWithMostUsed(
                    label = stringResource(Res.string.category),
                    title = selectedCategory?.name ?: stringResource(Res.string.select_category),
                    iconRes = selectedCategory?.let { FinTrackIcons.findIcon(it.iconId).resource },
                    mostUsedItems = mostUsedCategories.filter { it.type == draft.type }.take(4),
                    enabled = enabled,
                    onPick = { onPick(PickerType.Category) },
                    onItemClick = { item ->
                        val category = item as Category
                        onUpdate(draft.copy(categoryId = category.id))
                    }
                )

                // Source Selection
                val selectedSource = sources.find { it.id == draft.sourceId }
                FieldWithMostUsed(
                    label = stringResource(Res.string.source),
                    title = selectedSource?.name ?: stringResource(Res.string.select_source),
                    iconRes = selectedSource?.let { FinTrackIcons.findIcon(it.iconId).resource },
                    mostUsedItems = mostUsedSources.take(4),
                    enabled = enabled,
                    onPick = { onPick(PickerType.Source) },
                    onItemClick = { item ->
                        val source = item as Source
                        onUpdate(draft.copy(sourceId = source.id))
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FintrackButton(
                    text = stringResource(when {
                        isReady && error != null -> Res.string.sms_retry
                        isReady -> Res.string.notif_action_register
                        amountReview != null -> Res.string.sms_review_amount
                        else -> Res.string.sms_complete_details
                    }),
                    onClick = onQuickRegister,
                    enabled = enabled,
                    modifier = Modifier.weight(1.5f).heightIn(min = 48.dp)
                )

                OutlinedButton(
                    onClick = onEdit,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    border = BorderStroke(1.dp, glassColors.glassEdge.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.edit),
                        color = glassColors.text2
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldWithMostUsed(
    label: String,
    title: String,
    iconRes: Any?,
    mostUsedItems: List<Any>,
    enabled: Boolean,
    onPick: () -> Unit,
    onItemClick: (Any) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Field(
            label = label,
            required = true,
            enabled = enabled,
            onClick = onPick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FintrackBodyMediumText(
                    text = title,
                    fontWeight = FontWeight.SemiBold
                )
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes as DrawableResource),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        if (mostUsedItems.isNotEmpty()) {
            MostUsedRow(
                items = mostUsedItems,
                enabled = enabled,
                onItemClick = onItemClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedRow(
    items: List<Any>,
    enabled: Boolean,
    onItemClick: (Any) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.label_most_used),
            fontSize = 10.sp,
            color = LocalGlassColors.current.text3
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val name = when (item) {
                    is Category -> item.name
                    is Source -> item.name
                    else -> ""
                }
                
                Chip(
                    color = GlassGreen,
                    enabled = enabled,
                    modifier = Modifier.heightIn(min = 48.dp),
                    onClick = { onItemClick(item) }
                ) {
                    FintrackLabelSmallText(
                        text = name,
                        color = GlassGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

package com.kazemieh.utilities.ui.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.common.model.FAQItem
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FAQScreen(
    viewModel: FAQViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    FintrackScreen(
        title = stringResource(Res.string.title_faq),
        sub = stringResource(Res.string.sub_faq),
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(space.medium),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            items(state.faqs) { faq ->
                FAQCard(
                    question = stringResource(FAQStringMapper.getQuestion(faq.id)),
                    answer = stringResource(FAQStringMapper.getAnswer(faq.id)),
                    isExpanded = state.expandedIds.contains(faq.id),
                    onToggle = { viewModel.onIntent(FAQIntent.ToggleExpand(faq.id)) }
                )
            }
        }
    }
}

@Composable
private fun FAQCard(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val space = LocalSpacing.current
    val rotation by animateFloatAsState(if (isExpanded) -90f else 0f)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier.padding(space.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackTitleMediumText(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    FintrackBodySmallText(
                        text = answer,
                        modifier = Modifier.padding(top = space.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
                    )
                }
            }
        }
    }
}

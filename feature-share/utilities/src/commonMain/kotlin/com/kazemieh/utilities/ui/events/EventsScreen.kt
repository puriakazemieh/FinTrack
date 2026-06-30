package com.kazemieh.utilities.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.common.model.FinancialEvent
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.common.toPersianDigits
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventsScreen(
    viewModel: EventsViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    FintrackScreen(
        title = stringResource(Res.string.title_events),
        sub = stringResource(Res.string.sub_events),
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(space.medium),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            items(state.events) { event ->
                val jalali = JalaliCalendar.fromTimestamp(event.date.toEpochMilliseconds())
                EventCard(
                    title = stringResource(EventStringMapper.getTitle(event.id)),
                    description = stringResource(EventStringMapper.getDescription(event.id)),
                    isMajor = event.isMajor,
                    date = "${jalali.day.toPersianDigits()} ${jalali.monthString}"
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    title: String,
    description: String,
    isMajor: Boolean,
    date: String
) {
    val space = LocalSpacing.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            Icon(
                if (isMajor) Icons.Default.Flag else Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isMajor) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                FintrackTitleMediumText(text = title, fontWeight = FontWeight.Bold)
                FintrackBodySmallText(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FintrackLabelMediumText(
                text = date,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

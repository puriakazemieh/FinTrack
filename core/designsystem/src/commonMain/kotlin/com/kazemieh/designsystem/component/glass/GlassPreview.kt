package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import com.kazemieh.designsystem.component.*
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*

@Preview
@Composable
fun GlassComponentsPreview() {
    FintrackTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassBg0)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FintrackHeadlineMediumText(stringResource(Res.string.app_name), color = GlassText)
            
            // 1. ScreenHeader
            ScreenHeader(
                title = stringResource(Res.string.navigation_transactions),
                sub = stringResource(Res.string.empty_title, stringResource(Res.string.transaction)),
                onBack = {},
                actions = listOf(
                    HeaderAction(rememberVectorPainter(Icons.Default.Notifications), stringResource(Res.string.label_transaction_notification), {}, badge = 1)
                )
            )

            // 2. SearchBar
            var searchQuery by remember { mutableStateOf("") }
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })

            // 3. Tabs
            var selectedTab by remember { mutableStateOf(0) }
            Tabs(
                tabs = listOf(stringResource(Res.string.today), stringResource(Res.string.this_week), stringResource(Res.string.this_month)),
                active = selectedTab,
                onChange = { selectedTab = it },
                counts = listOf(12, 45, 120)
            )

            // 4. GlassCard Variations
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(tone = GlassTone.Default, modifier = Modifier.weight(1f)) {
                    FintrackBodyLargeText(stringResource(Res.string.label_active), color = GlassText)
                }
                GlassCard(tone = GlassTone.Strong, modifier = Modifier.weight(1f)) {
                    FintrackBodyLargeText(stringResource(Res.string.label_inactive), color = GlassText)
                }
            }
            
            // 5. WidgetCard
            WidgetCard(title = stringResource(Res.string.label_budgets), count = 6, badge = stringResource(Res.string.label_monthly), accent = GlassGreen, onMore = {}) {
                Row(
                    label = stringResource(Res.string.label_home_purchase),
                    sub = stringResource(Res.string.hint_remaining_amount, "2,000,000".toFa()),
                    icon = GlassIcon(rememberVectorPainter(Icons.Default.Notifications), GlassGreenSoft, GlassGreen),
                    value = { MoneyText(amount = 1250000) }
                )
            }
            
            // 6. Form Components (Field, Switch, Chip)
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    var switchOn by remember { mutableStateOf(true) }
                    Field(label = stringResource(Res.string.label_transaction_notification), hint = stringResource(Res.string.hint_sms_on_register)) {
                        Switch(on = switchOn, onToggle = { switchOn = it })
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(active = true, onClick = {}) { FintrackLabelSmallText(stringResource(Res.string.label_active), color = GlassText) }
                        Chip(active = false, onClick = {}) { FintrackLabelSmallText(stringResource(Res.string.label_inactive), color = GlassText) }
                    }
                }
            }

            // 7. Fab
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Fab(label = stringResource(Res.string.add_transaction), icon = rememberVectorPainter(Icons.Default.Notifications), onClick = {})
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

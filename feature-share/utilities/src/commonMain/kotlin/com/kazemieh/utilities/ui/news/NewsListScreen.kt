package com.kazemieh.utilities.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_read_time
import fintrack.core.designsystem.generated.resources.sub_news
import fintrack.core.designsystem.generated.resources.title_news
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewsListScreen(
    viewModel: NewsViewModel = koinViewModel(),
    onArticleClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    FintrackScreen(
        title = stringResource(Res.string.title_news),
        sub = stringResource(Res.string.sub_news),
        onBack = onBackClick
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(space.medium),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            items(state.news) { article ->
                NewsCard(
                    title = article.title,
                    category = stringResource(NewsStringMapper.getCategory(article.category)),
                    summary = article.summary,
                    readTime = article.readTimeMinutes,
                    onClick = { onArticleClick(article.id) }
                )
            }
        }
    }
}

@Composable
private fun NewsCard(
    title: String,
    category: String,
    summary: String,
    readTime: Int,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(space.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Chip(color = MaterialTheme.colorScheme.primaryContainer, onClick = {

                }) {
                    FintrackLabelMediumText(
                        text = category,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                FintrackLabelMediumText(
                    text = stringResource(Res.string.label_read_time, readTime),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FintrackTitleMediumText(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = space.small)
            )

            FintrackBodySmallText(
                text = summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

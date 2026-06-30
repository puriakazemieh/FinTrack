package com.kazemieh.utilities.ui.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArticleReaderScreen(
    articleId: String,
    viewModel: NewsViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    LaunchedEffect(articleId) {
        viewModel.onIntent(NewsIntent.SelectArticle(articleId))
    }

    val article = state.selectedArticle

    FintrackScreen(
        title = article?.let { stringResource(NewsStringMapper.getCategory(it.category)) } ?: stringResource(Res.string.label_article),
        onBack = onBackClick
    ) {
        if (article != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(space.medium)
            ) {
                FintrackTitleLargeText(
                    text = stringResource(NewsStringMapper.getTitle(article.id)),
                    fontWeight = FontWeight.Black,
//                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.2
                )
                
                Row(modifier = Modifier.padding(vertical = space.medium)) {
                    FintrackLabelMediumText(
                        text = stringResource(NewsStringMapper.getSource(article.source)),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.label_read_time, article.readTimeMinutes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FintrackBodyMediumText(
                    text = stringResource(NewsStringMapper.getContent(article.id)),
//                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                )
                
                Spacer(modifier = Modifier.height(space.extraLarge))
            }
        }
    }
}

package com.kazemieh.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.*
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                ProfileEditEffect.ProfileSaved -> onBack()
                is ProfileEditEffect.ShowError -> {
                    // Handle error show
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(Res.string.title_edit_profile),
                onBack = onBack,
                modifier = Modifier.padding(horizontal = space.large)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = space.large),
                verticalArrangement = Arrangement.spacedBy(space.medium),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    CompletionCard(state.completionPercentage)
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PhotoDrop(
                            photoBitmap = state.avatar?.decodeToImageBitmap(),
                            onImagePicked = { viewModel.onIntent(ProfileEditIntent.UpdateAvatar(it)) },
                            onRemove = { /* Handle remove avatar */ }
                        )
                    }
                }

                item {
                    WidgetCard(
                        title = stringResource(Res.string.section_personal_info),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileField(
                            label = stringResource(Res.string.label_first_name),
                            value = state.firstName,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateFirstName(it)) },
                            placeholder = stringResource(Res.string.hint_enter_first_name)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_last_name),
                            value = state.lastName,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateLastName(it)) },
                            placeholder = stringResource(Res.string.hint_enter_last_name)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_email),
                            value = state.email,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateEmail(it)) },
                            placeholder = stringResource(Res.string.hint_enter_email)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_phone),
                            value = state.phone,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdatePhone(it)) },
                            placeholder = stringResource(Res.string.hint_enter_phone)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_birthday),
                            value = state.birthday,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateBirthday(it)) },
                            placeholder = stringResource(Res.string.hint_enter_birthday)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_city),
                            value = state.city,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateCity(it)) },
                            placeholder = stringResource(Res.string.hint_enter_city)
                        )
                    }
                }

                item {
                    WidgetCard(
                        title = stringResource(Res.string.section_extra_info),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileField(
                            label = stringResource(Res.string.label_monthly_income),
                            value = state.income,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateIncome(it)) },
                            placeholder = stringResource(Res.string.hint_enter_income)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_job_title),
                            value = state.jobTitle,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateJobTitle(it)) },
                            placeholder = stringResource(Res.string.hint_enter_job)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_financial_goal),
                            value = state.financialGoal,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdateFinancialGoal(it)) },
                            placeholder = stringResource(Res.string.hint_enter_goal)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(space.large)
        ) {
            Fab(
                label = stringResource(Res.string.action_save_profile),
                icon = rememberVectorPainter(Icons.Default.Save),
                onClick = { viewModel.onIntent(ProfileEditIntent.SaveProfile) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val space = LocalSpacing.current
    Column(modifier = Modifier.padding(vertical = space.small)) {
        FintrackOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CompletionCard(percentage: Float) {
    val space = LocalSpacing.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FintrackTitleMediumText(text = stringResource(Res.string.profile_completion))
                FintrackBodySmallText(
                    text = stringResource(Res.string.profile_completion_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(space.medium))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                FintrackLabelMediumText(text = "${(percentage * 100).toInt()}%")
            }
        }
    }
}

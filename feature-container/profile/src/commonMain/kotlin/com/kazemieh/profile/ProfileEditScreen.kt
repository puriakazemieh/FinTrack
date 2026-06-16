package com.kazemieh.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FourDigitGroupingTransformation
import com.kazemieh.designsystem.component.glass.Fab
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.PhotoDrop
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.financialsource.ui.add.AddSourceIntent
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_save_profile
import fintrack.core.designsystem.generated.resources.hint_enter_birthday
import fintrack.core.designsystem.generated.resources.hint_enter_city
import fintrack.core.designsystem.generated.resources.hint_enter_email
import fintrack.core.designsystem.generated.resources.hint_enter_first_name
import fintrack.core.designsystem.generated.resources.hint_enter_goal
import fintrack.core.designsystem.generated.resources.hint_enter_income
import fintrack.core.designsystem.generated.resources.hint_enter_job
import fintrack.core.designsystem.generated.resources.hint_enter_last_name
import fintrack.core.designsystem.generated.resources.hint_enter_phone
import fintrack.core.designsystem.generated.resources.label_account_number
import fintrack.core.designsystem.generated.resources.label_birthday
import fintrack.core.designsystem.generated.resources.label_city
import fintrack.core.designsystem.generated.resources.label_email
import fintrack.core.designsystem.generated.resources.label_financial_goal
import fintrack.core.designsystem.generated.resources.label_first_name
import fintrack.core.designsystem.generated.resources.label_job_title
import fintrack.core.designsystem.generated.resources.label_last_name
import fintrack.core.designsystem.generated.resources.label_monthly_income
import fintrack.core.designsystem.generated.resources.label_optional_fa
import fintrack.core.designsystem.generated.resources.label_phone
import fintrack.core.designsystem.generated.resources.profile_completion
import fintrack.core.designsystem.generated.resources.profile_completion_desc
import fintrack.core.designsystem.generated.resources.section_extra_info
import fintrack.core.designsystem.generated.resources.section_personal_info
import fintrack.core.designsystem.generated.resources.title_edit_profile
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    val showDatePicker = remember { mutableStateOf(false) }

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

    JalaliDatePickerBottomSheet(
        openSheet = showDatePicker,
        initialDate = JalaliCalendar(1373, 1, 1),
        onConfirm = { jalali ->
            viewModel.onIntent(ProfileEditIntent.UpdateBirthday("${jalali.year}/${jalali.month}/${jalali.day}"))
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative background blobs for glass effect
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = 400.dp.toPx()
                ),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = 400.dp.toPx()
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = 500.dp.toPx()
                ),
                center = Offset(size.width * 0.2f, size.height * 0.8f),
                radius = 500.dp.toPx()
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(Res.string.title_edit_profile),
                onBack = onBack,
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = space.large),
                verticalArrangement = Arrangement.spacedBy(space.medium),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
//                item {
//                    CompletionCard(state.completionPercentage)
//                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = space.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        PhotoDrop(
                            photoBitmap = state.avatar?.decodeToImageBitmap(),
                            onImagePicked = { viewModel.onIntent(ProfileEditIntent.UpdateAvatar(it)) },
                            onRemove = { viewModel.onIntent(ProfileEditIntent.RemoveAvatar) },
                            modifier = Modifier.size(166.dp) // Larger square size
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
                            onValueChange = {
                                viewModel.onIntent(
                                    ProfileEditIntent.UpdateFirstName(
                                        it
                                    )
                                )
                            },
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
                            placeholder = stringResource(Res.string.hint_enter_email),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_phone),
                            value = state.phone,
                            onValueChange = { viewModel.onIntent(ProfileEditIntent.UpdatePhone(it)) },
                            placeholder = stringResource(Res.string.hint_enter_phone),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        ProfileField(
                            label = stringResource(Res.string.label_birthday),
                            value = state.birthday,
                            onValueChange = { },
                            placeholder = stringResource(Res.string.hint_enter_birthday),
                            readOnly = true,
                            onClick = { showDatePicker.value = true }
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
                            placeholder = stringResource(Res.string.hint_enter_income),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isPrice = true
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
                            onValueChange = {
                                viewModel.onIntent(
                                    ProfileEditIntent.UpdateFinancialGoal(
                                        it
                                    )
                                )
                            },
                            placeholder = stringResource(Res.string.hint_enter_goal)
                        )
                    }
                }
                item {
                    Fab(
                        label = stringResource(Res.string.action_save_profile),
                        icon = rememberVectorPainter(Icons.Default.Save),
                        onClick = { viewModel.onIntent(ProfileEditIntent.SaveProfile) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(Modifier.height(4.dp))
                }
            }

        }


    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPrice: Boolean = false,
    readOnly: Boolean = false,
    isOptional: Boolean = true,
    onClick: () -> Unit = {}
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    Column(modifier = Modifier.padding(vertical = space.small)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                FintrackBodyMediumText(
                    text = placeholder,
                    color = glassColors.text3
                )
            },
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FintrackLabelSmallText(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = glassColors.text2
                    )
                    if (isOptional) {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.optional_wrapper, stringResource(Res.string.label_optional_fa)),
                            style = MaterialTheme.typography.labelSmall,
                            color = glassColors.text2
                        )
                    }
                }
            },
            keyboardOptions = keyboardOptions,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = GlassGreen
            ),
        )
    }
}

@Composable
fun CompletionCard(percentage: Float) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FintrackTitleMediumText(text = stringResource(Res.string.profile_completion), color = glassColors.text)
                FintrackBodySmallText(
                    text = stringResource(Res.string.profile_completion_desc),
                    color = glassColors.text2
                )
            }
            Spacer(modifier = Modifier.width(space.medium))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = glassColors.glass
                )
                FintrackLabelMediumText(text = stringResource(Res.string.percentage_format, (percentage * 100).toInt()), color = glassColors.text)
            }
        }
    }
}

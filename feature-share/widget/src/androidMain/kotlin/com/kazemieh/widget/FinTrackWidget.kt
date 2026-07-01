package com.kazemieh.widget

import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProviders as GlanceColorProviders
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.nowPersianDate
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.ThemeMode
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

import android.content.Intent
import android.net.Uri

class FinTrackWidget : GlanceAppWidget(), KoinComponent {

    private val repository: TransactionRepository by inject()
    private val preferenceUseCases: PreferenceUseCases by inject()

    // Helper to get resource ID without relying on R class
    private fun getResId(context: Context, name: String, type: String, pkg: String = context.packageName): Int {
        return context.resources.getIdentifier(name, type, pkg)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        val now = Clock.System.nowPersianDate(TimeZone.currentSystemDefault())
        val startOfDay = PersianDateTime(now.year, now.month, now.day, 0, 0, 0).toEpochMilliseconds()
        val endOfDay = PersianDateTime(now.year, now.month, now.day, 23, 59, 59).toEpochMilliseconds()
        
        val params = TransactionFilterParams(
            fromTimestamp = startOfDay,
            toTimestamp = endOfDay
        )
        
        val categorySums = try {
            repository.observeCategorySums(params).first()
        } catch (e: Exception) {
            emptyList()
        }
        
        val income = categorySums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
        val expense = categorySums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }

        val colors = getWidgetColors(context)

        provideContent {
            GlanceTheme(colors = colors) {
                WidgetContent(
                    context = context,
                    income = income,
                    expense = expense
                )
            }
        }
    }

    private fun getWidgetColors(context: Context): GlanceColorProviders {
        val currentThemeName = preferenceUseCases.getStringPreference(
            FinTrackPreferences.PREF_THEME,
            AppTheme.GLASS_DARK.name
        )
        val themeModeName = preferenceUseCases.getStringPreference(
            FinTrackPreferences.PREF_THEME_MODE,
            ThemeMode.MANUAL.name
        )
        val themeStartTime = preferenceUseCases.getStringPreference(
            FinTrackPreferences.PREF_THEME_START_TIME,
            "20:00"
        )
        val themeEndTime = preferenceUseCases.getStringPreference(
            FinTrackPreferences.PREF_THEME_END_TIME,
            "07:00"
        )

        val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val theme = try { AppTheme.valueOf(currentThemeName) } catch (e: Exception) { AppTheme.GLASS_DARK }
        val mode = try { ThemeMode.valueOf(themeModeName) } catch (e: Exception) { ThemeMode.MANUAL }

        val calculatedTheme = if (mode == ThemeMode.MANUAL) {
            theme
        } else {
            val shouldBeDark = when (mode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.SUNRISE_SUNSET -> isTimeInRange("18:00", "06:00")
                ThemeMode.CUSTOM_TIME -> isTimeInRange(themeStartTime, themeEndTime)
                else -> theme.isDark
            }
            if (shouldBeDark) theme.toDark() else theme.toLight()
        }

        return when (calculatedTheme) {
            AppTheme.GLASS_DARK -> WidgetColorPalette.GlassDark
            AppTheme.GLASS_LIGHT -> WidgetColorPalette.GlassLight
            AppTheme.PLAIN_DARK -> WidgetColorPalette.PlainDark
            AppTheme.PLAIN_LIGHT -> WidgetColorPalette.PlainLight
        }
    }

    private fun isTimeInRange(start: String, end: String): Boolean {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            val startParts = start.split(":").map { it.toInt() }
            val endParts = end.split(":").map { it.toInt() }

            val startTime = LocalTime(startParts[0], startParts[1])
            val endTime = LocalTime(endParts[0], endParts[1])
            val currentTime = LocalTime(now.hour, now.minute)

            if (startTime <= endTime) {
                currentTime in startTime..endTime
            } else {
                currentTime >= startTime || currentTime <= endTime
            }
        } catch (e: Exception) {
            false
        }
    }

    @Composable
    private fun WidgetContent(context: Context, income: Long, expense: Long) {
        val bgId = getResId(context, "widget_background", "drawable")
        val todaySummaryId = getResId(context, "today_summary", "string", "com.kazemieh.designsystem")
        val incomeId = getResId(context, "income_widget", "string", "com.kazemieh.designsystem")
        val expenseId = getResId(context, "expense_widget", "string", "com.kazemieh.designsystem")
        val quickAddId = getResId(context, "quick_add", "string", "com.kazemieh.designsystem")

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(bgId))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(todaySummaryId),
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
                
                Text(
                    text = "↻",
                    style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 18.sp),
                    modifier = GlanceModifier.padding(4.dp).clickable(actionRunCallback<RefreshAction>())
                )
            }
            
            Spacer(modifier = GlanceModifier.padding(8.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    label = context.getString(incomeId),
                    amount = income,
                    color = GlanceTheme.colors.primary
                )
                Spacer(modifier = GlanceModifier.width(12.dp))
                SummaryItem(
                    label = context.getString(expenseId),
                    amount = expense,
                    color = GlanceTheme.colors.error
                )
            }
            
            Spacer(modifier = GlanceModifier.padding(12.dp))
            
            Button(
                text = context.getString(quickAddId),
                onClick = actionStartActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("fintrack://dashboard?showAddTransaction=true")
                    ).apply {
                        `package` = context.packageName
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun SummaryItem(label: String, amount: Long, color: ColorProvider) {
        val context = androidx.glance.LocalContext.current
        val itemBgId = getResId(context, "widget_item_background", "drawable")
        
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(itemBgId))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label, 
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = GlanceModifier.padding(2.dp))
            Text(
                text = amount.toPersianPrice(),
                style = TextStyle(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

object WidgetColorPalette {
    val GlassDark = ColorProviders(
        light = darkColorScheme(), // Not used for GlassDark
        dark = darkColorScheme(
            primary = Color(0xFF22C55E),
            onPrimary = Color(0xFF06281A),
            background = Color(0xFF06100E),
            onBackground = Color(0xFFF3F5F4),
            surface = Color(0xFF0C1D18),
            onSurface = Color(0xFFF3F5F4),
            onSurfaceVariant = Color(0xA6F3F5F4),
            error = Color(0xFFEF4444)
        )
    )

    val GlassLight = ColorProviders(
        light = lightColorScheme(
            primary = Color(0xFF16A34A),
            onPrimary = Color.White,
            background = Color(0xFFF0F4F3),
            onBackground = Color(0xFF06100E),
            surface = Color.White,
            onSurface = Color(0xFF06100E),
            onSurfaceVariant = Color(0xFF6B7280),
            error = Color(0xFFEF4444)
        ),
        dark = lightColorScheme() // Not used for GlassLight
    )

    val PlainDark = ColorProviders(
        light = darkColorScheme(),
        dark = darkColorScheme(
            primary = Color(0xFF7CDB76),
            onPrimary = Color(0xFF00390A),
            background = Color(0xFF222622),
            onBackground = Color(0xFFE8E9E3),
            surface = Color(0xFF404438),
            onSurface = Color(0xFFE8E9E3),
            onSurfaceVariant = Color(0xFFB5D1AE),
            error = Color(0xFFFFB4AB)
        )
    )

    val PlainLight = ColorProviders(
        light = lightColorScheme(
            primary = Color(0xFF006E1C),
            onPrimary = Color.White,
            background = Color(0xFFFCFDF6),
            onBackground = Color(0xFF1A1C18),
            surface = Color(0xFFFCFDF6),
            onSurface = Color(0xFF1A1C18),
            onSurfaceVariant = Color(0xFF52634F),
            error = Color(0xFFBA1A1A)
        ),
        dark = lightColorScheme()
    )
}

class RefreshAction : androidx.glance.appwidget.action.ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        FinTrackWidget().updateAll(context)
    }
}

package com.kazemieh.designsystem


import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.vazirmatn_regular
import fintrack.core.designsystem.generated.resources.vazirmatn_medium
import fintrack.core.designsystem.generated.resources.vazirmatn_semibold
import fintrack.core.designsystem.generated.resources.vazirmatn_bold
import fintrack.core.designsystem.generated.resources.vazirmatn_extrabold
import org.jetbrains.compose.resources.Font


@Composable
fun vazirmatnFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.vazirmatn_regular, FontWeight.W400),
        Font(Res.font.vazirmatn_medium, FontWeight.W500),
        Font(Res.font.vazirmatn_semibold, FontWeight.W600),
        Font(Res.font.vazirmatn_bold, FontWeight.W700),
        Font(Res.font.vazirmatn_extrabold, FontWeight.W800)
    )
}

@Composable
fun fintrackTypography(): Typography {
    val family = vazirmatnFontFamily()
    
    return Typography(
        // Hero balance: 32 / 700 / -0.02em
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W700,
            fontSize = 32.sp,
            letterSpacing = (-0.64).sp // 32 * -0.02
        ),
        // Screen title: 17-20 / 700 / -0.01em
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
            letterSpacing = (-0.2).sp // 20 * -0.01
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W700,
            fontSize = 18.sp,
            letterSpacing = (-0.18).sp
        ),
        // Card/section title: 13-15 / 600-700
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W700,
            fontSize = 15.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W600,
            fontSize = 13.sp
        ),
        // Body: 12-13 / 500
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W500,
            fontSize = 13.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp
        ),
        // Sub / meta: 10-11 / 400-500
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W500,
            fontSize = 11.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W400,
            fontSize = 10.sp
        ),
        // Micro / chip: 9-10 / 500-700
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W700,
            fontSize = 10.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.W500,
            fontSize = 9.sp
        )
    )
}
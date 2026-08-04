package com.qingyu.hermescompanion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppSans = FontFamily.SansSerif

private fun appText(weight: FontWeight, size: androidx.compose.ui.unit.TextUnit, line: androidx.compose.ui.unit.TextUnit) = TextStyle(
    fontFamily = AppSans,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
)

val HermesTypography = Typography(
    headlineLarge = appText(FontWeight.Bold, 26.sp, 34.sp),
    headlineMedium = appText(FontWeight.SemiBold, 23.sp, 30.sp),
    headlineSmall = appText(FontWeight.SemiBold, 20.sp, 27.sp),
    titleLarge = appText(FontWeight.SemiBold, 20.sp, 27.sp),
    titleMedium = appText(FontWeight.Medium, 16.sp, 23.sp),
    titleSmall = appText(FontWeight.Medium, 14.sp, 20.sp),
    bodyLarge = appText(FontWeight.Normal, 15.5.sp, 23.sp),
    bodyMedium = appText(FontWeight.Normal, 14.sp, 21.sp),
    bodySmall = appText(FontWeight.Normal, 12.5.sp, 18.sp),
    labelLarge = appText(FontWeight.Medium, 13.5.sp, 19.sp),
    labelMedium = appText(FontWeight.Medium, 12.sp, 17.sp),
    labelSmall = appText(FontWeight.Medium, 11.sp, 16.sp),
)

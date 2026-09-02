package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Blue Palette
val BokaroBluePrimary = Color(0xFF0052CC)
val BokaroBlueDark = Color(0xFF0747A6)
val BokaroBlueLight = Color(0xFFDEEBFF)
val BokaroBlueContainer = Color(0xFFEBF3FF)
val BokaroNavy = Color(0xFF172B4D)

// Secondary Sky / Cyan
val BokaroSky = Color(0xFF00A3BF)
val BokaroSkyContainer = Color(0xFFE6FCFF)

// Status Colors
val StatusPendingBg = Color(0xFFFEF3C7)
val StatusPendingText = Color(0xFFB45309)

val StatusAcceptedBg = Color(0xFFDBEAFE)
val StatusAcceptedText = Color(0xFF1D4ED8)

val StatusOnTheWayBg = Color(0xFFEDE9FE)
val StatusOnTheWayText = Color(0xFF6D28D9)

val StatusWorkStartedBg = Color(0xFFCFFAFE)
val StatusWorkStartedText = Color(0xFF0E7490)

val StatusCompletedBg = Color(0xFFD1FAE5)
val StatusCompletedText = Color(0xFF047857)

val StatusCancelledBg = Color(0xFFFEE2E2)
val StatusCancelledText = Color(0xFFB91C1C)

// Neutral Canvas & Surface
val CanvasBg = Color(0xFFF8FAFC)
val CardWhite = Color(0xFFFFFFFF)
val BorderSubtle = Color(0xFFE2E8F0)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF94A3B8)
val AccentGold = Color(0xFFF59E0B)

@androidx.compose.runtime.Composable
fun bokaroTextFieldColors(): androidx.compose.material3.TextFieldColors =
    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        disabledTextColor = Color(0xFF64748B),
        errorTextColor = Color(0xFFDC2626),
        focusedContainerColor = CardWhite,
        unfocusedContainerColor = CardWhite,
        disabledContainerColor = Color(0xFFF8FAFC),
        cursorColor = BokaroBluePrimary,
        errorCursorColor = Color(0xFFDC2626),
        focusedBorderColor = BokaroBluePrimary,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        disabledBorderColor = Color(0xFFE2E8F0),
        errorBorderColor = Color(0xFFDC2626),
        focusedLabelColor = BokaroBluePrimary,
        unfocusedLabelColor = TextSecondary,
        disabledLabelColor = TextMuted,
        errorLabelColor = Color(0xFFDC2626),
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted,
        focusedLeadingIconColor = BokaroBluePrimary,
        unfocusedLeadingIconColor = TextSecondary,
        focusedTrailingIconColor = BokaroBluePrimary,
        unfocusedTrailingIconColor = TextSecondary
    )

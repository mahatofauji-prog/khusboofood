package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// PREMIUM BLACK & GOLD THEME COLOR SYSTEM
// ==========================================

// Core Backgrounds (80% Luxurious Deep Black & Charcoal)
val APP_BACKGROUND = Color(0xFF0B0B0B)        // Deep Black Primary Background
val SECTION_BACKGROUND = Color(0xFF151515)    // Secondary Charcoal Background / Surfaces
val CARD_BACKGROUND = Color(0xFF1C1C1C)       // Dark Charcoal Card Background
val CARD_SURFACE_ELEVATED = Color(0xFF222222) // Elevated Surface Container

// Core Gold Palette (20% Premium Gold Accents)
val PRIMARY_GOLD = Color(0xFFD4AF37)          // Primary Classic Gold
val BRIGHT_GOLD = Color(0xFFF5C542)           // Bright Gold Accent / Price / Highlights
val MUTED_GOLD = Color(0xFFC59B27)            // Warm Deep Gold
val GOLD_CONTAINER = Color(0xFF262010)        // Dark Charcoal with Subtle Warm Gold Glow
val NAV_INDICATOR_COLOR = Color(0xFF242014)   // Navigation Bar Indicator Glow

// Typography Colors
val TEXT_PRIMARY = Color(0xFFFFFFFF)          // Pure White for Headings, Titles & Product Names
val TEXT_SECONDARY = Color(0xFFBDBDBD)        // Light Gray for Descriptions & Subtitles
val TEXT_MUTED = Color(0xFF8E8E93)            // Subtle Gray for Placeholders & Captions

// Borders & Dividers
val BORDER = Color(0xFF2C2818)                // Dark Gray with Subtle Gold Warmth
val BORDER_GOLD = Color(0xFF4A3E1E)           // Subtle Gold Accent Border
val BORDER_BRIGHT_GOLD = Color(0xFFD4AF37)    // Active Gold Border

// Component Mapping Constants (Maintains seamless functional compatibility across all screens)
val PRIMARY_YELLOW = Color(0xFF121212)        // TopAppBar & NavigationBar Deep Charcoal Background
val LIGHT_YELLOW = GOLD_CONTAINER             // Gold-tinted Container for active states
val ORANGE = BRIGHT_GOLD                      // Primary Action / Price / Highlight Gold
val DARK_ORANGE = PRIMARY_GOLD                // Selected Navigation / Active Tab Gold
val SUCCESS_GREEN = Color(0xFF4EBA6F)         // Luxury Emerald Green for Success
val ERROR_RED = Color(0xFFE55353)             // Vibrant Crimson Red for Errors & Cancel

// Compatibility Aliases for entire application
val BgPrimary = APP_BACKGROUND
val SoftWhite = CARD_BACKGROUND
val PrimarySoftYellow = PRIMARY_GOLD
val HeaderYellow = PRIMARY_YELLOW
val VeryLightYellow = SECTION_BACKGROUND
val WarmGoldAccent = BRIGHT_GOLD
val DarkText = Color(0xFF0B0B0B)              // Black Text for Gold Buttons & Selected Chips
val ButtonTextDark = Color(0xFF0B0B0B)        // High-contrast Black Text on Gold Buttons
val SecondaryText = TEXT_SECONDARY
val SECONDARY_TEXT = TEXT_SECONDARY
val BorderColor = BORDER
val InactiveIconColor = Color(0xFF8E8E93)      // Light Gray for Inactive Nav Icons
val SuccessGreen = SUCCESS_GREEN
val ErrorRed = ERROR_RED
val CREAM = SECTION_BACKGROUND
val TEXT = TEXT_PRIMARY
val WHITE = Color(0xFF0B0B0B)                 // Ensures all onPrimary / button text renders crisp Black on Gold
val PURE_WHITE = Color(0xFFFFFFFF)

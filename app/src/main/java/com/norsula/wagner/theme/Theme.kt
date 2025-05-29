package com.norsula.wagner.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import com.norsula.wagner.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle





private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

val IrinaFontFamily = FontFamily(
    Font(R.font.irinactt, FontWeight.Normal)
)

private val CustomTypography = Typography(
    displayLarge = TextStyle(fontFamily = IrinaFontFamily),
    displayMedium = TextStyle(fontFamily = IrinaFontFamily),
    displaySmall = TextStyle(fontFamily = IrinaFontFamily),
    headlineLarge = TextStyle(fontFamily = IrinaFontFamily),
    headlineMedium = TextStyle(fontFamily = IrinaFontFamily),
    headlineSmall = TextStyle(fontFamily = IrinaFontFamily),
    titleLarge = TextStyle(fontFamily = IrinaFontFamily),
    titleMedium = TextStyle(fontFamily = IrinaFontFamily),
    titleSmall = TextStyle(fontFamily = IrinaFontFamily),
    bodyLarge = TextStyle(fontFamily = IrinaFontFamily),
    bodyMedium = TextStyle(fontFamily = IrinaFontFamily),
    bodySmall = TextStyle(fontFamily = IrinaFontFamily),
    labelLarge = TextStyle(fontFamily = IrinaFontFamily),
    labelMedium = TextStyle(fontFamily = IrinaFontFamily),
    labelSmall = TextStyle(fontFamily = IrinaFontFamily)
)


@Composable
fun WagnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustomTypography,
        content = content
    )
}
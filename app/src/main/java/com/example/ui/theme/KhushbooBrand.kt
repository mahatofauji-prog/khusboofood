package com.example.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

enum class BrandSize {
    COMPACT, NORMAL, LARGE
}

// Deep Warm Roasted Cocoa Brown (#2E1805) for maximum crisp readability on #F8D96B yellow header
val GOLDEN_BRAND_COLOR = Color(0xFF2E1805)

@Composable
fun KhushbooFoodWordmark(
    modifier: Modifier = Modifier,
    brandSize: BrandSize = BrandSize.NORMAL,
    color: Color = GOLDEN_BRAND_COLOR
) {
    val wordmarkFontSize = when (brandSize) {
        BrandSize.COMPACT -> 16.sp
        BrandSize.NORMAL -> 20.sp
        BrandSize.LARGE -> 28.sp
    }

    Text(
        text = "Khushboo Food",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = wordmarkFontSize,
        letterSpacing = 0.5.sp,
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
fun KhushbooBrandHeader(
    modifier: Modifier = Modifier,
    subtitle: String = "✨ Taste Of India",
    brandSize: BrandSize = BrandSize.NORMAL,
    wordmarkColor: Color = GOLDEN_BRAND_COLOR
) {
    val logoSize = when (brandSize) {
        BrandSize.COMPACT -> 30.dp
        BrandSize.NORMAL -> 36.dp
        BrandSize.LARGE -> 52.dp
    }

    val subtitleFontSize = when (brandSize) {
        BrandSize.COMPACT -> 9.5.sp
        BrandSize.NORMAL -> 10.5.sp
        BrandSize.LARGE -> 13.sp
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_uploaded_logo),
            contentDescription = "Khushboo Food Logo",
            modifier = Modifier
                .size(logoSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            verticalArrangement = Arrangement.Top
        ) {
            KhushbooFoodWordmark(
                brandSize = brandSize,
                color = wordmarkColor
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = subtitleFontSize,
                    color = Color(0xFF5E3A0E), // Warm Golden Tagline Tone
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    maxLines = 1,
                    modifier = Modifier.offset(y = (-4).dp) // Tightly bring tagline close underneath brand name
                )
            }
        }
    }
}

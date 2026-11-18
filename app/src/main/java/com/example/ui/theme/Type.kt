package com.example.ui.theme

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.local.ImportedFontSpec
import java.io.File

val CairoFontFamily = FontFamily(Font(R.font.cairo, FontWeight.Normal))
val AmiriFontFamily = FontFamily(Font(R.font.amiri, FontWeight.Normal))
val VazirmatnFontFamily = FontFamily(Font(R.font.vazirmatn, FontWeight.Normal))
val TajawalFontFamily = FontFamily(Font(R.font.tajawal, FontWeight.Normal))
val AlmaraiFontFamily = FontFamily(Font(R.font.almarai, FontWeight.Normal))
val NotoKufiArabicFontFamily = FontFamily(Font(R.font.noto_kufi_arabic, FontWeight.Normal))
val PlusJakartaSansFontFamily = FontFamily(Font(R.font.plus_jakarta_sans, FontWeight.Normal))
val PlayfairDisplayFontFamily = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
val RobotoMonoFontFamily = FontFamily(Font(R.font.roboto_mono, FontWeight.Normal))
val MontserratFontFamily = FontFamily(Font(R.font.montserrat, FontWeight.Normal))
val OutfitFontFamily = FontFamily(Font(R.font.outfit, FontWeight.Normal))
val LoraFontFamily = FontFamily(Font(R.font.lora, FontWeight.Normal))

@Immutable
data class SlideFontOption(
    val id: String,
    val displayName: String,
    val composeFamily: FontFamily,
    val fontResId: Int?,
    val customFilePath: String? = null
) {
    val isCustom: Boolean get() = !customFilePath.isNullOrBlank()
}

object SlideFontRegistry {
    val builtInOptions: List<SlideFontOption> = listOf(
        SlideFontOption("cairo", "Cairo (عربي حديث)", CairoFontFamily, R.font.cairo),
        SlideFontOption("amiri", "Amiri (نسخ أكاديمي)", AmiriFontFamily, R.font.amiri),
        SlideFontOption("tajawal", "Tajawal (عربي هندسي)", TajawalFontFamily, R.font.tajawal),
        SlideFontOption("almarai", "Almarai (واضح للعناوين)", AlmaraiFontFamily, R.font.almarai),
        SlideFontOption("noto_kufi_arabic", "Noto Kufi (كوفي أنيق)", NotoKufiArabicFontFamily, R.font.noto_kufi_arabic),
        SlideFontOption("vazirmatn", "Vazirmatn (فارسی/عربی)", VazirmatnFontFamily, R.font.vazirmatn),
        SlideFontOption("plus_jakarta_sans", "Plus Jakarta Sans", PlusJakartaSansFontFamily, R.font.plus_jakarta_sans),
        SlideFontOption("montserrat", "Montserrat", MontserratFontFamily, R.font.montserrat),
        SlideFontOption("outfit", "Outfit Modern", OutfitFontFamily, R.font.outfit),
        SlideFontOption("playfair_display", "Playfair Display", PlayfairDisplayFontFamily, R.font.playfair_display),
        SlideFontOption("lora", "Lora Editorial", LoraFontFamily, R.font.lora),
        SlideFontOption("roboto_mono", "Roboto Mono", RobotoMonoFontFamily, R.font.roboto_mono),
        SlideFontOption("default", "System Sans", FontFamily.SansSerif, null)
    )

    var customOptions by mutableStateOf<List<SlideFontOption>>(emptyList())
        private set

    val options: List<SlideFontOption>
        get() = builtInOptions + customOptions

    fun setCustomFonts(entries: List<Pair<String, ImportedFontSpec>>) {
        customOptions = entries.mapNotNull { (_, spec) ->
            val file = File(spec.filePath)
            if (!file.exists()) return@mapNotNull null
            val family = runCatching { FontFamily(Font(file, FontWeight.Normal)) }
                .getOrDefault(FontFamily.SansSerif)
            SlideFontOption(
                id = spec.id,
                displayName = spec.displayName,
                composeFamily = family,
                fontResId = null,
                customFilePath = spec.filePath
            )
        }
    }

    fun registerCustomFontInMemory(spec: ImportedFontSpec) {
        val file = File(spec.filePath)
        val family = if (file.exists()) {
            runCatching { FontFamily(Font(file, FontWeight.Normal)) }.getOrDefault(FontFamily.SansSerif)
        } else {
            FontFamily.SansSerif
        }
        val opt = SlideFontOption(
            id = spec.id,
            displayName = spec.displayName,
            composeFamily = family,
            fontResId = null,
            customFilePath = spec.filePath
        )
        customOptions = customOptions.filterNot { it.id == spec.id } + opt
    }

    fun fontFamilyFor(id: String): FontFamily =
        options.firstOrNull { it.id == id }?.composeFamily ?: CairoFontFamily

    fun displayNameFor(id: String): String =
        options.firstOrNull { it.id == id }?.displayName ?: "Cairo"

    fun typefaceFor(context: Context, id: String, isBold: Boolean, isItalic: Boolean): Typeface {
        val option = options.firstOrNull { it.id == id }
        val base = when {
            option?.customFilePath != null -> {
                val f = File(option.customFilePath)
                if (f.exists()) runCatching { Typeface.createFromFile(f) }.getOrNull() ?: Typeface.SANS_SERIF
                else Typeface.SANS_SERIF
            }
            option?.fontResId != null -> {
                runCatching { ResourcesCompat.getFont(context, option.fontResId) }.getOrNull()
                    ?: Typeface.SANS_SERIF
            }
            else -> Typeface.SANS_SERIF
        }
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        return Typeface.create(base, style)
    }
}

val KatebTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

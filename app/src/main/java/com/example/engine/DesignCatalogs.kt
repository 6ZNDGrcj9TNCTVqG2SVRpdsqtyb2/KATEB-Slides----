package com.example.engine

import com.example.core.localization.AppLanguage

enum class ColorPaletteGroup(val ar: String, val en: String) {
    BLUES("أزرق وملكي", "Royal Blues"),
    GREENS("أخضر وزمردي", "Emerald & Teal"),
    GOLDS("ذهبي وعنبري", "Gold & Amber"),
    REDS("أحمر ومرجاني", "Crimson & Coral"),
    PURPLES("بنفسجي ونيلي", "Violet & Indigo"),
    NEUTRALS("محايد وأكاديمي", "Neutrals"),
    PASTELS("باستيل هادئ", "Soft Pastels"),
    DARKS("داكن فاخر", "Dark Luxury");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> ar
        else -> en
    }
}

data class NamedColorSpec(
    val hex: String,
    val nameAr: String,
    val nameEn: String,
    val group: ColorPaletteGroup
)

/**
 * 100 curated presentation colors grouped into 8 balanced families.
 */
object SlideColorCatalog {
    val colors: List<NamedColorSpec> = listOf(
        // Royal Blues (13)
        NamedColorSpec("#0D47A1", "أزرق ملكي", "Royal Blue", ColorPaletteGroup.BLUES),
        NamedColorSpec("#1565C0", "أزرق كاتب", "Kateb Cobalt", ColorPaletteGroup.BLUES),
        NamedColorSpec("#1976D2", "أزرق كلاسيكي", "Classic Blue", ColorPaletteGroup.BLUES),
        NamedColorSpec("#1E88E5", "أزرق سماوي مشرق", "Bright Azure", ColorPaletteGroup.BLUES),
        NamedColorSpec("#2196F3", "أزرق نقي", "Pure Blue", ColorPaletteGroup.BLUES),
        NamedColorSpec("#0284C7", "سماوي تقني", "Tech Sky", ColorPaletteGroup.BLUES),
        NamedColorSpec("#0369A1", "أزرق محيطي", "Ocean Deep", ColorPaletteGroup.BLUES),
        NamedColorSpec("#0288D1", "لازوردي", "Cerulean", ColorPaletteGroup.BLUES),
        NamedColorSpec("#0097A7", "سماوي بحري", "Marine Cyan", ColorPaletteGroup.BLUES),
        NamedColorSpec("#00BCD4", "سيان حيوي", "Vivid Cyan", ColorPaletteGroup.BLUES),
        NamedColorSpec("#2563EB", "أزرق رقمي", "Digital Blue", ColorPaletteGroup.BLUES),
        NamedColorSpec("#3B82F6", "أزرق معتدل", "Horizon Blue", ColorPaletteGroup.BLUES),
        NamedColorSpec("#60A5FA", "أزرق هادئ", "Soft Sky", ColorPaletteGroup.BLUES),

        // Emerald & Teal (13)
        NamedColorSpec("#004D40", "زمردي عميق", "Deep Forest", ColorPaletteGroup.GREENS),
        NamedColorSpec("#00695C", "أخضر تركوازي", "Teal Pine", ColorPaletteGroup.GREENS),
        NamedColorSpec("#00796B", "أخضر علاجي", "Clinical Teal", ColorPaletteGroup.GREENS),
        NamedColorSpec("#00897B", "زمردي", "Emerald Teal", ColorPaletteGroup.GREENS),
        NamedColorSpec("#0F766E", "أخضر بترولي", "Petrol Green", ColorPaletteGroup.GREENS),
        NamedColorSpec("#059669", "أخضر نمو", "Growth Emerald", ColorPaletteGroup.GREENS),
        NamedColorSpec("#10B981", "نعناعي حيوي", "Mint Vivid", ColorPaletteGroup.GREENS),
        NamedColorSpec("#16A34A", "أخضر طبيعي", "Nature Green", ColorPaletteGroup.GREENS),
        NamedColorSpec("#22C55E", "أخضر مشرق", "Fresh Green", ColorPaletteGroup.GREENS),
        NamedColorSpec("#2E7D32", "زيتوني أكاديمي", "Scholar Green", ColorPaletteGroup.GREENS),
        NamedColorSpec("#1B5E20", "أخضر تراثي", "Heritage Green", ColorPaletteGroup.GREENS),
        NamedColorSpec("#65A30D", "ليموني داكن", "Olive Lime", ColorPaletteGroup.GREENS),
        NamedColorSpec("#84CC16", "أخضر حمضي", "Citrus Green", ColorPaletteGroup.GREENS),

        // Gold & Amber (13)
        NamedColorSpec("#78350F", "عنبر داكن", "Dark Amber", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#92400E", "برونزي", "Bronze", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#B45309", "نحاسي دافئ", "Warm Copper", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#C9963A", "ذهبي عريق", "Heritage Gold", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#D97706", "عنبري", "Rich Amber", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#F59E0B", "ذهبي مشرق", "Bright Gold", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#FBBF24", "شمسي", "Sunburst", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#EAB308", "أصفر ملكي", "Royal Yellow", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#CA8A04", "خردلي أنيق", "Elegant Ochre", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#EA580C", "برتقالي طاقة", "Energy Orange", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#F97316", "يوسفي", "Tangerine", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#FB923C", "مشمشي", "Apricot", ColorPaletteGroup.GOLDS),
        NamedColorSpec("#D4AF37", "ذهب خالص", "Metallic Gold", ColorPaletteGroup.GOLDS),

        // Crimson & Coral (13)
        NamedColorSpec("#7F1D1D", "عنابي فاخر", "Deep Burgundy", ColorPaletteGroup.REDS),
        NamedColorSpec("#991B1B", "قرمزي داكن", "Dark Crimson", ColorPaletteGroup.REDS),
        NamedColorSpec("#B91C1C", "ياقوتي", "Ruby Red", ColorPaletteGroup.REDS),
        NamedColorSpec("#DC2626", "أحمر تنبيهي", "Vivid Crimson", ColorPaletteGroup.REDS),
        NamedColorSpec("#EF4444", "مرجاني أحمر", "Coral Red", ColorPaletteGroup.REDS),
        NamedColorSpec("#F87171", "سلموني ناعم", "Soft Coral", ColorPaletteGroup.REDS),
        NamedColorSpec("#BE123C", "وردي قاني", "Rose Crimson", ColorPaletteGroup.REDS),
        NamedColorSpec("#E11D48", "توتي مشرق", "Berry Rose", ColorPaletteGroup.REDS),
        NamedColorSpec("#F43F5E", "فوشيا دافئ", "Warm Rose", ColorPaletteGroup.REDS),
        NamedColorSpec("#DB2777", "وردي عميق", "Deep Pink", ColorPaletteGroup.REDS),
        NamedColorSpec("#EC4899", "ماجنتا", "Magenta Pop", ColorPaletteGroup.REDS),
        NamedColorSpec("#F472B6", "زهر فاتح", "Blossom Pink", ColorPaletteGroup.REDS),
        NamedColorSpec("#C2410C", "طيني", "Terracotta", ColorPaletteGroup.REDS),

        // Violet & Indigo (12)
        NamedColorSpec("#312E81", "نيلي ليلي", "Midnight Indigo", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#3730A3", "نيلي عميق", "Deep Indigo", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#4338CA", "نيلي تقني", "Tech Indigo", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#4F46E5", "بنفسجي نيلي", "Indigo Glow", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#6366F1", "لافندر كهربائي", "Electric Indigo", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#5B21B6", "أرجواني ملكي", "Royal Purple", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#6D28D9", "أرجواني حيوي", "Vibrant Violet", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#7C3AED", "بنفسجي عصري", "Modern Violet", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#8B5CF6", "أميثيست", "Amethyst", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#9333EA", "أوركيد", "Orchid Purple", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#A855F7", "ليلكي", "Lilac Bright", ColorPaletteGroup.PURPLES),
        NamedColorSpec("#C026D3", "بنفسجي فوشيا", "Fuchsia Violet", ColorPaletteGroup.PURPLES),

        // Neutrals (12)
        NamedColorSpec("#FFFFFF", "أبيض ناصع", "Pure White", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#F8FAFC", "أبيض ثلجي", "Snow Slate", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#F1F5F9", "رمادي سحابي", "Cloud Gray", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#E2E8F0", "فضي فاتح", "Light Silver", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#CBD5E1", "رمادي حدودي", "Border Slate", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#94A3B8", "فولاذي ناعم", "Cool Steel", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#64748B", "رمادي متوازن", "Slate Neutral", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#475569", "أردوازي داكن", "Charcoal Slate", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#334155", "جرافيتي", "Graphite Ink", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#FAF7F0", "ورق عاجي", "Ivory Paper", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#F5EFE6", "رق دافئ", "Warm Parchment", ColorPaletteGroup.NEUTRALS),
        NamedColorSpec("#E7E5E4", "حجري رملي", "Sandstone", ColorPaletteGroup.NEUTRALS),

        // Soft Pastels (12)
        NamedColorSpec("#DBEAFE", "أزرق ثلجي", "Ice Blue", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#E0F2FE", "سماوي باهت", "Mist Blue", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#CCFBF1", "تركوازي باستيل", "Mint Mist", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#D1FAE5", "أخضر ربيعي", "Spring Pastel", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#ECFCCB", "ليموني باهت", "Lime Mist", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#FEF3C7", "كريمي ذهبي", "Butter Cream", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#FFEDD5", "خوخي فاتح", "Peach Cream", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#FEE2E2", "وردي باهت", "Blush Rose", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#FCE7F3", "حرير وردي", "Pink Silk", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#EDE9FE", "لافندر ناعم", "Lavender Mist", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#E0E7FF", "نرجسي", "Periwinkle Light", ColorPaletteGroup.PASTELS),
        NamedColorSpec("#F3E8FF", "سحابي بنفسجي", "Orchid Mist", ColorPaletteGroup.PASTELS),

        // Dark Luxury (12)
        NamedColorSpec("#020617", "أوبسيديان ليلي", "Obsidian Night", ColorPaletteGroup.DARKS),
        NamedColorSpec("#090D16", "فحمي عميق", "Deep Void", ColorPaletteGroup.DARKS),
        NamedColorSpec("#0F172A", "ليلي كاتب", "Midnight Slate", ColorPaletteGroup.DARKS),
        NamedColorSpec("#111827", "كربون أنيق", "Carbon Gray", ColorPaletteGroup.DARKS),
        NamedColorSpec("#1E293B", "حبر داكن", "Dark Ink", ColorPaletteGroup.DARKS),
        NamedColorSpec("#172554", "كحلي ملكي", "Royal Navy", ColorPaletteGroup.DARKS),
        NamedColorSpec("#042F2E", "زمرد ليلي", "Night Emerald", ColorPaletteGroup.DARKS),
        NamedColorSpec("#2E1065", "بنفسجي ليلي", "Night Plum", ColorPaletteGroup.DARKS),
        NamedColorSpec("#450A0A", "ماهوغاني", "Dark Mahogany", ColorPaletteGroup.DARKS),
        NamedColorSpec("#1C1917", "بازلت دافئ", "Warm Basalt", ColorPaletteGroup.DARKS),
        NamedColorSpec("#27272A", "زنك فحمي", "Zinc Dark", ColorPaletteGroup.DARKS),
        NamedColorSpec("#000000", "أسود خالص", "Pure Black", ColorPaletteGroup.DARKS)
    )
}

enum class DecorationCategory(val ar: String, val en: String) {
    NONE("بدون", "None"),
    CORNERS("زوايا كلاسيكية", "Classic Corners"),
    FRAMES("إطارات هندسية", "Geometric Frames"),
    ARABESQUE("زخارف إسلامية وتراثية", "Arabesque & Heritage"),
    TECH("خطوط ودوائر تقنية", "Tech & Circuits"),
    ACADEMIC("أختام وأشرطة أكاديمية", "Academic & Seals"),
    MINIMAL("لمسات عصرية", "Modern Minimal");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> ar
        else -> en
    }
}

enum class OrnamentRenderKind {
    NONE,
    L_CORNERS,
    DOUBLE_FRAME,
    ARABESQUE_ARCH,
    DIAMOND_CHAIN,
    CIRCUIT_TRACE,
    TOP_BOTTOM_RIBBON,
    DOT_MATRIX,
    DIAGONAL_WAVES,
    HEX_WATERMARK,
    ART_DECO_CORNER,
    GOLD_BOOK_SEAL
}

data class DecorationSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val category: DecorationCategory,
    val renderKind: OrnamentRenderKind,
    val strokeDensity: Int = 1,
    val defaultHex: String = "#1565C0"
) {
    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> nameAr
        else -> nameEn
    }
}

/**
 * 100 named decorative ornament styles rendered identically on Compose Canvas & Bitmap export.
 */
object SlideDecorationCatalog {
    val decorations: List<DecorationSpec> = buildList {
        add(DecorationSpec("none", "بدون زخرفة", "No Ornament", DecorationCategory.NONE, OrnamentRenderKind.NONE, 0, "#1565C0"))

        val prefixes = listOf(
            "إطار لؤلؤي" to "Pearl Border",
            "زوايا ملكية" to "Royal Corners",
            "قوس أندلسي" to "Andalusian Arch",
            "سلسلة معينية" to "Diamond Chain",
            "مدار تقني" to "Circuit Orbit",
            "شريط أكاديمي" to "Academic Ribbon",
            "نسيج نقطي" to "Dot Matrix",
            "موجة ديناميكية" to "Dynamic Wave",
            "خلية سداسية" to "Hexagon Lattice",
            "أرت ديكو" to "Art Deco Trim",
            "ختم جامعي" to "University Seal"
        )
        val modifiers = listOf(
            "ذهبي" to "Gold",
            "رفيع" to "Fine",
            "مزدوج" to "Double",
            "تراثي" to "Heritage",
            "هندسي" to "Geometric",
            "حريري" to "Silky",
            "عصري" to "Modern",
            "كلاسيكي" to "Classic",
            "بارز" to "Bold"
        )
        val kinds = listOf(
            OrnamentRenderKind.DOUBLE_FRAME to DecorationCategory.FRAMES,
            OrnamentRenderKind.L_CORNERS to DecorationCategory.CORNERS,
            OrnamentRenderKind.ARABESQUE_ARCH to DecorationCategory.ARABESQUE,
            OrnamentRenderKind.DIAMOND_CHAIN to DecorationCategory.ARABESQUE,
            OrnamentRenderKind.CIRCUIT_TRACE to DecorationCategory.TECH,
            OrnamentRenderKind.TOP_BOTTOM_RIBBON to DecorationCategory.ACADEMIC,
            OrnamentRenderKind.DOT_MATRIX to DecorationCategory.MINIMAL,
            OrnamentRenderKind.DIAGONAL_WAVES to DecorationCategory.MINIMAL,
            OrnamentRenderKind.HEX_WATERMARK to DecorationCategory.TECH,
            OrnamentRenderKind.ART_DECO_CORNER to DecorationCategory.CORNERS,
            OrnamentRenderKind.GOLD_BOOK_SEAL to DecorationCategory.ACADEMIC
        )
        val sampleColors = listOf("#1565C0", "#C9963A", "#00897B", "#7C3AED", "#0F172A", "#DC2626", "#0284C7", "#059669", "#B45309")

        var count = 1
        outer@ for (mIdx in modifiers.indices) {
            for (pIdx in prefixes.indices) {
                if (count >= 100) break@outer
                val (prefAr, prefEn) = prefixes[pIdx]
                val (modAr, modEn) = modifiers[mIdx]
                val (kind, cat) = kinds[pIdx]
                add(
                    DecorationSpec(
                        id = "ornament_${count}",
                        nameAr = "$prefAr • $modAr #$count",
                        nameEn = "$modEn $prefEn #$count",
                        category = cat,
                        renderKind = kind,
                        strokeDensity = (mIdx % 3) + 1,
                        defaultHex = sampleColors[(count - 1) % sampleColors.size]
                    )
                )
                count++
            }
        }
    }

    fun findById(id: String?): DecorationSpec =
        decorations.firstOrNull { it.id == id } ?: decorations.first()
}

enum class EquationCategory(val ar: String, val en: String) {
    PHYSICS("الفيزياء", "Physics"),
    CHEMISTRY("الكيمياء", "Chemistry"),
    MATH_CALCULUS("الرياضيات والتفاضل", "Math & Calculus"),
    STATISTICS("الإحصاء والاحتمالات", "Statistics & AI"),
    ECONOMICS_LAW("المالية والأعمال والمنطق", "Finance, Law & Logic");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> ar
        else -> en
    }
}

data class EquationTemplateSpec(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val category: EquationCategory,
    val expression: String
) {
    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> titleAr
        else -> titleEn
    }
}

/**
 * 100 Scientific and Non-Scientific equation & formula presets.
 */
object EquationTemplateCatalog {
    val equations: List<EquationTemplateSpec> = listOf(
        // Physics (20)
        EquationTemplateSpec("eq_01", "تكافؤ الكتلة والطاقة", "Mass-Energy Equivalence", EquationCategory.PHYSICS, "E = mc^2"),
        EquationTemplateSpec("eq_02", "قانون نيوتن الثاني", "Newton's Second Law", EquationCategory.PHYSICS, "F = m \\cdot a"),
        EquationTemplateSpec("eq_03", "الجذب العام", "Universal Gravitation", EquationCategory.PHYSICS, "F = G \\frac{m_1 m_2}{r^2}"),
        EquationTemplateSpec("eq_04", "طاقة الحركة", "Kinetic Energy", EquationCategory.PHYSICS, "K = \\frac{1}{2}mv^2"),
        EquationTemplateSpec("eq_05", "طاقة الوضع", "Potential Energy", EquationCategory.PHYSICS, "U = mgh"),
        EquationTemplateSpec("eq_06", "طاقة الفوتون (بلانك)", "Planck Photon Energy", EquationCategory.PHYSICS, "E = h\\nu = \\frac{hc}{\\lambda}"),
        EquationTemplateSpec("eq_07", "معادلة شرودنغر", "Schrödinger Equation", EquationCategory.PHYSICS, "i\\hbar \\frac{\\partial \\psi}{\\partial t} = \\hat{H}\\psi"),
        EquationTemplateSpec("eq_08", "مبدأ هايزنبرغ", "Heisenberg Uncertainty", EquationCategory.PHYSICS, "\\Delta x \\cdot \\Delta p \\ge \\frac{\\hbar}{2}"),
        EquationTemplateSpec("eq_09", "قانون أوم", "Ohm's Law", EquationCategory.PHYSICS, "V = I \\cdot R"),
        EquationTemplateSpec("eq_10", "القدرة الكهربائية", "Electrical Power", EquationCategory.PHYSICS, "P = V \\cdot I = I^2 R"),
        EquationTemplateSpec("eq_11", "قانون كولوم", "Coulomb's Law", EquationCategory.PHYSICS, "F = k_e \\frac{|q_1 q_2|}{r^2}"),
        EquationTemplateSpec("eq_12", "قانون فاراداي للحث", "Faraday's Law", EquationCategory.PHYSICS, "\\mathcal{E} = -\\frac{d\\Phi_B}{dt}"),
        EquationTemplateSpec("eq_13", "معدل الموجة", "Wave Velocity", EquationCategory.PHYSICS, "v = \\lambda \\cdot f"),
        EquationTemplateSpec("eq_14", "قانون سنيل للانكسار", "Snell's Law", EquationCategory.PHYSICS, "n_1 \\sin\\theta_1 = n_2 \\sin\\theta_2"),
        EquationTemplateSpec("eq_15", "تمدد الزمن النسبي", "Time Dilation", EquationCategory.PHYSICS, "\\Delta t' = \\frac{\\Delta t}{\\sqrt{1 - v^2/c^2}}"),
        EquationTemplateSpec("eq_16", "الضغط", "Pressure", EquationCategory.PHYSICS, "P = \\frac{F}{A}"),
        EquationTemplateSpec("eq_17", "معادلة برنولي", "Bernoulli's Principle", EquationCategory.PHYSICS, "P + \\frac{1}{2}\\rho v^2 + \\rho gh = const"),
        EquationTemplateSpec("eq_18", "الشغل الميكانيكي", "Mechanical Work", EquationCategory.PHYSICS, "W = F \\cdot d \\cos\\theta"),
        EquationTemplateSpec("eq_19", "طول موجة دي برولي", "de Broglie Wavelength", EquationCategory.PHYSICS, "\\lambda = \\frac{h}{mv}"),
        EquationTemplateSpec("eq_20", "قانون ستيفان بولتزمان", "Stefan-Boltzmann Law", EquationCategory.PHYSICS, "P = \\sigma A T^4"),

        // Chemistry (20)
        EquationTemplateSpec("eq_21", "قانون الغاز المثالي", "Ideal Gas Law", EquationCategory.CHEMISTRY, "PV = nRT"),
        EquationTemplateSpec("eq_22", "الأس الهيدروجيني", "pH Definition", EquationCategory.CHEMISTRY, "pH = -\\log_{10}[H^+]"),
        EquationTemplateSpec("eq_23", "هندرسون-هاسلبالخ", "Henderson-Hasselbalch", EquationCategory.CHEMISTRY, "pH = pK_a + \\log\\frac{[A^-]}{[HA]}"),
        EquationTemplateSpec("eq_24", "طاقة غيبس الحرة", "Gibbs Free Energy", EquationCategory.CHEMISTRY, "\\Delta G = \\Delta H - T\\Delta S"),
        EquationTemplateSpec("eq_25", "معادلة أرهينيوس", "Arrhenius Equation", EquationCategory.CHEMISTRY, "k = A e^{-E_a / RT}"),
        EquationTemplateSpec("eq_26", "معادلة نيرنست", "Nernst Equation", EquationCategory.CHEMISTRY, "E = E^\\circ - \\frac{RT}{nF}\\ln Q"),
        EquationTemplateSpec("eq_27", "المولارية", "Molarity", EquationCategory.CHEMISTRY, "M = \\frac{n_{solute}}{V_{solution}}"),
        EquationTemplateSpec("eq_28", "قانون التخفيف", "Dilution Formula", EquationCategory.CHEMISTRY, "C_1 V_1 = C_2 V_2"),
        EquationTemplateSpec("eq_29", "المحتوى الحراري", "Enthalpy", EquationCategory.CHEMISTRY, "H = U + PV"),
        EquationTemplateSpec("eq_30", "معادلة البناء الضوئي", "Photosynthesis", EquationCategory.CHEMISTRY, "6CO_2 + 6H_2O \\rightarrow C_6H_{12}O_6 + 6O_2"),
        EquationTemplateSpec("eq_31", "الكتلة المولية", "Moles from Mass", EquationCategory.CHEMISTRY, "n = \\frac{m}{M}"),
        EquationTemplateSpec("eq_32", "ثابت الاتزان", "Equilibrium Constant", EquationCategory.CHEMISTRY, "K_c = \\frac{[C]^c [D]^d}{[A]^a [B]^b}"),
        EquationTemplateSpec("eq_33", "الضغط الأسموزي", "Osmotic Pressure", EquationCategory.CHEMISTRY, "\\Pi = iMRT"),
        EquationTemplateSpec("eq_34", "سرعة التفاعل", "Reaction Rate", EquationCategory.CHEMISTRY, "Rate = k[A]^m[B]^n"),
        EquationTemplateSpec("eq_35", "نصف عمر الرتبة الأولى", "First-Order Half-Life", EquationCategory.CHEMISTRY, "t_{1/2} = \\frac{\\ln 2}{k}"),
        EquationTemplateSpec("eq_36", "انخفاض درجة التجمد", "Freezing Point Depression", EquationCategory.CHEMISTRY, "\\Delta T_f = i K_f m"),
        EquationTemplateSpec("eq_37", "قانون بويل", "Boyle's Law", EquationCategory.CHEMISTRY, "P_1 V_1 = P_2 V_2"),
        EquationTemplateSpec("eq_38", "قانون شارل", "Charles's Law", EquationCategory.CHEMISTRY, "\\frac{V_1}{T_1} = \\frac{V_2}{T_2}"),
        EquationTemplateSpec("eq_39", "الكثافة", "Density", EquationCategory.CHEMISTRY, "\\rho = \\frac{m}{V}"),
        EquationTemplateSpec("eq_40", "النسبة المئوية للناتج", "Percent Yield", EquationCategory.CHEMISTRY, "\\%Yield = \\frac{Actual}{Theoretical} \\times 100"),

        // Math & Calculus (20)
        EquationTemplateSpec("eq_41", "القانون التربيعي", "Quadratic Formula", EquationCategory.MATH_CALCULUS, "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}"),
        EquationTemplateSpec("eq_42", "مبرهنة فيثاغورس", "Pythagorean Theorem", EquationCategory.MATH_CALCULUS, "a^2 + b^2 = c^2"),
        EquationTemplateSpec("eq_43", "متطابقة أويلر", "Euler's Identity", EquationCategory.MATH_CALCULUS, "e^{i\\pi} + 1 = 0"),
        EquationTemplateSpec("eq_44", "تكامل ريمان", "Definite Integral", EquationCategory.MATH_CALCULUS, "\\int_a^b f(x)\\,dx = F(b) - F(a)"),
        EquationTemplateSpec("eq_45", "تعريف المشتقة", "Limit Derivative", EquationCategory.MATH_CALCULUS, "f'(x) = \\lim_{h \\to 0} \\frac{f(x+h)-f(x)}{h}"),
        EquationTemplateSpec("eq_46", "متسلسلة تايلور", "Taylor Series", EquationCategory.MATH_CALCULUS, "f(x) = \\sum_{n=0}^{\\infty} \\frac{f^{(n)}(a)}{n!}(x-a)^n"),
        EquationTemplateSpec("eq_47", "التكامل الغاوسي", "Gaussian Integral", EquationCategory.MATH_CALCULUS, "\\int_{-\\infty}^{\\infty} e^{-x^2}\\,dx = \\sqrt{\\pi}"),
        EquationTemplateSpec("eq_48", "مساحة الدائرة", "Circle Area", EquationCategory.MATH_CALCULUS, "A = \\pi r^2"),
        EquationTemplateSpec("eq_49", "حجم الكرة", "Sphere Volume", EquationCategory.MATH_CALCULUS, "V = \\frac{4}{3}\\pi r^3"),
        EquationTemplateSpec("eq_50", "المسافة الإقليدية", "Euclidean Distance", EquationCategory.MATH_CALCULUS, "d = \\sqrt{(x_2-x_1)^2 + (y_2-y_1)^2}"),
        EquationTemplateSpec("eq_51", "قانون جيب التمام", "Law of Cosines", EquationCategory.MATH_CALCULUS, "c^2 = a^2 + b^2 - 2ab\\cos\\gamma"),
        EquationTemplateSpec("eq_52", "المتطابقة المثلثية", "Trig Identity", EquationCategory.MATH_CALCULUS, "\\sin^2\\theta + \\cos^2\\theta = 1"),
        EquationTemplateSpec("eq_53", "ذات الحدين", "Binomial Theorem", EquationCategory.MATH_CALCULUS, "(x+y)^n = \\sum_{k=0}^n \\binom{n}{k}x^{n-k}y^k"),
        EquationTemplateSpec("eq_54", "مجموع المتتالية الحسابية", "Arithmetic Sum", EquationCategory.MATH_CALCULUS, "S_n = \\frac{n}{2}(a_1 + a_n)"),
        EquationTemplateSpec("eq_55", "مجموع الهندسية اللانهائية", "Geometric Series", EquationCategory.MATH_CALCULUS, "\\sum_{k=0}^{\\infty} ar^k = \\frac{a}{1-r}"),
        EquationTemplateSpec("eq_56", "قاعدة السلسلة", "Chain Rule", EquationCategory.MATH_CALCULUS, "\\frac{dy}{dx} = \\frac{dy}{du} \\cdot \\frac{du}{dx}"),
        EquationTemplateSpec("eq_57", "التكامل بالتجزيء", "Integration by Parts", EquationCategory.MATH_CALCULUS, "\\int u\\,dv = uv - \\int v\\,du"),
        EquationTemplateSpec("eq_58", "محدد المصفوفة 2×2", "2x2 Determinant", EquationCategory.MATH_CALCULUS, "\\det(A) = ad - bc"),
        EquationTemplateSpec("eq_59", "صيغة القيم الذاتية", "Eigenvalue Equation", EquationCategory.MATH_CALCULUS, "A\\mathbf{v} = \\lambda\\mathbf{v}"),
        EquationTemplateSpec("eq_60", "تحويل فورييه", "Fourier Transform", EquationCategory.MATH_CALCULUS, "\\hat{f}(\\xi) = \\int_{-\\infty}^{\\infty} f(x)e^{-2\\pi i x \\xi}dx"),

        // Statistics & AI (20)
        EquationTemplateSpec("eq_61", "المتوسط الحسابي", "Arithmetic Mean", EquationCategory.STATISTICS, "\\bar{x} = \\frac{1}{n}\\sum_{i=1}^n x_i"),
        EquationTemplateSpec("eq_62", "الانحراف المعياري", "Standard Deviation", EquationCategory.STATISTICS, "\\sigma = \\sqrt{\\frac{1}{N}\\sum_{i=1}^N (x_i - \\mu)^2}"),
        EquationTemplateSpec("eq_63", "نظرية بايز", "Bayes' Theorem", EquationCategory.STATISTICS, "P(A|B) = \\frac{P(B|A)P(A)}{P(B)}"),
        EquationTemplateSpec("eq_64", "التوزيع الطبيعي", "Normal Distribution", EquationCategory.STATISTICS, "f(x) = \\frac{1}{\\sigma\\sqrt{2\\pi}}e^{-\\frac{(x-\\mu)^2}{2\\sigma^2}}"),
        EquationTemplateSpec("eq_65", "معامل الارتباط", "Pearson Correlation", EquationCategory.STATISTICS, "r = \\frac{\\sum (x_i-\\bar{x})(y_i-\\bar{y})}{\\sqrt{\\sum(x_i-\\bar{x})^2 \\sum(y_i-\\bar{y})^2}}"),
        EquationTemplateSpec("eq_66", "الانحدار الخطي", "Linear Regression", EquationCategory.STATISTICS, "\\hat{y} = \\beta_0 + \\beta_1 x + \\varepsilon"),
        EquationTemplateSpec("eq_67", "دالة سيغمويد", "Sigmoid Activation", EquationCategory.STATISTICS, "\\sigma(z) = \\frac{1}{1 + e^{-z}}"),
        EquationTemplateSpec("eq_68", "سوفت ماكس", "Softmax Function", EquationCategory.STATISTICS, "\\text{Softmax}(z_i) = \\frac{e^{z_i}}{\\sum_j e^{z_j}}"),
        EquationTemplateSpec("eq_69", "التشابه الدلالي (كوساين)", "Cosine Similarity", EquationCategory.STATISTICS, "\\cos(\\theta) = \\frac{\\mathbf{A}\\cdot\\mathbf{B}}{\\|\\mathbf{A}\\|\\|\\mathbf{B}\\|}"),
        EquationTemplateSpec("eq_70", "إنتروبيا شانون", "Shannon Entropy", EquationCategory.STATISTICS, "H(X) = -\\sum p(x)\\log_2 p(x)"),
        EquationTemplateSpec("eq_71", "الانتباه الذاتي (AI)", "Scaled Dot-Product Attention", EquationCategory.STATISTICS, "\\text{Attention}(Q,K,V) = \\text{softmax}\\left(\\frac{QK^T}{\\sqrt{d_k}}\\right)V"),
        EquationTemplateSpec("eq_72", "خطأ متوسط المربعات", "Mean Squared Error", EquationCategory.STATISTICS, "MSE = \\frac{1}{n}\\sum_{i=1}^n (y_i - \\hat{y}_i)^2"),
        EquationTemplateSpec("eq_73", "نتيجة Z المعيارية", "Z-Score", EquationCategory.STATISTICS, "z = \\frac{x - \\mu}{\\sigma}"),
        EquationTemplateSpec("eq_74", "تباين العينة", "Sample Variance", EquationCategory.STATISTICS, "s^2 = \\frac{1}{n-1}\\sum (x_i - \\bar{x})^2"),
        EquationTemplateSpec("eq_75", "التوافيق", "Combinations", EquationCategory.STATISTICS, "\\binom{n}{k} = \\frac{n!}{k!(n-k)!}"),
        EquationTemplateSpec("eq_76", "التباديل", "Permutations", EquationCategory.STATISTICS, "P(n,k) = \\frac{n!}{(n-k)!}"),
        EquationTemplateSpec("eq_77", "مقياس F1", "F1 Score", EquationCategory.STATISTICS, "F_1 = 2 \\cdot \\frac{\\text{Precision} \\cdot \\text{Recall}}{\\text{Precision} + \\text{Recall}}"),
        EquationTemplateSpec("eq_78", "توزيع بواسون", "Poisson PMF", EquationCategory.STATISTICS, "P(X=k) = \\frac{\\lambda^k e^{-\\lambda}}{k!}"),
        EquationTemplateSpec("eq_79", "قيمة t الاختبارية", "One-Sample t-Test", EquationCategory.STATISTICS, "t = \\frac{\\bar{x} - \\mu_0}{s / \\sqrt{n}}"),
        EquationTemplateSpec("eq_80", "كاى مربع", "Chi-Square Test", EquationCategory.STATISTICS, "\\chi^2 = \\sum \\frac{(O_i - E_i)^2}{E_i}"),

        // Non-Scientific: Economics, Finance, Business, Law, Humanities (20)
        EquationTemplateSpec("eq_81", "العائد على الاستثمار", "Return on Investment (ROI)", EquationCategory.ECONOMICS_LAW, "ROI = \\frac{Net\\ Profit}{Cost} \\times 100\\%"),
        EquationTemplateSpec("eq_82", "الفائدة المركبة", "Compound Interest", EquationCategory.ECONOMICS_LAW, "A = P\\left(1 + \\frac{r}{n}\\right)^{nt}"),
        EquationTemplateSpec("eq_83", "صافي القيمة الحالية", "Net Present Value (NPV)", EquationCategory.ECONOMICS_LAW, "NPV = \\sum_{t=0}^n \\frac{CF_t}{(1+r)^t}"),
        EquationTemplateSpec("eq_84", "نقطة التعادل", "Break-Even Point", EquationCategory.ECONOMICS_LAW, "BEP = \\frac{Fixed\\ Costs}{Price - Variable\\ Cost}"),
        EquationTemplateSpec("eq_85", "معدل النمو السنوي المركب", "CAGR", EquationCategory.ECONOMICS_LAW, "CAGR = \\left(\\frac{V_{final}}{V_{initial}}\\right)^{1/n} - 1"),
        EquationTemplateSpec("eq_86", "هامش الربح الإجمالي", "Gross Profit Margin", EquationCategory.ECONOMICS_LAW, "Margin = \\frac{Revenue - COGS}{Revenue} \\times 100\\%"),
        EquationTemplateSpec("eq_87", "الناتج المحلي الإجمالي", "GDP Expenditure", EquationCategory.ECONOMICS_LAW, "GDP = C + I + G + (X - M)"),
        EquationTemplateSpec("eq_88", "مرونة الطلب السعرية", "Price Elasticity", EquationCategory.ECONOMICS_LAW, "E_d = \\frac{\\%\\Delta Q}{\\%\\Delta P}"),
        EquationTemplateSpec("eq_89", "معادلة فيشر للتضخم", "Fisher Equation", EquationCategory.ECONOMICS_LAW, "r \\approx i - \\pi"),
        EquationTemplateSpec("eq_90", "معدل التحويل الرقمي", "Conversion Rate", EquationCategory.ECONOMICS_LAW, "CVR = \\frac{Conversions}{Visitors} \\times 100\\%"),
        EquationTemplateSpec("eq_91", "القيمة الدائمة للعميل", "Customer Lifetime Value", EquationCategory.ECONOMICS_LAW, "CLV = ARPU \\times Avg\\ Lifespan"),
        EquationTemplateSpec("eq_92", "نسبة شارب للمخاطر", "Sharpe Ratio", EquationCategory.ECONOMICS_LAW, "S = \\frac{R_p - R_f}{\\sigma_p}"),
        EquationTemplateSpec("eq_93", "معادلة الميزانية العمومية", "Accounting Equation", EquationCategory.ECONOMICS_LAW, "Assets = Liabilities + Equity"),
        EquationTemplateSpec("eq_94", "المعدل التراكمي الجامعي", "GPA Weighted Mean", EquationCategory.ECONOMICS_LAW, "GPA = \\frac{\\sum (Grade_i \\times Credits_i)}{\\sum Credits_i}"),
        EquationTemplateSpec("eq_95", "تكلفة الاستحواذ", "Customer Acquisition Cost", EquationCategory.ECONOMICS_LAW, "CAC = \\frac{Marketing + Sales}{New\\ Customers}"),
        EquationTemplateSpec("eq_96", "قيمة المخاطرة القانونية", "Risk Matrix Score", EquationCategory.ECONOMICS_LAW, "Risk = Likelihood \\times Impact"),
        EquationTemplateSpec("eq_97", "القياس المنطقي", "Logical Implication", EquationCategory.ECONOMICS_LAW, "(P \\rightarrow Q) \\land P \\vdash Q"),
        EquationTemplateSpec("eq_98", "مؤشر الكثافة البحثية", "Citation H-Index", EquationCategory.ECONOMICS_LAW, "h = \\max\\{i : C_i \\ge i\\}"),
        EquationTemplateSpec("eq_99", "إنتاجية الفريق", "Team Velocity", EquationCategory.ECONOMICS_LAW, "Velocity = \\frac{\\sum Story\\ Points}{Sprints}"),
        EquationTemplateSpec("eq_100", "المردود الزمني للمشروع", "Payback Period", EquationCategory.ECONOMICS_LAW, "Payback = \\frac{Initial\\ Investment}{Annual\\ Cash\\ Flow}")
    )
}

enum class MotionThemeCategory(val ar: String, val en: String) {
    STEALTH("الوضع الخفي", "Stealth & Subtle"),
    EXCITING("الوضع المثير", "Exciting & Show"),
    DYNAMIC("الوضع الديناميكي", "Dynamic Motion"),
    SCIENTIFIC("الوضع العلمي", "Scientific Precision"),
    ACADEMIC("الوضع الأكاديمي", "Academic Flow");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> ar
        else -> en
    }
}

data class MotionThemeSpec(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val category: MotionThemeCategory,
    val transitionType: SlideTransitionType,
    val entranceType: ElementAnimationType,
    val exitType: ElementAnimationType,
    val direction: AnimationDirection,
    val motionRange: MotionRange,
    val durationMs: Int,
    val staggerMs: Int
) {
    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> nameAr
        else -> nameEn
    }
}

/**
 * 100 coordinated Slide + Element Choreography Motion Themes covering
 * Stealth, Exciting, Dynamic, Scientific, and Academic modes.
 */
object MotionThemeCatalog {
    val themes: List<MotionThemeSpec> = buildList {
        val configs = listOf(
            MotionThemeCategory.STEALTH to Triple(SlideTransitionType.STEALTH, ElementAnimationType.FADE_IN, ElementAnimationType.EXIT_FADE),
            MotionThemeCategory.EXCITING to Triple(SlideTransitionType.EXCITING, ElementAnimationType.BOUNCE, ElementAnimationType.EXIT_ZOOM),
            MotionThemeCategory.DYNAMIC to Triple(SlideTransitionType.DYNAMIC, ElementAnimationType.FLY_IN, ElementAnimationType.EXIT_FLY),
            MotionThemeCategory.SCIENTIFIC to Triple(SlideTransitionType.SCIENTIFIC, ElementAnimationType.ZOOM_IN, ElementAnimationType.EXIT_FADE),
            MotionThemeCategory.ACADEMIC to Triple(SlideTransitionType.ACADEMIC, ElementAnimationType.FLOAT, ElementAnimationType.EXIT_FADE)
        )
        val extraTransitions = listOf(
            SlideTransitionType.FADE, SlideTransitionType.PUSH, SlideTransitionType.WIPE,
            SlideTransitionType.SPLIT, SlideTransitionType.REVEAL, SlideTransitionType.ZOOM,
            SlideTransitionType.FLIP, SlideTransitionType.DISSOLVE, SlideTransitionType.CURTAIN,
            SlideTransitionType.MORPH
        )
        val styles = listOf(
            "ناعم" to "Silk",
            "سريع" to "Swift",
            "متدرج" to "Cascade",
            "سينمائي" to "Cinema",
            "متوازن" to "Balanced",
            "ممتد" to "Extended",
            "بؤري" to "Focal",
            "إيقاعي" to "Rhythmic",
            "احترافي" to "Pro",
            "حاسوبي" to "Digital",
            "تحليلي" to "Analytic",
            "متناغم" to "Harmonic",
            "نابض" to "Pulse",
            "معماري" to "Architect",
            "فلكي" to "Orbital",
            "خطي" to "Linear",
            "منحني" to "Curved",
            "أفقي" to "Horizontal",
            "عمودي" to "Vertical",
            "تتابعي" to "Sequential"
        )
        val directions = AnimationDirection.entries
        val ranges = MotionRange.entries

        var idx = 1
        for (styleIdx in styles.indices) {
            for (catIdx in configs.indices) {
                val (cat, triple) = configs[catIdx]
                val (baseTr, entrance, exit) = triple
                val tr = if (styleIdx % 3 == 0) baseTr else extraTransitions[(idx + styleIdx) % extraTransitions.size]
                val (stAr, stEn) = styles[styleIdx]
                val dir = directions[idx % directions.size]
                val range = ranges[idx % ranges.size]
                add(
                    MotionThemeSpec(
                        id = "motion_${idx}",
                        nameAr = "${cat.ar}: $stAr #$idx",
                        nameEn = "${cat.en}: $stEn #$idx",
                        category = cat,
                        transitionType = tr,
                        entranceType = entrance,
                        exitType = exit,
                        direction = dir,
                        motionRange = range,
                        durationMs = 350 + ((idx % 6) * 100),
                        staggerMs = 60 + ((idx % 4) * 35)
                    )
                )
                idx++
            }
        }
    }
}

enum class ToolCommandCategory(val ar: String, val en: String) {
    INSERT("إدراج وعناصر", "Insert & Objects"),
    TEXT_FONT("النصوص والخطوط", "Typography & Text"),
    SMARTART_3D("سمارت آرت و3D", "SmartArt & 3D"),
    SLIDE_DESIGN("تصميم وزخرفة", "Design & Ornaments"),
    MOTION_VIEW("حركات وطرق عرض", "Motion & Views"),
    ARRANGE_EXPORT("تنظيم وتصدير", "Arrange & Export");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> ar
        else -> en
    }
}

data class ToolCommandSpec(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val category: ToolCommandCategory
) {
    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> labelAr
        else -> labelEn
    }
}

/**
 * 100 real, mapped interactive tools in the Editor Ribbon / Command Palette.
 */
object ToolCommandCatalog {
    val commands: List<ToolCommandSpec> = listOf(
        // Insert & Objects (18)
        ToolCommandSpec("cmd_add_text", "إضافة مربع نص", "Add Text Box", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_bullet", "إضافة قائمة نقطية", "Add Bullet List", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_numbered", "إضافة قائمة مرقمة", "Add Numbered List", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_checkbox", "إضافة قائمة خانات اختيار ☑", "Add Checkbox List ☑", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_quote", "إضافة بطاقة اقتباس", "Add Quote Callout", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_image", "إدراج صورة", "Insert Image", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_table", "إدراج جدول", "Insert Table", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_chart_bar", "رسم بياني أعمدة", "Insert Bar Chart", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_chart_pie", "رسم بياني دائري", "Insert Pie Chart", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_chart_line", "رسم بياني خطي", "Insert Line Chart", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_chart_area", "رسم بياني مساحي", "Insert Area Chart", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_equation", "معرض المعادلات (100)", "Equations Library (100)", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_icon", "مكتبة الأيقونات (100)", "Icons Library (100)", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_shape_rect", "إدراج مستطيل", "Insert Rectangle", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_shape_circle", "إدراج دائرة", "Insert Circle", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_shape_arrow", "إدراج سهم", "Insert Arrow", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_shape_star", "إدراج نجمة", "Insert Star", ToolCommandCategory.INSERT),
        ToolCommandSpec("cmd_add_speaker_notes", "تعديل ملاحظات المتحدث", "Edit Speaker Notes", ToolCommandCategory.INSERT),

        // Typography & Text (18)
        ToolCommandSpec("cmd_font_picker", "اختيار نوع الخط", "Choose Font Family", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_import_font", "استيراد خط مخصص TTF/OTF", "Import Custom Font", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_font_size_up", "تكبير حجم الخط +4", "Font Size +4", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_font_size_down", "تصغير حجم الخط -4", "Font Size -4", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_toggle_bold", "خط عريض B", "Toggle Bold", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_toggle_italic", "خط مائل I", "Toggle Italic", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_toggle_underline", "تسطير U", "Toggle Underline", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_toggle_strike", "يتوسطه خط S", "Toggle Strikethrough", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_dir_rtl", "اتجاه النص من اليمين RTL", "Text Direction RTL", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_dir_ltr", "اتجاه النص من اليسار LTR", "Text Direction LTR", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_dir_auto", "اتجاه النص تلقائي", "Text Direction Auto", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_text_align_start", "محاذاة لبداية السطر", "Align Text Start", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_text_align_center", "توسيط النص", "Align Text Center", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_text_align_end", "محاذاة لنهاية السطر", "Align Text End", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_text_line_tight", "تباعد أسطر ضيق", "Tight Line Spacing", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_text_line_wide", "تباعد أسطر مريح", "Relaxed Line Spacing", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_text_letter_wide", "تباعد أحرف أوسع", "Wider Letter Spacing", ToolCommandCategory.TEXT_FONT),
        ToolCommandSpec("cmd_ai_proofread", "تدقيق لغوي بالذكاء", "AI Proofread Text", ToolCommandCategory.TEXT_FONT),

        // SmartArt & 3D (20)
        ToolCommandSpec("cmd_smartart_gallery", "معرض SmartArt الكامل", "Open SmartArt Gallery", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_process", "SmartArt: مسار خطوات", "SmartArt: Process Arrows", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_cycle", "SmartArt: دورة متكررة", "SmartArt: Cycle Ring", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_hierarchy", "SmartArt: هيكل هرمي", "SmartArt: Hierarchy Org", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_pyramid", "SmartArt: هرم مستويات", "SmartArt: Pyramid", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_venn", "SmartArt: مخطط فن", "SmartArt: Venn Overlap", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_matrix", "SmartArt: مصفوفة 2×2", "SmartArt: Matrix 2x2", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_funnel", "SmartArt: قمع مراحل", "SmartArt: Funnel Stages", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_radial", "SmartArt: مخطط محوري", "SmartArt: Radial Hub", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_timeline", "SmartArt: خط زمني", "SmartArt: Timeline", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_swot", "SmartArt: تحليل SWOT", "SmartArt: SWOT Grid", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_smartart_edit_colors", "تلوين عناصر SmartArt مخصص", "Customize SmartArt Colors", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_shapes_3d_gallery", "معرض الأشكال ثلاثية الأبعاد 3D", "3D Shapes Gallery", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_cube", "إدراج مكعب ثلاثي الأبعاد 3D", "Insert 3D Cube", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_cylinder", "إدراج أسطوانة 3D", "Insert 3D Cylinder", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_pyramid", "إدراج هرم ثلاثي الأبعاد 3D", "Insert 3D Pyramid", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_sphere", "إدراج كرة 3D مظللة", "Insert 3D Sphere", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_toggle", "تفعيل/تعطيل العمق ثلاثي الأبعاد", "Toggle Element 3D Depth", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_depth_plus", "زيادة عمق 3D", "Increase 3D Depth", ToolCommandCategory.SMARTART_3D),
        ToolCommandSpec("cmd_3d_depth_minus", "تقليل عمق 3D", "Decrease 3D Depth", ToolCommandCategory.SMARTART_3D),

        // Slide Design & Ornaments (14)
        ToolCommandSpec("cmd_ornaments_gallery", "معرض الزخارف (100) والألوان (100)", "Ornaments (100) & Colors (100)", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_bg_picker", "تغيير خلفية الشريحة", "Change Slide Background", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_bg_apply_all", "تطبيق الخلفية على الكل", "Apply Background to All", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_ornament_apply_all", "تطبيق الزخرفة على الكل", "Apply Ornament to All", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_layouts_sheet", "تبديل تخطيط الشريحة", "Switch Slide Layout", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_templates_gallery", "قوالب جاهزة (100 قالب)", "Templates Gallery (100)", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_ratio_16_9", "حجم الشريحة 16:9 عريض", "Slide Ratio 16:9", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_ratio_4_3", "حجم الشريحة 4:3 قياسي", "Slide Ratio 4:3", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_ratio_a4", "حجم الشريحة A4 تعليمي", "Slide Ratio A4", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_ratio_square", "حجم الشريحة 1:1 مربع", "Slide Ratio 1:1", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_ratio_portrait", "حجم الشريحة 9:16 عمودي", "Slide Ratio 9:16", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_grid_toggle", "إظهار/إخفاء الشبكة", "Toggle Grid Overlay", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_margins_toggle", "إظهار/إخفاء الهوامش الآمنة", "Toggle Safe Margins", ToolCommandCategory.SLIDE_DESIGN),
        ToolCommandSpec("cmd_smart_theme_ai", "محرك السمات الذكي (AI)", "AI Smart Theme Engine", ToolCommandCategory.SLIDE_DESIGN),

        // Motion & Views (16)
        ToolCommandSpec("cmd_motion_themes", "السمات التحريكية (100) والخروج", "Motion Themes (100) & Exit", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_tr_stealth", "انتقال: الوضع الخفي", "Transition: Stealth", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_tr_exciting", "انتقال: الوضع المثير", "Transition: Exciting", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_tr_dynamic", "انتقال: الوضع الديناميكي", "Transition: Dynamic", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_tr_scientific", "انتقال: الوضع العلمي", "Transition: Scientific", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_tr_academic", "انتقال: الوضع الأكاديمي", "Transition: Academic", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_tr_apply_all", "تطبيق الانتقال على جميع الشرائح", "Apply Transition to All", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_view_design", "طريقة العرض: التصميم", "View Mode: Design", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_view_outline", "طريقة العرض: المخطط التفصيلي", "View Mode: Outline", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_view_sorter", "طريقة العرض: فرز الشرائح", "View Mode: Slide Sorter", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_focus_mode", "طريقة العرض: وضع التركيز", "View Mode: Focus Mode", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_present_start", "عرض تقديمي من البداية", "Present from Start", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_present_current", "عرض من الشريحة الحالية", "Present Current Slide", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_zoom_in", "تكبير اللوحة +15%", "Zoom In +15%", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_zoom_out", "تصغير اللوحة -15%", "Zoom Out -15%", ToolCommandCategory.MOTION_VIEW),
        ToolCommandSpec("cmd_zoom_reset", "إعادة التكبير 100%", "Reset Zoom 100%", ToolCommandCategory.MOTION_VIEW),

        // Arrange & Export (14)
        ToolCommandSpec("cmd_add_slide", "إضافة شريحة جديدة", "Add New Slide", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_duplicate_slide", "تكرار الشريحة", "Duplicate Slide", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_auto_arrange", "ترتيب هندسي تلقائي", "Auto-Arrange Overlap", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_bring_front", "إحضار للأمام", "Bring to Front", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_send_back", "إرسال للخلف", "Send to Back", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_duplicate_el", "تكرار العنصر المحدد", "Duplicate Selected", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_delete_el", "حذف العنصر المحدد", "Delete Selected", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_stabilize_lock", "تثبيت/تحرير الشاشة", "Toggle Screen Stabilization", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_slide_settings", "إعدادات الشريحة والنقل", "Slide Settings & Move", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_pdf_preview", "معاينة وتنظيم صفحات PDF", "PDF Preview & Reorder", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_export_pptx", "تصدير PowerPoint / Keynote", "Export PPTX", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_export_mp4", "تصدير فيديو MP4", "Export MP4 Video", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_export_html_ios", "تصدير تفاعلي لمتصفح iOS / Safari", "Export Interactive Web/iOS", ToolCommandCategory.ARRANGE_EXPORT),
        ToolCommandSpec("cmd_save_now", "حفظ العرض الآن", "Save Presentation", ToolCommandCategory.ARRANGE_EXPORT)
    )
}

package com.example.data.templates

import com.example.core.localization.AppLanguage
import com.example.engine.ChartSeries
import com.example.engine.ChartType
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.Presentation
import com.example.engine.PresentationSourceType
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.TableCell
import com.example.engine.TextAlignment
import com.example.engine.TextListType
import com.example.engine.WritingDirection
import com.example.ui.theme.SlideFontRegistry
import java.util.UUID

enum class TemplateCategory(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val titleFa: String
) {
    EDUCATIONAL("educational", "تعليمية", "Educational", "آموزشی"),
    UNIVERSITY("university", "جامعية وأكاديمية", "University & Academic", "دانشگاهی"),
    SCIENTIFIC("scientific", "بحث علمي", "Scientific Research", "پژوهش علمی"),
    TECHNOLOGY("technology", "تقنية وذكاء اصطناعي", "Tech & AI", "فناوری و هوش مصنوعی"),
    ADMINISTRATIVE("administrative", "إدارية", "Administrative", "اداری"),
    ECONOMIC("economic", "اقتصادية ومالية", "Economic & Finance", "اقتصادی"),
    MEDICAL("medical", "طبية وصحية", "Medical & Health", "پزشکی"),
    ENGINEERING("engineering", "هندسية وتقنية", "Engineering & Tech", "مهندسی"),
    LAW("law", "قانون وحوكمة", "Law & Governance", "حقوق و قوانین"),
    LITERATURE("literature", "أدب ولغات", "Literature & Arts", "ادبیات و زبان"),
    HISTORICAL("historical", "تاريخية", "Historical", "تاریخی"),
    ISLAMIC("islamic", "إسلامية وتراثية", "Islamic & Heritage", "اسلامی"),
    CREATIVE("creative", "إبداع وبورتفوليو", "Creative Portfolio", "طراحی خلاقانه"),
    BUSINESS("business", "أعمال ومشاريع", "Business & Projects", "کسب‌وکار");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.EN -> titleEn
        AppLanguage.FA -> titleFa
        AppLanguage.RU -> when (this) {
            EDUCATIONAL -> "Образование"
            UNIVERSITY -> "Университет"
            SCIENTIFIC -> "Наука"
            TECHNOLOGY -> "Технологии и ИИ"
            ADMINISTRATIVE -> "Управление"
            ECONOMIC -> "Экономика"
            MEDICAL -> "Медицина"
            ENGINEERING -> "Инженерия"
            LAW -> "Право"
            LITERATURE -> "Литература"
            HISTORICAL -> "История"
            ISLAMIC -> "Наследие"
            CREATIVE -> "Креатив"
            BUSINESS -> "Бизнес"
        }
        AppLanguage.IT -> when (this) {
            EDUCATIONAL -> "Istruzione"
            UNIVERSITY -> "Università"
            SCIENTIFIC -> "Ricerca"
            TECHNOLOGY -> "Tech e AI"
            ADMINISTRATIVE -> "Amministrazione"
            ECONOMIC -> "Economia"
            MEDICAL -> "Medicina"
            ENGINEERING -> "Ingegneria"
            LAW -> "Diritto"
            LITERATURE -> "Letteratura"
            HISTORICAL -> "Storia"
            ISLAMIC -> "Patrimonio"
            CREATIVE -> "Creativo"
            BUSINESS -> "Business"
        }
        AppLanguage.UR -> when (this) {
            EDUCATIONAL -> "تعلیمی"
            UNIVERSITY -> "یونیورسٹی"
            SCIENTIFIC -> "سائنسی تحقیق"
            TECHNOLOGY -> "ٹیکنالوجی و AI"
            ADMINISTRATIVE -> "انتظامی"
            ECONOMIC -> "معاشیات"
            MEDICAL -> "طب و صحت"
            ENGINEERING -> "انجینئرنگ"
            LAW -> "قانون"
            LITERATURE -> "ادب"
            HISTORICAL -> "تاریخی"
            ISLAMIC -> "اسلامی ورثہ"
            CREATIVE -> "تخلیقی"
            BUSINESS -> "کاروبار"
        }
        AppLanguage.ZH -> when (this) {
            EDUCATIONAL -> "教育"
            UNIVERSITY -> "学术论文"
            SCIENTIFIC -> "科学研究"
            TECHNOLOGY -> "科技与AI"
            ADMINISTRATIVE -> "行政管理"
            ECONOMIC -> "经济金融"
            MEDICAL -> "医疗健康"
            ENGINEERING -> "工程技术"
            LAW -> "法律治理"
            LITERATURE -> "文学艺术"
            HISTORICAL -> "历史文化"
            ISLAMIC -> "传统遗产"
            CREATIVE -> "创意作品"
            BUSINESS -> "商业计划"
        }
    }
}

enum class CoverDesignStyle {
    SIDE_RIBBON,
    DIAGONAL_SPLIT,
    FRAME_GOLD,
    MODERN_CARDS,
    TOP_BANNER,
    CENTER_BADGE
}

enum class SlideLayoutPreset(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val titleFa: String
) {
    BLANK("blank", "شريحة فارغة", "Blank Slide", "اسلاید خالی"),
    TITLE_COVER("cover", "شريحة عنوان رئيسي", "Title Cover", "اسلاید عنوان"),
    TITLE_CONTENT("content", "عنوان ومحتوى", "Title & Content", "عنوان و محتوا"),
    TWO_COLUMN("two_col", "عمودان للمقارنة", "Two Columns", "دو ستونی"),
    THREE_CARDS("three_cards", "ثلاث بطاقات محاور", "Three Pillars", "سه کارت کلیدی"),
    SECTION_HEADER("section", "فاصل قسم", "Section Divider", "جداکننده بخش"),
    KPI_METRICS("kpi_metrics", "مؤشرات وأرقام بارزة", "KPI Metrics Trio", "شاخص‌های کلیدی"),
    TIMELINE_STEPS("timeline", "خارطة طريق ومراحل", "Timeline Roadmap", "خط زمانی"),
    CHART_INSIGHT("chart_insight", "رسم بياني مع تحليل", "Chart & Takeaways", "نمودار و تحلیل"),
    QUOTE_SPOTLIGHT("quote_spotlight", "اقتباس مميز", "Quote Spotlight", "نقل‌قول برجسته");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.EN -> titleEn
        AppLanguage.FA -> titleFa
        AppLanguage.RU -> when (this) {
            BLANK -> "Пустой слайд"
            TITLE_COVER -> "Титульный"
            TITLE_CONTENT -> "Заголовок и текст"
            TWO_COLUMN -> "Две колонки"
            THREE_CARDS -> "Три карточки"
            SECTION_HEADER -> "Разделитель"
            KPI_METRICS -> "Ключевые KPI"
            TIMELINE_STEPS -> "Этапы / Таймлайн"
            CHART_INSIGHT -> "График и выводы"
            QUOTE_SPOTLIGHT -> "Цитата"
        }
        AppLanguage.IT -> when (this) {
            BLANK -> "Vuota"
            TITLE_COVER -> "Copertina"
            TITLE_CONTENT -> "Titolo e Testo"
            TWO_COLUMN -> "Due Colonne"
            THREE_CARDS -> "Tre Schede"
            SECTION_HEADER -> "Sezione"
            KPI_METRICS -> "Metriche KPI"
            TIMELINE_STEPS -> "Timeline Fasi"
            CHART_INSIGHT -> "Grafico e Note"
            QUOTE_SPOTLIGHT -> "Citazione"
        }
        AppLanguage.UR -> when (this) {
            BLANK -> "خالی سلائیڈ"
            TITLE_COVER -> "مرکزی عنوان"
            TITLE_CONTENT -> "عنوان اور متن"
            TWO_COLUMN -> "دو کالم"
            THREE_CARDS -> "تین کارڈز"
            SECTION_HEADER -> "سیکشن ہیڈر"
            KPI_METRICS -> "اہم KPIs"
            TIMELINE_STEPS -> "ٹائم لائن"
            CHART_INSIGHT -> "چارٹ اور نکات"
            QUOTE_SPOTLIGHT -> "اہم اقتباس"
        }
        AppLanguage.ZH -> when (this) {
            BLANK -> "空白页"
            TITLE_COVER -> "封面页"
            TITLE_CONTENT -> "标题与内容"
            TWO_COLUMN -> "双栏对比"
            THREE_CARDS -> "三栏卡片"
            SECTION_HEADER -> "章节过渡"
            KPI_METRICS -> "核心指标卡"
            TIMELINE_STEPS -> "时间轴路线"
            CHART_INSIGHT -> "图表与洞察"
            QUOTE_SPOTLIGHT -> "金句高亮"
        }
    }
}

data class PresentationTemplate(
    val id: String,
    val category: TemplateCategory,
    val nameAr: String,
    val nameEn: String,
    val nameFa: String,
    val primaryHex: String,
    val secondaryHex: String,
    val accentHex: String,
    val bgHex: String,
    val titleColorHex: String,
    val bodyColorHex: String,
    val headingFontId: String = "cairo",
    val bodyFontId: String = "cairo",
    val defaultTransition: SlideTransitionType = SlideTransitionType.FADE,
    val coverStyle: CoverDesignStyle = CoverDesignStyle.SIDE_RIBBON
) {
    fun name(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> nameAr
        AppLanguage.FA -> nameFa
        AppLanguage.EN -> nameEn
        AppLanguage.UR -> "${category.label(AppLanguage.UR)} • $nameAr"
        AppLanguage.RU -> "${category.label(AppLanguage.RU)} • $nameEn"
        AppLanguage.IT -> "${category.label(AppLanguage.IT)} • $nameEn"
        AppLanguage.ZH -> "${category.label(AppLanguage.ZH)} • $nameEn"
    }

    fun resolveHeadingFont(language: AppLanguage): String = when (language) {
        AppLanguage.AR, AppLanguage.UR -> when (headingFontId) {
            "amiri", "cairo", "tajawal", "almarai", "noto_kufi_arabic", "vazirmatn" -> headingFontId
            "playfair_display", "lora" -> "amiri"
            "montserrat", "outfit" -> "tajawal"
            "roboto_mono" -> "noto_kufi_arabic"
            else -> "cairo"
        }
        AppLanguage.FA -> when (headingFontId) {
            "vazirmatn", "amiri", "cairo" -> headingFontId
            else -> "vazirmatn"
        }
        AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> when (headingFontId) {
            "plus_jakarta_sans", "montserrat", "outfit", "playfair_display", "lora", "roboto_mono" -> headingFontId
            "amiri" -> "playfair_display"
            "noto_kufi_arabic" -> "outfit"
            "tajawal", "almarai" -> "montserrat"
            else -> "plus_jakarta_sans"
        }
    }

    fun resolveBodyFont(language: AppLanguage): String = when (language) {
        AppLanguage.AR, AppLanguage.UR -> when (bodyFontId) {
            "cairo", "tajawal", "almarai", "amiri", "noto_kufi_arabic", "vazirmatn" -> bodyFontId
            "lora", "playfair_display" -> "amiri"
            "montserrat", "outfit" -> "tajawal"
            else -> "cairo"
        }
        AppLanguage.FA -> "vazirmatn"
        AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> when (bodyFontId) {
            "plus_jakarta_sans", "montserrat", "outfit", "playfair_display", "lora", "roboto_mono" -> bodyFontId
            "tajawal", "almarai" -> "outfit"
            "amiri" -> "lora"
            else -> "plus_jakarta_sans"
        }
    }

    val isDarkBackground: Boolean
        get() {
            val clean = bgHex.removePrefix("#")
            if (clean.length != 6) return false
            val r = clean.substring(0, 2).toIntOrNull(16) ?: 255
            val g = clean.substring(2, 4).toIntOrNull(16) ?: 255
            val b = clean.substring(4, 6).toIntOrNull(16) ?: 255
            return (0.299 * r + 0.587 * g + 0.114 * b) < 128.0
        }

    val cardFillHex: String
        get() = if (isDarkBackground) "#1E293B" else "#FFFFFF"
}

object TemplateRepository {

    val templates: List<PresentationTemplate> = listOf(
        // === 1-4: EDUCATIONAL ===
        PresentationTemplate(
            id = "tpl_educational",
            category = TemplateCategory.EDUCATIONAL,
            nameAr = "التعليم التفاعلي الحديث",
            nameEn = "Minimal Education",
            nameFa = "آموزش مدرن",
            primaryHex = "#4D7C0F", secondaryHex = "#84CC16", accentHex = "#166534",
            bgHex = "#FFFFFF", titleColorHex = "#14532D", bodyColorHex = "#334155",
            headingFontId = "tajawal", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_edu_classroom",
            category = TemplateCategory.EDUCATIONAL,
            nameAr = "سبورة الصف الذكية",
            nameEn = "Smart Chalkboard Green",
            nameFa = "تخته هوشمند کلاسی",
            primaryHex = "#14532D", secondaryHex = "#064E3B", accentHex = "#FACC15",
            bgHex = "#052E16", titleColorHex = "#FEF9C3", bodyColorHex = "#DCFCE7",
            headingFontId = "almarai", bodyFontId = "tajawal",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_edu_stem",
            category = TemplateCategory.EDUCATIONAL,
            nameAr = "مناهج STEM والابتكار",
            nameEn = "STEM Discovery Bright",
            nameFa = "آموزش خلاق STEM",
            primaryHex = "#0284C7", secondaryHex = "#2563EB", accentHex = "#F97316",
            bgHex = "#F0F9FF", titleColorHex = "#0C4A6E", bodyColorHex = "#1E293B",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_edu_pastel",
            category = TemplateCategory.EDUCATIONAL,
            nameAr = "الدفتر التعليمي الدافئ",
            nameEn = "Warm Study Notebook",
            nameFa = "دفترچه آموزشی گرم",
            primaryHex = "#D97706", secondaryHex = "#EA580C", accentHex = "#0D9488",
            bgHex = "#FFFBEB", titleColorHex = "#78350F", bodyColorHex = "#451A03",
            headingFontId = "cairo", bodyFontId = "tajawal",
            defaultTransition = SlideTransitionType.SLIDE_UP, coverStyle = CoverDesignStyle.TOP_BANNER
        ),

        // === 5-8: UNIVERSITY ===
        PresentationTemplate(
            id = "tpl_university",
            category = TemplateCategory.UNIVERSITY,
            nameAr = "رسائل الماجستير والدكتوراه",
            nameEn = "Academic Thesis Navy",
            nameFa = "پایان‌نامه دانشگاهی",
            primaryHex = "#1E293B", secondaryHex = "#3B5998", accentHex = "#D4AF37",
            bgHex = "#F8FAFC", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "amiri", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_university_crimson",
            category = TemplateCategory.UNIVERSITY,
            nameAr = "البحث الجامعي الكلاسيكي",
            nameEn = "Classic Crimson University",
            nameFa = "تحقیقات کلاسیک دانشگاهی",
            primaryHex = "#7F1D1D", secondaryHex = "#B91C1C", accentHex = "#F59E0B",
            bgHex = "#FFFBEB", titleColorHex = "#450A0A", bodyColorHex = "#374151",
            headingFontId = "playfair_display", bodyFontId = "lora",
            defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_uni_oxford",
            category = TemplateCategory.UNIVERSITY,
            nameAr = "أطروحة أكسفورد الملكية",
            nameEn = "Oxford Royal Colloquium",
            nameFa = "سمینار آکسفورد",
            primaryHex = "#172554", secondaryHex = "#1E3A8A", accentHex = "#EAB308",
            bgHex = "#F8FAFC", titleColorHex = "#172554", bodyColorHex = "#334155",
            headingFontId = "lora", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.REVEAL, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_uni_seminar",
            category = TemplateCategory.UNIVERSITY,
            nameAr = "ندوة الدراسات العليا",
            nameEn = "Graduate Symposium Teal",
            nameFa = "نشست تحصیلات تکمیلی",
            primaryHex = "#115E59", secondaryHex = "#0F766E", accentHex = "#F43F5E",
            bgHex = "#FFFFFF", titleColorHex = "#134E4A", bodyColorHex = "#334155",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.WIPE, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),

        // === 9-12: SCIENTIFIC ===
        PresentationTemplate(
            id = "tpl_scientific",
            category = TemplateCategory.SCIENTIFIC,
            nameAr = "المختبر والبحث العلمي",
            nameEn = "Scientific Research Lab",
            nameFa = "پژوهش آزمایشگاهی",
            primaryHex = "#0284C7", secondaryHex = "#06B6D4", accentHex = "#0F766E",
            bgHex = "#F0FDFA", titleColorHex = "#0C4A6E", bodyColorHex = "#1E293B",
            headingFontId = "almarai", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.WIPE, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_sci_quantum",
            category = TemplateCategory.SCIENTIFIC,
            nameAr = "الفيزياء والفضاء الكوني",
            nameEn = "Quantum & Cosmos Deep",
            nameFa = "فیزیک کوانتوم و فضا",
            primaryHex = "#0B1120", secondaryHex = "#1E1B4B", accentHex = "#38BDF8",
            bgHex = "#090D16", titleColorHex = "#E0F2FE", bodyColorHex = "#CBD5E1",
            headingFontId = "montserrat", bodyFontId = "outfit",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_sci_bio",
            category = TemplateCategory.SCIENTIFIC,
            nameAr = "الأحياء الجزيئية والبيئة",
            nameEn = "BioGenetics & Ecology",
            nameFa = "زیست‌شناسی و محیط زیست",
            primaryHex = "#047857", secondaryHex = "#059669", accentHex = "#0EA5E9",
            bgHex = "#ECFDF5", titleColorHex = "#064E3B", bodyColorHex = "#1F2937",
            headingFontId = "tajawal", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.SPLIT, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_sci_chemistry",
            category = TemplateCategory.SCIENTIFIC,
            nameAr = "المؤتمر الكيميائي التحليلي",
            nameEn = "Analytical Chemistry Indigo",
            nameFa = "شیمی تحلیلی",
            primaryHex = "#4338CA", secondaryHex = "#3730A3", accentHex = "#14B8A6",
            bgHex = "#EEF2FF", titleColorHex = "#1E1B4B", bodyColorHex = "#312E81",
            headingFontId = "roboto_mono", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.TOP_BANNER
        ),

        // === 13-16: TECHNOLOGY ===
        PresentationTemplate(
            id = "tpl_tech_ai",
            category = TemplateCategory.TECHNOLOGY,
            nameAr = "الذكاء الاصطناعي والمستقبل",
            nameEn = "AI & Cyber Future",
            nameFa = "هوش مصنوعی و آینده",
            primaryHex = "#312E81", secondaryHex = "#6366F1", accentHex = "#22D3EE",
            bgHex = "#0F172A", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "noto_kufi_arabic", bodyFontId = "tajawal",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_tech_neon",
            category = TemplateCategory.TECHNOLOGY,
            nameAr = "الحوسبة السحابية النيون",
            nameEn = "Cloud Matrix Neon",
            nameFa = "ابر محاسباتی نئون",
            primaryHex = "#18181B", secondaryHex = "#27272A", accentHex = "#A855F7",
            bgHex = "#09090B", titleColorHex = "#FAFAFA", bodyColorHex = "#D4D4D8",
            headingFontId = "outfit", bodyFontId = "roboto_mono",
            defaultTransition = SlideTransitionType.FLIP, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_tech_saas",
            category = TemplateCategory.TECHNOLOGY,
            nameAr = "إطلاق المنتجات الرقمية SaaS",
            nameEn = "Clean Product SaaS Blue",
            nameFa = "معرفی محصول دیجیتال",
            primaryHex = "#2563EB", secondaryHex = "#3B82F6", accentHex = "#10B981",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#475569",
            headingFontId = "plus_jakarta_sans", bodyFontId = "outfit",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_tech_cyber",
            category = TemplateCategory.TECHNOLOGY,
            nameAr = "الأمن السيبراني والشبكات",
            nameEn = "Cyber Defense Terminal",
            nameFa = "امنیت سایبری و شبکه",
            primaryHex = "#064E3B", secondaryHex = "#0F172A", accentHex = "#22C55E",
            bgHex = "#020617", titleColorHex = "#4ADE80", bodyColorHex = "#E2E8F0",
            headingFontId = "roboto_mono", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.WIPE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),

        // === 17-19: ADMINISTRATIVE ===
        PresentationTemplate(
            id = "tpl_administrative",
            category = TemplateCategory.ADMINISTRATIVE,
            nameAr = "التقارير الإدارية والمؤسسية",
            nameEn = "Executive Administration",
            nameFa = "مدیریت سازمانی",
            primaryHex = "#1D4ED8", secondaryHex = "#0F172A", accentHex = "#F59E0B",
            bgHex = "#FFFFFF", titleColorHex = "#1E3A8A", bodyColorHex = "#334155",
            headingFontId = "almarai", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_admin_board",
            category = TemplateCategory.ADMINISTRATIVE,
            nameAr = "اجتماع مجلس الإدارة",
            nameEn = "Boardroom Slate & Silver",
            nameFa = "گزارش هیئت‌مدیره",
            primaryHex = "#334155", secondaryHex = "#1E293B", accentHex = "#0EA5E9",
            bgHex = "#F8FAFC", titleColorHex = "#0F172A", bodyColorHex = "#475569",
            headingFontId = "montserrat", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_admin_hr",
            category = TemplateCategory.ADMINISTRATIVE,
            nameAr = "الموارد البشرية والتطوير",
            nameEn = "People & Culture Coral",
            nameFa = "منابع انسانی و توسعه",
            primaryHex = "#BE123C", secondaryHex = "#E11D48", accentHex = "#0D9488",
            bgHex = "#FFF1F2", titleColorHex = "#881337", bodyColorHex = "#374151",
            headingFontId = "tajawal", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.SLIDE_UP, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),

        // === 20-23: ECONOMIC ===
        PresentationTemplate(
            id = "tpl_economic",
            category = TemplateCategory.ECONOMIC,
            nameAr = "التحليل الاقتصادي والمؤشرات",
            nameEn = "Economic & Market Insights",
            nameFa = "تحلیل اقتصادی",
            primaryHex = "#065F46", secondaryHex = "#10B981", accentHex = "#D97706",
            bgHex = "#F8FAFC", titleColorHex = "#064E3B", bodyColorHex = "#1F2937",
            headingFontId = "cairo", bodyFontId = "tajawal",
            defaultTransition = SlideTransitionType.SPLIT, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_econ_fintech",
            category = TemplateCategory.ECONOMIC,
            nameAr = "التقنية المالية والاستثمار",
            nameEn = "FinTech Ledger Navy",
            nameFa = "فین‌تک و سرمایه‌گذاری",
            primaryHex = "#0F172A", secondaryHex = "#1E3A8A", accentHex = "#22C55E",
            bgHex = "#0B1120", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_econ_quarterly",
            category = TemplateCategory.ECONOMIC,
            nameAr = "التقرير المالي الربع سنوي",
            nameEn = "Quarterly Fiscal Gold",
            nameFa = "گزارش مالی فصلی",
            primaryHex = "#1E293B", secondaryHex = "#0F172A", accentHex = "#EAB308",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "playfair_display", bodyFontId = "montserrat",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_econ_banking",
            category = TemplateCategory.ECONOMIC,
            nameAr = "المصارف وأسواق المال",
            nameEn = "Banking & Treasury",
            nameFa = "بانکداری و بورس",
            primaryHex = "#0C4A6E", secondaryHex = "#0369A1", accentHex = "#F59E0B",
            bgHex = "#F0F9FF", titleColorHex = "#0C4A6E", bodyColorHex = "#1E293B",
            headingFontId = "almarai", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.TOP_BANNER
        ),

        // === 24-27: MEDICAL ===
        PresentationTemplate(
            id = "tpl_medical",
            category = TemplateCategory.MEDICAL,
            nameAr = "الدراسات الطبية والسريرية",
            nameEn = "Medical & Clinical Deck",
            nameFa = "مطالعات پزشکی",
            primaryHex = "#0F766E", secondaryHex = "#14B8A6", accentHex = "#EF4444",
            bgHex = "#F0FDFA", titleColorHex = "#134E4A", bodyColorHex = "#1E293B",
            headingFontId = "cairo", bodyFontId = "tajawal",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_med_cardio",
            category = TemplateCategory.MEDICAL,
            nameAr = "مؤتمر الطب السريري",
            nameEn = "Cardiology Summit Clean",
            nameFa = "کنگره قلب و عروق",
            primaryHex = "#991B1B", secondaryHex = "#DC2626", accentHex = "#0EA5E9",
            bgHex = "#FFFFFF", titleColorHex = "#7F1D1D", bodyColorHex = "#334155",
            headingFontId = "montserrat", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.REVEAL, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_med_pharma",
            category = TemplateCategory.MEDICAL,
            nameAr = "الصيدلة والتجارب الدوائية",
            nameEn = "Pharma Trials Cyan",
            nameFa = "داروسازی و کارآزمایی",
            primaryHex = "#0369A1", secondaryHex = "#06B6D4", accentHex = "#10B981",
            bgHex = "#F8FAFC", titleColorHex = "#0C4A6E", bodyColorHex = "#334155",
            headingFontId = "almarai", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.WIPE, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_med_public",
            category = TemplateCategory.MEDICAL,
            nameAr = "الصحة العامة والتوعية",
            nameEn = "Public Health Mint",
            nameFa = "بهداشت عمومی",
            primaryHex = "#059669", secondaryHex = "#34D399", accentHex = "#0284C7",
            bgHex = "#ECFDF5", titleColorHex = "#065F46", bodyColorHex = "#1F2937",
            headingFontId = "tajawal", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.SLIDE_UP, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),

        // === 28-31: ENGINEERING ===
        PresentationTemplate(
            id = "tpl_engineering",
            category = TemplateCategory.ENGINEERING,
            nameAr = "الهندسة والابتكار التقني",
            nameEn = "Engineering Blueprint",
            nameFa = "مهندسی و فناوری",
            primaryHex = "#0D47A1", secondaryHex = "#2563EB", accentHex = "#38BDF8",
            bgHex = "#0F172A", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.WIPE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_eng_arch",
            category = TemplateCategory.ENGINEERING,
            nameAr = "العمارة والتخطيط العمراني",
            nameEn = "Architectural Minimalist",
            nameFa = "معماری و شهرسازی",
            primaryHex = "#292524", secondaryHex = "#57534E", accentHex = "#EA580C",
            bgHex = "#FAFAF9", titleColorHex = "#1C1917", bodyColorHex = "#44403C",
            headingFontId = "montserrat", bodyFontId = "outfit",
            defaultTransition = SlideTransitionType.SPLIT, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_eng_energy",
            category = TemplateCategory.ENGINEERING,
            nameAr = "الطاقة المتجددة والاستدامة",
            nameEn = "Clean Solar Engineering",
            nameFa = "انرژی تجدیدپذیر",
            primaryHex = "#0F766E", secondaryHex = "#0284C7", accentHex = "#FACC15",
            bgHex = "#F0FDFA", titleColorHex = "#134E4A", bodyColorHex = "#1E293B",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_eng_robotics",
            category = TemplateCategory.ENGINEERING,
            nameAr = "الميكانيك والروبوتات الصناعية",
            nameEn = "Robotics & CAD Steel",
            nameFa = "رباتیک و مکانیک",
            primaryHex = "#3F3F46", secondaryHex = "#18181B", accentHex = "#F97316",
            bgHex = "#18181B", titleColorHex = "#FAFAFA", bodyColorHex = "#D4D4D8",
            headingFontId = "roboto_mono", bodyFontId = "outfit",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),

        // === 32-34: LAW ===
        PresentationTemplate(
            id = "tpl_law",
            category = TemplateCategory.LAW,
            nameAr = "الدراسات القانونية والحوكمة",
            nameEn = "Legal Brief & Governance",
            nameFa = "مطالعات حقوقی",
            primaryHex = "#334155", secondaryHex = "#475569", accentHex = "#B45309",
            bgHex = "#F8FAFC", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "amiri", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_law_court",
            category = TemplateCategory.LAW,
            nameAr = "المرافعات والأنظمة التشريعية",
            nameEn = "Chambers Burgundy Gold",
            nameFa = "وکالت و آیین دادرسی",
            primaryHex = "#450A0A", secondaryHex = "#7F1D1D", accentHex = "#D4AF37",
            bgHex = "#FDFBF7", titleColorHex = "#450A0A", bodyColorHex = "#292524",
            headingFontId = "lora", bodyFontId = "playfair_display",
            defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_law_compliance",
            category = TemplateCategory.LAW,
            nameAr = "الامتثال المؤسسي والسياسات",
            nameEn = "Corporate Compliance",
            nameFa = "انطباق سازمانی و قوانین",
            primaryHex = "#1E3A8A", secondaryHex = "#334155", accentHex = "#0EA5E9",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "almarai", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.TOP_BANNER
        ),

        // === 35-37: LITERATURE ===
        PresentationTemplate(
            id = "tpl_literature",
            category = TemplateCategory.LITERATURE,
            nameAr = "الأدب والعلوم الإنسانية",
            nameEn = "Editorial Humanities",
            nameFa = "ادبیات و علوم انسانی",
            primaryHex = "#581C87", secondaryHex = "#7E22CE", accentHex = "#EC4899",
            bgHex = "#FAF5FF", titleColorHex = "#3B0764", bodyColorHex = "#374151",
            headingFontId = "amiri", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_lit_poetry",
            category = TemplateCategory.LITERATURE,
            nameAr = "ديوان البلاغة والشعر العربي",
            nameEn = "Classic Ivory Anthology",
            nameFa = "شعر و بلاغت کلاسیک",
            primaryHex = "#7C2D12", secondaryHex = "#9A3412", accentHex = "#D97706",
            bgHex = "#FFFBF5", titleColorHex = "#431407", bodyColorHex = "#44403C",
            headingFontId = "amiri", bodyFontId = "vazirmatn",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_lit_journalism",
            category = TemplateCategory.LITERATURE,
            nameAr = "الإعلام والصحافة الثقافية",
            nameEn = "Magazine Press Noir",
            nameFa = "روزنامه‌نگاری و رسانه",
            primaryHex = "#18181B", secondaryHex = "#3F3F46", accentHex = "#E11D48",
            bgHex = "#FAFAFA", titleColorHex = "#09090B", bodyColorHex = "#27272A",
            headingFontId = "playfair_display", bodyFontId = "lora",
            defaultTransition = SlideTransitionType.SLIDE_UP, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),

        // === 38-40: HISTORICAL ===
        PresentationTemplate(
            id = "tpl_historical",
            category = TemplateCategory.HISTORICAL,
            nameAr = "التاريخ والحضارات",
            nameEn = "Historical & Heritage",
            nameFa = "تاریخ و تمدن‌ها",
            primaryHex = "#78350F", secondaryHex = "#B45309", accentHex = "#D97706",
            bgHex = "#FAF5EE", titleColorHex = "#451A03", bodyColorHex = "#44403C",
            headingFontId = "amiri", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.REVEAL, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_hist_babylon",
            category = TemplateCategory.HISTORICAL,
            nameAr = "حضارات وادي الرافدين",
            nameEn = "Ancient Mesopotamia Lapis",
            nameFa = "تمدن بین‌النهرین",
            primaryHex = "#1E3A8A", secondaryHex = "#1D4ED8", accentHex = "#F59E0B",
            bgHex = "#FEFCE8", titleColorHex = "#1E3A8A", bodyColorHex = "#422006",
            headingFontId = "noto_kufi_arabic", bodyFontId = "amiri",
            defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_hist_museum",
            category = TemplateCategory.HISTORICAL,
            nameAr = "أرشيف المتاحف والآثار",
            nameEn = "Museum Archive Bronze",
            nameFa = "موزه و باستان‌شناسی",
            primaryHex = "#292524", secondaryHex = "#44403C", accentHex = "#CA8A04",
            bgHex = "#1C1917", titleColorHex = "#FEF3C7", bodyColorHex = "#E7E5E4",
            headingFontId = "playfair_display", bodyFontId = "lora",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.TOP_BANNER
        ),

        // === 41-44: ISLAMIC ===
        PresentationTemplate(
            id = "tpl_islamic",
            category = TemplateCategory.ISLAMIC,
            nameAr = "الدراسات الإسلامية والتراث",
            nameEn = "Islamic Classic Gold",
            nameFa = "مطالعات اسلامی",
            primaryHex = "#064E3B", secondaryHex = "#047857", accentHex = "#D4AF37",
            bgHex = "#FDFBF7", titleColorHex = "#064E3B", bodyColorHex = "#1F2937",
            headingFontId = "amiri", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.FADE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_islamic_andalus",
            category = TemplateCategory.ISLAMIC,
            nameAr = "الزخرفة الأندلسية الزرقاء",
            nameEn = "Andalusian Cobalt & Gold",
            nameFa = "معماری اندلسی",
            primaryHex = "#1E3A8A", secondaryHex = "#0F766E", accentHex = "#EAB308",
            bgHex = "#F8FAFC", titleColorHex = "#172554", bodyColorHex = "#1E293B",
            headingFontId = "noto_kufi_arabic", bodyFontId = "amiri",
            defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_islamic_quran",
            category = TemplateCategory.ISLAMIC,
            nameAr = "الحلقات القرآنية والشرعية",
            nameEn = "Parchment Manuscript",
            nameFa = "پژوهش‌های قرآنی",
            primaryHex = "#3F6212", secondaryHex = "#166534", accentHex = "#B45309",
            bgHex = "#FEFCE8", titleColorHex = "#365314", bodyColorHex = "#3F3F46",
            headingFontId = "amiri", bodyFontId = "tajawal",
            defaultTransition = SlideTransitionType.REVEAL, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_islamic_night",
            category = TemplateCategory.ISLAMIC,
            nameAr = "المناسبات الدينية الليلي",
            nameEn = "Crescent Night Emerald",
            nameFa = "شب‌های نورانی زمردی",
            primaryHex = "#022C22", secondaryHex = "#064E3B", accentHex = "#FBBF24",
            bgHex = "#06231C", titleColorHex = "#FEF3C7", bodyColorHex = "#D1FAE5",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),

        // === 45-47: CREATIVE ===
        PresentationTemplate(
            id = "tpl_creative",
            category = TemplateCategory.CREATIVE,
            nameAr = "الإبداع والتصميم البصري",
            nameEn = "Creative Studio Gradient",
            nameFa = "استودیو طراحی خلاقانه",
            primaryHex = "#BE185D", secondaryHex = "#F43F5E", accentHex = "#F59E0B",
            bgHex = "#FFF1F2", titleColorHex = "#881337", bodyColorHex = "#374151",
            headingFontId = "tajawal", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.FLIP, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_creative_sunset",
            category = TemplateCategory.CREATIVE,
            nameAr = "معرض التصميم العصري",
            nameEn = "Modern Design Peach",
            nameFa = "پورتفولیو مدرن",
            primaryHex = "#EA580C", secondaryHex = "#DB2777", accentHex = "#7C3AED",
            bgHex = "#FFF7ED", titleColorHex = "#7C2D12", bodyColorHex = "#334155",
            headingFontId = "outfit", bodyFontId = "montserrat",
            defaultTransition = SlideTransitionType.SLIDE_UP, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_creative_minimal",
            category = TemplateCategory.CREATIVE,
            nameAr = "البورتفوليو البسيط المونوكروم",
            nameEn = "Swiss Minimal Portfolio",
            nameFa = "پورتفولیو مینیمال سوئیسی",
            primaryHex = "#18181B", secondaryHex = "#27272A", accentHex = "#EF4444",
            bgHex = "#FFFFFF", titleColorHex = "#09090B", bodyColorHex = "#3F3F46",
            headingFontId = "montserrat", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.SPLIT, coverStyle = CoverDesignStyle.TOP_BANNER
        ),

        // === 48-50: BUSINESS ===
        PresentationTemplate(
            id = "tpl_business",
            category = TemplateCategory.BUSINESS,
            nameAr = "نموذج خطة العمل والمشاريع",
            nameEn = "Modern Corporate Pitch",
            nameFa = "طرح کسب‌وکار",
            primaryHex = "#1E3A8A", secondaryHex = "#2563EB", accentHex = "#F59E0B",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#475569",
            headingFontId = "almarai", bodyFontId = "cairo",
            defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_startup_dark",
            category = TemplateCategory.BUSINESS,
            nameAr = "عرض الشركات الناشئة الليلي",
            nameEn = "Dark Executive Startup",
            nameFa = "استارتاپ مدرن تیره",
            primaryHex = "#090D16", secondaryHex = "#1E293B", accentHex = "#10B981",
            bgHex = "#0F172A", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_biz_venture",
            category = TemplateCategory.BUSINESS,
            nameAr = "ملف المستثمرين والنمو",
            nameEn = "Venture Capital Violet",
            nameFa = "ارائه سرمایه‌گذاران",
            primaryHex = "#4C1D95", secondaryHex = "#6D28D9", accentHex = "#06B6D4",
            bgHex = "#F5F3FF", titleColorHex = "#2E1065", bodyColorHex = "#334155",
            headingFontId = "montserrat", bodyFontId = "plus_jakarta_sans",
            defaultTransition = SlideTransitionType.REVEAL, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),

        // === 51-100: EXTENDED PROFESSIONAL STUDIO SUITE (50 additional unique templates) ===
        PresentationTemplate(
            id = "tpl_edu_stem_51", category = TemplateCategory.EDUCATIONAL,
            nameAr = "مختبرات STEM التفاعلية", nameEn = "Interactive STEM Lab", nameFa = "آزمایشگاه STEM",
            primaryHex = "#0284C7", secondaryHex = "#0EA5E9", accentHex = "#F59E0B",
            bgHex = "#F0F9FF", titleColorHex = "#0C4A6E", bodyColorHex = "#1E293B",
            headingFontId = "tajawal", bodyFontId = "cairo", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_edu_k12_52", category = TemplateCategory.EDUCATIONAL,
            nameAr = "بوابة المناهج الذكية", nameEn = "Smart Curriculum Portal", nameFa = "درگاه آموزش هوشمند",
            primaryHex = "#059669", secondaryHex = "#10B981", accentHex = "#F97316",
            bgHex = "#ECFDF5", titleColorHex = "#064E3B", bodyColorHex = "#1F2937",
            headingFontId = "almarai", bodyFontId = "cairo", defaultTransition = SlideTransitionType.EXCITING, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_edu_linguistics_53", category = TemplateCategory.EDUCATIONAL,
            nameAr = "أكاديمية اللغات والترجمة", nameEn = "Global Linguistics", nameFa = "آکادمی زبان‌ها",
            primaryHex = "#4338CA", secondaryHex = "#6366F1", accentHex = "#EC4899",
            bgHex = "#EEF2FF", titleColorHex = "#1E1B4B", bodyColorHex = "#334155",
            headingFontId = "cairo", bodyFontId = "tajawal", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_uni_thesis_54", category = TemplateCategory.UNIVERSITY,
            nameAr = "الدكتوراه والماجستير الملكي", nameEn = "Doctoral Defense Royal", nameFa = "دفاع دکتری سلطنتی",
            primaryHex = "#172554", secondaryHex = "#1E3A8A", accentHex = "#D4AF37",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "amiri", bodyFontId = "cairo", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_uni_seminar_55", category = TemplateCategory.UNIVERSITY,
            nameAr = "ندوة الدراسات العليا", nameEn = "Graduate Colloquium", nameFa = "سمینار تحصیلات تکمیلی",
            primaryHex = "#312E81", secondaryHex = "#4F46E5", accentHex = "#14B8A6",
            bgHex = "#F8FAFC", titleColorHex = "#1E1B4B", bodyColorHex = "#334155",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_uni_oxbridge_56", category = TemplateCategory.UNIVERSITY,
            nameAr = "تقاليد الجامعات العريقة", nameEn = "Oxbridge Classic Garnet", nameFa = "کلاسیک دانشگاهی",
            primaryHex = "#7F1D1D", secondaryHex = "#991B1B", accentHex = "#C9963A",
            bgHex = "#FAF7F0", titleColorHex = "#450A0A", bodyColorHex = "#292524",
            headingFontId = "playfair_display", bodyFontId = "lora", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_uni_dean_57", category = TemplateCategory.UNIVERSITY,
            nameAr = "التقرير الأكاديمي السنوي", nameEn = "Annual Dean Report", nameFa = "گزارش سالانه دانشکده",
            primaryHex = "#0F766E", secondaryHex = "#0D9488", accentHex = "#F59E0B",
            bgHex = "#F8FAFC", titleColorHex = "#134E4A", bodyColorHex = "#334155",
            headingFontId = "almarai", bodyFontId = "tajawal", defaultTransition = SlideTransitionType.CURTAIN, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_sci_quantum_58", category = TemplateCategory.SCIENTIFIC,
            nameAr = "فيزياء الكم والطاقة", nameEn = "Quantum Physics Dark", nameFa = "فیزیک کوانتوم",
            primaryHex = "#020617", secondaryHex = "#1E1B4B", accentHex = "#22D3EE",
            bgHex = "#090D16", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "roboto_mono", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.SCIENTIFIC, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_sci_biotech_59", category = TemplateCategory.SCIENTIFIC,
            nameAr = "الجينوم والتقنية الحيوية", nameEn = "Genomics & Biotech", nameFa = "ژنوم و بیوتکنولوژی",
            primaryHex = "#047857", secondaryHex = "#059669", accentHex = "#38BDF8",
            bgHex = "#F0FDF4", titleColorHex = "#064E3B", bodyColorHex = "#1E293B",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.SCIENTIFIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_sci_chem_60", category = TemplateCategory.SCIENTIFIC,
            nameAr = "مختبر التحليل الكيميائي", nameEn = "Analytical Chemistry Teal", nameFa = "شیمی تجزیه",
            primaryHex = "#00695C", secondaryHex = "#00897B", accentHex = "#F97316",
            bgHex = "#FFFFFF", titleColorHex = "#004D40", bodyColorHex = "#334155",
            headingFontId = "cairo", bodyFontId = "tajawal", defaultTransition = SlideTransitionType.SCIENTIFIC, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_sci_astro_61", category = TemplateCategory.SCIENTIFIC,
            nameAr = "الفلك وعلوم الفضاء", nameEn = "Deep Space Astrophysics", nameFa = "نجوم و فضا",
            primaryHex = "#0F172A", secondaryHex = "#312E81", accentHex = "#FBBF24",
            bgHex = "#020617", titleColorHex = "#FEF3C7", bodyColorHex = "#E2E8F0",
            headingFontId = "montserrat", bodyFontId = "outfit", defaultTransition = SlideTransitionType.ZOOM, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_sci_climate_62", category = TemplateCategory.SCIENTIFIC,
            nameAr = "المناخ والاستدامة البيئية", nameEn = "Climate & Earth Science", nameFa = "اقلیم و زمین‌شناسی",
            primaryHex = "#1B5E20", secondaryHex = "#2E7D32", accentHex = "#0284C7",
            bgHex = "#F7FEE7", titleColorHex = "#14532D", bodyColorHex = "#334155",
            headingFontId = "cairo", bodyFontId = "cairo", defaultTransition = SlideTransitionType.MORPH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_tech_llm_63", category = TemplateCategory.TECHNOLOGY,
            nameAr = "نماذج اللغات الكبرى LLM", nameEn = "Neural LLM Architecture", nameFa = "هوش مصنوعی پیشرفته",
            primaryHex = "#1E1B4B", secondaryHex = "#4338CA", accentHex = "#A855F7",
            bgHex = "#0F172A", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "outfit", bodyFontId = "roboto_mono", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_tech_cyber_64", category = TemplateCategory.TECHNOLOGY,
            nameAr = "الأمن السيبراني والسيادة", nameEn = "Cyber Defense Grid", nameFa = "امنیت سایبری",
            primaryHex = "#090D16", secondaryHex = "#111827", accentHex = "#10B981",
            bgHex = "#020617", titleColorHex = "#D1FAE5", bodyColorHex = "#94A3B8",
            headingFontId = "roboto_mono", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_tech_cloud_65", category = TemplateCategory.TECHNOLOGY,
            nameAr = "الحوسبة السحابية الحديثة", nameEn = "Cloud Native Azure", nameFa = "رایانش ابری",
            primaryHex = "#0369A1", secondaryHex = "#0284C7", accentHex = "#06B6D4",
            bgHex = "#F0F9FF", titleColorHex = "#0C4A6E", bodyColorHex = "#334155",
            headingFontId = "plus_jakarta_sans", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_tech_robotics_66", category = TemplateCategory.TECHNOLOGY,
            nameAr = "الروبوتات والأتمتة الذكية", nameEn = "Robotics & Automation", nameFa = "رباتیک و اتوماسیون",
            primaryHex = "#27272A", secondaryHex = "#3F3F46", accentHex = "#F97316",
            bgHex = "#FAFAFA", titleColorHex = "#18181B", bodyColorHex = "#3F3F46",
            headingFontId = "montserrat", bodyFontId = "outfit", defaultTransition = SlideTransitionType.EXCITING, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_adm_board_67", category = TemplateCategory.ADMINISTRATIVE,
            nameAr = "مجلس الإدارة والحوكمة", nameEn = "Executive Boardroom", nameFa = "هیئت مدیره",
            primaryHex = "#0F172A", secondaryHex = "#334155", accentHex = "#C9963A",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#475569",
            headingFontId = "almarai", bodyFontId = "cairo", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_adm_hr_68", category = TemplateCategory.ADMINISTRATIVE,
            nameAr = "الموارد البشرية والكفاءات", nameEn = "People & Talent Strategy", nameFa = "منابع انسانی",
            primaryHex = "#0D47A1", secondaryHex = "#1976D2", accentHex = "#22C55E",
            bgHex = "#F8FAFC", titleColorHex = "#0D47A1", bodyColorHex = "#334155",
            headingFontId = "tajawal", bodyFontId = "cairo", defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_adm_ops_69", category = TemplateCategory.ADMINISTRATIVE,
            nameAr = "إدارة الجودة والعمليات ISO", nameEn = "Quality & Operations ISO", nameFa = "مدیریت کیفیت",
            primaryHex = "#1E293B", secondaryHex = "#475569", accentHex = "#3B82F6",
            bgHex = "#F1F5F9", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "cairo", bodyFontId = "cairo", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_econ_banking_70", category = TemplateCategory.ECONOMIC,
            nameAr = "التقنية المالية والمصارف", nameEn = "FinTech & Capital Vault", nameFa = "فین‌تک و بانکداری",
            primaryHex = "#042F2E", secondaryHex = "#0F766E", accentHex = "#FBBF24",
            bgHex = "#06231C", titleColorHex = "#FEF3C7", bodyColorHex = "#D1FAE5",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo", defaultTransition = SlideTransitionType.CURTAIN, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_econ_macro_71", category = TemplateCategory.ECONOMIC,
            nameAr = "الاقتصاد الكلي والأسواق", nameEn = "Macroeconomics Briefing", nameFa = "اقتصاد کلان",
            primaryHex = "#1565C0", secondaryHex = "#1E88E5", accentHex = "#EF4444",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "almarai", bodyFontId = "cairo", defaultTransition = SlideTransitionType.SCIENTIFIC, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_econ_audit_72", category = TemplateCategory.ECONOMIC,
            nameAr = "الميزانية والتدقيق المالي", nameEn = "Fiscal Audit Ledger", nameFa = "حسابرسی مالی",
            primaryHex = "#78350F", secondaryHex = "#B45309", accentHex = "#1565C0",
            bgHex = "#FEFCE8", titleColorHex = "#451A03", bodyColorHex = "#44403C",
            headingFontId = "cairo", bodyFontId = "cairo", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_med_cardio_73", category = TemplateCategory.MEDICAL,
            nameAr = "مؤتمر الطب السريري", nameEn = "Clinical Medicine Slate", nameFa = "پزشکی بالینی",
            primaryHex = "#00796B", secondaryHex = "#0097A7", accentHex = "#E11D48",
            bgHex = "#FFFFFF", titleColorHex = "#004D40", bodyColorHex = "#334155",
            headingFontId = "tajawal", bodyFontId = "cairo", defaultTransition = SlideTransitionType.SCIENTIFIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_med_pharma_74", category = TemplateCategory.MEDICAL,
            nameAr = "الأبحاث الدوائية والصيدلة", nameEn = "Pharmaceutical Trials", nameFa = "داروسازی و تحقیقات",
            primaryHex = "#0288D1", secondaryHex = "#0369A1", accentHex = "#10B981",
            bgHex = "#F0F9FF", titleColorHex = "#0C4A6E", bodyColorHex = "#1E293B",
            headingFontId = "plus_jakarta_sans", bodyFontId = "cairo", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_med_neuro_75", category = TemplateCategory.MEDICAL,
            nameAr = "علوم الأعصاب والدماغ", nameEn = "Neuroscience Horizon", nameFa = "علوم اعصاب",
            primaryHex = "#3730A3", secondaryHex = "#4F46E5", accentHex = "#00BCD4",
            bgHex = "#EEF2FF", titleColorHex = "#1E1B4B", bodyColorHex = "#334155",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.MORPH, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_eng_arch_76", category = TemplateCategory.ENGINEERING,
            nameAr = "العمارة والتخطيط العمراني", nameEn = "Architectural Blueprint", nameFa = "معماری و شهرسازی",
            primaryHex = "#172554", secondaryHex = "#1D4ED8", accentHex = "#F59E0B",
            bgHex = "#EFF6FF", titleColorHex = "#172554", bodyColorHex = "#1E293B",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_eng_civil_77", category = TemplateCategory.ENGINEERING,
            nameAr = "الهندسة الإنشائية والجسور", nameEn = "Structural Civil Grid", nameFa = "مهندسی سازه",
            primaryHex = "#334155", secondaryHex = "#475569", accentHex = "#EA580C",
            bgHex = "#F8FAFC", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "montserrat", bodyFontId = "cairo", defaultTransition = SlideTransitionType.PUSH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_eng_energy_78", category = TemplateCategory.ENGINEERING,
            nameAr = "الطاقة المتجددة والشبكات", nameEn = "Renewable Solar Grid", nameFa = "انرژی تجدیدپذیر",
            primaryHex = "#16A34A", secondaryHex = "#059669", accentHex = "#FACC15",
            bgHex = "#F0FDF4", titleColorHex = "#14532D", bodyColorHex = "#1F2937",
            headingFontId = "almarai", bodyFontId = "cairo", defaultTransition = SlideTransitionType.EXCITING, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_eng_aero_79", category = TemplateCategory.ENGINEERING,
            nameAr = "هندسة الطيران والمحركات", nameEn = "Aerospace Propulsion", nameFa = "مهندسی هوافضا",
            primaryHex = "#0F172A", secondaryHex = "#1E293B", accentHex = "#38BDF8",
            bgHex = "#090D16", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "outfit", bodyFontId = "roboto_mono", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_law_court_80", category = TemplateCategory.LAW,
            nameAr = "المرافعات والدستور العالي", nameEn = "Supreme Charter Navy", nameFa = "آیین دادرسی و قانون",
            primaryHex = "#1E293B", secondaryHex = "#0F172A", accentHex = "#D4AF37",
            bgHex = "#FAF7F0", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "amiri", bodyFontId = "cairo", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_law_intl_81", category = TemplateCategory.LAW,
            nameAr = "القانون الدولي والاتفاقيات", nameEn = "International Treaties", nameFa = "حقوق بین‌الملل",
            primaryHex = "#0D47A1", secondaryHex = "#1E3A8A", accentHex = "#C9963A",
            bgHex = "#FFFFFF", titleColorHex = "#0D47A1", bodyColorHex = "#334155",
            headingFontId = "playfair_display", bodyFontId = "cairo", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_law_compliance_82", category = TemplateCategory.LAW,
            nameAr = "الامتثال والرقابة المؤسسية", nameEn = "Regulatory Compliance", nameFa = "انطباق مقررات",
            primaryHex = "#7F1D1D", secondaryHex = "#991B1B", accentHex = "#0284C7",
            bgHex = "#FFFDFB", titleColorHex = "#450A0A", bodyColorHex = "#292524",
            headingFontId = "cairo", bodyFontId = "tajawal", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_lit_poetry_83", category = TemplateCategory.LITERATURE,
            nameAr = "ديوان الشعر والبلاغة", nameEn = "Classical Poetry Parchment", nameFa = "شعر و بلاغت",
            primaryHex = "#92400E", secondaryHex = "#78350F", accentHex = "#BE123C",
            bgHex = "#F5EFE6", titleColorHex = "#451A03", bodyColorHex = "#44403C",
            headingFontId = "amiri", bodyFontId = "amiri", defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_lit_novel_84", category = TemplateCategory.LITERATURE,
            nameAr = "النقد الأدبي والرواية", nameEn = "Literary Criticism Velvet", nameFa = "نقد ادبی",
            primaryHex = "#5B21B6", secondaryHex = "#7C3AED", accentHex = "#F59E0B",
            bgHex = "#FAF5FF", titleColorHex = "#3B0764", bodyColorHex = "#334155",
            headingFontId = "playfair_display", bodyFontId = "lora", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_lit_theater_85", category = TemplateCategory.LITERATURE,
            nameAr = "الفنون المسرحية والأداء", nameEn = "Dramatic Arts Curtain", nameFa = "هنرهای نمایشی",
            primaryHex = "#881337", secondaryHex = "#BE123C", accentHex = "#FBBF24",
            bgHex = "#FFF1F2", titleColorHex = "#4C0519", bodyColorHex = "#374151",
            headingFontId = "amiri", bodyFontId = "cairo", defaultTransition = SlideTransitionType.CURTAIN, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_hist_silkroad_86", category = TemplateCategory.HISTORICAL,
            nameAr = "طريق الحرير والحضارات", nameEn = "Silk Road Chronicles", nameFa = "جاده ابریشم",
            primaryHex = "#78350F", secondaryHex = "#CA8A04", accentHex = "#0F766E",
            bgHex = "#FAF5EE", titleColorHex = "#451A03", bodyColorHex = "#44403C",
            headingFontId = "amiri", bodyFontId = "cairo", defaultTransition = SlideTransitionType.CURTAIN, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_hist_archive_87", category = TemplateCategory.HISTORICAL,
            nameAr = "الوثائق والمخطوطات النادرة", nameEn = "Royal Manuscript Ledger", nameFa = "نسخ خطی تاریخی",
            primaryHex = "#44403C", secondaryHex = "#292524", accentHex = "#D97706",
            bgHex = "#F5EFE6", titleColorHex = "#1C1917", bodyColorHex = "#44403C",
            headingFontId = "noto_kufi_arabic", bodyFontId = "amiri", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_hist_renaissance_88", category = TemplateCategory.HISTORICAL,
            nameAr = "عصر النهضة والعلوم", nameEn = "Renaissance Florentine", nameFa = "دوران رنسانس",
            primaryHex = "#450A0A", secondaryHex = "#7F1D1D", accentHex = "#D4AF37",
            bgHex = "#FAF7F0", titleColorHex = "#450A0A", bodyColorHex = "#292524",
            headingFontId = "playfair_display", bodyFontId = "lora", defaultTransition = SlideTransitionType.REVEAL, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_isl_cordoba_89", category = TemplateCategory.ISLAMIC,
            nameAr = "مكتبة قرطبة المذهبة", nameEn = "Cordoba Golden Library", nameFa = "کتابخانه قرطبه",
            primaryHex = "#064E3B", secondaryHex = "#0F766E", accentHex = "#D4AF37",
            bgHex = "#FDFBF7", titleColorHex = "#064E3B", bodyColorHex = "#1F2937",
            headingFontId = "amiri", bodyFontId = "cairo", defaultTransition = SlideTransitionType.ACADEMIC, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_isl_ramadan_90", category = TemplateCategory.ISLAMIC,
            nameAr = "الليالي الروحانية الذهبية", nameEn = "Sacred Crescent Indigo", nameFa = "شب‌های معنوی",
            primaryHex = "#172554", secondaryHex = "#312E81", accentHex = "#FBBF24",
            bgHex = "#090D16", titleColorHex = "#FEF3C7", bodyColorHex = "#E2E8F0",
            headingFontId = "noto_kufi_arabic", bodyFontId = "amiri", defaultTransition = SlideTransitionType.CURTAIN, coverStyle = CoverDesignStyle.CENTER_BADGE
        ),
        PresentationTemplate(
            id = "tpl_isl_calligraphy_91", category = TemplateCategory.ISLAMIC,
            nameAr = "فنون الخط العربي الأصيل", nameEn = "Arabic Calligraphy Ivory", nameFa = "خوشنویسی اصیل",
            primaryHex = "#1B5E20", secondaryHex = "#2E7D32", accentHex = "#C9963A",
            bgHex = "#FAF7F0", titleColorHex = "#1B5E20", bodyColorHex = "#334155",
            headingFontId = "amiri", bodyFontId = "tajawal", defaultTransition = SlideTransitionType.DISSOLVE, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_cre_neon_92", category = TemplateCategory.CREATIVE,
            nameAr = "نيون استوديو الإبداعي", nameEn = "Neon Cyber Studio", nameFa = "استودیو نئون",
            primaryHex = "#111827", secondaryHex = "#2E1065", accentHex = "#EC4899",
            bgHex = "#020617", titleColorHex = "#F8FAFC", bodyColorHex = "#CBD5E1",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.EXCITING, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_cre_bauhaus_93", category = TemplateCategory.CREATIVE,
            nameAr = "باوهاوس الهندسي المعاصر", nameEn = "Bauhaus Geometric Pop", nameFa = "باوهاوس مدرن",
            primaryHex = "#DC2626", secondaryHex = "#1D4ED8", accentHex = "#EAB308",
            bgHex = "#FAF7F0", titleColorHex = "#18181B", bodyColorHex = "#27272A",
            headingFontId = "montserrat", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_cre_pastel_94", category = TemplateCategory.CREATIVE,
            nameAr = "حرير الباستيل الناعم", nameEn = "Soft Silk Pastel", nameFa = "پاستل ابریشمی",
            primaryHex = "#6366F1", secondaryHex = "#EC4899", accentHex = "#14B8A6",
            bgHex = "#FDF2F8", titleColorHex = "#312E81", bodyColorHex = "#334155",
            headingFontId = "tajawal", bodyFontId = "cairo", defaultTransition = SlideTransitionType.MORPH, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_cre_monolith_95", category = TemplateCategory.CREATIVE,
            nameAr = "مونوليث الحجري الأنيق", nameEn = "Monolith Gallery Beige", nameFa = "گالری مینیمال",
            primaryHex = "#292524", secondaryHex = "#57534E", accentHex = "#C2410C",
            bgHex = "#F5EFE6", titleColorHex = "#1C1917", bodyColorHex = "#44403C",
            headingFontId = "playfair_display", bodyFontId = "outfit", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.SIDE_RIBBON
        ),
        PresentationTemplate(
            id = "tpl_biz_saas_96", category = TemplateCategory.BUSINESS,
            nameAr = "منصات SaaS والنمو الرقمي", nameEn = "SaaS Rocket Metrics", nameFa = "رشد SaaS",
            primaryHex = "#2563EB", secondaryHex = "#4F46E5", accentHex = "#10B981",
            bgHex = "#FFFFFF", titleColorHex = "#0F172A", bodyColorHex = "#475569",
            headingFontId = "outfit", bodyFontId = "plus_jakarta_sans", defaultTransition = SlideTransitionType.DYNAMIC, coverStyle = CoverDesignStyle.MODERN_CARDS
        ),
        PresentationTemplate(
            id = "tpl_biz_luxury_97", category = TemplateCategory.BUSINESS,
            nameAr = "العلامات الفاخرة والضيافة", nameEn = "Luxury Obsidian & Gold", nameFa = "برند لوکس طلایی",
            primaryHex = "#090D16", secondaryHex = "#1C1917", accentHex = "#D4AF37",
            bgHex = "#020617", titleColorHex = "#FEF3C7", bodyColorHex = "#E2E8F0",
            headingFontId = "playfair_display", bodyFontId = "montserrat", defaultTransition = SlideTransitionType.CURTAIN, coverStyle = CoverDesignStyle.FRAME_GOLD
        ),
        PresentationTemplate(
            id = "tpl_biz_retail_98", category = TemplateCategory.BUSINESS,
            nameAr = "التجارة الإلكترونية والتجزئة", nameEn = "Omnichannel Commerce", nameFa = "تجارت الکترونیک",
            primaryHex = "#C2410C", secondaryHex = "#EA580C", accentHex = "#0284C7",
            bgHex = "#FFF7ED", titleColorHex = "#7C2D12", bodyColorHex = "#334155",
            headingFontId = "almarai", bodyFontId = "cairo", defaultTransition = SlideTransitionType.EXCITING, coverStyle = CoverDesignStyle.DIAGONAL_SPLIT
        ),
        PresentationTemplate(
            id = "tpl_biz_consult_99", category = TemplateCategory.BUSINESS,
            nameAr = "الاستشارات الإدارية العليا", nameEn = "McKinsey-Style Consulting", nameFa = "مشاوره مدیریت",
            primaryHex = "#0D47A1", secondaryHex = "#0369A1", accentHex = "#00897B",
            bgHex = "#F8FAFC", titleColorHex = "#0F172A", bodyColorHex = "#334155",
            headingFontId = "cairo", bodyFontId = "cairo", defaultTransition = SlideTransitionType.STEALTH, coverStyle = CoverDesignStyle.TOP_BANNER
        ),
        PresentationTemplate(
            id = "tpl_biz_vision2030_100", category = TemplateCategory.BUSINESS,
            nameAr = "رؤية المستقبل والتحول الوطني", nameEn = "Future Vision 2030 Emerald", nameFa = "چشم‌انداز آینده",
            primaryHex = "#004D40", secondaryHex = "#059669", accentHex = "#D4AF37",
            bgHex = "#FFFFFF", titleColorHex = "#064E3B", bodyColorHex = "#1E293B",
            headingFontId = "noto_kufi_arabic", bodyFontId = "cairo", defaultTransition = SlideTransitionType.MORPH, coverStyle = CoverDesignStyle.CENTER_BADGE
        )
    )

    fun byIdOrNull(id: String): PresentationTemplate? =
        templates.firstOrNull { it.id == id }

    fun byId(id: String): PresentationTemplate =
        byIdOrNull(id) ?: templates.first()

    private fun defaultFontFor(language: AppLanguage): String = when (language) {
        AppLanguage.AR, AppLanguage.UR -> "cairo"
        AppLanguage.FA -> "vazirmatn"
        AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> "plus_jakarta_sans"
    }

    fun buildPresentationFromTemplate(
        template: PresentationTemplate,
        title: String,
        description: String,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio,
        slideCount: Int = 5
    ): Presentation {
        val w = aspectRatio.defaultWidth
        val h = aspectRatio.defaultHeight
        val isRtl = language.isRtl
        val dir = if (isRtl) WritingDirection.RTL else WritingDirection.LTR
        val headingFont = template.resolveHeadingFont(language)
        val bodyFont = template.resolveBodyFont(language)
        val targetCount = slideCount.coerceIn(1, MAX_SLIDES_PER_DECK)

        val slides = mutableListOf<Slide>()

        // Slide 1: Cover shaped by template.coverStyle
        slides.add(
            buildCoverSlide(
                template = template,
                title = title.ifBlank { template.name(language) },
                subtitle = description.ifBlank { defaultSubtitle(language) },
                w = w,
                h = h,
                isRtl = isRtl,
                dir = dir,
                headingFont = headingFont,
                bodyFont = bodyFont
            )
        )

        if (targetCount >= 2) {
            val archetypes = listOf(
                SlideArchetype.THREE_PILLARS,
                SlideArchetype.CHART_BAR,
                SlideArchetype.COMPARISON_TABLE,
                SlideArchetype.TWO_COLUMN,
                SlideArchetype.KPI_METRICS,
                SlideArchetype.SECTION_DIVIDER,
                SlideArchetype.TIMELINE_ROADMAP,
                SlideArchetype.QUOTE_SPOTLIGHT,
                SlideArchetype.CHART_LINE
            )
            for (i in 1 until targetCount) {
                val isLast = i == targetCount - 1
                if (isLast) {
                    slides.add(
                        buildConclusionSlide(
                            order = i,
                            template = template,
                            language = language,
                            w = w,
                            h = h,
                            dir = dir,
                            headingFont = headingFont,
                            bodyFont = bodyFont
                        )
                    )
                } else {
                    val archetype = archetypes[(i - 1) % archetypes.size]
                    slides.add(
                        buildMiddleSlideByArchetype(
                            archetype = archetype,
                            order = i,
                            sectionNum = i,
                            template = template,
                            language = language,
                            w = w,
                            h = h,
                            isRtl = isRtl,
                            dir = dir,
                            headingFont = headingFont,
                            bodyFont = bodyFont
                        )
                    )
                }
            }
        }

        return Presentation(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { template.name(language) },
            description = description,
            language = language,
            aspectRatio = aspectRatio,
            canvasWidth = w,
            canvasHeight = h,
            themeId = template.id,
            sourceType = PresentationSourceType.TEMPLATE,
            slides = slides
        )
    }

    private enum class SlideArchetype {
        THREE_PILLARS,
        CHART_BAR,
        COMPARISON_TABLE,
        TWO_COLUMN,
        KPI_METRICS,
        SECTION_DIVIDER,
        TIMELINE_ROADMAP,
        QUOTE_SPOTLIGHT,
        CHART_LINE
    }

    private fun buildCoverSlide(
        template: PresentationTemplate,
        title: String,
        subtitle: String,
        w: Float,
        h: Float,
        isRtl: Boolean,
        dir: WritingDirection,
        headingFont: String,
        bodyFont: String
    ): Slide {
        val els = mutableListOf<SlideElement>()
        when (template.coverStyle) {
            CoverDesignStyle.SIDE_RIBBON -> {
                els.add(
                    SlideElement.ShapeElement(
                        x = if (isRtl) 0f else w * 0.72f, y = 0f, width = w * 0.28f, height = h,
                        zIndex = 1, shapeType = ShapeType.RECTANGLE,
                        fillColorHex = template.accentHex, strokeColorHex = template.accentHex,
                        strokeWidth = 0f, opacity = 0.24f
                    )
                )
            }
            CoverDesignStyle.DIAGONAL_SPLIT -> {
                els.add(
                    SlideElement.ShapeElement(
                        x = w * 0.65f, y = -h * 0.1f, width = w * 0.45f, height = h * 0.8f,
                        rotation = 18f, zIndex = 1, shapeType = ShapeType.ROUNDED_RECT,
                        fillColorHex = template.accentHex, strokeColorHex = template.accentHex,
                        strokeWidth = 0f, opacity = 0.22f
                    )
                )
            }
            CoverDesignStyle.FRAME_GOLD -> {
                els.add(
                    SlideElement.ShapeElement(
                        x = w * 0.04f, y = h * 0.06f, width = w * 0.92f, height = h * 0.88f,
                        zIndex = 1, shapeType = ShapeType.ROUNDED_RECT,
                        fillColorHex = "#00000000", strokeColorHex = template.accentHex, strokeWidth = 5f
                    )
                )
            }
            CoverDesignStyle.MODERN_CARDS -> {
                els.add(
                    SlideElement.ShapeElement(
                        x = w * 0.06f, y = h * 0.16f, width = w * 0.88f, height = h * 0.68f,
                        zIndex = 1, shapeType = ShapeType.ROUNDED_RECT,
                        fillColorHex = "#1AFFFFFF", strokeColorHex = template.accentHex, strokeWidth = 2f
                    )
                )
            }
            CoverDesignStyle.TOP_BANNER -> {
                els.add(
                    SlideElement.ShapeElement(
                        x = 0f, y = 0f, width = w, height = 28f,
                        zIndex = 1, shapeType = ShapeType.RECTANGLE,
                        fillColorHex = template.accentHex, strokeColorHex = template.accentHex, strokeWidth = 0f
                    )
                )
            }
            CoverDesignStyle.CENTER_BADGE -> {
                els.add(
                    SlideElement.ShapeElement(
                        x = w * 0.44f, y = h * 0.14f, width = w * 0.12f, height = 10f,
                        zIndex = 1, shapeType = ShapeType.ROUNDED_RECT,
                        fillColorHex = template.accentHex, strokeColorHex = template.accentHex, strokeWidth = 0f
                    )
                )
            }
        }

        val centered = template.coverStyle == CoverDesignStyle.CENTER_BADGE ||
            template.coverStyle == CoverDesignStyle.FRAME_GOLD
        els.add(
            SlideElement.TextElement(
                x = w * 0.09f, y = h * 0.27f, width = w * 0.82f, height = h * 0.24f,
                zIndex = 2, text = title,
                fontFamilyId = headingFont, fontSizeSp = 64f, textColorHex = "#FFFFFF",
                isBold = true, textAlign = if (centered) TextAlignment.CENTER else TextAlignment.START,
                writingDirection = dir
            )
        )
        els.add(
            SlideElement.TextElement(
                x = w * 0.09f, y = h * 0.55f, width = w * 0.82f, height = h * 0.18f,
                zIndex = 3, text = subtitle,
                fontFamilyId = bodyFont, fontSizeSp = 30f, textColorHex = "#E2E8F0",
                textAlign = if (centered) TextAlignment.CENTER else TextAlignment.START,
                writingDirection = dir
            )
        )

        return Slide(
            order = 0,
            background = SlideBackground.Gradient(template.primaryHex, template.secondaryHex, 135f),
            transition = SlideTransition(template.defaultTransition, 480),
            elements = els
        )
    }

    private fun buildMiddleSlideByArchetype(
        archetype: SlideArchetype,
        order: Int,
        sectionNum: Int,
        template: PresentationTemplate,
        language: AppLanguage,
        w: Float,
        h: Float,
        isRtl: Boolean,
        dir: WritingDirection,
        headingFont: String,
        bodyFont: String
    ): Slide {
        val cardFill = template.cardFillHex
        val cardTextColor = if (template.isDarkBackground) "#E2E8F0" else template.bodyColorHex
        val topAccent = SlideElement.ShapeElement(
            x = 0f, y = 0f, width = w, height = 14f, zIndex = 1,
            shapeType = ShapeType.RECTANGLE, fillColorHex = template.primaryHex,
            strokeColorHex = template.primaryHex, strokeWidth = 0f
        )

        return when (archetype) {
            SlideArchetype.THREE_PILLARS -> {
                val objTitle = when (language) {
                    AppLanguage.AR -> "الأهداف والمحاور ($sectionNum)"
                    AppLanguage.FA -> "اهداف و محورها ($sectionNum)"
                    AppLanguage.UR -> "اہم مقاصد اور ستون ($sectionNum)"
                    AppLanguage.RU -> "Ключевые цели ($sectionNum)"
                    AppLanguage.IT -> "Obiettivi Principali ($sectionNum)"
                    AppLanguage.ZH -> "核心目标与支柱 ($sectionNum)"
                    AppLanguage.EN -> "Key Objectives & Pillars ($sectionNum)"
                }
                val cardLabels = when (language) {
                    AppLanguage.AR -> listOf("01 المحور الأول\nعرض الفكرة الأساسية والتحليل", "02 المحور الثاني\nالمنهجية وتفصيل البيانات", "03 المحور الثالث\nالنتائج والتوصيات العملية")
                    AppLanguage.FA -> listOf("01 محور اول\nمعرفی ایده اصلی و تحلیل", "02 محور دوم\nروش‌شناسی و بررسی داده‌ها", "03 محور سوم\nنتایج و پیشنهادها")
                    AppLanguage.UR -> listOf("01 پہلا ستون\nبنیادی تصور اور تجزیہ", "02 دوسرا ستون\nطریقہ کار اور ڈیٹا", "03 تیسرا ستون\nاہم نتائج اور سفارشات")
                    AppLanguage.RU -> listOf("01 Этап 1\nОсновная идея и анализ", "02 Этап 2\nМетодология и данные", "03 Этап 3\nВыводы и план действий")
                    AppLanguage.IT -> listOf("01 Pilastro 1\nConcetto chiave e analisi", "02 Pilastro 2\nMetodologia e dati", "03 Pilastro 3\nRisultati e piano")
                    AppLanguage.ZH -> listOf("01 核心一\n主要概念与背景分析", "02 核心二\n方法论与结构化数据", "03 核心三\n核心结论与行动计划")
                    AppLanguage.EN -> listOf("01 Pillar One\nCore concept & background", "02 Pillar Two\nMethodology & structured data", "03 Pillar Three\nKey findings & roadmap")
                }
                val els = mutableListOf<SlideElement>(
                    topAccent,
                    SlideElement.TextElement(
                        x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 2,
                        text = objTitle, fontFamilyId = headingFont, fontSizeSp = 44f,
                        textColorHex = template.titleColorHex, isBold = true, writingDirection = dir
                    )
                )
                val cardW = w * 0.26f
                val gap = w * 0.04f
                val startX = w * 0.07f
                cardLabels.forEachIndexed { idx, txt ->
                    val cx = startX + idx * (cardW + gap)
                    els.add(
                        SlideElement.ShapeElement(
                            x = cx, y = h * 0.28f, width = cardW, height = h * 0.56f, zIndex = 3 + idx * 2,
                            shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                            strokeColorHex = template.accentHex, strokeWidth = 3f, hasShadow = true
                        )
                    )
                    els.add(
                        SlideElement.TextElement(
                            x = cx + 24f, y = h * 0.34f, width = cardW - 48f, height = h * 0.44f, zIndex = 4 + idx * 2,
                            text = txt, fontFamilyId = bodyFont, fontSizeSp = 28f,
                            textColorHex = cardTextColor, writingDirection = dir, textAlign = TextAlignment.CENTER
                        )
                    )
                }
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(template.defaultTransition, 450),
                    elements = els
                )
            }
            SlideArchetype.CHART_BAR, SlideArchetype.CHART_LINE -> {
                val chartType = if (archetype == SlideArchetype.CHART_BAR) ChartType.BAR else ChartType.LINE
                val chartTitle = when (language) {
                    AppLanguage.AR -> "المؤشرات والنتائج الإحصائية ($sectionNum)"
                    AppLanguage.FA -> "شاخص‌ها و نتایج آماری ($sectionNum)"
                    AppLanguage.UR -> "اعداد و شمار اور نتائج ($sectionNum)"
                    AppLanguage.RU -> "Показатели и динамика ($sectionNum)"
                    AppLanguage.IT -> "Dati e Indicatori ($sectionNum)"
                    AppLanguage.ZH -> "数据洞察与趋势 ($sectionNum)"
                    AppLanguage.EN -> "Data Insights & Metrics ($sectionNum)"
                }
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(SlideTransitionType.WIPE, 450),
                    elements = listOf(
                        topAccent,
                        SlideElement.TextElement(
                            x = w * 0.07f, y = h * 0.07f, width = w * 0.86f, height = h * 0.13f, zIndex = 2,
                            text = chartTitle, fontFamilyId = headingFont, fontSizeSp = 44f,
                            textColorHex = template.titleColorHex, isBold = true, writingDirection = dir
                        ),
                        SlideElement.ChartElement(
                            x = w * 0.08f, y = h * 0.24f, width = w * 0.84f, height = h * 0.66f, zIndex = 3,
                            chartType = chartType,
                            title = chartTitle,
                            labels = listOf("Q1", "Q2", "Q3", "Q4"),
                            series = listOf(
                                ChartSeries("A", template.primaryHex, listOf(42f, 60f, 75f, 89f)),
                                ChartSeries("B", template.accentHex, listOf(55f, 72f, 86f, 96f))
                            )
                        )
                    )
                )
            }
            SlideArchetype.COMPARISON_TABLE -> {
                val tblTitle = when (language) {
                    AppLanguage.AR -> "جدول المقارنة والخطة ($sectionNum)"
                    AppLanguage.FA -> "جدول مقایسه و برنامه ($sectionNum)"
                    AppLanguage.UR -> "موازنہ اور عملی جدول ($sectionNum)"
                    AppLanguage.RU -> "Таблица сравнения ($sectionNum)"
                    AppLanguage.IT -> "Matrice di Confronto ($sectionNum)"
                    AppLanguage.ZH -> "对比分析矩阵 ($sectionNum)"
                    AppLanguage.EN -> "Comparison & Action Matrix ($sectionNum)"
                }
                val headers = when (language) {
                    AppLanguage.AR -> listOf("المرحلة", "الوصف", "نسبة الإنجاز")
                    AppLanguage.FA -> listOf("مرحله", "توضیحات", "پیشرفت")
                    AppLanguage.UR -> listOf("مرحلہ", "تفصیل", "پیشرفت")
                    AppLanguage.RU -> listOf("Этап", "Задача", "Прогресс")
                    AppLanguage.IT -> listOf("Fase", "Attività", "Avanzamento")
                    AppLanguage.ZH -> listOf("阶段", "交付内容", "完成度")
                    AppLanguage.EN -> listOf("Phase", "Deliverable", "Progress")
                }
                val cells = listOf(
                    TableCell(0, 0, headers[0], template.primaryHex, "#FFFFFF", isHeader = true, isBold = true),
                    TableCell(0, 1, headers[1], template.primaryHex, "#FFFFFF", isHeader = true, isBold = true),
                    TableCell(0, 2, headers[2], template.primaryHex, "#FFFFFF", isHeader = true, isBold = true),
                    TableCell(1, 0, "01", "#F8FAFC", "#0F172A"),
                    TableCell(1, 1, "Analysis & Scope", "#F8FAFC", "#0F172A"),
                    TableCell(1, 2, "100%", "#F8FAFC", "#0F172A"),
                    TableCell(2, 0, "02", "#FFFFFF", "#0F172A"),
                    TableCell(2, 1, "Execution & Review", "#FFFFFF", "#0F172A"),
                    TableCell(2, 2, "85%", "#FFFFFF", "#0F172A")
                )
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(SlideTransitionType.SPLIT, 450),
                    elements = listOf(
                        topAccent,
                        SlideElement.TextElement(
                            x = w * 0.07f, y = h * 0.07f, width = w * 0.86f, height = h * 0.13f, zIndex = 2,
                            text = tblTitle, fontFamilyId = headingFont, fontSizeSp = 44f,
                            textColorHex = template.titleColorHex, isBold = true, writingDirection = dir
                        ),
                        SlideElement.TableElement(
                            x = w * 0.08f, y = h * 0.26f, width = w * 0.84f, height = h * 0.58f, zIndex = 3,
                            rows = 3, cols = 3, cells = cells
                        )
                    )
                )
            }
            SlideArchetype.TWO_COLUMN -> {
                val heading = when (language) {
                    AppLanguage.AR -> "مقارنة وتحليل ثنائي ($sectionNum)"
                    AppLanguage.FA -> "تحلیل و مقایسه دوگانه ($sectionNum)"
                    AppLanguage.UR -> listOf("دو کالم تجزیہ ($sectionNum)").first()
                    AppLanguage.RU -> "Двухколоночный анализ ($sectionNum)"
                    AppLanguage.IT -> "Analisi Comparata ($sectionNum)"
                    AppLanguage.ZH -> "双栏对比分析 ($sectionNum)"
                    AppLanguage.EN -> "Dual Perspective Analysis ($sectionNum)"
                }
                val colTextA = when (language) {
                    AppLanguage.AR -> "نقاط القوة والفرص\n• وضوح الرؤية الاستراتيجية\n• كفاءة التنفيذ والقياس"
                    AppLanguage.FA -> "نقاط قوت و فرصت‌ها\n• وضوح چشم‌انداز\n• بهره‌وری اجرایی"
                    else -> "Strengths & Scope\n• Clear strategic vision\n• Measurable execution milestones"
                }
                val colTextB = when (language) {
                    AppLanguage.AR -> "التحديات والحلول\n• معالجة المخاطر مبكراً\n• تطوير الأداء المستمر"
                    AppLanguage.FA -> "چالش‌ها و راهکارها\n• مدیریت ریسک\n• بهبود مستمر"
                    else -> "Challenges & Actions\n• Early risk mitigation\n• Iterative optimization"
                }
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(SlideTransitionType.PUSH, 450),
                    elements = listOf(
                        topAccent,
                        SlideElement.TextElement(
                            x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 2,
                            text = heading, fontFamilyId = headingFont, fontSizeSp = 44f,
                            textColorHex = template.titleColorHex, isBold = true, writingDirection = dir
                        ),
                        SlideElement.ShapeElement(
                            x = w * 0.07f, y = h * 0.26f, width = w * 0.41f, height = h * 0.62f, zIndex = 3,
                            shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                            strokeColorHex = template.primaryHex, strokeWidth = 3f, hasShadow = true
                        ),
                        SlideElement.TextElement(
                            x = w * 0.09f, y = h * 0.30f, width = w * 0.37f, height = h * 0.54f, zIndex = 4,
                            text = colTextA, fontFamilyId = bodyFont, fontSizeSp = 28f,
                            textColorHex = cardTextColor, writingDirection = dir
                        ),
                        SlideElement.ShapeElement(
                            x = w * 0.52f, y = h * 0.26f, width = w * 0.41f, height = h * 0.62f, zIndex = 5,
                            shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                            strokeColorHex = template.accentHex, strokeWidth = 3f, hasShadow = true
                        ),
                        SlideElement.TextElement(
                            x = w * 0.54f, y = h * 0.30f, width = w * 0.37f, height = h * 0.54f, zIndex = 6,
                            text = colTextB, fontFamilyId = bodyFont, fontSizeSp = 28f,
                            textColorHex = cardTextColor, writingDirection = dir
                        )
                    )
                )
            }
            SlideArchetype.KPI_METRICS -> {
                val heading = when (language) {
                    AppLanguage.AR -> "مؤشرات الأداء الرئيسية ($sectionNum)"
                    AppLanguage.FA -> "شاخص‌های کلیدی عملکرد ($sectionNum)"
                    else -> "Key Performance Indicators ($sectionNum)"
                }
                val metrics = listOf("94%", "3.8x", "+48K")
                val descs = when (language) {
                    AppLanguage.AR -> listOf("نسبة الدقة والإنجاز", "معدل النمو السنوي", "المستفيدون والمخرجات")
                    AppLanguage.FA -> listOf("نرخ دقت و پیشرفت", "رشد سالانه", "مخاطبان و دستاوردها")
                    else -> listOf("Completion Rate", "Annual Growth Factor", "Verified Reach")
                }
                val els = mutableListOf<SlideElement>(
                    topAccent,
                    SlideElement.TextElement(
                        x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 2,
                        text = heading, fontFamilyId = headingFont, fontSizeSp = 44f,
                        textColorHex = template.titleColorHex, isBold = true, writingDirection = dir
                    )
                )
                val boxW = w * 0.26f
                val gap = w * 0.04f
                val startX = w * 0.07f
                metrics.forEachIndexed { idx, num ->
                    val cx = startX + idx * (boxW + gap)
                    els.add(
                        SlideElement.ShapeElement(
                            x = cx, y = h * 0.30f, width = boxW, height = h * 0.50f, zIndex = 3 + idx * 3,
                            shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                            strokeColorHex = template.primaryHex, strokeWidth = 3f, hasShadow = true
                        )
                    )
                    els.add(
                        SlideElement.TextElement(
                            x = cx + 16f, y = h * 0.36f, width = boxW - 32f, height = h * 0.18f, zIndex = 4 + idx * 3,
                            text = num, fontFamilyId = headingFont, fontSizeSp = 60f,
                            textColorHex = template.accentHex, isBold = true, textAlign = TextAlignment.CENTER
                        )
                    )
                    els.add(
                        SlideElement.TextElement(
                            x = cx + 20f, y = h * 0.58f, width = boxW - 40f, height = h * 0.16f, zIndex = 5 + idx * 3,
                            text = descs[idx], fontFamilyId = bodyFont, fontSizeSp = 26f,
                            textColorHex = cardTextColor, textAlign = TextAlignment.CENTER, writingDirection = dir
                        )
                    )
                }
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(SlideTransitionType.ZOOM, 450),
                    elements = els
                )
            }
            SlideArchetype.SECTION_DIVIDER -> {
                val sectionTitle = when (language) {
                    AppLanguage.AR -> "القسم $sectionNum • محور رئيسي"
                    AppLanguage.FA -> "بخش $sectionNum • فصل جدید"
                    AppLanguage.UR -> "سیکشن $sectionNum • مرکزی باب"
                    AppLanguage.RU -> "Раздел $sectionNum"
                    AppLanguage.IT -> "Sezione $sectionNum"
                    AppLanguage.ZH -> "第 $sectionNum 部分"
                    AppLanguage.EN -> "Section $sectionNum • Core Chapter"
                }
                Slide(
                    order = order,
                    background = SlideBackground.Gradient(template.primaryHex, template.secondaryHex, 135f),
                    transition = SlideTransition(SlideTransitionType.SLIDE_UP, 450),
                    elements = listOf(
                        SlideElement.ShapeElement(
                            x = w * 0.40f, y = h * 0.30f, width = w * 0.20f, height = 10f, zIndex = 1,
                            shapeType = ShapeType.RECTANGLE, fillColorHex = template.accentHex,
                            strokeColorHex = template.accentHex, strokeWidth = 0f
                        ),
                        SlideElement.TextElement(
                            x = w * 0.10f, y = h * 0.38f, width = w * 0.80f, height = h * 0.24f, zIndex = 2,
                            text = sectionTitle, fontFamilyId = headingFont, fontSizeSp = 56f,
                            textColorHex = "#FFFFFF", isBold = true, textAlign = TextAlignment.CENTER, writingDirection = dir
                        )
                    )
                )
            }
            SlideArchetype.TIMELINE_ROADMAP -> {
                val heading = when (language) {
                    AppLanguage.AR -> "خارطة الطريق والمراحل الزمنية ($sectionNum)"
                    AppLanguage.FA -> "نقشه راه و مراحل اجرایی ($sectionNum)"
                    else -> "Roadmap & Execution Timeline ($sectionNum)"
                }
                val steps = when (language) {
                    AppLanguage.AR -> listOf("المرحلة 1\nالتخطيط والبحث", "المرحلة 2\nالبناء والتطبيق", "المرحلة 3\nالمراجعة والإطلاق")
                    AppLanguage.FA -> listOf("مرحله ۱\nبرنامه‌ریزی و پژوهش", "مرحله ۲\nطراحی و اجرا", "مرحله ۳\nارزیابی و انتشار")
                    else -> listOf("Step 1\nDiscovery & Plan", "Step 2\nBuild & Validate", "Step 3\nLaunch & Scale")
                }
                val els = mutableListOf<SlideElement>(
                    topAccent,
                    SlideElement.TextElement(
                        x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 2,
                        text = heading, fontFamilyId = headingFont, fontSizeSp = 44f,
                        textColorHex = template.titleColorHex, isBold = true, writingDirection = dir
                    )
                )
                val stepW = w * 0.24f
                val arrowW = w * 0.05f
                val baseX = w * 0.08f
                steps.forEachIndexed { idx, txt ->
                    val x = baseX + idx * (stepW + arrowW + 16f)
                    els.add(
                        SlideElement.ShapeElement(
                            x = x, y = h * 0.36f, width = stepW, height = h * 0.38f, zIndex = 3 + idx * 3,
                            shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                            strokeColorHex = template.primaryHex, strokeWidth = 3f, hasShadow = true
                        )
                    )
                    els.add(
                        SlideElement.TextElement(
                            x = x + 16f, y = h * 0.42f, width = stepW - 32f, height = h * 0.26f, zIndex = 4 + idx * 3,
                            text = txt, fontFamilyId = bodyFont, fontSizeSp = 28f,
                            textColorHex = cardTextColor, textAlign = TextAlignment.CENTER, writingDirection = dir
                        )
                    )
                    if (idx < 2) {
                        els.add(
                            SlideElement.ShapeElement(
                                x = x + stepW + 8f, y = h * 0.50f, width = arrowW, height = 56f, zIndex = 5 + idx * 3,
                                shapeType = if (isRtl) ShapeType.ARROW_LEFT else ShapeType.ARROW_RIGHT,
                                fillColorHex = template.accentHex, strokeColorHex = template.accentHex, strokeWidth = 0f
                            )
                        )
                    }
                }
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(SlideTransitionType.PUSH, 450),
                    elements = els
                )
            }
            SlideArchetype.QUOTE_SPOTLIGHT -> {
                val quote = when (language) {
                    AppLanguage.AR -> "«التصميم الدقيق والمنظم يحوّل البيانات المعقدة إلى رسالة واضحة ومؤثرة.»"
                    AppLanguage.FA -> "«طراحی دقیق و منظم، داده‌های پیچیده را به پیامی روشن و اثرگذار تبدیل می‌کند.»"
                    else -> "“Thoughtful structure turns complex ideas into a clear, memorable narrative.”"
                }
                val caption = when (language) {
                    AppLanguage.AR -> "ملاحظة محورية — الشريحة ${order + 1}"
                    AppLanguage.FA -> "نکته محوری — اسلاید ${order + 1}"
                    else -> "Key Insight — Slide ${order + 1}"
                }
                Slide(
                    order = order,
                    background = SlideBackground.Solid(template.bgHex),
                    transition = SlideTransition(SlideTransitionType.DISSOLVE, 450),
                    elements = listOf(
                        topAccent,
                        SlideElement.IconElement(
                            x = w * 0.44f, y = h * 0.14f, width = 140f, height = 140f, zIndex = 2,
                            symbolKey = "lightbulb", tintColorHex = template.accentHex, bgColorHex = "#1A3B82F6"
                        ),
                        SlideElement.TextElement(
                            x = w * 0.12f, y = h * 0.34f, width = w * 0.76f, height = h * 0.32f, zIndex = 3,
                            text = quote, fontFamilyId = headingFont, fontSizeSp = 40f,
                            textColorHex = template.titleColorHex, isBold = true, isBlockQuote = true,
                            textAlign = TextAlignment.CENTER, writingDirection = dir
                        ),
                        SlideElement.TextElement(
                            x = w * 0.20f, y = h * 0.72f, width = w * 0.60f, height = h * 0.12f, zIndex = 4,
                            text = caption, fontFamilyId = bodyFont, fontSizeSp = 24f,
                            textColorHex = template.accentHex, textAlign = TextAlignment.CENTER, writingDirection = dir
                        )
                    )
                )
            }
        }
    }

    private fun buildConclusionSlide(
        order: Int,
        template: PresentationTemplate,
        language: AppLanguage,
        w: Float,
        h: Float,
        dir: WritingDirection,
        headingFont: String,
        bodyFont: String
    ): Slide {
        val heading = when (language) {
            AppLanguage.AR -> "الخلاصة والمراجع — شكراً لحسن استماعكم"
            AppLanguage.FA -> "جمع‌بندی و منابع — سپاس از توجه شما"
            AppLanguage.UR -> "خلاصہ اور حوالہ جات — شکریہ"
            AppLanguage.RU -> "Заключение и спасибо за внимание"
            AppLanguage.IT -> "Conclusioni e Grazie"
            AppLanguage.ZH -> "总结与致谢"
            AppLanguage.EN -> "Conclusion & Thank You"
        }
        val body = when (language) {
            AppLanguage.AR -> "نقطة رئيسية أولى تلخص الفكرة\nنتائج مدعومة بالبيانات والتحليل\nالمرجع الأكاديمي والتوصيات المستقبلية"
            AppLanguage.FA -> "نکته کلیدی اول در خلاصه بحث\nنتایج مبتنی بر داده‌ها و تحلیل\nمنابع علمی و پیشنهادهای آینده"
            AppLanguage.UR -> "اہم نکتہ جو مرکزی خیال بیان کرے\nاعداد و شمار پر مبنی نتائج\nحوالہ جات اور آئندہ اقدامات"
            AppLanguage.RU -> "Ключевой вывод исследования\nПодтверждение данными и анализом\nИсточники и дальнейшие шаги"
            AppLanguage.IT -> "Sintesi del concetto principale\nRisultati supportati dai dati\nRiferimenti e prossimi passi"
            AppLanguage.ZH -> "核心观点总结\n数据支持与深度分析\n参考文献与后续计划"
            AppLanguage.EN -> "Primary takeaway summarizing key insight\nEvidence-backed analytical conclusion\nAcademic references & next steps"
        }
        return Slide(
            order = order,
            background = SlideBackground.Gradient(template.primaryHex, template.secondaryHex, 135f),
            transition = SlideTransition(SlideTransitionType.ZOOM, 450),
            elements = listOf(
                SlideElement.TextElement(
                    x = w * 0.08f, y = h * 0.16f, width = w * 0.84f, height = h * 0.18f, zIndex = 1,
                    text = heading, fontFamilyId = headingFont, fontSizeSp = 48f,
                    textColorHex = "#FFFFFF", isBold = true, textAlign = TextAlignment.CENTER,
                    writingDirection = dir
                ),
                SlideElement.TextElement(
                    x = w * 0.08f, y = h * 0.40f, width = w * 0.84f, height = h * 0.42f, zIndex = 2,
                    text = body, fontFamilyId = bodyFont, fontSizeSp = 30f,
                    textColorHex = "#E2E8F0", listType = TextListType.BULLET, writingDirection = dir
                )
            )
        )
    }

    private fun defaultSubtitle(language: AppLanguage): String = when (language) {
        AppLanguage.AR -> "عرض احترافي مُعد بواسطة KATEB Slides"
        AppLanguage.FA -> "ارائه حرفه‌ای ساخته شده با KATEB Slides"
        AppLanguage.UR -> "KATEB Slides کے ساتھ تیار کردہ پیشہ ورانہ پریزنٹیشن"
        AppLanguage.RU -> "Профессиональная презентация в KATEB Slides"
        AppLanguage.IT -> "Presentazione professionale creata con KATEB Slides"
        AppLanguage.ZH -> "使用 KATEB Slides 创建的专业演示文稿"
        AppLanguage.EN -> "Professional presentation created with KATEB Slides"
    }

    fun applyThemeColorsToPresentation(
        presentation: Presentation,
        template: PresentationTemplate
    ): Presentation {
        val headingFont = template.resolveHeadingFont(presentation.language)
        val bodyFont = template.resolveBodyFont(presentation.language)
        val availableIds = SlideFontRegistry.options.map { it.id }.toSet()
        val safeHeading = if (headingFont in availableIds) headingFont else defaultFontFor(presentation.language)
        val safeBody = if (bodyFont in availableIds) bodyFont else defaultFontFor(presentation.language)

        val updatedSlides = presentation.slides.mapIndexed { idx, slide ->
            val bg = if (idx == 0) {
                SlideBackground.Gradient(template.primaryHex, template.secondaryHex, 135f)
            } else {
                SlideBackground.Solid(template.bgHex)
            }
            val updatedElements = slide.elements.map { el ->
                when (el) {
                    is SlideElement.TextElement -> {
                        val isTitle = el.isBold && el.fontSizeSp >= 38f
                        el.copy(
                            fontFamilyId = if (isTitle) safeHeading else safeBody,
                            textColorHex = when {
                                idx == 0 -> "#FFFFFF"
                                isTitle -> template.titleColorHex
                                else -> template.bodyColorHex
                            }
                        )
                    }
                    is SlideElement.ShapeElement -> el.copy(
                        fillColorHex = if (el.fillColorHex == "#FFFFFF" || el.fillColorHex == "#1E293B") {
                            template.cardFillHex
                        } else {
                            template.primaryHex
                        },
                        strokeColorHex = template.accentHex
                    )
                    is SlideElement.IconElement -> el.copy(
                        tintColorHex = template.accentHex
                    )
                    else -> el
                }
            }
            slide.copy(
                background = bg,
                transition = SlideTransition(template.defaultTransition, 450),
                elements = updatedElements
            )
        }
        return presentation.copy(themeId = template.id, slides = updatedSlides, updatedAt = System.currentTimeMillis())
    }

    fun buildSlideFromLayoutPreset(
        preset: SlideLayoutPreset,
        order: Int,
        presentation: Presentation
    ): Slide {
        val tpl = byId(presentation.themeId)
        val w = presentation.canvasWidth
        val h = presentation.canvasHeight
        val lang = presentation.language
        val isRtl = lang.isRtl
        val dir = if (isRtl) WritingDirection.RTL else WritingDirection.LTR
        val headingFont = tpl.resolveHeadingFont(lang)
        val bodyFont = tpl.resolveBodyFont(lang)
        val cardFill = tpl.cardFillHex
        val cardTextColor = if (tpl.isDarkBackground) "#E2E8F0" else "#1E293B"
        val titleSample = when (lang) {
            AppLanguage.AR -> "عنوان الشريحة"
            AppLanguage.FA -> "عنوان اسلاید"
            AppLanguage.UR -> "سلائیڈ کا عنوان"
            AppLanguage.RU -> "Заголовок слайда"
            AppLanguage.IT -> "Titolo Diapositiva"
            AppLanguage.ZH -> "幻灯片标题"
            AppLanguage.EN -> "Slide Title"
        }
        val bodySample = when (lang) {
            AppLanguage.AR -> "انقر مرتين لتحرير النص والمحتوى"
            AppLanguage.FA -> "برای ویرایش متن دوبار ضربه بزنید"
            AppLanguage.UR -> "متن میں ترمیم کے لیے دو بار ٹیپ کریں"
            AppLanguage.RU -> "Нажмите дважды для редактирования"
            AppLanguage.IT -> "Tocca due volte per modificare"
            AppLanguage.ZH -> "双击此处编辑内容"
            AppLanguage.EN -> "Double tap to edit content"
        }

        return when (preset) {
            SlideLayoutPreset.BLANK -> Slide(
                order = order,
                background = SlideBackground.Solid(tpl.bgHex)
            )
            SlideLayoutPreset.TITLE_COVER -> Slide(
                order = order,
                background = SlideBackground.Gradient(tpl.primaryHex, tpl.secondaryHex, 135f),
                elements = listOf(
                    SlideElement.TextElement(
                        x = w * 0.1f, y = h * 0.3f, width = w * 0.8f, height = h * 0.22f, zIndex = 1,
                        text = titleSample, fontFamilyId = headingFont, fontSizeSp = 60f, textColorHex = "#FFFFFF",
                        isBold = true, textAlign = TextAlignment.CENTER, writingDirection = dir
                    ),
                    SlideElement.TextElement(
                        x = w * 0.15f, y = h * 0.56f, width = w * 0.7f, height = h * 0.15f, zIndex = 2,
                        text = bodySample, fontFamilyId = bodyFont, fontSizeSp = 28f, textColorHex = "#E2E8F0",
                        textAlign = TextAlignment.CENTER, writingDirection = dir
                    )
                )
            )
            SlideLayoutPreset.TITLE_CONTENT -> Slide(
                order = order,
                background = SlideBackground.Solid(tpl.bgHex),
                elements = listOf(
                    SlideElement.ShapeElement(
                        x = 0f, y = 0f, width = w, height = 16f, zIndex = 1,
                        shapeType = ShapeType.RECTANGLE, fillColorHex = tpl.primaryHex,
                        strokeColorHex = tpl.primaryHex, strokeWidth = 0f
                    ),
                    SlideElement.TextElement(
                        x = w * 0.08f, y = h * 0.09f, width = w * 0.84f, height = h * 0.15f, zIndex = 2,
                        text = titleSample, fontFamilyId = headingFont, fontSizeSp = 46f, textColorHex = tpl.titleColorHex,
                        isBold = true, writingDirection = dir
                    ),
                    SlideElement.TextElement(
                        x = w * 0.08f, y = h * 0.28f, width = w * 0.84f, height = h * 0.6f, zIndex = 3,
                        text = "$bodySample\n$bodySample", fontFamilyId = bodyFont, fontSizeSp = 30f,
                        textColorHex = tpl.bodyColorHex, listType = TextListType.BULLET, writingDirection = dir
                    )
                )
            )
            SlideLayoutPreset.TWO_COLUMN -> Slide(
                order = order,
                background = SlideBackground.Solid(tpl.bgHex),
                elements = listOf(
                    SlideElement.TextElement(
                        x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 1,
                        text = titleSample, fontFamilyId = headingFont, fontSizeSp = 44f, textColorHex = tpl.titleColorHex,
                        isBold = true, writingDirection = dir
                    ),
                    SlideElement.ShapeElement(
                        x = w * 0.07f, y = h * 0.26f, width = w * 0.41f, height = h * 0.62f, zIndex = 2,
                        shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                        strokeColorHex = tpl.primaryHex, strokeWidth = 3f, hasShadow = true
                    ),
                    SlideElement.TextElement(
                        x = w * 0.09f, y = h * 0.30f, width = w * 0.37f, height = h * 0.54f, zIndex = 3,
                        text = bodySample, fontFamilyId = bodyFont, fontSizeSp = 28f,
                        textColorHex = cardTextColor, writingDirection = dir
                    ),
                    SlideElement.ShapeElement(
                        x = w * 0.52f, y = h * 0.26f, width = w * 0.41f, height = h * 0.62f, zIndex = 4,
                        shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                        strokeColorHex = tpl.accentHex, strokeWidth = 3f, hasShadow = true
                    ),
                    SlideElement.TextElement(
                        x = w * 0.54f, y = h * 0.30f, width = w * 0.37f, height = h * 0.54f, zIndex = 5,
                        text = bodySample, fontFamilyId = bodyFont, fontSizeSp = 28f,
                        textColorHex = cardTextColor, writingDirection = dir
                    )
                )
            )
            SlideLayoutPreset.THREE_CARDS -> {
                val cardW = w * 0.26f
                val gap = w * 0.04f
                val startX = w * 0.07f
                val items = mutableListOf<SlideElement>(
                    SlideElement.TextElement(
                        x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 1,
                        text = titleSample, fontFamilyId = headingFont, fontSizeSp = 44f,
                        textColorHex = tpl.titleColorHex, isBold = true, writingDirection = dir
                    )
                )
                repeat(3) { idx ->
                    val cx = startX + idx * (cardW + gap)
                    items.add(
                        SlideElement.ShapeElement(
                            x = cx, y = h * 0.28f, width = cardW, height = h * 0.56f, zIndex = 2 + idx * 2,
                            shapeType = ShapeType.ROUNDED_RECT, fillColorHex = cardFill,
                            strokeColorHex = tpl.accentHex, strokeWidth = 3f, hasShadow = true
                        )
                    )
                    items.add(
                        SlideElement.TextElement(
                            x = cx + 20f, y = h * 0.34f, width = cardW - 40f, height = h * 0.44f, zIndex = 3 + idx * 2,
                            text = "0${idx + 1}\n$bodySample", fontFamilyId = bodyFont, fontSizeSp = 26f,
                            textColorHex = cardTextColor, textAlign = TextAlignment.CENTER, writingDirection = dir
                        )
                    )
                }
                Slide(order = order, background = SlideBackground.Solid(tpl.bgHex), elements = items)
            }
            SlideLayoutPreset.SECTION_HEADER -> Slide(
                order = order,
                background = SlideBackground.Gradient(tpl.primaryHex, tpl.secondaryHex, 135f),
                elements = listOf(
                    SlideElement.ShapeElement(
                        x = w * 0.4f, y = h * 0.32f, width = w * 0.2f, height = 10f, zIndex = 1,
                        shapeType = ShapeType.RECTANGLE, fillColorHex = tpl.accentHex,
                        strokeColorHex = tpl.accentHex, strokeWidth = 0f
                    ),
                    SlideElement.TextElement(
                        x = w * 0.1f, y = h * 0.38f, width = w * 0.8f, height = h * 0.24f, zIndex = 2,
                        text = titleSample, fontFamilyId = headingFont, fontSizeSp = 56f,
                        textColorHex = "#FFFFFF", isBold = true, textAlign = TextAlignment.CENTER, writingDirection = dir
                    )
                )
            )
            SlideLayoutPreset.KPI_METRICS -> buildMiddleSlideByArchetype(
                SlideArchetype.KPI_METRICS, order, order + 1, tpl, lang, w, h, isRtl, dir, headingFont, bodyFont
            )
            SlideLayoutPreset.TIMELINE_STEPS -> buildMiddleSlideByArchetype(
                SlideArchetype.TIMELINE_ROADMAP, order, order + 1, tpl, lang, w, h, isRtl, dir, headingFont, bodyFont
            )
            SlideLayoutPreset.CHART_INSIGHT -> buildMiddleSlideByArchetype(
                SlideArchetype.CHART_BAR, order, order + 1, tpl, lang, w, h, isRtl, dir, headingFont, bodyFont
            )
            SlideLayoutPreset.QUOTE_SPOTLIGHT -> buildMiddleSlideByArchetype(
                SlideArchetype.QUOTE_SPOTLIGHT, order, order + 1, tpl, lang, w, h, isRtl, dir, headingFont, bodyFont
            )
        }
    }
}

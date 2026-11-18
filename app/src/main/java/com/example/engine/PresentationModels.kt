package com.example.engine

import com.example.core.localization.AppLanguage
import java.util.UUID

const val MAX_SLIDES_PER_DECK = 100

enum class SlideViewMode {
    DESIGN,
    OUTLINE,
    SLIDE_SORTER
}

enum class SlideAspectRatio(
    val id: String,
    val label: String,
    val defaultWidth: Float,
    val defaultHeight: Float
) {
    RATIO_16_9("16:9", "16:9 Widescreen", 1920f, 1080f),
    RATIO_4_3("4:3", "4:3 Standard", 1440f, 1080f),
    A4("A4", "A4 Educational", 1240f, 1754f),
    SQUARE_1_1("1:1", "1:1 Square", 1200f, 1200f),
    PORTRAIT_9_16("9:16", "9:16 Vertical", 1080f, 1920f),
    BANNER_21_9("21:9", "21:9 Cinema", 2100f, 900f),
    CUSTOM("custom", "Custom", 1920f, 1080f);

    fun label(lang: AppLanguage): String = when (this) {
        RATIO_16_9 -> when (lang) {
            AppLanguage.AR -> "16:9 شاشة عريضة"
            AppLanguage.FA -> "16:9 عریض"
            AppLanguage.RU -> "16:9 Широкий"
            AppLanguage.IT -> "16:9 Panoramico"
            AppLanguage.UR -> "16:9 وائیڈ اسکرین"
            AppLanguage.ZH -> "16:9 宽屏"
            AppLanguage.EN -> label
        }
        RATIO_4_3 -> when (lang) {
            AppLanguage.AR -> "4:3 قياسي"
            AppLanguage.FA -> "4:3 استاندارد"
            AppLanguage.RU -> "4:3 Стандарт"
            AppLanguage.IT -> "4:3 Standard"
            AppLanguage.UR -> "4:3 معیاری"
            AppLanguage.ZH -> "4:3 标准"
            AppLanguage.EN -> label
        }
        A4 -> when (lang) {
            AppLanguage.AR -> "A4 تعليمي"
            AppLanguage.FA -> "A4 آموزشی"
            AppLanguage.RU -> "A4 Учебный"
            AppLanguage.IT -> "A4 Educativo"
            AppLanguage.UR -> "A4 تعلیمی"
            AppLanguage.ZH -> "A4 教育"
            AppLanguage.EN -> label
        }
        SQUARE_1_1 -> when (lang) {
            AppLanguage.AR -> "1:1 مربع"
            AppLanguage.FA -> "1:1 مربعی"
            AppLanguage.RU -> "1:1 Квадрат"
            AppLanguage.IT -> "1:1 Quadrato"
            AppLanguage.UR -> "1:1 مربع"
            AppLanguage.ZH -> "1:1 方形"
            AppLanguage.EN -> label
        }
        PORTRAIT_9_16 -> when (lang) {
            AppLanguage.AR -> "9:16 عمودي للهاتف"
            AppLanguage.FA -> "9:16 عمودی"
            AppLanguage.RU -> "9:16 Вертикальный"
            AppLanguage.IT -> "9:16 Verticale"
            AppLanguage.UR -> "9:16 عمودی"
            AppLanguage.ZH -> "9:16 竖屏"
            AppLanguage.EN -> label
        }
        BANNER_21_9 -> when (lang) {
            AppLanguage.AR -> "21:9 سينمائي"
            AppLanguage.FA -> "21:9 سینمایی"
            AppLanguage.RU -> "21:9 Кино"
            AppLanguage.IT -> "21:9 Cinema"
            AppLanguage.UR -> "21:9 سینما"
            AppLanguage.ZH -> "21:9 影院"
            AppLanguage.EN -> label
        }
        CUSTOM -> when (lang) {
            AppLanguage.AR -> "مخصص"
            AppLanguage.FA -> "سفارشی"
            AppLanguage.RU -> "Свой размер"
            AppLanguage.IT -> "Personalizzato"
            AppLanguage.UR -> "حسب ضرورت"
            AppLanguage.ZH -> "自定义"
            AppLanguage.EN -> label
        }
    }

    companion object {
        fun fromId(id: String?): SlideAspectRatio =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: RATIO_16_9
    }
}

enum class PresentationSourceType {
    BLANK, TEMPLATE, AI, IMPORTED_PPTX, IMPORTED_DOCX, IMPORTED_PDF, IMPORTED_TXT, IMPORTED_IMAGES
}

enum class SlideTransitionType(val displayName: String) {
    NONE("None"),
    FADE("Fade"),
    PUSH("Push"),
    WIPE("Wipe"),
    SPLIT("Split"),
    REVEAL("Reveal"),
    ZOOM("Zoom"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    FLIP("Flip 3D"),
    DISSOLVE("Dissolve"),
    STEALTH("Stealth (Subtle)"),
    EXCITING("Exciting Showcase"),
    DYNAMIC("Dynamic Flow"),
    SCIENTIFIC("Scientific Grid"),
    ACADEMIC("Academic Serif"),
    CURTAIN("Curtain Drape"),
    MORPH("Smart Morph");

    fun label(lang: AppLanguage): String = when (this) {
        NONE -> when (lang) {
            AppLanguage.AR -> "بدون انتقال"
            AppLanguage.FA -> "بدون گذار"
            AppLanguage.RU -> "Нет"
            AppLanguage.IT -> "Nessuna"
            AppLanguage.UR -> "کوئی نہیں"
            AppLanguage.ZH -> "无"
            AppLanguage.EN -> displayName
        }
        FADE -> when (lang) {
            AppLanguage.AR -> "تلاشي ناعم"
            AppLanguage.FA -> "محو"
            AppLanguage.RU -> "Затухание"
            AppLanguage.IT -> "Dissolvenza"
            AppLanguage.UR -> "دھندلا"
            AppLanguage.ZH -> "淡入"
            AppLanguage.EN -> displayName
        }
        PUSH -> when (lang) {
            AppLanguage.AR -> "دفع جانبي"
            AppLanguage.FA -> "هل دادن"
            AppLanguage.RU -> "Сдвиг"
            AppLanguage.IT -> "Scorrimento"
            AppLanguage.UR -> "دھکیلنا"
            AppLanguage.ZH -> "推入"
            AppLanguage.EN -> displayName
        }
        WIPE -> when (lang) {
            AppLanguage.AR -> "مسح"
            AppLanguage.FA -> "پاک کردن"
            AppLanguage.RU -> "Вытеснение"
            AppLanguage.IT -> "Comparsa"
            AppLanguage.UR -> "وائپ"
            AppLanguage.ZH -> "擦除"
            AppLanguage.EN -> displayName
        }
        SPLIT -> when (lang) {
            AppLanguage.AR -> "انقسام"
            AppLanguage.FA -> "شکاف"
            AppLanguage.RU -> "Разделение"
            AppLanguage.IT -> "Dividi"
            AppLanguage.UR -> "تقسیم"
            AppLanguage.ZH -> "拆分"
            AppLanguage.EN -> displayName
        }
        REVEAL -> when (lang) {
            AppLanguage.AR -> "كشف"
            AppLanguage.FA -> "آشکارسازی"
            AppLanguage.RU -> "Появление"
            AppLanguage.IT -> "Rivela"
            AppLanguage.UR -> "ظاہر کرنا"
            AppLanguage.ZH -> "揭开"
            AppLanguage.EN -> displayName
        }
        ZOOM -> when (lang) {
            AppLanguage.AR -> "تكبير"
            AppLanguage.FA -> "بزرگنمایی"
            AppLanguage.RU -> "Масштаб"
            AppLanguage.IT -> "Zoom"
            AppLanguage.UR -> "زوم"
            AppLanguage.ZH -> "缩放"
            AppLanguage.EN -> displayName
        }
        SLIDE_UP -> when (lang) {
            AppLanguage.AR -> "انزلاق للأعلى"
            AppLanguage.FA -> "لغزش به بالا"
            AppLanguage.RU -> "Вверх"
            AppLanguage.IT -> "Scorri su"
            AppLanguage.UR -> "اوپر سلائیڈ"
            AppLanguage.ZH -> "向上滑动"
            AppLanguage.EN -> displayName
        }
        SLIDE_DOWN -> when (lang) {
            AppLanguage.AR -> "انزلاق للأسفل"
            AppLanguage.FA -> "لغزش به پایین"
            AppLanguage.RU -> "Вниз"
            AppLanguage.IT -> "Scorri giù"
            AppLanguage.UR -> "نیچے سلائیڈ"
            AppLanguage.ZH -> "向下滑动"
            AppLanguage.EN -> displayName
        }
        FLIP -> when (lang) {
            AppLanguage.AR -> "انقلاب ثلاثي"
            AppLanguage.FA -> "چرخش سه‌بعدی"
            AppLanguage.RU -> "Переворот 3D"
            AppLanguage.IT -> "Capovolgi 3D"
            AppLanguage.UR -> "تھری ڈی پلٹیں"
            AppLanguage.ZH -> "3D 翻转"
            AppLanguage.EN -> displayName
        }
        DISSOLVE -> when (lang) {
            AppLanguage.AR -> "ذوبان سينمائي"
            AppLanguage.FA -> "انحلال"
            AppLanguage.RU -> "Растворение"
            AppLanguage.IT -> "Dissolvi"
            AppLanguage.UR -> "تحلیل"
            AppLanguage.ZH -> "叠化"
            AppLanguage.EN -> displayName
        }
        STEALTH -> when (lang) {
            AppLanguage.AR -> "الوضع الخفي"
            AppLanguage.FA -> "حالت پنهان"
            AppLanguage.RU -> "Скрытый"
            AppLanguage.IT -> "Discreto"
            AppLanguage.UR -> "خفیہ موڈ"
            AppLanguage.ZH -> "隐匿过渡"
            AppLanguage.EN -> displayName
        }
        EXCITING -> when (lang) {
            AppLanguage.AR -> "الوضع المثير"
            AppLanguage.FA -> "حالت هیجان‌انگیز"
            AppLanguage.RU -> "Яркий"
            AppLanguage.IT -> "Dinamico Show"
            AppLanguage.UR -> "پرجوش موڈ"
            AppLanguage.ZH -> "华丽转场"
            AppLanguage.EN -> displayName
        }
        DYNAMIC -> when (lang) {
            AppLanguage.AR -> "الوضع الديناميكي"
            AppLanguage.FA -> "حالت پویا"
            AppLanguage.RU -> "Динамика"
            AppLanguage.IT -> "Dinamico"
            AppLanguage.UR -> "متحرک موڈ"
            AppLanguage.ZH -> "动态转场"
            AppLanguage.EN -> displayName
        }
        SCIENTIFIC -> when (lang) {
            AppLanguage.AR -> "الوضع العلمي"
            AppLanguage.FA -> "حالت علمی"
            AppLanguage.RU -> "Научный"
            AppLanguage.IT -> "Scientifico"
            AppLanguage.UR -> "سائنسی موڈ"
            AppLanguage.ZH -> "科学严谨"
            AppLanguage.EN -> displayName
        }
        ACADEMIC -> when (lang) {
            AppLanguage.AR -> "الوضع الأكاديمي"
            AppLanguage.FA -> "حالت دانشگاهی"
            AppLanguage.RU -> "Академический"
            AppLanguage.IT -> "Accademico"
            AppLanguage.UR -> "تعلیمی موڈ"
            AppLanguage.ZH -> "学术演进"
            AppLanguage.EN -> displayName
        }
        CURTAIN -> when (lang) {
            AppLanguage.AR -> "ستارة عرض"
            AppLanguage.FA -> "پرده"
            AppLanguage.RU -> "Занавес"
            AppLanguage.IT -> "Sipario"
            AppLanguage.UR -> "پردہ"
            AppLanguage.ZH -> "帷幕"
            AppLanguage.EN -> displayName
        }
        MORPH -> when (lang) {
            AppLanguage.AR -> "تحول سلس (Morph)"
            AppLanguage.FA -> "تبدیل نرم"
            AppLanguage.RU -> "Морфинг"
            AppLanguage.IT -> "Morphing"
            AppLanguage.UR -> "مورف"
            AppLanguage.ZH -> "平滑变体"
            AppLanguage.EN -> displayName
        }
    }
}

data class SlideTransition(
    val type: SlideTransitionType = SlideTransitionType.FADE,
    val durationMs: Int = 450
)

enum class MotionRange(val scaleFactor: Float) {
    SUBTLE(0.55f),
    NORMAL(1f),
    WIDE(1.65f);

    fun label(lang: AppLanguage): String = when (this) {
        SUBTLE -> when (lang) {
            AppLanguage.AR -> "امتداد هادئ"
            AppLanguage.FA -> "دامنه ملایم"
            AppLanguage.RU -> "Мягкий"
            AppLanguage.IT -> "Delicato"
            AppLanguage.UR -> "ہلکا"
            AppLanguage.ZH -> "轻柔"
            AppLanguage.EN -> "Subtle"
        }
        NORMAL -> when (lang) {
            AppLanguage.AR -> "امتداد متوازن"
            AppLanguage.FA -> "دامنه متعادل"
            AppLanguage.RU -> "Обычный"
            AppLanguage.IT -> "Normale"
            AppLanguage.UR -> "متوازن"
            AppLanguage.ZH -> "标准"
            AppLanguage.EN -> "Normal"
        }
        WIDE -> when (lang) {
            AppLanguage.AR -> "امتداد واسع"
            AppLanguage.FA -> "دامنه گسترده"
            AppLanguage.RU -> "Широкий"
            AppLanguage.IT -> "Ampio"
            AppLanguage.UR -> "وسیع"
            AppLanguage.ZH -> "大幅度"
            AppLanguage.EN -> "Wide"
        }
    }
}

enum class ElementAnimationType(val displayName: String, val isExit: Boolean = false) {
    NONE("None"),
    FLY_IN("Fly In"),
    FADE_IN("Fade In"),
    ZOOM_IN("Zoom In"),
    BOUNCE("Bounce"),
    FLOAT("Float"),
    ROTATE("Rotate"),
    PULSE_EMPHASIS("Pulse Emphasis"),
    SWING_EMPHASIS("Swing Emphasis"),
    ORBIT_PATH("Orbit Trajectory"),
    EXIT_FADE("Exit Fade", true),
    EXIT_FLY("Exit Fly Out", true),
    EXIT_ZOOM("Exit Shrink Out", true);

    fun label(lang: AppLanguage): String = when (this) {
        NONE -> when (lang) {
            AppLanguage.AR -> "بدون حركة"
            AppLanguage.FA -> "بدون انیمیشن"
            AppLanguage.RU -> "Нет"
            AppLanguage.IT -> "Nessuna"
            AppLanguage.UR -> "کوئی نہیں"
            AppLanguage.ZH -> "无"
            AppLanguage.EN -> displayName
        }
        FLY_IN -> when (lang) {
            AppLanguage.AR -> "دخول طائر"
            AppLanguage.FA -> "ورود پروازی"
            AppLanguage.RU -> "Вылет"
            AppLanguage.IT -> "Entrata"
            AppLanguage.UR -> "فلائی ان"
            AppLanguage.ZH -> "飞入"
            AppLanguage.EN -> displayName
        }
        FADE_IN -> when (lang) {
            AppLanguage.AR -> "ظهور تدريجي"
            AppLanguage.FA -> "ظهور تدریجی"
            AppLanguage.RU -> "Появление"
            AppLanguage.IT -> "Dissolvenza"
            AppLanguage.UR -> "فیڈ ان"
            AppLanguage.ZH -> "淡入"
            AppLanguage.EN -> displayName
        }
        ZOOM_IN -> when (lang) {
            AppLanguage.AR -> "تكبير للداخل"
            AppLanguage.FA -> "بزرگنمایی"
            AppLanguage.RU -> "Приближение"
            AppLanguage.IT -> "Zoom avanti"
            AppLanguage.UR -> "زوم ان"
            AppLanguage.ZH -> "放大"
            AppLanguage.EN -> displayName
        }
        BOUNCE -> when (lang) {
            AppLanguage.AR -> "ارتداد"
            AppLanguage.FA -> "جهش"
            AppLanguage.RU -> "Отскок"
            AppLanguage.IT -> "Rimbalzo"
            AppLanguage.UR -> "اچھال"
            AppLanguage.ZH -> "弹跳"
            AppLanguage.EN -> displayName
        }
        FLOAT -> when (lang) {
            AppLanguage.AR -> "طفو للأعلى"
            AppLanguage.FA -> "شناور"
            AppLanguage.RU -> "Всплытие"
            AppLanguage.IT -> "Fluttuazione"
            AppLanguage.UR -> "تیرنا"
            AppLanguage.ZH -> "浮入"
            AppLanguage.EN -> displayName
        }
        ROTATE -> when (lang) {
            AppLanguage.AR -> "دوران"
            AppLanguage.FA -> "چرخش"
            AppLanguage.RU -> "Вращение"
            AppLanguage.IT -> "Rotazione"
            AppLanguage.UR -> "گھومنا"
            AppLanguage.ZH -> "旋转"
            AppLanguage.EN -> displayName
        }
        PULSE_EMPHASIS -> when (lang) {
            AppLanguage.AR -> "توكيد نبضي"
            AppLanguage.FA -> "تاکید نبضی"
            AppLanguage.RU -> "Пульсация"
            AppLanguage.IT -> "Impulso"
            AppLanguage.UR -> "نبض"
            AppLanguage.ZH -> "脉冲强调"
            AppLanguage.EN -> displayName
        }
        SWING_EMPHASIS -> when (lang) {
            AppLanguage.AR -> "توكيد متأرجح"
            AppLanguage.FA -> "نوسان"
            AppLanguage.RU -> "Покачивание"
            AppLanguage.IT -> "Oscillazione"
            AppLanguage.UR -> "جھولا"
            AppLanguage.ZH -> "摇摆强调"
            AppLanguage.EN -> displayName
        }
        ORBIT_PATH -> when (lang) {
            AppLanguage.AR -> "مسار مداري"
            AppLanguage.FA -> "مسیر مداری"
            AppLanguage.RU -> "Орбита"
            AppLanguage.IT -> "Traiettoria"
            AppLanguage.UR -> "مدار"
            AppLanguage.ZH -> "轨道路径"
            AppLanguage.EN -> displayName
        }
        EXIT_FADE -> when (lang) {
            AppLanguage.AR -> "خروج: تلاشي"
            AppLanguage.FA -> "خروج محو"
            AppLanguage.RU -> "Выход: затухание"
            AppLanguage.IT -> "Uscita dissolv."
            AppLanguage.UR -> "خروج فیڈ"
            AppLanguage.ZH -> "淡出"
            AppLanguage.EN -> displayName
        }
        EXIT_FLY -> when (lang) {
            AppLanguage.AR -> "خروج: انطلاق"
            AppLanguage.FA -> "خروج پروازی"
            AppLanguage.RU -> "Выход: вылет"
            AppLanguage.IT -> "Uscita volo"
            AppLanguage.UR -> "خروج پرواز"
            AppLanguage.ZH -> "飞出"
            AppLanguage.EN -> displayName
        }
        EXIT_ZOOM -> when (lang) {
            AppLanguage.AR -> "خروج: تقلص"
            AppLanguage.FA -> "خروج کوچک"
            AppLanguage.RU -> "Выход: сжатие"
            AppLanguage.IT -> "Uscita zoom"
            AppLanguage.UR -> "خروج سکڑنا"
            AppLanguage.ZH -> "缩出"
            AppLanguage.EN -> displayName
        }
    }
}

enum class AnimationDirection(val displayName: String) {
    FROM_LEFT("From Left"),
    FROM_RIGHT("From Right"),
    FROM_TOP("From Top"),
    FROM_BOTTOM("From Bottom");

    fun label(lang: AppLanguage): String = when (this) {
        FROM_LEFT -> when (lang) {
            AppLanguage.AR -> "من اليسار"
            AppLanguage.FA -> "از چپ"
            AppLanguage.RU -> "Слева"
            AppLanguage.IT -> "Da sinistra"
            AppLanguage.UR -> "بائیں سے"
            AppLanguage.ZH -> "自左"
            AppLanguage.EN -> displayName
        }
        FROM_RIGHT -> when (lang) {
            AppLanguage.AR -> "من اليمين"
            AppLanguage.FA -> "از راست"
            AppLanguage.RU -> "Справа"
            AppLanguage.IT -> "Da destra"
            AppLanguage.UR -> "دائیں سے"
            AppLanguage.ZH -> "自右"
            AppLanguage.EN -> displayName
        }
        FROM_TOP -> when (lang) {
            AppLanguage.AR -> "من الأعلى"
            AppLanguage.FA -> "از بالا"
            AppLanguage.RU -> "Сверху"
            AppLanguage.IT -> "Dall'alto"
            AppLanguage.UR -> "اوپر سے"
            AppLanguage.ZH -> "自顶"
            AppLanguage.EN -> displayName
        }
        FROM_BOTTOM -> when (lang) {
            AppLanguage.AR -> "من الأسفل"
            AppLanguage.FA -> "از پایین"
            AppLanguage.RU -> "Снизу"
            AppLanguage.IT -> "Dal basso"
            AppLanguage.UR -> "نیچے سے"
            AppLanguage.ZH -> "自底"
            AppLanguage.EN -> displayName
        }
    }
}

data class ElementAnimation(
    val type: ElementAnimationType = ElementAnimationType.NONE,
    val exitType: ElementAnimationType = ElementAnimationType.NONE,
    val direction: AnimationDirection = AnimationDirection.FROM_BOTTOM,
    val motionRange: MotionRange = MotionRange.NORMAL,
    val durationMs: Int = 500,
    val delayMs: Int = 0,
    val orderIndex: Int = 0
)

sealed interface SlideBackground {
    data class Solid(val colorHex: String) : SlideBackground
    data class Gradient(
        val startColorHex: String,
        val endColorHex: String,
        val angleDegrees: Float = 135f
    ) : SlideBackground
    data class Image(val localImagePath: String, val overlayColorHex: String = "#33000000") : SlideBackground
}

enum class TextAlignment { START, CENTER, END, JUSTIFY }
enum class VerticalAlignment { TOP, CENTER, BOTTOM }
enum class WritingDirection { AUTO, RTL, LTR }
enum class TextListType { NONE, BULLET, NUMBERED, CHECKBOX }

enum class ShapeType(val displayName: String, val pptxPreset: String, val is3DShape: Boolean = false) {
    RECTANGLE("Rectangle", "rect"),
    ROUNDED_RECT("Rounded", "roundRect"),
    CIRCLE("Circle", "ellipse"),
    TRIANGLE("Triangle", "triangle"),
    ARROW_RIGHT("Arrow Right", "rightArrow"),
    ARROW_LEFT("Arrow Left", "leftArrow"),
    LINE("Line", "line"),
    STAR("Star", "star5"),
    DIAMOND("Diamond", "diamond"),
    CALLOUT("Callout", "wedgeRoundRectCallout"),
    HEXAGON("Hexagon", "hexagon"),
    PENTAGON("Pentagon", "pentagon"),
    OCTAGON("Octagon", "octagon"),
    CHEVRON("Chevron", "chevron"),
    PARALLELOGRAM("Parallelogram", "parallelogram"),
    CROSS_PLUS("Plus / Cross", "plus"),
    CUBE_3D("3D Cube", "cube", true),
    CYLINDER_3D("3D Cylinder", "can", true),
    PYRAMID_3D("3D Pyramid", "triangle", true),
    CONE_3D("3D Cone", "triangle", true),
    SPHERE_3D("3D Sphere", "ellipse", true),
    PRISM_3D("3D Prism", "rect", true),
    PILLAR_3D("3D Pillar", "roundRect", true),
    BEVEL_3D("3D Bevel Frame", "bevel", true);

    fun label(lang: AppLanguage): String = when (this) {
        RECTANGLE -> if (lang == AppLanguage.AR) "مستطيل" else displayName
        ROUNDED_RECT -> if (lang == AppLanguage.AR) "مستطيل دائري" else displayName
        CIRCLE -> if (lang == AppLanguage.AR) "دائرة" else displayName
        TRIANGLE -> if (lang == AppLanguage.AR) "مثلث" else displayName
        ARROW_RIGHT -> if (lang == AppLanguage.AR) "سهم يمين" else displayName
        ARROW_LEFT -> if (lang == AppLanguage.AR) "سهم يسار" else displayName
        LINE -> if (lang == AppLanguage.AR) "خط" else displayName
        STAR -> if (lang == AppLanguage.AR) "نجمة" else displayName
        DIAMOND -> if (lang == AppLanguage.AR) "معيّن" else displayName
        CALLOUT -> if (lang == AppLanguage.AR) "وسيلة شرح" else displayName
        HEXAGON -> if (lang == AppLanguage.AR) "سداسي" else displayName
        PENTAGON -> if (lang == AppLanguage.AR) "خماسي" else displayName
        OCTAGON -> if (lang == AppLanguage.AR) "ثماني" else displayName
        CHEVRON -> if (lang == AppLanguage.AR) "شعار متدرج" else displayName
        PARALLELOGRAM -> if (lang == AppLanguage.AR) "متوازي أضلاع" else displayName
        CROSS_PLUS -> if (lang == AppLanguage.AR) "علامة جمع +" else displayName
        CUBE_3D -> if (lang == AppLanguage.AR) "مكعب 3D" else displayName
        CYLINDER_3D -> if (lang == AppLanguage.AR) "أسطوانة 3D" else displayName
        PYRAMID_3D -> if (lang == AppLanguage.AR) "هرم 3D" else displayName
        CONE_3D -> if (lang == AppLanguage.AR) "مخروط 3D" else displayName
        SPHERE_3D -> if (lang == AppLanguage.AR) "كرة 3D" else displayName
        PRISM_3D -> if (lang == AppLanguage.AR) "منشور 3D" else displayName
        PILLAR_3D -> if (lang == AppLanguage.AR) "عمود 3D" else displayName
        BEVEL_3D -> if (lang == AppLanguage.AR) "إطار مجسم 3D" else displayName
    }
}

enum class SmartArtCategory(val ar: String, val en: String) {
    LIST("قوائم", "List"),
    PROCESS("عمليات ومسارات", "Process"),
    CYCLE("دورات", "Cycle"),
    HIERARCHY("هيكل هرمي", "Hierarchy"),
    RELATIONSHIP("علاقات ومصفوفات", "Relationship"),
    MATRIX_PYRAMID("هرم وتحليل", "Pyramid & SWOT");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> ar
        else -> en
    }
}

enum class SmartArtLayoutType(
    val displayNameEn: String,
    val displayNameAr: String,
    val category: SmartArtCategory
) {
    PROCESS_ARROWS("Horizontal Process", "مسار خطوات متتابعة", SmartArtCategory.PROCESS),
    CHEVRON_FLOW("Chevron Steps", "أسهم مراحل مترابطة", SmartArtCategory.PROCESS),
    CYCLE_RING("Cycle Diagram", "دورة متكررة", SmartArtCategory.CYCLE),
    HIERARCHY_ORG("Hierarchy Tree", "هيكل تنظيمي هرمي", SmartArtCategory.HIERARCHY),
    PYRAMID_LEVELS("Pyramid Levels", "هرم مستويات", SmartArtCategory.MATRIX_PYRAMID),
    VENN_OVERLAP("Venn Diagram", "دوائر فن المتداخلة", SmartArtCategory.RELATIONSHIP),
    MATRIX_2X2("2x2 Matrix", "مصفوفة رباعية 2×2", SmartArtCategory.RELATIONSHIP),
    FUNNEL_STAGES("Funnel Stages", "قمع التصفية والمراحل", SmartArtCategory.PROCESS),
    RADIAL_HUB("Radial Hub", "محور مركزي متشعب", SmartArtCategory.CYCLE),
    TIMELINE_ROADMAP("Roadmap Timeline", "خط زمني للمشاريع", SmartArtCategory.PROCESS),
    VERTICAL_LIST("Chevron Stack List", "قائمة بطاقات رأسية", SmartArtCategory.LIST),
    ICON_GRID("Pillar Blocks", "شبكة أعمدة متوازية", SmartArtCategory.LIST),
    STEP_UP_STAIRS("Ascending Stairs", "درج النمو المتصاعد", SmartArtCategory.PROCESS),
    GEAR_SYSTEM("Connected Gears", "تروس نظام مترابط", SmartArtCategory.CYCLE),
    SWOT_GRID("SWOT Analysis", "مصفوفة SWOT الاستراتيجية", SmartArtCategory.MATRIX_PYRAMID),
    BALANCE_COMPARE("Two-Side Balance", "مقارنة كفتي الميزان", SmartArtCategory.RELATIONSHIP);

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> displayNameAr
        else -> displayNameEn
    }
}

data class SmartArtNode(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val fillColorHex: String,
    val textColorHex: String = "#FFFFFF"
)

fun defaultSmartArtNodes(
    layout: SmartArtLayoutType,
    lang: AppLanguage = AppLanguage.AR
): List<SmartArtNode> {
    val arLabels = when (layout) {
        SmartArtLayoutType.HIERARCHY_ORG -> listOf("الإدارة العليا", "القسم العلمي", "القسم التقني", "الجودة")
        SmartArtLayoutType.PYRAMID_LEVELS -> listOf("الرؤية العليا", "الاستراتيجية", "التنفيذ", "الأساس")
        SmartArtLayoutType.SWOT_GRID -> listOf("نقاط القوة S", "نقاط الضعف W", "الفرص O", "التحديات T")
        SmartArtLayoutType.MATRIX_2X2 -> listOf("عاجل ومهم", "مهم ومخطط", "أثر مرتفع", "متابعة")
        SmartArtLayoutType.VENN_OVERLAP -> listOf("الفكرة", "التقنية", "الأثر")
        SmartArtLayoutType.BALANCE_COMPARE -> listOf("المميزات والإيجابيات", "المتطلبات والتحديات")
        else -> listOf("المرحلة الأولى", "المرحلة الثانية", "المرحلة الثالثة", "النتيجة")
    }
    val enLabels = when (layout) {
        SmartArtLayoutType.HIERARCHY_ORG -> listOf("Leadership", "Research", "Engineering", "Quality")
        SmartArtLayoutType.PYRAMID_LEVELS -> listOf("Vision", "Strategy", "Operations", "Foundation")
        SmartArtLayoutType.SWOT_GRID -> listOf("Strengths", "Weaknesses", "Opportunities", "Threats")
        SmartArtLayoutType.MATRIX_2X2 -> listOf("High Impact", "Strategic Plan", "Quick Wins", "Monitor")
        SmartArtLayoutType.VENN_OVERLAP -> listOf("Concept", "Technology", "Impact")
        SmartArtLayoutType.BALANCE_COMPARE -> listOf("Advantages", "Considerations")
        else -> listOf("Phase 1", "Phase 2", "Phase 3", "Outcome")
    }
    val palette = listOf("#1565C0", "#00897B", "#F59E0B", "#7C3AED", "#DC2626")
    val titles = if (lang.isRtl) arLabels else enLabels
    return titles.mapIndexed { idx, title ->
        SmartArtNode(
            text = title,
            fillColorHex = palette[idx % palette.size],
            textColorHex = "#FFFFFF"
        )
    }
}

enum class ChartType(val displayName: String) {
    BAR("Bar"),
    PIE("Pie"),
    LINE("Line"),
    AREA("Area"),
    COMPARISON("Comparison");

    fun label(lang: AppLanguage): String = when (this) {
        BAR -> when (lang) {
            AppLanguage.AR -> "أعمدة"
            AppLanguage.FA -> "میله‌ای"
            AppLanguage.RU -> "Столбцы"
            AppLanguage.IT -> "Barre"
            AppLanguage.UR -> "بار"
            AppLanguage.ZH -> "柱状图"
            AppLanguage.EN -> displayName
        }
        PIE -> when (lang) {
            AppLanguage.AR -> "دائري"
            AppLanguage.FA -> "دایره‌ای"
            AppLanguage.RU -> "Круговая"
            AppLanguage.IT -> "Torta"
            AppLanguage.UR -> "پائی"
            AppLanguage.ZH -> "饼图"
            AppLanguage.EN -> displayName
        }
        LINE -> when (lang) {
            AppLanguage.AR -> "خطي"
            AppLanguage.FA -> "خطی"
            AppLanguage.RU -> "Линейная"
            AppLanguage.IT -> "Linee"
            AppLanguage.UR -> "لائن"
            AppLanguage.ZH -> "折线图"
            AppLanguage.EN -> displayName
        }
        AREA -> when (lang) {
            AppLanguage.AR -> "مساحي"
            AppLanguage.FA -> "مساحتی"
            AppLanguage.RU -> "Область"
            AppLanguage.IT -> "Area"
            AppLanguage.UR -> "ایریا"
            AppLanguage.ZH -> "面积图"
            AppLanguage.EN -> displayName
        }
        COMPARISON -> when (lang) {
            AppLanguage.AR -> "مقارنة"
            AppLanguage.FA -> "مقایسه‌ای"
            AppLanguage.RU -> "Сравнение"
            AppLanguage.IT -> "Confronto"
            AppLanguage.UR -> "موازنہ"
            AppLanguage.ZH -> "对比图"
            AppLanguage.EN -> displayName
        }
    }
}

data class ChartSeries(
    val name: String,
    val colorHex: String,
    val values: List<Float>
)

data class TableCell(
    val row: Int,
    val col: Int,
    val text: String = "",
    val bgColorHex: String = "#FFFFFF",
    val textColorHex: String = "#0F172A",
    val isHeader: Boolean = false,
    val isBold: Boolean = false,
    val rowSpan: Int = 1,
    val colSpan: Int = 1,
    val mergedIntoRow: Int? = null,
    val mergedIntoCol: Int? = null
)

data class CropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
)

sealed interface SlideElement {
    val id: String
    val x: Float
    val y: Float
    val width: Float
    val height: Float
    val rotation: Float
    val zIndex: Int
    val opacity: Float
    val groupId: String?
    val animation: ElementAnimation

    fun withTransform(
        x: Float = this.x,
        y: Float = this.y,
        width: Float = this.width,
        height: Float = this.height,
        rotation: Float = this.rotation,
        zIndex: Int = this.zIndex,
        opacity: Float = this.opacity,
        groupId: String? = this.groupId,
        animation: ElementAnimation = this.animation
    ): SlideElement

    fun withNewId(newId: String = UUID.randomUUID().toString()): SlideElement

    data class TextElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 160f,
        override val y: Float = 160f,
        override val width: Float = 960f,
        override val height: Float = 220f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val text: String = "",
        val fontFamilyId: String = "cairo",
        val fontSizeSp: Float = 38f,
        val textColorHex: String = "#0F172A",
        val backgroundColorHex: String = "#00000000",
        val highlightColorHex: String = "#00000000",
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val isStrikethrough: Boolean = false,
        val textAlign: TextAlignment = TextAlignment.START,
        val verticalAlign: VerticalAlignment = VerticalAlignment.TOP,
        val writingDirection: WritingDirection = WritingDirection.AUTO,
        val lineHeightMultiplier: Float = 1.25f,
        val letterSpacing: Float = 0f,
        val listType: TextListType = TextListType.NONE,
        val isBlockQuote: Boolean = false,
        val linkUrl: String = "",
        val borderColorHex: String = "#00000000",
        val borderWidth: Float = 0f,
        val hasShadow: Boolean = false
    ) : SlideElement {
        override fun withTransform(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            rotation: Float,
            zIndex: Int,
            opacity: Float,
            groupId: String?,
            animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class ImageElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 260f,
        override val y: Float = 180f,
        override val width: Float = 640f,
        override val height: Float = 440f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val localImagePath: String,
        val fillCropMode: Boolean = true,
        val cropRect: CropRect = CropRect(),
        val borderColorHex: String = "#00000000",
        val borderWidth: Float = 0f,
        val cornerRadius: Float = 16f,
        val hasShadow: Boolean = false
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class ShapeElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 320f,
        override val y: Float = 240f,
        override val width: Float = 420f,
        override val height: Float = 280f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val shapeType: ShapeType = ShapeType.ROUNDED_RECT,
        val fillColorHex: String = "#1565C0",
        val strokeColorHex: String = "#0D47A1",
        val strokeWidth: Float = 4f,
        val hasShadow: Boolean = false,
        val labelText: String = "",
        val labelColorHex: String = "#FFFFFF",
        val is3D: Boolean = false,
        val depth3D: Float = 24f,
        val extrudeColorHex: String = "#0A2E66",
        val lightAngle3D: Float = 45f
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class SmartArtElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 140f,
        override val y: Float = 240f,
        override val width: Float = 1640f,
        override val height: Float = 660f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val layoutType: SmartArtLayoutType = SmartArtLayoutType.PROCESS_ARROWS,
        val nodes: List<SmartArtNode> = defaultSmartArtNodes(SmartArtLayoutType.PROCESS_ARROWS),
        val fontFamilyId: String = "cairo",
        val is3D: Boolean = true,
        val connectorColorHex: String = "#64748B"
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class TableElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 180f,
        override val y: Float = 220f,
        override val width: Float = 1280f,
        override val height: Float = 540f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val rows: Int = 3,
        val cols: Int = 3,
        val cells: List<TableCell> = defaultTableCells(3, 3),
        val borderColorHex: String = "#CBD5E1",
        val borderWidth: Float = 2f,
        val fontSizeSp: Float = 24f
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class ChartElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 220f,
        override val y: Float = 200f,
        override val width: Float = 1120f,
        override val height: Float = 640f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val chartType: ChartType = ChartType.BAR,
        val title: String = "Chart",
        val labels: List<String> = listOf("Q1", "Q2", "Q3", "Q4"),
        val series: List<ChartSeries> = listOf(
            ChartSeries("2025", "#1565C0", listOf(42f, 68f, 55f, 89f)),
            ChartSeries("2026", "#00897B", listOf(58f, 74f, 81f, 96f))
        ),
        val showLegend: Boolean = true,
        val showValues: Boolean = true,
        val bgColorHex: String = "#FFFFFF"
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class EquationElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 260f,
        override val y: Float = 320f,
        override val width: Float = 960f,
        override val height: Float = 220f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val expression: String = "\\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
        val fontSizeSp: Float = 44f,
        val colorHex: String = "#0D47A1",
        val bgColorHex: String = "#F8FAFC"
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }

    data class IconElement(
        override val id: String = UUID.randomUUID().toString(),
        override val x: Float = 760f,
        override val y: Float = 360f,
        override val width: Float = 220f,
        override val height: Float = 220f,
        override val rotation: Float = 0f,
        override val zIndex: Int = 1,
        override val opacity: Float = 1f,
        override val groupId: String? = null,
        override val animation: ElementAnimation = ElementAnimation(),
        val symbolKey: String = "science",
        val tintColorHex: String = "#0D47A1",
        val bgColorHex: String = "#1ADCE8FF"
    ) : SlideElement {
        override fun withTransform(
            x: Float, y: Float, width: Float, height: Float,
            rotation: Float, zIndex: Int, opacity: Float, groupId: String?, animation: ElementAnimation
        ) = copy(
            x = x, y = y, width = width, height = height,
            rotation = rotation, zIndex = zIndex, opacity = opacity,
            groupId = groupId, animation = animation
        )

        override fun withNewId(newId: String) = copy(id = newId)
    }
}

data class Slide(
    val id: String = UUID.randomUUID().toString(),
    val order: Int = 0,
    val background: SlideBackground = SlideBackground.Solid("#FFFFFF"),
    val transition: SlideTransition = SlideTransition(),
    val speakerNotes: String = "",
    val elements: List<SlideElement> = emptyList(),
    val autoAdvanceSeconds: Int = 5,
    val decorationId: String = "none",
    val decorationColorHex: String = "#1565C0"
)

data class Presentation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val language: AppLanguage = AppLanguage.AR,
    val aspectRatio: SlideAspectRatio = SlideAspectRatio.RATIO_16_9,
    val canvasWidth: Float = SlideAspectRatio.RATIO_16_9.defaultWidth,
    val canvasHeight: Float = SlideAspectRatio.RATIO_16_9.defaultHeight,
    val themeId: String = "kateb_academic",
    val sourceType: PresentationSourceType = PresentationSourceType.BLANK,
    val slides: List<Slide> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun defaultTableCells(rows: Int, cols: Int, lang: AppLanguage = AppLanguage.EN): List<TableCell> = buildList {
    val headerPrefix = when (lang) {
        AppLanguage.AR -> "عنوان"
        AppLanguage.FA -> "ستون"
        AppLanguage.RU -> "Заголовок"
        AppLanguage.IT -> "Intest."
        AppLanguage.UR -> "ہیڈر"
        AppLanguage.ZH -> "表头"
        AppLanguage.EN -> "Header"
    }
    val itemPrefix = when (lang) {
        AppLanguage.AR -> "بند"
        AppLanguage.FA -> "مورد"
        AppLanguage.RU -> "Элем."
        AppLanguage.IT -> "Voce"
        AppLanguage.UR -> "آئٹم"
        AppLanguage.ZH -> "项目"
        AppLanguage.EN -> "Item"
    }
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            add(
                TableCell(
                    row = r,
                    col = c,
                    text = if (r == 0) "$headerPrefix ${c + 1}" else "$itemPrefix ${r}.${c + 1}",
                    bgColorHex = if (r == 0) "#0D47A1" else if (r % 2 == 1) "#F8FAFC" else "#FFFFFF",
                    textColorHex = if (r == 0) "#FFFFFF" else "#0F172A",
                    isHeader = r == 0,
                    isBold = r == 0
                )
            )
        }
    }
}

fun detectTextIsRtl(text: String, direction: WritingDirection = WritingDirection.AUTO): Boolean {
    return when (direction) {
        WritingDirection.RTL -> true
        WritingDirection.LTR -> false
        WritingDirection.AUTO -> {
            val firstStrong = text.firstOrNull { ch ->
                val dir = Character.getDirectionality(ch)
                dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                    dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC ||
                    dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT
            }
            if (firstStrong == null) false
            else {
                val dir = Character.getDirectionality(firstStrong)
                dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                    dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
            }
        }
    }
}

fun parseColorInt(hex: String, fallback: Int = 0xFF0F172A.toInt()): Int {
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> (0xFF000000L or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

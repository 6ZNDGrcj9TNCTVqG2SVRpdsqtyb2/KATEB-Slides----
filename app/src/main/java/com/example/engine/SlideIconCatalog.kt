package com.example.engine

import com.example.core.localization.AppLanguage

enum class IconCategory(val ar: String, val en: String, val fa: String) {
    SCIENCE("علوم", "Science", "علوم"),
    MEDICAL("طب وصحة", "Medical", "پزشکی"),
    BUSINESS("أعمال ومالية", "Business", "کسب‌وکار"),
    EDUCATION("تعليم وأبحاث", "Education", "آموزش"),
    TECH("تقنية وذكاء", "Tech & AI", "فناوری"),
    ENGINEERING("هندسة وبناء", "Engineering", "مهندسی"),
    LAW("قانون وحوكمة", "Law & Policy", "حقوق"),
    CULTURE("تراث وحضارة", "Culture", "فرهنگ"),
    DESIGN("تصميم وإعلام", "Design & Media", "طراحی"),
    BADGES("رموز وشارات", "Badges & UI", "نشان‌ها");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> ar
        AppLanguage.EN -> en
        AppLanguage.FA -> fa
        AppLanguage.RU -> when (this) {
            SCIENCE -> "Наука"
            MEDICAL -> "Медицина"
            BUSINESS -> "Бизнес"
            EDUCATION -> "Обучение"
            TECH -> "ИИ и Tech"
            ENGINEERING -> "Инженерия"
            LAW -> "Право"
            CULTURE -> "Культура"
            DESIGN -> "Дизайн"
            BADGES -> "Значки"
        }
        AppLanguage.IT -> when (this) {
            SCIENCE -> "Scienza"
            MEDICAL -> "Medicina"
            BUSINESS -> "Business"
            EDUCATION -> "Studio"
            TECH -> "Tech e AI"
            ENGINEERING -> "Ingegneria"
            LAW -> "Diritto"
            CULTURE -> "Cultura"
            DESIGN -> "Design"
            BADGES -> "Badge"
        }
        AppLanguage.UR -> when (this) {
            SCIENCE -> "سائنس"
            MEDICAL -> "طب"
            BUSINESS -> "کاروبار"
            EDUCATION -> "تعلیم"
            TECH -> "ٹیکنالوجی"
            ENGINEERING -> "انجینئرنگ"
            LAW -> "قانون"
            CULTURE -> "ثقافت"
            DESIGN -> "ڈیزائن"
            BADGES -> "نشان"
        }
        AppLanguage.ZH -> when (this) {
            SCIENCE -> "科学"
            MEDICAL -> "医疗"
            BUSINESS -> "商业"
            EDUCATION -> "教育"
            TECH -> "科技"
            ENGINEERING -> "工程"
            LAW -> "法律"
            CULTURE -> "文化"
            DESIGN -> "设计"
            BADGES -> "徽章"
        }
    }
}

data class SlideIconSpec(
    val key: String,
    val glyph: String,
    val ar: String,
    val en: String,
    val fa: String,
    val category: IconCategory,
    val usageAr: String = "",
    val usageEn: String = ""
) {
    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> ar
        AppLanguage.FA -> fa
        AppLanguage.EN -> en
        AppLanguage.UR -> "${category.label(AppLanguage.UR)} • $ar"
        AppLanguage.RU -> "${category.label(AppLanguage.RU)} • $en"
        AppLanguage.IT -> "${category.label(AppLanguage.IT)} • $en"
        AppLanguage.ZH -> "${category.label(AppLanguage.ZH)} • $en"
    }

    fun usageHint(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> usageAr.ifBlank { ar }
        AppLanguage.FA, AppLanguage.UR -> usageAr.ifBlank { fa }
        else -> usageEn.ifBlank { en }
    }
}

/**
 * 100 multi-disciplinary slide icons across 10 specialized categories.
 */
object SlideIconCatalog {
    val icons: List<SlideIconSpec> = listOf(
        // Science (10)
        SlideIconSpec("science", "⚗", "مختبر كيمياء", "Lab Flask", "آزمایشگاه", IconCategory.SCIENCE, "للتجارب والتفاعلات", "Chemistry & lab results"),
        SlideIconSpec("atom", "⚛", "نواة وذرة", "Atom Orbit", "اتم", IconCategory.SCIENCE, "للفيزياء والطاقة الذرية", "Physics & quantum data"),
        SlideIconSpec("dna", "🧬", "شريط وراثي", "DNA Helix", "دی‌ان‌ای", IconCategory.SCIENCE, "للأحياء والوراثة", "Biology & genetics"),
        SlideIconSpec("microscope", "🔬", "مجهر علمي", "Microscope", "میکروسکوپ", IconCategory.SCIENCE, "للفحص المجهري", "Micro analysis & labs"),
        SlideIconSpec("magnet", "🧲", "مجال مغناطيسي", "Magnet Field", "مغناطیس", IconCategory.SCIENCE, "للقوى الفيزيائية", "Forces & attraction"),
        SlideIconSpec("telescope", "🔭", "تلسكوب فلكي", "Telescope", "تلسکوپ", IconCategory.SCIENCE, "للفضاء والفلك", "Astronomy & space"),
        SlideIconSpec("planet", "🪐", "كوكب وحلقات", "Ringed Planet", "سیاره", IconCategory.SCIENCE, "لعلوم الفضاء", "Planetary science"),
        SlideIconSpec("petri", "🧫", "طبق بتري", "Petri Dish", "کشت سلولی", IconCategory.SCIENCE, "للأحياء الدقيقة", "Microbiology"),
        SlideIconSpec("testtube", "🧪", "أنبوب اختبار", "Test Tube", "لوله آزمایش", IconCategory.SCIENCE, "للعينات والتحاليل", "Samples & assays"),
        SlideIconSpec("leaf_eco", "🌿", "بيئة واستدامة", "Ecology Leaf", "محیط زیست", IconCategory.SCIENCE, "للطاقة النظيفة والبيئة", "Sustainability & botany"),

        // Medical (10)
        SlideIconSpec("medical", "✚", "صليب طبي", "Medical Cross", "پزشکی", IconCategory.MEDICAL, "للمحتوى الطبي", "Clinical & healthcare"),
        SlideIconSpec("heart", "♥", "نبض الصحة", "Heart Pulse", "سلامت", IconCategory.MEDICAL, "لمؤشرات القلب والصحة", "Cardiology & wellness"),
        SlideIconSpec("brain", "🧠", "ذكاء وعقل", "Brain Network", "مغز", IconCategory.MEDICAL, "للأعصاب والإدراك", "Neuroscience & cognition"),
        SlideIconSpec("stethoscope", "🩺", "سماعة طبيب", "Stethoscope", "گوشی پزشکی", IconCategory.MEDICAL, "للفحص السريري", "Diagnostics & care"),
        SlideIconSpec("pill", "💊", "دواء وصيدلة", "Pharmacology", "دارو", IconCategory.MEDICAL, "للعلاج والصيدلة", "Medicine & dosage"),
        SlideIconSpec("syringe", "💉", "لقاح ومناعة", "Immunology", "واکسن", IconCategory.MEDICAL, "للمناعة واللقاحات", "Vaccination & trials"),
        SlideIconSpec("hospital", "🏥", "مستشفى ورعاية", "Hospital Care", "بیمارستان", IconCategory.MEDICAL, "للمؤسسات الصحية", "Hospital management"),
        SlideIconSpec("tooth", "🦷", "طب الأسنان", "Dental Care", "دندانپزشکی", IconCategory.MEDICAL, "لصحة الفم والأسنان", "Dentistry"),
        SlideIconSpec("eye_med", "👁", "بصريات", "Ophthalmology", "چشم‌پزشکی", IconCategory.MEDICAL, "للبصر والرؤية", "Vision & optics"),
        SlideIconSpec("bone", "🦴", "عظام وتأهيل", "Orthopedics", "ارتوپدی", IconCategory.MEDICAL, "لطب العظام", "Anatomy & recovery"),

        // Tech & AI (10)
        SlideIconSpec("rocket", "🚀", "إطلاق وابتكار", "Launch Rocket", "پرتاب", IconCategory.TECH, "لانطلاق المشاريع", "Product launch & growth"),
        SlideIconSpec("bolt", "⚡", "طاقة وسرعة", "Energy Bolt", "انرژی", IconCategory.TECH, "للكفاءة والأداء", "Performance & power"),
        SlideIconSpec("laptop", "💻", "حاسوب وبرمجة", "Code Laptop", "رایانه", IconCategory.TECH, "للأنظمة والبرمجيات", "Software & coding"),
        SlideIconSpec("satellite", "📡", "إشارة واتصالات", "Signal Dish", "مخابرات", IconCategory.TECH, "للشبكات والاتصال", "Networks & cloud"),
        SlideIconSpec("shield", "🛡", "درع الحماية", "Security Shield", "امنیت", IconCategory.TECH, "للأمن السيبراني", "Cybersecurity"),
        SlideIconSpec("robot", "🤖", "ذكاء اصطناعي", "AI Robot", "ربات هوش مصنوعی", IconCategory.TECH, "للأتمتة والذكاء", "Automation & agents"),
        SlideIconSpec("chip", "🖥", "معالج وخادم", "Server & CPU", "پردازنده", IconCategory.TECH, "للبنية السحابية", "Compute & infrastructure"),
        SlideIconSpec("mobile", "📱", "تطبيق ذكي", "Mobile App", "موبایل", IconCategory.TECH, "لتجربة الهاتف", "Mobile platforms"),
        SlideIconSpec("cloud", "☁", "الحوسبة السحابية", "Cloud Sync", "ابر", IconCategory.TECH, "للتخزين والمزامنة", "Cloud storage"),
        SlideIconSpec("lock_sec", "🔒", "تشفير وخصوصية", "Encryption", "رمزگذاری", IconCategory.TECH, "لحماية البيانات", "Data privacy"),

        // Engineering (10)
        SlideIconSpec("engineering", "⚙", "ترس هندسي", "Engineering Gear", "مهندسی", IconCategory.ENGINEERING, "للعمليات والميكانيكا", "Systems & workflows"),
        SlideIconSpec("wrench", "🔧", "صيانة وضبط", "Precision Tool", "آچار", IconCategory.ENGINEERING, "للضبط والتطوير", "Maintenance & tuning"),
        SlideIconSpec("crane", "🏗", "تشييد ومعمار", "Construction", "ساخت‌وساز", IconCategory.ENGINEERING, "لمشاريع البناء", "Civil engineering"),
        SlideIconSpec("ruler", "📐", "مثلث رسم هندسي", "Drafting Angle", "گونیا", IconCategory.ENGINEERING, "للتصميم والقياس", "Geometry & CAD"),
        SlideIconSpec("battery", "🔋", "بطارية وتخزين", "Energy Cell", "باتری", IconCategory.ENGINEERING, "لمصادر الطاقة", "Power systems"),
        SlideIconSpec("factory", "🏭", "تصنيع وإنتاج", "Industry Plant", "کارخانه", IconCategory.ENGINEERING, "لخطوط الإنتاج", "Manufacturing"),
        SlideIconSpec("bridge", "🌉", "بنية تحتية", "Infrastructure", "زیرساخت", IconCategory.ENGINEERING, "للجسور والنقل", "Structural design"),
        SlideIconSpec("solar", "☀", "طاقة شمسية", "Solar Energy", "انرژی خورشیدی", IconCategory.ENGINEERING, "للطاقة المتجددة", "Renewables"),
        SlideIconSpec("chip_board", "🔌", "دائرة كهربائية", "Circuit Plug", "مدار برق", IconCategory.ENGINEERING, "للهندسة الكهربائية", "Electrical circuits"),
        SlideIconSpec("helicopter", "🛸", "طيران ومسيرات", "Aero Drone", "هوافضا", IconCategory.ENGINEERING, "لهندسة الطيران", "Aerospace & drones"),

        // Business & Finance (10)
        SlideIconSpec("chart", "📊", "مخطط أعمدة", "Analytics Chart", "آمار", IconCategory.BUSINESS, "لتلخيص الإحصاءات", "KPIs & quarterly data"),
        SlideIconSpec("trend_up", "📈", "صعود ونمو", "Growth Trend", "رشد", IconCategory.BUSINESS, "لإبراز النمو والأرباح", "Revenue & expansion"),
        SlideIconSpec("briefcase", "💼", "حقيبة أعمال", "Briefcase", "کسب‌وکار", IconCategory.BUSINESS, "للمشاريع والشركات", "Corporate & enterprise"),
        SlideIconSpec("target", "🎯", "تصويب الهدف", "Target Goal", "هدف", IconCategory.BUSINESS, "لتحديد الأهداف", "Milestones & KPIs"),
        SlideIconSpec("coins", "🪙", "تمويل ومالية", "Finance Coins", "مالی", IconCategory.BUSINESS, "للميزانية والعوائد", "Budget & investments"),
        SlideIconSpec("bank", "🏦", "بنك ومصرف", "Banking", "بانک", IconCategory.BUSINESS, "للمؤسسات المالية", "Finance & capital"),
        SlideIconSpec("handshake", "🤝", "شراكة واتفاق", "Partnership", "شراکت", IconCategory.BUSINESS, "للعقود والتحالفات", "Deals & alliances"),
        SlideIconSpec("diamond_val", "💎", "قيمة مضافة", "High Value", "ارزش", IconCategory.BUSINESS, "للميزة التنافسية", "Premium value"),
        SlideIconSpec("cart", "🛒", "تجارة إلكترونية", "Commerce", "تجارت", IconCategory.BUSINESS, "للمبيعات والتجزئة", "Retail & sales"),
        SlideIconSpec("megaphone", "📣", "تسويق وإعلان", "Marketing", "بازاریابی", IconCategory.BUSINESS, "للحملات والوصول", "Campaigns & PR"),

        // Education & Research (10)
        SlideIconSpec("book", "📖", "كتاب ومصدر", "Open Book", "کتاب", IconCategory.EDUCATION, "للمراجع والمناهج", "Curriculum & references"),
        SlideIconSpec("grad", "🎓", "قبعة تخرج", "Graduation Cap", "دانشگاهی", IconCategory.EDUCATION, "للرسائل العلمية", "Academic & thesis"),
        SlideIconSpec("lightbulb", "💡", "مصباح فكرة", "Bright Idea", "ایده", IconCategory.EDUCATION, "للفرضيات والأفكار", "Insights & hypotheses"),
        SlideIconSpec("pen", "✒", "قلم وتأليف", "Fountain Pen", "نگارش", IconCategory.EDUCATION, "للتدوين والتحرير", "Authoring & notes"),
        SlideIconSpec("search", "🔍", "عدسة بحث", "Search Lens", "پژوهش", IconCategory.EDUCATION, "للمنهج البحثي", "Discovery & research"),
        SlideIconSpec("clock", "⏱", "مؤقت زمني", "Chronometer", "زمان", IconCategory.EDUCATION, "للجدول الزمني", "Timelines & schedules"),
        SlideIconSpec("books", "📚", "مكتبة ومصادر", "Library Stack", "کتابخانه", IconCategory.EDUCATION, "للفهرس والمراجع", "Bibliography"),
        SlideIconSpec("notebook", "📓", "دفتر ملاحظات", "Notebook", "دفترچه", IconCategory.EDUCATION, "لمحاضرات الدراسة", "Study notes"),
        SlideIconSpec("teacher", "🧑‍🏫", "محاضر وتدريس", "Lecturer", "مدرس", IconCategory.EDUCATION, "للتدريب والتدريس", "Teaching & workshops"),
        SlideIconSpec("abacus", "🧮", "عد وحساب", "Abacus Math", "چرتکه", IconCategory.EDUCATION, "للرياضيات والمنطق", "Arithmetic & logic"),

        // Law & Governance (10)
        SlideIconSpec("balance", "⚖", "ميزان العدالة", "Justice Scale", "عدالت", IconCategory.LAW, "للقانون والحوكمة", "Law & parity"),
        SlideIconSpec("gavel", "🔨", "مطرقة قضاء", "Judge Gavel", "قضاوت", IconCategory.LAW, "للأحكام والتشريع", "Courts & rulings"),
        SlideIconSpec("scroll", "📜", "وثيقة وميثاق", "Legal Charter", "منشور", IconCategory.LAW, "للدساتير واللوائح", "Charters & statutes"),
        SlideIconSpec("stamp", "🔏", "تصديق رسمي", "Signed Seal", "مهر رسمی", IconCategory.LAW, "للاعتماد والتوقيع", "Contracts & notary"),
        SlideIconSpec("capitol", "🏛", "مؤسسة وتشريع", "Parliament", "نهاد قانون", IconCategory.LAW, "للهيئات الحكومية", "Governance & policy"),
        SlideIconSpec("clipboard_law", "📋", "لائحة امتثال", "Compliance List", "انطباق", IconCategory.LAW, "لقوائم التدقيق", "Audit & compliance"),
        SlideIconSpec("id_badge", "🪪", "هوية وتصريح", "Credentials", "مجوز", IconCategory.LAW, "للصلاحيات والهوية", "Identity & permits"),
        SlideIconSpec("key_access", "🔑", "مفتاح سيادي", "Access Key", "کلید", IconCategory.LAW, "للسياسات والوصول", "Access & rights"),
        SlideIconSpec("guard", "🪖", "دفاع وأنظمة", "Public Safety", "پدافند", IconCategory.LAW, "للسلامة والنظام", "Safety regulations"),
        SlideIconSpec("dossier", "🗂", "ملف قضية", "Case Dossier", "پرونده", IconCategory.LAW, "لتنظيم الملفات", "Case archives"),

        // Culture & Heritage (10)
        SlideIconSpec("mosque", "☪", "هلال وتراث", "Crescent & Dome", "اسلامی", IconCategory.CULTURE, "للدراسات الإسلامية", "Islamic studies & art"),
        SlideIconSpec("history", "🏺", "آثار وتاريخ", "Ancient Amphora", "تاریخی", IconCategory.CULTURE, "للحضارات القديمة", "Civilizations & history"),
        SlideIconSpec("globe", "🌐", "تواصل عالمي", "Global Globe", "جهانی", IconCategory.CULTURE, "للجغرافيا والعالم", "Geography & world view"),
        SlideIconSpec("compass", "🧭", "بوصلة اتجاه", "Compass", "قطب‌نما", IconCategory.CULTURE, "للتوجيه والرحلات", "Direction & expeditions"),
        SlideIconSpec("map", "🗺", "خريطة جغرافية", "World Map", "نقشه", IconCategory.CULTURE, "للمواقع والأقاليم", "Regions & cartography"),
        SlideIconSpec("palm", "🌴", "تراث وبيئة", "Oasis Heritage", "نخل", IconCategory.CULTURE, "للبيئة العربية", "Arabian heritage"),
        SlideIconSpec("hourglass", "⏳", "ساعة رملية", "Hourglass", "ساعت شنی", IconCategory.CULTURE, "للحقب التاريخية", "Historical eras"),
        SlideIconSpec("lantern", "🏮", "فانوس ثقافي", "Heritage Lantern", "فانوس", IconCategory.CULTURE, "للمناسبات الثقافية", "Cultural festivals"),
        SlideIconSpec("castle", "🏰", "قلعة تاريخية", "Citadel", "قلعه", IconCategory.CULTURE, "للتاريخ العمراني", "Historic architecture"),
        SlideIconSpec("crown", "👑", "تاج الملوك", "Royal Crown", "تاج", IconCategory.CULTURE, "للسير والملوك", "Monarchies & leadership"),

        // Design & Media (10)
        SlideIconSpec("palette_art", "🎨", "لوحة ألوان", "Color Palette", "پالت رنگ", IconCategory.DESIGN, "للفنون والتصميم", "Visual design"),
        SlideIconSpec("camera_media", "📷", "عدسة وتصوير", "Photography", "دوربین", IconCategory.DESIGN, "للتصوير والوسائط", "Photography"),
        SlideIconSpec("cinema", "🎬", "إخراج ومشهد", "Cinema Slate", "سینما", IconCategory.DESIGN, "للفيديو والعرض", "Video & storyboarding"),
        SlideIconSpec("mic_cast", "🎙", "بودكاست وصوت", "Studio Mic", "میکروفون", IconCategory.DESIGN, "للتعليق الصوتي", "Audio & voiceover"),
        SlideIconSpec("music_note", "🎵", "نغمة وموسيقى", "Music Note", "موسیقی", IconCategory.DESIGN, "للموسيقى والسمعيات", "Audio tracks"),
        SlideIconSpec("layers_des", "🧩", "تركيب وتناغم", "Puzzle Fit", "پازل", IconCategory.DESIGN, "لتكامل العناصر", "Integration & UX"),
        SlideIconSpec("feather_art", "🪶", "ريشة وأدب", "Quill Feather", "قلم پر", IconCategory.DESIGN, "للأدب والشعر", "Literature & poetry"),
        SlideIconSpec("theater", "🎭", "مسرح وفنون", "Performing Arts", "تئاتر", IconCategory.DESIGN, "للفنون الأدائية", "Drama & arts"),
        SlideIconSpec("magic_wand", "🪄", "تأثير إبداعي", "Magic Effect", "جادو", IconCategory.DESIGN, "للتحسين الإبداعي", "Creative polish"),
        SlideIconSpec("frame_pic", "🖼", "لوحة ومعرض", "Framed Art", "قاب", IconCategory.DESIGN, "لمعارض الصور", "Gallery showcase"),

        // Badges & UI (10)
        SlideIconSpec("star", "★", "نجمة تميز", "Gold Star", "ستاره", IconCategory.BADGES, "لإبراز النقاط المهمة", "Key takeaways & ratings"),
        SlideIconSpec("trophy", "🏆", "كأس إنجاز", "Winner Trophy", "جایزه", IconCategory.BADGES, "للجوائز والنجاحات", "Achievements & awards"),
        SlideIconSpec("check", "✔", "علامة اعتماد", "Verified Check", "تأیید", IconCategory.BADGES, "للمهام المكتملة", "Completed & approved"),
        SlideIconSpec("sparkles", "✦", "لمعان إبداعي", "Creative Spark", "درخشش", IconCategory.BADGES, "للإضافة الذكية", "AI magic & highlights"),
        SlideIconSpec("medal", "🥇", "ميدالية أولى", "First Medal", "مدال", IconCategory.BADGES, "للمراتب المتقدمة", "Top rankings"),
        SlideIconSpec("flag_done", "🚩", "راية محطة", "Milestone Flag", "پرچم", IconCategory.BADGES, "لمراحل المشروع", "Project milestones"),
        SlideIconSpec("pin_loc", "📍", "دبوس موقع", "Location Pin", "نشانگر", IconCategory.BADGES, "لتحديد النقاط", "Location & focus"),
        SlideIconSpec("bell_alert", "🔔", "تنبيه وإشعار", "Alert Bell", "زنگ", IconCategory.BADGES, "للملاحظات المهمة", "Reminders & alerts"),
        SlideIconSpec("fire_hot", "🔥", "اتجاه متصدر", "Trending Flame", "داغ", IconCategory.BADGES, "للمؤشرات الساخنة", "Trending & momentum"),
        SlideIconSpec("infinity", "∞", "استمرارية لا نهائية", "Infinity Loop", "بینهایت", IconCategory.BADGES, "للاستدامة والتكرار", "Continuous cycles")
    )

    fun glyphFor(key: String): String =
        icons.firstOrNull { it.key == key }?.glyph ?: "★"

    fun specFor(key: String): SlideIconSpec =
        icons.firstOrNull { it.key == key } ?: icons.first()
}

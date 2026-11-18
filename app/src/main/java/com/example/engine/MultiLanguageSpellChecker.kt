package com.example.engine

import com.example.core.localization.AppLanguage
import kotlin.math.abs
import kotlin.math.min

data class SpellIssue(
    val word: String,
    val startIndex: Int,
    val endIndex: Int,
    val suggestions: List<String>
)

/**
 * Offline + multi-language presentation spellchecker supporting Arabic, English, Persian,
 * Urdu, Russian, Italian, and Chinese. Detects misspelled tokens using curated academic &
 * productivity lexicons, script rules, and Damerau/Levenshtein suggestion ranking.
 */
object MultiLanguageSpellChecker {

    private val enDictionary = setOf(
        "presentation", "presentations", "slide", "slides", "kateb", "summary", "research",
        "introduction", "conclusion", "objectives", "objective", "analysis", "methodology",
        "results", "discussion", "references", "reference", "university", "academic", "project",
        "business", "strategy", "growth", "market", "revenue", "profit", "chart", "table",
        "data", "design", "layout", "theme", "color", "colors", "template", "templates",
        "background", "export", "import", "video", "image", "images", "text", "content",
        "language", "arabic", "english", "persian", "russian", "italian", "urdu", "chinese",
        "science", "technology", "engineering", "medical", "clinical", "history", "culture",
        "future", "artificial", "intelligence", "neural", "network", "system", "systems",
        "education", "learning", "student", "students", "teacher", "lecture", "chapter",
        "overview", "roadmap", "timeline", "milestone", "milestones", "phase", "deliverable",
        "progress", "quality", "security", "performance", "management", "executive", "report",
        "quarterly", "annual", "financial", "economic", "innovation", "creative", "solution",
        "solutions", "problem", "challenge", "opportunity", "recommendation", "recommendations",
        "welcome", "thank", "thanks", "please", "double", "tap", "edit", "header", "item",
        "comparison", "insight", "insights", "metrics", "balance", "symmetry", "notes", "speaker",
        "title", "subtitle", "section", "pillar", "pillars", "important", "example", "examples",
        "about", "above", "after", "again", "against", "between", "create", "created", "with",
        "from", "this", "that", "these", "those", "which", "where", "when", "what", "should",
        "would", "could", "there", "their", "professional", "modern", "studio", "digital",
        "cloud", "local", "backup", "battery", "saver", "biometric", "fingerprint", "protect"
    )

    private val enTypoMap = mapOf(
        "teh" to listOf("the"),
        "recieve" to listOf("receive"),
        "seperate" to listOf("separate"),
        "occured" to listOf("occurred"),
        "definately" to listOf("definitely"),
        "acheive" to listOf("achieve"),
        "untill" to listOf("until"),
        "adress" to listOf("address"),
        "begining" to listOf("beginning"),
        "calender" to listOf("calendar"),
        "enviroment" to listOf("environment"),
        "goverment" to listOf("government"),
        "knowlege" to listOf("knowledge"),
        "managment" to listOf("management"),
        "neccessary" to listOf("necessary"),
        "occurence" to listOf("occurrence"),
        "persistant" to listOf("persistent"),
        "publically" to listOf("publicly"),
        "refered" to listOf("referred"),
        "reasearch" to listOf("research"),
        "reserch" to listOf("research"),
        "presentaton" to listOf("presentation"),
        "presntation" to listOf("presentation"),
        "sumary" to listOf("summary"),
        "summery" to listOf("summary"),
        "concluson" to listOf("conclusion"),
        "introducton" to listOf("introduction"),
        "methodolgy" to listOf("methodology"),
        "refrences" to listOf("references"),
        "langauge" to listOf("language"),
        "tecnology" to listOf("technology"),
        "analisys" to listOf("analysis"),
        "anlysis" to listOf("analysis"),
        "bussiness" to listOf("business"),
        "strategey" to listOf("strategy"),
        "objectve" to listOf("objective"),
        "desgin" to listOf("design"),
        "layot" to listOf("layout")
    )

    private val arDictionary = setOf(
        "العرض", "التقديمي", "شريحة", "شرائح", "مقدمة", "الخلاصة", "المراجع", "الأهداف",
        "البحث", "العلمي", "المنهجية", "النتائج", "التحليل", "التوصيات", "المشروع",
        "التصميم", "الإدارة", "الاقتصاد", "التقنية", "الذكاء", "الاصطناعي", "التعليم",
        "الجامعة", "الأكاديمي", "الدراسة", "المحتوى", "العنوان", "الملاحظات", "البيانات",
        "المقارنة", "المستقبل", "الابتكار", "التطوير", "النجاح", "الإنجاز", "الجودة",
        "الأمان", "الحماية", "البيئة", "التاريخ", "الحضارة", "الثقافة", "الطب", "الهندسة",
        "القانون", "الحوكمة", "الفكرة", "النقطة", "الرئيسية", "المحور", "المؤشرات",
        "النمو", "الأعمال", "التقرير", "العمل", "تعاون", "مشاركة", "تصدير", "حفظ",
        "شكرا", "لحسن", "استماعكم", "مرحبا", "أفكارك", "تحول", "عروض", "احترافية",
        "مساعد", "قوالب", "تخطيط", "جدول", "مخطط", "صورة", "معادلة", "كتابة"
    )

    private val arTypoMap = mapOf(
        "العرظ" to listOf("العرض"),
        "شريحه" to listOf("شريحة"),
        "شراءح" to listOf("شرائح"),
        "مقدمه" to listOf("مقدمة"),
        "الخلاصه" to listOf("الخلاصة"),
        "المنهجيه" to listOf("المنهجية"),
        "التقنيه" to listOf("التقنية"),
        "الاداره" to listOf("الإدارة"),
        "الادارة" to listOf("الإدارة"),
        "الذكاء" to listOf("الذكاء"),
        "الاصطناعى" to listOf("الاصطناعي"),
        "العلمى" to listOf("العلمي"),
        "الاكاديمى" to listOf("الأكاديمي"),
        "الاکاديمي" to listOf("الأكاديمي"),
        "احترافيه" to listOf("احترافية"),
        "التوصياات" to listOf("التوصيات"),
        "النثائج" to listOf("النتائج"),
        "النناىج" to listOf("النتائج"),
        "البياناات" to listOf("البيانات"),
        "الاهداف" to listOf("الأهداف"),
        "استماعكمم" to listOf("استماعكم"),
        "انشاء" to listOf("إنشاء"),
        "اقتصادى" to listOf("اقتصادي"),
        "مقارنه" to listOf("مقارنة"),
        "فكره" to listOf("فكرة")
    )

    private val faDictionary = setOf(
        "ارائه", "اسلاید", "مقدمه", "جمع‌بندی", "پژوهش", "روش‌شناسی", "نتایج", "تحلیل",
        "منابع", "اهداف", "طراحی", "هوش", "مصنوعی", "فناوری", "دانشگاه", "آموزش",
        "مدیریت", "اقتصاد", "پزشکی", "مهندسی", "حقوق", "ادبیات", "تاریخ", "کسب‌وکار",
        "نمودار", "جدول", "عنوان", "متن", "یادداشت", "سخنران", "ذخیره", "خروجی"
    )

    private val faTypoMap = mapOf(
        "ارایه" to listOf("ارائه"),
        "پژوهشس" to listOf("پژوهش"),
        "مصنوعي" to listOf("مصنوعی"),
        "اسلايد" to listOf("اسلاید"),
        "موفقیتس" to listOf("موفقیت")
    )

    private val urDictionary = setOf(
        "پریزنٹیشن", "سلائیڈ", "سلائیڈز", "تعارف", "خلاصہ", "تحقیق", "نتائج", "تجزیہ",
        "مقاصد", "طریقہ", "حوالہ", "حوالہ جات", "تعلیم", "یونیورسٹی", "ٹیکنالوجی",
        "کاروبار", "معاشیات", "صحت", "انجینئرنگ", "قانون", "تاریخ", "عنوان", "متن",
        "نوٹس", "محفوظ", "فائل", "جدول", "چارٹ", "ترتیب", "شکریہ"
    )

    private val urTypoMap = mapOf(
        "پریزینٹیشن" to listOf("پریزنٹیشن"),
        "سلايڈ" to listOf("سلائیڈ"),
        "تحقیقں" to listOf("تحقیق"),
        "شکریه" to listOf("شکریہ")
    )

    private val ruDictionary = setOf(
        "презентация", "слайд", "слайды", "введение", "заключение", "исследование", "анализ",
        "методология", "результаты", "выводы", "источники", "цели", "задачи", "проект",
        "бизнес", "стратегия", "экономика", "технология", "интеллект", "наука", "данные",
        "график", "таблица", "дизайн", "макет", "текст", "заметки", "докладчик", "спасибо"
    )

    private val ruTypoMap = mapOf(
        "призентация" to listOf("презентация"),
        "слаид" to listOf("слайд"),
        "иследование" to listOf("исследование"),
        "результатты" to listOf("результаты"),
        "ввиденние" to listOf("введение"),
        "спасиба" to listOf("спасибо")
    )

    private val itDictionary = setOf(
        "presentazione", "diapositiva", "diapositive", "introduzione", "conclusione",
        "conclusioni", "ricerca", "analisi", "metodologia", "risultati", "riferimenti",
        "obiettivi", "progetto", "strategia", "economia", "tecnologia", "intelligenza",
        "scienza", "dati", "grafico", "tabella", "design", "testo", "note", "relatore",
        "grazie", "sintesi", "mercato", "crescita", "futuro", "istruzione", "azienda"
    )

    private val itTypoMap = mapOf(
        "presentazzione" to listOf("presentazione"),
        "obbiettivi" to listOf("obiettivi"),
        "introdzione" to listOf("introduzione"),
        "conclussione" to listOf("conclusione"),
        "risultatti" to listOf("risultati"),
        "tecnlogia" to listOf("tecnologia")
    )

    private val zhCorrections = mapOf(
        "演试" to listOf("演示"),
        "幻灯片片" to listOf("幻灯片"),
        "目摽" to listOf("目标"),
        "结伦" to listOf("结论"),
        "分祈" to listOf("分析"),
        "技述" to listOf("技术"),
        "研宄" to listOf("研究"),
        "参巧" to listOf("参考"),
        "数椐" to listOf("数据"),
        "谢射" to listOf("谢谢")
    )

    fun checkText(text: String, language: AppLanguage): List<SpellIssue> {
        if (text.isBlank()) return emptyList()
        val issues = mutableListOf<SpellIssue>()

        if (language == AppLanguage.ZH) {
            zhCorrections.forEach { (typo, fixes) ->
                var searchFrom = 0
                while (true) {
                    val idx = text.indexOf(typo, searchFrom)
                    if (idx < 0) break
                    issues.add(
                        SpellIssue(
                            word = typo,
                            startIndex = idx,
                            endIndex = idx + typo.length,
                            suggestions = fixes
                        )
                    )
                    searchFrom = idx + typo.length
                }
            }
            // Also check embedded Latin words in Chinese slides
            checkWordTokens(text, AppLanguage.EN, issues)
            return issues.sortedBy { it.startIndex }
        }

        checkWordTokens(text, language, issues)
        return issues.distinctBy { it.startIndex }.sortedBy { it.startIndex }
    }

    private fun checkWordTokens(
        text: String,
        language: AppLanguage,
        outIssues: MutableList<SpellIssue>
    ) {
        val tokenRegex = Regex("[\\p{L}\\p{M}]{3,}")
        for (match in tokenRegex.findAll(text)) {
            val raw = match.value
            val detectedLang = inferTokenLanguage(raw, language)
            val dict = dictionaryFor(detectedLang)
            val typos = typoMapFor(detectedLang)
            val lower = raw.lowercase()

            // Explicit known typo match first
            typos[lower]?.let { fixes ->
                outIssues.add(
                    SpellIssue(
                        word = raw,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        suggestions = fixes
                    )
                )
                continue
            }

            if (lower in dict || raw in dict) continue

            // Find fuzzy edit-distance <= 2 candidates (only flag when confident suggestion exists)
            val maxDist = if (lower.length >= 6) 2 else 1
            val candidates = dict
                .asSequence()
                .filter { abs(it.length - lower.length) <= maxDist }
                .map { candidate -> candidate to levenshtein(lower, candidate.lowercase()) }
                .filter { (_, dist) -> dist in 1..maxDist }
                .sortedBy { it.second }
                .map { it.first }
                .distinct()
                .take(3)
                .toList()

            if (candidates.isNotEmpty()) {
                outIssues.add(
                    SpellIssue(
                        word = raw,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        suggestions = candidates
                    )
                )
            }
        }
    }

    private fun inferTokenLanguage(token: String, fallback: AppLanguage): AppLanguage {
        val first = token.firstOrNull() ?: return fallback
        return when {
            first in '\u0400'..'\u04FF' -> AppLanguage.RU
            first in '\u0600'..'\u06FF' -> when (fallback) {
                AppLanguage.FA, AppLanguage.UR, AppLanguage.AR -> fallback
                else -> AppLanguage.AR
            }
            first in 'a'..'z' || first in 'A'..'Z' -> {
                if (fallback == AppLanguage.IT) AppLanguage.IT else AppLanguage.EN
            }
            else -> fallback
        }
    }

    private fun dictionaryFor(lang: AppLanguage): Set<String> = when (lang) {
        AppLanguage.AR -> arDictionary
        AppLanguage.FA -> faDictionary + arDictionary
        AppLanguage.UR -> urDictionary + arDictionary
        AppLanguage.RU -> ruDictionary
        AppLanguage.IT -> itDictionary
        AppLanguage.ZH, AppLanguage.EN -> enDictionary
    }

    private fun typoMapFor(lang: AppLanguage): Map<String, List<String>> = when (lang) {
        AppLanguage.AR -> arTypoMap
        AppLanguage.FA -> faTypoMap + arTypoMap
        AppLanguage.UR -> urTypoMap
        AppLanguage.RU -> ruTypoMap
        AppLanguage.IT -> itTypoMap
        AppLanguage.ZH, AppLanguage.EN -> enTypoMap
    }

    fun applyFix(sourceText: String, issue: SpellIssue, replacement: String): String {
        if (issue.startIndex < 0 || issue.endIndex > sourceText.length || issue.startIndex >= issue.endIndex) {
            return sourceText.replaceFirst(issue.word, replacement)
        }
        return buildString {
            append(sourceText.substring(0, issue.startIndex))
            append(replacement)
            append(sourceText.substring(issue.endIndex))
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            System.arraycopy(curr, 0, prev, 0, b.length + 1)
        }
        return prev[b.length]
    }
}

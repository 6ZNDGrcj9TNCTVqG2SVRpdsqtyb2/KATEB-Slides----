package com.example.engine

import com.example.core.localization.AppLanguage
import org.json.JSONArray
import org.json.JSONObject

object PresentationJsonSerializer {

    fun toJsonString(presentation: Presentation): String {
        val root = JSONObject().apply {
            put("id", presentation.id)
            put("title", presentation.title)
            put("description", presentation.description)
            put("language", presentation.language.code)
            put("aspectRatio", presentation.aspectRatio.id)
            put("canvasWidth", presentation.canvasWidth.toDouble())
            put("canvasHeight", presentation.canvasHeight.toDouble())
            put("themeId", presentation.themeId)
            put("sourceType", presentation.sourceType.name)
            put("createdAt", presentation.createdAt)
            put("updatedAt", presentation.updatedAt)
            put("slides", JSONArray().apply {
                presentation.slides.forEach { put(slideToJson(it)) }
            })
        }
        return root.toString(2)
    }

    fun fromJsonString(jsonString: String): Presentation {
        val root = JSONObject(jsonString)
        val slidesArray = root.optJSONArray("slides") ?: JSONArray()
        val slides = (0 until slidesArray.length()).map { idx ->
            jsonToSlide(slidesArray.getJSONObject(idx), idx)
        }
        val aspectRatio = SlideAspectRatio.fromId(root.optString("aspectRatio", "16:9"))
        return Presentation(
            id = root.getString("id"),
            title = root.optString("title", "Untitled"),
            description = root.optString("description", ""),
            language = AppLanguage.fromCode(root.optString("language", "ar")),
            aspectRatio = aspectRatio,
            canvasWidth = root.optDouble("canvasWidth", aspectRatio.defaultWidth.toDouble()).toFloat(),
            canvasHeight = root.optDouble("canvasHeight", aspectRatio.defaultHeight.toDouble()).toFloat(),
            themeId = root.optString("themeId", "kateb_academic"),
            sourceType = runCatching {
                PresentationSourceType.valueOf(root.optString("sourceType", "BLANK"))
            }.getOrDefault(PresentationSourceType.BLANK),
            slides = slides,
            createdAt = root.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun slideToJson(slide: Slide): JSONObject = JSONObject().apply {
        put("id", slide.id)
        put("order", slide.order)
        put("speakerNotes", slide.speakerNotes)
        put("autoAdvanceSeconds", slide.autoAdvanceSeconds)
        put("decorationId", slide.decorationId)
        put("decorationColorHex", slide.decorationColorHex)
        put("background", backgroundToJson(slide.background))
        put("transition", JSONObject().apply {
            put("type", slide.transition.type.name)
            put("durationMs", slide.transition.durationMs)
        })
        put("elements", JSONArray().apply {
            slide.elements.forEach { put(elementToJson(it)) }
        })
    }

    private fun jsonToSlide(obj: JSONObject, fallbackOrder: Int): Slide {
        val bgObj = obj.optJSONObject("background")
        val background = if (bgObj != null) jsonToBackground(bgObj) else SlideBackground.Solid("#FFFFFF")
        val trObj = obj.optJSONObject("transition")
        val transition = SlideTransition(
            type = runCatching {
                SlideTransitionType.valueOf(trObj?.optString("type", "FADE") ?: "FADE")
            }.getOrDefault(SlideTransitionType.FADE),
            durationMs = trObj?.optInt("durationMs", 450) ?: 450
        )
        val elementsArray = obj.optJSONArray("elements") ?: JSONArray()
        val elements = (0 until elementsArray.length()).mapNotNull { i ->
            runCatching { jsonToElement(elementsArray.getJSONObject(i)) }.getOrNull()
        }
        return Slide(
            id = obj.getString("id"),
            order = obj.optInt("order", fallbackOrder),
            background = background,
            transition = transition,
            speakerNotes = obj.optString("speakerNotes", ""),
            elements = elements,
            autoAdvanceSeconds = obj.optInt("autoAdvanceSeconds", 5),
            decorationId = obj.optString("decorationId", "none"),
            decorationColorHex = obj.optString("decorationColorHex", "#1565C0")
        )
    }

    private fun backgroundToJson(bg: SlideBackground): JSONObject = when (bg) {
        is SlideBackground.Solid -> JSONObject().apply {
            put("kind", "solid")
            put("colorHex", bg.colorHex)
        }
        is SlideBackground.Gradient -> JSONObject().apply {
            put("kind", "gradient")
            put("startColorHex", bg.startColorHex)
            put("endColorHex", bg.endColorHex)
            put("angleDegrees", bg.angleDegrees.toDouble())
        }
        is SlideBackground.Image -> JSONObject().apply {
            put("kind", "image")
            put("localImagePath", bg.localImagePath)
            put("overlayColorHex", bg.overlayColorHex)
        }
    }

    private fun jsonToBackground(obj: JSONObject): SlideBackground = when (obj.optString("kind", "solid")) {
        "gradient" -> SlideBackground.Gradient(
            startColorHex = obj.optString("startColorHex", "#0D47A1"),
            endColorHex = obj.optString("endColorHex", "#1976D2"),
            angleDegrees = obj.optDouble("angleDegrees", 135.0).toFloat()
        )
        "image" -> SlideBackground.Image(
            localImagePath = obj.optString("localImagePath", ""),
            overlayColorHex = obj.optString("overlayColorHex", "#33000000")
        )
        else -> SlideBackground.Solid(obj.optString("colorHex", "#FFFFFF"))
    }

    private fun animationToJson(anim: ElementAnimation): JSONObject = JSONObject().apply {
        put("type", anim.type.name)
        put("exitType", anim.exitType.name)
        put("direction", anim.direction.name)
        put("motionRange", anim.motionRange.name)
        put("durationMs", anim.durationMs)
        put("delayMs", anim.delayMs)
        put("orderIndex", anim.orderIndex)
    }

    private fun jsonToAnimation(obj: JSONObject?): ElementAnimation {
        if (obj == null) return ElementAnimation()
        return ElementAnimation(
            type = runCatching { ElementAnimationType.valueOf(obj.optString("type", "NONE")) }
                .getOrDefault(ElementAnimationType.NONE),
            exitType = runCatching { ElementAnimationType.valueOf(obj.optString("exitType", "NONE")) }
                .getOrDefault(ElementAnimationType.NONE),
            direction = runCatching { AnimationDirection.valueOf(obj.optString("direction", "FROM_BOTTOM")) }
                .getOrDefault(AnimationDirection.FROM_BOTTOM),
            motionRange = runCatching { MotionRange.valueOf(obj.optString("motionRange", "NORMAL")) }
                .getOrDefault(MotionRange.NORMAL),
            durationMs = obj.optInt("durationMs", 500),
            delayMs = obj.optInt("delayMs", 0),
            orderIndex = obj.optInt("orderIndex", 0)
        )
    }

    private fun elementToJson(el: SlideElement): JSONObject = JSONObject().apply {
        put("id", el.id)
        put("x", el.x.toDouble())
        put("y", el.y.toDouble())
        put("width", el.width.toDouble())
        put("height", el.height.toDouble())
        put("rotation", el.rotation.toDouble())
        put("zIndex", el.zIndex)
        put("opacity", el.opacity.toDouble())
        put("groupId", el.groupId ?: JSONObject.NULL)
        put("animation", animationToJson(el.animation))

        when (el) {
            is SlideElement.TextElement -> {
                put("type", "text")
                put("text", el.text)
                put("fontFamilyId", el.fontFamilyId)
                put("fontSizeSp", el.fontSizeSp.toDouble())
                put("textColorHex", el.textColorHex)
                put("backgroundColorHex", el.backgroundColorHex)
                put("highlightColorHex", el.highlightColorHex)
                put("isBold", el.isBold)
                put("isItalic", el.isItalic)
                put("isUnderline", el.isUnderline)
                put("isStrikethrough", el.isStrikethrough)
                put("textAlign", el.textAlign.name)
                put("verticalAlign", el.verticalAlign.name)
                put("writingDirection", el.writingDirection.name)
                put("lineHeightMultiplier", el.lineHeightMultiplier.toDouble())
                put("letterSpacing", el.letterSpacing.toDouble())
                put("listType", el.listType.name)
                put("isBlockQuote", el.isBlockQuote)
                put("linkUrl", el.linkUrl)
                put("borderColorHex", el.borderColorHex)
                put("borderWidth", el.borderWidth.toDouble())
                put("hasShadow", el.hasShadow)
            }
            is SlideElement.ImageElement -> {
                put("type", "image")
                put("localImagePath", el.localImagePath)
                put("fillCropMode", el.fillCropMode)
                put("cropRect", JSONObject().apply {
                    put("left", el.cropRect.left.toDouble())
                    put("top", el.cropRect.top.toDouble())
                    put("right", el.cropRect.right.toDouble())
                    put("bottom", el.cropRect.bottom.toDouble())
                })
                put("borderColorHex", el.borderColorHex)
                put("borderWidth", el.borderWidth.toDouble())
                put("cornerRadius", el.cornerRadius.toDouble())
                put("hasShadow", el.hasShadow)
            }
            is SlideElement.ShapeElement -> {
                put("type", "shape")
                put("shapeType", el.shapeType.name)
                put("fillColorHex", el.fillColorHex)
                put("strokeColorHex", el.strokeColorHex)
                put("strokeWidth", el.strokeWidth.toDouble())
                put("hasShadow", el.hasShadow)
                put("labelText", el.labelText)
                put("labelColorHex", el.labelColorHex)
                put("is3D", el.is3D)
                put("depth3D", el.depth3D.toDouble())
                put("extrudeColorHex", el.extrudeColorHex)
                put("lightAngle3D", el.lightAngle3D.toDouble())
            }
            is SlideElement.SmartArtElement -> {
                put("type", "smartart")
                put("layoutType", el.layoutType.name)
                put("fontFamilyId", el.fontFamilyId)
                put("is3D", el.is3D)
                put("connectorColorHex", el.connectorColorHex)
                put("nodes", JSONArray().apply {
                    el.nodes.forEach { n ->
                        put(JSONObject().apply {
                            put("id", n.id)
                            put("text", n.text)
                            put("fillColorHex", n.fillColorHex)
                            put("textColorHex", n.textColorHex)
                        })
                    }
                })
            }
            is SlideElement.TableElement -> {
                put("type", "table")
                put("rows", el.rows)
                put("cols", el.cols)
                put("borderColorHex", el.borderColorHex)
                put("borderWidth", el.borderWidth.toDouble())
                put("fontSizeSp", el.fontSizeSp.toDouble())
                put("cells", JSONArray().apply {
                    el.cells.forEach { cell ->
                        put(JSONObject().apply {
                            put("row", cell.row)
                            put("col", cell.col)
                            put("text", cell.text)
                            put("bgColorHex", cell.bgColorHex)
                            put("textColorHex", cell.textColorHex)
                            put("isHeader", cell.isHeader)
                            put("isBold", cell.isBold)
                            put("rowSpan", cell.rowSpan)
                            put("colSpan", cell.colSpan)
                            put("mergedIntoRow", cell.mergedIntoRow ?: JSONObject.NULL)
                            put("mergedIntoCol", cell.mergedIntoCol ?: JSONObject.NULL)
                        })
                    }
                })
            }
            is SlideElement.ChartElement -> {
                put("type", "chart")
                put("chartType", el.chartType.name)
                put("title", el.title)
                put("showLegend", el.showLegend)
                put("showValues", el.showValues)
                put("bgColorHex", el.bgColorHex)
                put("labels", JSONArray(el.labels))
                put("series", JSONArray().apply {
                    el.series.forEach { s ->
                        put(JSONObject().apply {
                            put("name", s.name)
                            put("colorHex", s.colorHex)
                            put("values", JSONArray(s.values.map { it.toDouble() }))
                        })
                    }
                })
            }
            is SlideElement.EquationElement -> {
                put("type", "equation")
                put("expression", el.expression)
                put("fontSizeSp", el.fontSizeSp.toDouble())
                put("colorHex", el.colorHex)
                put("bgColorHex", el.bgColorHex)
            }
            is SlideElement.IconElement -> {
                put("type", "icon")
                put("symbolKey", el.symbolKey)
                put("tintColorHex", el.tintColorHex)
                put("bgColorHex", el.bgColorHex)
            }
        }
    }

    private fun jsonToElement(obj: JSONObject): SlideElement {
        val id = obj.getString("id")
        val x = obj.optDouble("x", 100.0).toFloat()
        val y = obj.optDouble("y", 100.0).toFloat()
        val w = obj.optDouble("width", 400.0).toFloat()
        val h = obj.optDouble("height", 200.0).toFloat()
        val rot = obj.optDouble("rotation", 0.0).toFloat()
        val z = obj.optInt("zIndex", 1)
        val opacity = obj.optDouble("opacity", 1.0).toFloat()
        val groupId = if (obj.isNull("groupId")) null else obj.optString("groupId", "").ifBlank { null }
        val anim = jsonToAnimation(obj.optJSONObject("animation"))

        return when (obj.getString("type")) {
            "text" -> SlideElement.TextElement(
                id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                opacity = opacity, groupId = groupId, animation = anim,
                text = obj.optString("text", ""),
                fontFamilyId = obj.optString("fontFamilyId", "cairo"),
                fontSizeSp = obj.optDouble("fontSizeSp", 36.0).toFloat(),
                textColorHex = obj.optString("textColorHex", "#0F172A"),
                backgroundColorHex = obj.optString("backgroundColorHex", "#00000000"),
                highlightColorHex = obj.optString("highlightColorHex", "#00000000"),
                isBold = obj.optBoolean("isBold", false),
                isItalic = obj.optBoolean("isItalic", false),
                isUnderline = obj.optBoolean("isUnderline", false),
                isStrikethrough = obj.optBoolean("isStrikethrough", false),
                textAlign = runCatching { TextAlignment.valueOf(obj.optString("textAlign", "START")) }
                    .getOrDefault(TextAlignment.START),
                verticalAlign = runCatching { VerticalAlignment.valueOf(obj.optString("verticalAlign", "TOP")) }
                    .getOrDefault(VerticalAlignment.TOP),
                writingDirection = runCatching { WritingDirection.valueOf(obj.optString("writingDirection", "AUTO")) }
                    .getOrDefault(WritingDirection.AUTO),
                lineHeightMultiplier = obj.optDouble("lineHeightMultiplier", 1.25).toFloat(),
                letterSpacing = obj.optDouble("letterSpacing", 0.0).toFloat(),
                listType = runCatching { TextListType.valueOf(obj.optString("listType", "NONE")) }
                    .getOrDefault(TextListType.NONE),
                isBlockQuote = obj.optBoolean("isBlockQuote", false),
                linkUrl = obj.optString("linkUrl", ""),
                borderColorHex = obj.optString("borderColorHex", "#00000000"),
                borderWidth = obj.optDouble("borderWidth", 0.0).toFloat(),
                hasShadow = obj.optBoolean("hasShadow", false)
            )
            "image" -> {
                val cropObj = obj.optJSONObject("cropRect")
                val cropRect = if (cropObj != null) {
                    CropRect(
                        left = cropObj.optDouble("left", 0.0).toFloat(),
                        top = cropObj.optDouble("top", 0.0).toFloat(),
                        right = cropObj.optDouble("right", 1.0).toFloat(),
                        bottom = cropObj.optDouble("bottom", 1.0).toFloat()
                    )
                } else CropRect()
                SlideElement.ImageElement(
                    id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                    opacity = opacity, groupId = groupId, animation = anim,
                    localImagePath = obj.optString("localImagePath", ""),
                    fillCropMode = obj.optBoolean("fillCropMode", true),
                    cropRect = cropRect,
                    borderColorHex = obj.optString("borderColorHex", "#00000000"),
                    borderWidth = obj.optDouble("borderWidth", 0.0).toFloat(),
                    cornerRadius = obj.optDouble("cornerRadius", 16.0).toFloat(),
                    hasShadow = obj.optBoolean("hasShadow", false)
                )
            }
            "shape" -> SlideElement.ShapeElement(
                id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                opacity = opacity, groupId = groupId, animation = anim,
                shapeType = runCatching { ShapeType.valueOf(obj.optString("shapeType", "ROUNDED_RECT")) }
                    .getOrDefault(ShapeType.ROUNDED_RECT),
                fillColorHex = obj.optString("fillColorHex", "#1565C0"),
                strokeColorHex = obj.optString("strokeColorHex", "#0D47A1"),
                strokeWidth = obj.optDouble("strokeWidth", 4.0).toFloat(),
                hasShadow = obj.optBoolean("hasShadow", false),
                labelText = obj.optString("labelText", ""),
                labelColorHex = obj.optString("labelColorHex", "#FFFFFF"),
                is3D = obj.optBoolean("is3D", false),
                depth3D = obj.optDouble("depth3D", 24.0).toFloat(),
                extrudeColorHex = obj.optString("extrudeColorHex", "#0A2E66"),
                lightAngle3D = obj.optDouble("lightAngle3D", 45.0).toFloat()
            )
            "smartart" -> {
                val layout = runCatching {
                    SmartArtLayoutType.valueOf(obj.optString("layoutType", "PROCESS_ARROWS"))
                }.getOrDefault(SmartArtLayoutType.PROCESS_ARROWS)
                val nodesArr = obj.optJSONArray("nodes") ?: JSONArray()
                val parsedNodes = (0 until nodesArr.length()).map { idx ->
                    val n = nodesArr.getJSONObject(idx)
                    SmartArtNode(
                        id = n.optString("id", java.util.UUID.randomUUID().toString()),
                        text = n.optString("text", ""),
                        fillColorHex = n.optString("fillColorHex", "#1565C0"),
                        textColorHex = n.optString("textColorHex", "#FFFFFF")
                    )
                }.ifEmpty { defaultSmartArtNodes(layout) }
                SlideElement.SmartArtElement(
                    id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                    opacity = opacity, groupId = groupId, animation = anim,
                    layoutType = layout,
                    nodes = parsedNodes,
                    fontFamilyId = obj.optString("fontFamilyId", "cairo"),
                    is3D = obj.optBoolean("is3D", true),
                    connectorColorHex = obj.optString("connectorColorHex", "#64748B")
                )
            }
            "table" -> {
                val cellsArr = obj.optJSONArray("cells") ?: JSONArray()
                val cells = (0 until cellsArr.length()).map { idx ->
                    val c = cellsArr.getJSONObject(idx)
                    TableCell(
                        row = c.getInt("row"),
                        col = c.getInt("col"),
                        text = c.optString("text", ""),
                        bgColorHex = c.optString("bgColorHex", "#FFFFFF"),
                        textColorHex = c.optString("textColorHex", "#0F172A"),
                        isHeader = c.optBoolean("isHeader", false),
                        isBold = c.optBoolean("isBold", false),
                        rowSpan = c.optInt("rowSpan", 1),
                        colSpan = c.optInt("colSpan", 1),
                        mergedIntoRow = if (c.isNull("mergedIntoRow")) null else c.optInt("mergedIntoRow"),
                        mergedIntoCol = if (c.isNull("mergedIntoCol")) null else c.optInt("mergedIntoCol")
                    )
                }
                SlideElement.TableElement(
                    id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                    opacity = opacity, groupId = groupId, animation = anim,
                    rows = obj.optInt("rows", 3),
                    cols = obj.optInt("cols", 3),
                    cells = cells,
                    borderColorHex = obj.optString("borderColorHex", "#CBD5E1"),
                    borderWidth = obj.optDouble("borderWidth", 2.0).toFloat(),
                    fontSizeSp = obj.optDouble("fontSizeSp", 24.0).toFloat()
                )
            }
            "chart" -> {
                val labelsArr = obj.optJSONArray("labels") ?: JSONArray()
                val labels = (0 until labelsArr.length()).map { labelsArr.getString(it) }
                val seriesArr = obj.optJSONArray("series") ?: JSONArray()
                val series = (0 until seriesArr.length()).map { idx ->
                    val s = seriesArr.getJSONObject(idx)
                    val valsArr = s.optJSONArray("values") ?: JSONArray()
                    val vals = (0 until valsArr.length()).map { valsArr.getDouble(it).toFloat() }
                    ChartSeries(
                        name = s.optString("name", "Series"),
                        colorHex = s.optString("colorHex", "#1565C0"),
                        values = vals
                    )
                }
                SlideElement.ChartElement(
                    id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                    opacity = opacity, groupId = groupId, animation = anim,
                    chartType = runCatching { ChartType.valueOf(obj.optString("chartType", "BAR")) }
                        .getOrDefault(ChartType.BAR),
                    title = obj.optString("title", "Chart"),
                    labels = labels,
                    series = series,
                    showLegend = obj.optBoolean("showLegend", true),
                    showValues = obj.optBoolean("showValues", true),
                    bgColorHex = obj.optString("bgColorHex", "#FFFFFF")
                )
            }
            "equation" -> SlideElement.EquationElement(
                id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                opacity = opacity, groupId = groupId, animation = anim,
                expression = obj.optString("expression", "E = mc^2"),
                fontSizeSp = obj.optDouble("fontSizeSp", 44.0).toFloat(),
                colorHex = obj.optString("colorHex", "#0D47A1"),
                bgColorHex = obj.optString("bgColorHex", "#F8FAFC")
            )
            "icon" -> SlideElement.IconElement(
                id = id, x = x, y = y, width = w, height = h, rotation = rot, zIndex = z,
                opacity = opacity, groupId = groupId, animation = anim,
                symbolKey = obj.optString("symbolKey", "science"),
                tintColorHex = obj.optString("tintColorHex", "#0D47A1"),
                bgColorHex = obj.optString("bgColorHex", "#1ADCE8FF")
            )
            else -> throw IllegalArgumentException("Unknown element type")
        }
    }
}

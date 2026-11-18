package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.engine.Presentation
import com.example.engine.SlideTransitionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Exports a self-contained, offline-capable interactive HTML5 presentation bundle compatible with
 * Apple Safari on all iOS & iPadOS versions, macOS, and modern mobile/desktop browsers.
 * Supports touch swipe navigation, keyboard arrows, CSS slide transitions matching SlideTransitionType,
 * progress bar, fullscreen toggle, and collapsible speaker notes overlay.
 */
object Html5ExportEngine {

    suspend fun exportToHtml5(
        context: Context,
        presentation: Presentation,
        outputFile: File,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        outputFile.parentFile?.mkdirs()
        if (presentation.slides.isEmpty()) {
            throw IllegalStateException("empty_presentation_slides")
        }

        val w = presentation.canvasWidth.toInt().coerceIn(640, 1440)
        val h = presentation.canvasHeight.toInt().coerceIn(400, 1440)
        val total = presentation.slides.size.coerceAtLeast(1)

        val slidesHtml = StringBuilder()
        presentation.slides.forEachIndexed { idx, slide ->
            val bmp = SlideBitmapRenderer.renderSlideToBitmap(context, presentation, slide, w, h)
            val b64 = ByteArrayOutputStream().use { baos ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 88, baos)
                bmp.recycle()
                Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            }
            val transitionClass = cssTransitionClass(slide.transition.type)
            val escapedNotes = escapeHtml(slide.speakerNotes)
            val activeAttr = if (idx == 0) " active" else ""
            slidesHtml.appendLine(
                """
                <section class="slide$activeAttr $transitionClass" data-index="$idx" data-notes="$escapedNotes" data-duration="${slide.transition.durationMs}">
                  <img src="data:image/jpeg;base64,$b64" alt="Slide ${idx + 1}" draggable="false"/>
                </section>
                """.trimIndent()
            )
            onProgress(0.1f + ((idx + 1).toFloat() / total.toFloat()) * 0.8f)
        }

        val dir = if (presentation.language.isRtl) "rtl" else "ltr"
        val escapedTitle = escapeHtml(presentation.title)
        val html = """<!DOCTYPE html>
<html lang="${presentation.language.code}" dir="$dir">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover"/>
<meta name="apple-mobile-web-app-capable" content="yes"/>
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"/>
<title>$escapedTitle</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
  html, body { width: 100%; height: 100%; background: #0B1120; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; overflow: hidden; }
  .stage { position: relative; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
  .slide { position: absolute; inset: 12px 12px 68px 12px; display: flex; align-items: center; justify-content: center; opacity: 0; pointer-events: none; transition: opacity 0.45s ease, transform 0.45s cubic-bezier(0.22,1,0.36,1), filter 0.45s ease; }
  .slide.active { opacity: 1; pointer-events: auto; transform: translate3d(0,0,0) scale(1); filter: blur(0); }
  .slide.tr-stealth:not(.active) { opacity: 0; transform: scale(0.99); }
  .slide.tr-exciting:not(.active) { opacity: 0; transform: scale(1.12) rotate(-1.5deg); }
  .slide.tr-dynamic:not(.active) { opacity: 0; transform: translate3d(60px, 0, 0); }
  .slide.tr-zoom:not(.active) { opacity: 0; transform: scale(0.84); }
  .slide.tr-up:not(.active) { opacity: 0; transform: translate3d(0, 48px, 0); }
  .slide img { max-width: 100%; max-height: 100%; border-radius: 14px; box-shadow: 0 18px 48px rgba(0,0,0,0.55); object-fit: contain; }
  .hud { position: fixed; left: 0; right: 0; bottom: 0; height: 56px; padding: 0 16px env(safe-area-inset-bottom); background: rgba(15, 23, 42, 0.92); -webkit-backdrop-filter: blur(12px); backdrop-filter: blur(12px); display: flex; align-items: center; justify-content: space-between; gap: 10px; border-top: 1px solid rgba(148,163,184,0.2); z-index: 20; }
  .btn { background: #1E293B; color: #F8FAFC; border: 1px solid rgba(148,163,184,0.3); border-radius: 10px; padding: 8px 14px; font-size: 14px; font-weight: 600; cursor: pointer; }
  .btn:active { background: #2563EB; }
  .counter { font-size: 14px; font-weight: 700; color: #CBD5E1; }
  .progress { position: fixed; top: 0; left: 0; height: 3px; background: #38BDF8; transition: width 0.25s ease; z-index: 30; }
  .notes-drawer { position: fixed; left: 16px; right: 16px; bottom: 68px; max-height: 36vh; overflow-y: auto; background: rgba(15, 23, 42, 0.94); border: 1px solid rgba(56,189,248,0.45); border-radius: 14px; padding: 14px 18px; display: none; z-index: 25; line-height: 1.55; }
  .notes-drawer.open { display: block; }
</style>
</head>
<body>
<div class="progress" id="progressBar"></div>
<main class="stage" id="stage">
$slidesHtml
</main>
<aside class="notes-drawer" id="notesBox"></aside>
<footer class="hud">
  <button class="btn" id="prevBtn">&#9664;</button>
  <span class="counter" id="counter">1 / $total</span>
  <button class="btn" id="notesBtn">Notes</button>
  <button class="btn" id="nextBtn">&#9654;</button>
</footer>
<script>
(function() {
  var slides = Array.prototype.slice.call(document.querySelectorAll('.slide'));
  var idx = 0;
  var counter = document.getElementById('counter');
  var notesBox = document.getElementById('notesBox');
  var progressBar = document.getElementById('progressBar');
  function show(n) {
    if (!slides.length) return;
    idx = (n + slides.length) % slides.length;
    slides.forEach(function(s, i) { s.classList.toggle('active', i === idx); });
    counter.textContent = (idx + 1) + ' / ' + slides.length;
    progressBar.style.width = (((idx + 1) / slides.length) * 100) + '%';
    var note = slides[idx].getAttribute('data-notes') || '';
    notesBox.textContent = note || '—';
  }
  document.getElementById('prevBtn').onclick = function() { show(idx - 1); };
  document.getElementById('nextBtn').onclick = function() { show(idx + 1); };
  document.getElementById('notesBtn').onclick = function() { notesBox.classList.toggle('open'); };
  document.addEventListener('keydown', function(e) {
    if (e.key === 'ArrowRight' || e.key === ' ') show(idx + 1);
    if (e.key === 'ArrowLeft') show(idx - 1);
  });
  var touchX = null;
  document.getElementById('stage').addEventListener('touchstart', function(e) {
    if (e.changedTouches && e.changedTouches.length) touchX = e.changedTouches[0].clientX;
  }, { passive: true });
  document.getElementById('stage').addEventListener('touchend', function(e) {
    if (touchX === null || !e.changedTouches.length) return;
    var dx = e.changedTouches[0].clientX - touchX;
    if (Math.abs(dx) > 40) show(dx < 0 ? idx + 1 : idx - 1);
    touchX = null;
  }, { passive: true });
  show(0);
})();
</script>
</body>
</html>"""

        outputFile.writeText(html, Charsets.UTF_8)
        onProgress(1f)
        outputFile
    }

    private fun cssTransitionClass(type: SlideTransitionType): String = when (type) {
        SlideTransitionType.STEALTH -> "tr-stealth"
        SlideTransitionType.EXCITING, SlideTransitionType.FLIP -> "tr-exciting"
        SlideTransitionType.DYNAMIC, SlideTransitionType.PUSH, SlideTransitionType.MORPH -> "tr-dynamic"
        SlideTransitionType.ZOOM, SlideTransitionType.SCIENTIFIC -> "tr-zoom"
        SlideTransitionType.SLIDE_UP, SlideTransitionType.SLIDE_DOWN, SlideTransitionType.ACADEMIC -> "tr-up"
        else -> "tr-stealth"
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}

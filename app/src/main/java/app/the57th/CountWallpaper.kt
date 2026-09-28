package app.the57th

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.SurfaceHolder

/**
 * Live wallpaper: plain paper with the count set below the middle, so it sits under
 * the lock screen's real clock rather than on top of it.
 */
class CountWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine = CountEngine()

    private inner class CountEngine : Engine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private val handler = Handler(Looper.getMainLooper())
        private val redraw = Runnable { draw() }
        private var visible = false

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            AnchorStore.prefs(this@CountWallpaper).registerOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            AnchorStore.prefs(this@CountWallpaper).unregisterOnSharedPreferenceChangeListener(this)
            handler.removeCallbacks(redraw)
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) draw() else handler.removeCallbacks(redraw)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(redraw)
            super.onSurfaceDestroyed(holder)
        }

        // Anchor or wording changed in the app.
        override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
            if (visible) draw()
        }

        private fun draw() {
            handler.removeCallbacks(redraw)
            val holder = surfaceHolder
            val canvas = runCatching { holder.lockCanvas() }.getOrNull() ?: return
            try {
                val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
                val paper = if (night) Color.rgb(0x14, 0x13, 0x11) else Color.rgb(0xF7, 0xF3, 0xEA)
                val ink = if (night) Color.rgb(0xED, 0xE8, 0xDC) else Color.rgb(0x1B, 0x1B, 0x1B)
                val muted = if (night) Color.rgb(0x9A, 0x95, 0x8A) else Color.rgb(0x6E, 0x6A, 0x62)

                val w = canvas.width
                val h = canvas.height
                canvas.drawColor(paper)

                val textWidth = (w * 0.84f).toInt()
                val count = layout(AnchorStore.todayText(this@CountWallpaper), w * 0.085f, ink, bold = true, textWidth)
                val caption = layout(getString(R.string.today_is), w * 0.04f, muted, bold = false, textWidth)

                val gap = w * 0.02f
                val top = h * 0.62f - (caption.height + gap + count.height) / 2f
                canvas.save()
                canvas.translate((w - textWidth) / 2f, top)
                caption.draw(canvas)
                canvas.translate(0f, caption.height + gap)
                count.draw(canvas)
                canvas.restore()
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
            // Roll over just after midnight while we're on screen; becoming visible redraws too.
            if (visible) {
                handler.postDelayed(redraw, Today.nextMidnightMillis() - System.currentTimeMillis() + 1_000L)
            }
        }

        private fun layout(text: String, size: Float, color: Int, bold: Boolean, width: Int): StaticLayout {
            val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
                textSize = size
                this.color = color
                typeface = Typeface.create(Typeface.SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
            }
            return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .build()
        }
    }
}

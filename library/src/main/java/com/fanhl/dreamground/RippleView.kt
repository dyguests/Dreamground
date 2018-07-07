package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView

/**
 * 波纹
 *
 * https://blog.csdn.net/Holmofy/article/details/66583879
 *
 * @author fanhl
 */
class RippleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr) {
    private var renderThread: RenderThread? = null

    private val paint = Paint()

    // ------------------------------------------ Input ------------------------------------------

    private val backgroundColor: Int


    var xx = 0.0f
    var yy = 0.0f
    var speedX = 5.0f
    var speedY = 3.0f

    init {
        paint.color = -0xff0100

        backgroundColor = Color.WHITE

        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                renderThread = RenderThread(surface ?: return, ::updateSurface)
                renderThread?.start()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                renderThread?.stopRendering()
                renderThread = null
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }
        }
    }

    /**
     * 刷新Surface
     */
    private fun updateSurface(surface: Surface) {
        val canvas = surface.lockCanvas(null)
        try {
            if (canvas != null) {
                updateCanvas(canvas)
            }
        } finally {
            surface.unlockCanvasAndPost(canvas)
        }
    }

    /**
     * 刷新Canvas
     */
    private fun updateCanvas(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        canvas.drawRect(xx, yy, xx + 20.0f, yy + 20.0f, paint)

        if (xx + 20.0f + speedX >= canvas.width || xx + speedX <= 0.0f) {
            speedX = -speedX
        }
        if (yy + 20.0f + speedY >= canvas.height || yy + speedY <= 0.0f) {
            speedY = -speedY
        }

        xx += speedX
        yy += speedY

        canvas.drawCircle(100f, 100f, 100f, paint)
    }

    companion object {
        /**刷新间隔*/
        private const val REFRESH_INTERVAL = 20L
    }

    /**
     * 单独的绘制线程
     */
    internal class RenderThread(surfaceTexture: SurfaceTexture, private val updateSurface: (Surface) -> Unit) : Thread() {
        private val surface = Surface(surfaceTexture)

        @Volatile
        private var running = true

        override fun run() {
            while (running && !Thread.interrupted()) {
                updateSurface(surface)
                try {
                    sleep(REFRESH_INTERVAL)
                } catch (e: InterruptedException) {
                }
            }
        }

        fun stopRendering() {
            interrupt()
            running = false
        }
    }
}
package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView

abstract class BaseTextureView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr) {
    private var renderThread: RenderThread? = null

    init {
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

    abstract fun updateCanvas(canvas: Canvas)

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
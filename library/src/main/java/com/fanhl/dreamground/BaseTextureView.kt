package com.fanhl.dreamground

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView

abstract class BaseTextureView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr) {
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
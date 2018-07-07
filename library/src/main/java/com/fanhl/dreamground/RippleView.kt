package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.support.annotation.FloatRange
import android.support.v4.util.Pools
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import java.util.*
import kotlin.collections.ArrayList

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

    private val random = Random()

    /** 缓存所有ripple */
    private val ripples = ArrayList<Trace>()

    // ------------------------------------------ Input ------------------------------------------

    private val refreshInterval: Long

    private val backgroundColor: Int

    private var rippleType: Int
    private var rippleColor: Int
    private var rippleRadius: Float
    /** 设置rippleRadius可以波动的范围 */
    @FloatRange(from = 0.0, to = 1.0)
    private var rippleRadiusFluctuation: Float = 0f
        set(value) {
            field = when {
                value < 0.0 -> 0f
                value > 1.0 -> 1f
                else -> value
            }
        }
    /** ripple寿命 */
    private var rippleLifetime: Long
    /** 孵化间隔（每隔多久生成一个新的ripple） */
    private var rippleIncubateInterval: Long
    /** 孵化间隔的波动范围 */
    @FloatRange(from = 0.0, to = 1.0)
    private var rippleIncubateIntervalFluctuation: Float = 0F
        set(value) {
            field = when {
                value < 0.0 -> 0f
                value > 1.0 -> 1f
                else -> value
            }
        }
    // ------------------------------------------ Operation ------------------------------------------

    /** 上次孵化的时间 */
    private var lastIncubateTime = 0L

    init {
        refreshInterval = REFRESH_INTERVAL_DEFAULT

        backgroundColor = Color.WHITE

        rippleType = 0
        rippleColor = -0xff0100
        rippleRadius = 100f
        rippleRadiusFluctuation = .2f
        rippleLifetime = 2000L
        rippleIncubateInterval = 200L
        rippleIncubateIntervalFluctuation = .5f

        paint.color = rippleColor

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
        // clear
        canvas.drawColor(backgroundColor)

        // draw ripples

        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastIncubateTime >= rippleIncubateInterval) {
            lastIncubateTime = currentTimeMillis
            ripples.add(acquireTrace().apply {
                x = random.nextFloat() * canvas.width
                y = random.nextFloat() * canvas.height
            })
        }

        ripples.iterator().forEach {
            if (currentTimeMillis - it.birth > rippleLifetime) {
                try {
                    ripples.remove(it)
                } finally {
                    releaseTrace(it)
                }
            }
        }

        ripples.forEach {
            // draw ripple
            canvas.drawCircle(
                    it.x,
                    it.y,
                    100f,
                    paint
            )
        }

    }

    private fun acquireTrace(): Trace {
        return pointPool.acquire()
                ?.apply {
                    x = 0f
                    y = 0f
                    birth = System.currentTimeMillis()
                }
                ?: Trace(
                        0f,
                        0f,
                        System.currentTimeMillis()
                )
    }

    private fun releaseTrace(trace: Trace) {
        trace.clear()
        pointPool.release(trace)
    }

    companion object {
        /**刷新间隔*/
        private const val REFRESH_INTERVAL_DEFAULT = 20L

        private val pointPool = Pools.SynchronizedPool<Trace>(24)
    }

    /**
     * 单独的绘制线程
     */
    internal inner class RenderThread(surfaceTexture: SurfaceTexture, private val updateSurface: (Surface) -> Unit) : Thread() {
        private val surface = Surface(surfaceTexture)

        @Volatile
        private var running = true

        override fun run() {
            while (running && !Thread.interrupted()) {
                updateSurface(surface)
                try {
                    sleep(refreshInterval)
                } catch (e: InterruptedException) {
                }
            }
        }

        fun stopRendering() {
            interrupt()
            running = false
        }
    }

    /**
     * 痕迹
     *
     * 用来记录一个Ripple的相关参数
     * @param birth 诞生时间
     */
    private data class Trace(
            var x: Float,
            var y: Float,
            var birth: Long
    ) {
        fun clear() {
            x = 0f
            y = 0f
            birth = 0L
        }
    }
}
package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.support.annotation.FloatRange
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pools
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
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

    /** ripple半径的插值器 */
    var rippleRadiusInterpolator: Interpolator
    /** 透明度插值器 */
    var rippleAlphaInterpolator: Interpolator

    /** 缓存所有ripple */
    private val ripples = ArrayList<Trace>()

    // ------------------------------------------ Input ------------------------------------------

    private var refreshInterval: Long

    private var bgColor: Int

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

    /** 临时的用来存放要移除的ripple的列表 */
    private val removeRipples = ArrayList<Trace>()

    /** 上次孵化的时间 */
    private var lastIncubateTime = 0L

    init {
        refreshInterval = REFRESH_INTERVAL_DEFAULT

        val resources = context.resources
        val a = context.obtainStyledAttributes(attrs, R.styleable.RippleView, defStyleAttr, R.style.Widget_Dreamground_RippleView)

        bgColor = a.getColor(R.styleable.RippleView_bgColor, ContextCompat.getColor(context, R.color.bg_color_default))

        rippleType = 0
        rippleColor = a.getColor(R.styleable.RippleView_rippleColor, ContextCompat.getColor(context, R.color.ripple_color_default))
        rippleRadius = 100f
        rippleRadiusFluctuation = .2f
        rippleLifetime = a.getInteger(R.styleable.RippleView_rippleLifetime, 5000).toLong()
        rippleIncubateInterval = a.getInteger(R.styleable.RippleView_rippleIncubateInterval, 200).toLong()
        rippleIncubateIntervalFluctuation = .5f

        a.recycle()

        //根据不同的ripple类型，显示不同的效果
        when (rippleType) {
            1 -> {
                rippleRadiusInterpolator = DropRadiusInterpolator()
                rippleAlphaInterpolator = DropAlphaInterpolator()
            }
            else /*0*/ -> {
                rippleRadiusInterpolator = RippleRadiusInterpolator()
                rippleAlphaInterpolator = RippleAlphaInterpolator()
            }
        }

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
        canvas.drawColor(bgColor)

        // draw ripples

        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastIncubateTime >= rippleIncubateInterval) {
            lastIncubateTime = currentTimeMillis
            ripples.add(acquireTrace().apply {
                x = random.nextFloat() * canvas.width
                y = random.nextFloat() * canvas.height
            })
        }

        ripples.forEach {
            if (currentTimeMillis - it.birth <= rippleLifetime) {
                // draw ripple
                drawRipple(canvas, it, currentTimeMillis)
            } else {
                removeRipples.add(it)
            }
        }

        ripples.removeAll(removeRipples)
        removeRipples.forEach {
            releaseTrace(it)
        }
        removeRipples.clear()
    }

    /**
     * 绘制单个ripple
     */
    private fun drawRipple(canvas: Canvas, it: Trace, currentTimeMillis: Long) {
        val progress = (currentTimeMillis - it.birth).toFloat() / rippleLifetime

        val radius = rippleRadius * rippleRadiusInterpolator.getInterpolation(progress)
        paint.color = computeColorProduct(rippleColor, rippleAlphaInterpolator.getInterpolation(progress))

        canvas.drawCircle(
                it.x,
                it.y,
                radius,
                paint
        )
    }

    /**
     * 计算颜色与透明度的乘积
     */
    private fun computeColorProduct(color: Int, alpha: Float): Int {
        val lAlpha = (Color.alpha(color) * alpha).toInt().let {
            when {
                it > 255 -> 255
                it < 0 -> 0
                else -> it
            }
        }
        return Color.argb(lAlpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    companion object {
        /**刷新间隔*/
        private const val REFRESH_INTERVAL_DEFAULT = 20L

        private val pointPool = Pools.SynchronizedPool<Trace>(24)

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
     *
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

    /**
     * 水滴半径
     *
     * see [https://www.desmos.com/calculator/hzagww2dyv]
     */
    class RippleRadiusInterpolator : Interpolator {
        private val a = -12.0004f
        private val b = 26.5141f
        private val c = -19.4514f
        private val d = 7.92571f
        private val f = 0f

        override fun getInterpolation(input: Float): Float {
            return a * input * input * input * input + b * input * input * input + c * input * input + d * input + f
        }
    }

    /** 水滴透明度 */
    class RippleAlphaInterpolator : Interpolator {
        private val a = -11.8164f
        private val b = 30.828f
        private val c = -28.244f
        private val d = 9.23137f
        private val f = 0f

        override fun getInterpolation(input: Float): Float {
            return a * input * input * input * input + b * input * input * input + c * input * input + d * input + f
        }
    }

    /**
     * 水滴半径
     *
     * see [https://www.desmos.com/calculator/hzagww2dyv]
     */
    class DropRadiusInterpolator : Interpolator {
        private val a = -8.013f
        private val b = 19.9082f
        private val c = -17.699f
        private val d = 7.80757f
        private val f = 0f

        override fun getInterpolation(input: Float): Float {
            return a * input * input * input * input + b * input * input * input + c * input * input + d * input + f
        }
    }

    /** 水滴透明度 */
    class DropAlphaInterpolator : Interpolator {
        private val a = -11.8164f
        private val b = 30.828f
        private val c = -28.244f
        private val d = 9.23137f
        private val f = 0f

        override fun getInterpolation(input: Float): Float {
            return a * input * input * input * input + b * input * input * input + c * input * input + d * input + f
        }
    }
}
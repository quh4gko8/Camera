package app.grapheneos.camera.ui.seekbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.os.Handler
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import android.graphics.drawable.BitmapDrawable

import android.graphics.Bitmap
import android.os.Looper

import android.widget.TextView

import android.view.View

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.camera.core.ZoomState
import app.grapheneos.camera.R
import kotlin.math.roundToInt
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import app.grapheneos.camera.ui.activities.MainActivity


class ZoomBar : AppCompatSeekBar {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    companion object {
        private const val PANEL_VISIBILITY_DURATION = 2000L
    }

    private val closePanelHandler: Handler = Handler(Looper.getMainLooper())

    private val closePanelRunnable = Runnable {
        hidePanel()
    }

    @SuppressLint("InflateParams")
    private var thumbView: View = LayoutInflater.from(context)
        .inflate(R.layout.zoom_bar_thumb, null, false)

    private lateinit var mainActivity: MainActivity

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }

    private fun showPanel() {
        togglePanel(View.VISIBLE)
        closePanelHandler.removeCallbacks(closePanelRunnable)
        closePanelHandler.postDelayed(closePanelRunnable, PANEL_VISIBILITY_DURATION)
    }

    private fun hidePanel() {
        togglePanel(View.GONE)
    }

    private fun togglePanel(visibility: Int) {
        val transition: Transition = Fade()
        if (visibility == View.GONE) {
            transition.duration = 300
        } else {
            transition.duration = 0
        }
        transition.addTarget(R.id.zoom_bar_panel)

        TransitionManager.beginDelayedTransition(
            mainActivity.window.decorView.rootView as ViewGroup, transition)
        mainActivity.zoomBarPanel.visibility = visibility
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    fun updateThumb() {
        val zoomState: ZoomState? = mainActivity.config.camera?.cameraInfo?.zoomState
            ?.value

        showPanel()

        var zoomRatio = 1.0f
        var linearZoom = 0.0f

        if (zoomState!=null) {
            zoomRatio = zoomState.zoomRatio
            linearZoom = zoomState.linearZoom
        }

        progress = (linearZoom * 100).roundToInt()

        val textView: TextView = thumbView.findViewById(R.id.progress) as TextView
        val text = String.format("%.1fx", zoomRatio)

        textView.text = text

        thumbView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(
            thumbView.measuredWidth,
            thumbView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        thumbView.layout(0, 0, thumbView.measuredWidth, thumbView.measuredHeight)
        thumbView.draw(canvas)
        thumb = BitmapDrawable(resources, bitmap)
        onSizeChanged(width, height, 0, 0)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val progress = max - (max * event.y / height).toInt()
                mainActivity.config.camera?.cameraControl?.setLinearZoom(progress/100f)
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }
}

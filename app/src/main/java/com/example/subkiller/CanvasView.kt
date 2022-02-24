package com.example.subkiller


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * This class hold the canvas view
 */

class CanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    /**
     * dimension of canvas, this value change on the method onSizeChanged()
     */
    private var w: Float = 0F
    private var h: Float = 0F

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val _typeFace = Typeface.create("Arial", Typeface.BOLD)
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = 34F
        typeface = _typeFace
    }

    private var mainActivity: MainActivity = context as MainActivity


    /**
     * Called whenever the view changes size.
     * Since the view starts out with no size, this is also called after
     * the view has been inflated and has a valid size.
     */

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        this.w = w.toFloat()
        this.h = h.toFloat()
        mainActivity.setUp(this.w, this.h)

    }

    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    fun paintFrame(hits: Int, missed: Int, focused: Boolean, strokeW : Float) {
        extraCanvas.drawColor(Color.GREEN)

        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        extraCanvas.drawText("Hits: $hits", 40F, 50F, paint)
        extraCanvas.drawText("Misses: $missed", 260F, 50F, paint)

        if (focused) {
            paint.color = (Color.CYAN)
        } else {
            paint.color = Color.BLACK
            extraCanvas.drawText("(Paused)", 480F, 50F, paint)
            paint.color = (Color.GRAY)

        }

        // make border,depending the state of the game
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeW
        extraCanvas.drawRect(strokeW / 2, strokeW / 2, w - strokeW / 2, h - strokeW / 2, paint)
        paint.style = Paint.Style.FILL //set up paint for drawing 3 object
    }

    fun drawBase(x: Float, y: Float, w: Float, h: Float, color: Int) {
        paint.color = color
        extraCanvas.drawRoundRect(x, y, x + w, y + h, 25F, 25F, paint)
    }

    fun drawBomb(x: Float, y: Float, w: Float, color: Int) {
        paint.color = color
        extraCanvas.drawOval(x, y, x + w, y + w, paint)
    }

    fun drawSubmarine(x: Float, y: Float, w: Float, h: Float, color: Int) {
        paint.color = color
        extraCanvas.drawOval(x, y, x + w, y + h, paint)
    }

    fun doExplosion(centerX: Float, centerY: Float, explosionFrameNumber: Int) {
        paint.color = Color.YELLOW
        paint.style = Paint.Style.FILL

        var left = centerX - 3 * explosionFrameNumber
        var top = centerY - 1.5F * explosionFrameNumber
        extraCanvas.drawOval(
            left,
            top,
            left + 6 * explosionFrameNumber,
            top + 3 * explosionFrameNumber,
            paint
        )

        paint.color = Color.RED
        left = centerX - 1.5F * explosionFrameNumber
        top = centerY - explosionFrameNumber / 2
        extraCanvas.drawOval(
            left,
            top,
            left + 3 * explosionFrameNumber,
            top + explosionFrameNumber.toFloat(),
            paint
        )

    }

}
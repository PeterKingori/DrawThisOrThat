package com.pkndegwa.kiddiedrawing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

/**
 * This class contains the attributes for the main layout of our application.
 */
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mDrawPath: CustomPath? = null // An variable of CustomPath inner class.
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null // The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.0f
    private var color = Color.BLACK
    private val mPaths = ArrayList<CustomPath>()

    /**
     * A variable for canvas which will be initialized later and used.
     *
     *The Canvas class holds the "draw" calls. To draw something, you need 4 basic components:
     * A Bitmap to hold the pixels, a Canvas to host the draw calls (writing into the bitmap),
     * a drawing primitive (e.g. Rect, Path, text, Bitmap), and
     * a paint (to describe the colors and styles for the drawing)
     */
    private var canvas: Canvas? = null

    init {
        setupDrawing()
    }

    /**
     * This method initialises the attributes of the DrawingView class.
     */
    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPaint?.color = color
        mDrawPaint?.style = Paint.Style.STROKE
        mDrawPaint?.strokeJoin = Paint.Join.ROUND
        mDrawPaint?.strokeCap = Paint.Cap.ROUND
        mDrawPath = CustomPath(color, mBrushSize)
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    /**
     * This method is called when a stroke is drawn on the canvas as a part of the painting.
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!, 0.0f, 0.0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint?.strokeWidth  = path.brushThickness
            mDrawPaint?.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath?.isEmpty!!) {
            mDrawPaint?.strokeWidth  = mDrawPath!!.brushThickness
            mDrawPaint?.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    /**
     * This method acts as an event listener when a touch event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    // A function to set the brush size based on the new size entered but adjusted to the screen size.
    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    /**
     * An inner class for the custome path.
     */
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}
package com.edmodo.rangebar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import kotlin.math.abs

internal class Thumb(
    ctx: Context,
    y: Float,
    thumbColorNormal: Int,
    thumbColorPressed: Int,
    thumbRadiusDP: Float,
    thumbImageNormal: Int,
    thumbImagePressed: Int
) {
    // Member Variables ////////////////////////////////////////////////////////
    // Radius (in pixels) of the touch area of the thumb.
    private val mTargetRadiusPx: Float

    // The normal and pressed images to display for the thumbs.
    private val mImageNormal: Bitmap
    private val mImagePressed: Bitmap

    // Package-Private Methods /////////////////////////////////////////////////
    // Variables to store half the width/height for easier calculation.
    val halfWidth: Float
    val halfHeight: Float
    private val mHalfWidthPressed: Float
    private val mHalfHeightPressed: Float

    // Indicates whether this thumb is currently pressed and active.
    var isPressed = false
        private set

    // The y-position of the thumb in the parent view. This should not change.
    private val mY: Float

    // The current x-position of the thumb in the parent view.
    var x: Float

    // mPaint to draw the thumbs if attributes are selected
    private var mPaintNormal: Paint? = null
    private var mPaintPressed: Paint? = null

    // Radius of the new thumb if selected
    private var mThumbRadiusPx = 0f

    // Toggle to select bitmap thumbImage or not
    private var mUseBitmap = false

    // Colors of the thumbs if they are to be drawn
    private var mThumbColorNormal = 0
    private var mThumbColorPressed = 0
    fun press() {
        isPressed = true
    }

    fun release() {
        isPressed = false
    }

    /**
     * Determines if the input coordinate is close enough to this thumb to
     * consider it a press.
     *
     * @param x the x-coordinate of the user touch
     * @param y the y-coordinate of the user touch
     * @return true if the coordinates are within this thumb's target area;
     * false otherwise
     */
    fun isInTargetZone(x: Float, y: Float): Boolean {
        return abs(x - this.x) <= mTargetRadiusPx && abs(y - mY) <= mTargetRadiusPx
    }

    /**
     * Draws this thumb on the provided canvas.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     * View#onDraw()}
     */
    fun draw(canvas: Canvas) {

        // If a bitmap is to be printed. Determined by thumbRadius attribute.
        if (mUseBitmap) {
            val bitmap = if (isPressed) mImagePressed else mImageNormal
            if (isPressed) {
                val topPressed = mY - mHalfHeightPressed
                val leftPressed = x - mHalfWidthPressed
                canvas.drawBitmap(bitmap, leftPressed, topPressed, null)
            } else {
                val topNormal = mY - halfHeight
                val leftNormal = x - halfWidth
                canvas.drawBitmap(bitmap, leftNormal, topNormal, null)
            }
        } else {

            // Otherwise use a circle to display.
            if (isPressed) canvas.drawCircle(
                x,
                mY,
                mThumbRadiusPx,
                mPaintPressed!!
            ) else canvas.drawCircle(
                x, mY, mThumbRadiusPx, mPaintNormal!!
            )
        }
    }

    companion object {
        // Private Constants ///////////////////////////////////////////////////////
        // The radius (in dp) of the touchable area around the thumb. We are basing
        // this value off of the recommended 48dp Rhythm. See:
        // http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm
        private const val MINIMUM_TARGET_RADIUS_DP = 24f

        // Sets the default values for radius, normal, pressed if circle is to be
        // drawn but no value is given.
        private const val DEFAULT_THUMB_RADIUS_DP = 14f

        // Corresponds to android.R.color.holo_blue_light.
        private const val DEFAULT_THUMB_COLOR_NORMAL = -0xcc4a1b
        private const val DEFAULT_THUMB_COLOR_PRESSED = -0xcc4a1b
    }

    // Constructors ////////////////////////////////////////////////////////////
    init {
        val res = ctx.resources
        mImageNormal = BitmapFactory.decodeResource(res, thumbImageNormal)
        mImagePressed = BitmapFactory.decodeResource(res, thumbImagePressed)

        // If any of the attributes are set, toggle bitmap off
        if (thumbRadiusDP == -1f && thumbColorNormal == -1 && thumbColorPressed == -1) {
            mUseBitmap = true
        } else {
            mUseBitmap = false

            // If one of the attributes are set, but the others aren't, set the
            // attributes to default
            mThumbRadiusPx = if (thumbRadiusDP == -1f) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_THUMB_RADIUS_DP,
                res.displayMetrics
            ) else TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                thumbRadiusDP,
                res.displayMetrics
            )
            mThumbColorNormal =
                if (thumbColorNormal == -1) DEFAULT_THUMB_COLOR_NORMAL else thumbColorNormal
            mThumbColorPressed =
                if (thumbColorPressed == -1) DEFAULT_THUMB_COLOR_PRESSED else thumbColorPressed

            // Creates the paint and sets the Paint values
            mPaintNormal = Paint().apply {
                color = mThumbColorNormal
                isAntiAlias = true
            }
            mPaintPressed = Paint().apply {
                color = mThumbColorPressed
                isAntiAlias = true
            }
        }
        halfWidth = mImageNormal.width / 2f
        halfHeight = mImageNormal.height / 2f
        mHalfWidthPressed = mImagePressed.width / 2f
        mHalfHeightPressed = mImagePressed.height / 2f

        // Sets the minimum touchable area, but allows it to expand based on
        // image size
        val targetRadius =
            Math.max(MINIMUM_TARGET_RADIUS_DP, thumbRadiusDP).toInt()
        mTargetRadiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            targetRadius.toFloat(),
            res.displayMetrics
        )
        x = halfWidth
        mY = y
    }
}

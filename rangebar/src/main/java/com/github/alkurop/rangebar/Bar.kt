package com.github.alkurop.rangebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
internal class Bar(
    ctx: Context,
    /**
     * Get the x-coordinate of the left edge of the bar.
     *
     * @return x-coordinate of the left edge of the bar
     */
    // Left-coordinate of the horizontal bar.
    val leftX: Float,
    y: Float,
    length: Float,
    tickCount: Int,
    tickHeightDP: Float,
    BarWeight: Float,
    BarColor: Int
) {
    // Member Variables ////////////////////////////////////////////////////////
    private val mPaint: Paint

    /**
     * Get the x-coordinate of the right edge of the bar.
     *
     * @return x-coordinate of the right edge of the bar
     */
    val rightX: Float
    private val mY: Float
    private var mNumSegments: Int
    private var mTickDistance: Float
    private val mTickHeight: Float
    private val mTickStartY: Float
    private val mTickEndY: Float
    // Package-Private Methods /////////////////////////////////////////////////
    /**
     * Draws the bar on the given Canvas.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     * View#onDraw()}
     */
    fun draw(canvas: Canvas) {
        canvas.drawLine(leftX, mY, rightX, mY, mPaint)
        drawTicks(canvas)
    }

    /**
     * Gets the x-coordinate of the nearest tick to the given x-coordinate.
     *
     * @param x the x-coordinate to find the nearest tick for
     * @return the x-coordinate of the nearest tick
     */
    fun getNearestTickCoordinate(thumb: Thumb): Float {
        val nearestTickIndex = getNearestTickIndex(thumb)
        return leftX + nearestTickIndex * mTickDistance
    }

    /**
     * Gets the zero-based index of the nearest tick to the given thumb.
     *
     * @param thumb the Thumb to find the nearest tick for
     * @return the zero-based index of the nearest tick
     */
    fun getNearestTickIndex(thumb: Thumb): Int {
        return ((thumb.x - leftX + mTickDistance / 2f) / mTickDistance).toInt()
    }

    /**
     * Set the number of ticks that will appear in the RangeBar.
     *
     * @param tickCount the number of ticks
     */
    fun setTickCount(tickCount: Int) {
        val barLength = rightX - leftX
        mNumSegments = tickCount - 1
        mTickDistance = barLength / mNumSegments
    }
    // Private Methods /////////////////////////////////////////////////////////
    /**
     * Draws the tick marks on the bar.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     * View#onDraw()}
     */
    private fun drawTicks(canvas: Canvas) {

        // Loop through and draw each tick (except final tick).
        for (i in 0 until mNumSegments) {
            val x = i * mTickDistance + leftX
            canvas.drawLine(x, mTickStartY, x, mTickEndY, mPaint)
        }
        // Draw final tick. We draw the final tick outside the loop to avoid any
        // rounding discrepancies.
        canvas.drawLine(rightX, mTickStartY, rightX, mTickEndY, mPaint)
    }

    // Constructor /////////////////////////////////////////////////////////////
    init {
        rightX = leftX + length
        mY = y
        mNumSegments = tickCount - 1
        mTickDistance = length / mNumSegments
        mTickHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            tickHeightDP,
            ctx.resources.displayMetrics
        )
        mTickStartY = mY - mTickHeight / 2f
        mTickEndY = mY + mTickHeight / 2f

        // Initialize the paint.
        mPaint = Paint()
        mPaint.color = BarColor
        mPaint.strokeWidth = BarWeight
        mPaint.isAntiAlias = true
    }
}

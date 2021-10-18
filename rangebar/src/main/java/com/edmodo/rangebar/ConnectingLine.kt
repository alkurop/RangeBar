package com.edmodo.rangebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue

/**
 * Class representing the blue connecting line between the two thumbs.
 */
internal class ConnectingLine(
    ctx: Context,
    y: Float,
    connectingLineWeight: Float,
    connectingLineColor: Int
) {
    // Member Variables ////////////////////////////////////////////////////////
    private val mPaint: Paint
    private val mConnectingLineWeight: Float
    private val mY: Float
    // Package-Private Methods /////////////////////////////////////////////////
    /**
     * Draw the connecting line between the two thumbs.
     *
     * @param canvas the Canvas to draw to
     * @param leftThumb the left thumb
     * @param rightThumb the right thumb
     */
    fun draw(canvas: Canvas, leftThumb: Thumb, rightThumb: Thumb) {
        canvas.drawLine(leftThumb.x, mY, rightThumb.x, mY, mPaint)
    }

    // Constructor /////////////////////////////////////////////////////////////
    init {
        val res = ctx.resources
        mConnectingLineWeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            connectingLineWeight,
            res.displayMetrics
        )

        // Initialize the paint, set values
        mPaint = Paint()
        mPaint.color = connectingLineColor
        mPaint.strokeWidth = mConnectingLineWeight
        mPaint.isAntiAlias = true
        mY = y
    }
}

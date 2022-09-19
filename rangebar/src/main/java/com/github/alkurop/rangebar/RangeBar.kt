package com.github.alkurop.rangebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * The RangeBar is a double-sided version of a [android.widget.SeekBar]
 * with discrete values. Whereas the thumb for the SeekBar can be dragged to any
 * position in the bar, the RangeBar only allows its thumbs to be dragged to
 * discrete positions (denoted by tick marks) in the bar. When released, a
 * RangeBar thumb will snap to the nearest tick mark.
 *
 *
 * Clients of the RangeBar can attach a
 * [RangeBar.OnRangeBarChangeListener] to be notified when the thumbs have
 * been moved.
 */

typealias OnRangeBarChangeListener = (RangeBar.Tick) -> Unit


class RangeBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    data class Tick(
        val start: Int,
        val end: Int,
        val range: Int
    )

    // Instance variables for all of the customizable attributes
    private var mTickHeightDP = DEFAULT_TICK_HEIGHT_DP
    private var mBarWeight = DEFAULT_BAR_WEIGHT_PX
    private var mBarColor = DEFAULT_BAR_COLOR
    private var mConnectingLineWeight = DEFAULT_CONNECTING_LINE_WEIGHT_PX
    private var mConnectingLineColor = DEFAULT_CONNECTING_LINE_COLOR
    private var mThumbImageNormal = DEFAULT_THUMB_IMAGE_NORMAL
    private var mThumbImagePressed = DEFAULT_THUMB_IMAGE_PRESSED
    private var mThumbRadiusDP = DEFAULT_THUMB_RADIUS_DP
    private var mThumbColorNormal = DEFAULT_THUMB_COLOR_NORMAL
    private var mThumbColorPressed = DEFAULT_THUMB_COLOR_PRESSED
    private var deactivateAlpha = DEFAULT_DEACTIVATE_ALPHA

    // setTickCount only resets indices before a thumb has been pressed or a
    // setThumbIndices() is called, to correspond with intended usage
    private var mFirstSetTickCount = true
    private val mDefaultWidth = 500
    private val mDefaultHeight = 100
    private var mLeftThumb: Thumb? = null
    private var mRightThumb: Thumb? = null
    private var mBar: Bar? = null
    private var mConnectingLine: ConnectingLine? = null
    private var mListener: ((Tick) -> Unit)? = null

    var tick = Tick(
        start = 0,
        end = DEFAULT_TICK_COUNT - 1,
        range = DEFAULT_TICK_COUNT
    )

    init {
        rangeBarInit(context, attrs)
        isActivated = true
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        alpha = if (activated) 1f else deactivateAlpha
        isClickable = isActivated
    }

    // View Methods ////////////////////////////////////////////////////////////
    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putInt("TICK_COUNT", tick.range)
        bundle.putFloat("TICK_HEIGHT_DP", mTickHeightDP)
        bundle.putFloat("BAR_WEIGHT", mBarWeight)
        bundle.putInt("BAR_COLOR", mBarColor)
        bundle.putFloat("CONNECTING_LINE_WEIGHT", mConnectingLineWeight)
        bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor)
        bundle.putInt("THUMB_IMAGE_NORMAL", mThumbImageNormal)
        bundle.putInt("THUMB_IMAGE_PRESSED", mThumbImagePressed)
        bundle.putFloat("THUMB_RADIUS_DP", mThumbRadiusDP)
        bundle.putInt("THUMB_COLOR_NORMAL", mThumbColorNormal)
        bundle.putInt("THUMB_COLOR_PRESSED", mThumbColorPressed)
        bundle.putInt("LEFT_INDEX", tick.start)
        bundle.putInt("RIGHT_INDEX", tick.end)
        bundle.putBoolean("FIRST_SET_TICK_COUNT", mFirstSetTickCount)
        bundle.putBoolean("IS_ACTIVATED", isActivated)
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            mTickHeightDP = bundle.getFloat("TICK_HEIGHT_DP")
            mBarWeight = bundle.getFloat("BAR_WEIGHT")
            mBarColor = bundle.getInt("BAR_COLOR")
            mConnectingLineWeight = bundle.getFloat("CONNECTING_LINE_WEIGHT")
            mConnectingLineColor = bundle.getInt("CONNECTING_LINE_COLOR")
            mThumbImageNormal = bundle.getInt("THUMB_IMAGE_NORMAL")
            mThumbImagePressed = bundle.getInt("THUMB_IMAGE_PRESSED")
            mThumbRadiusDP = bundle.getFloat("THUMB_RADIUS_DP")
            mThumbColorNormal = bundle.getInt("THUMB_COLOR_NORMAL")
            mThumbColorPressed = bundle.getInt("THUMB_COLOR_PRESSED")
            tick = Tick(
                range = bundle.getInt("TICK_COUNT"),
                start = bundle.getInt("LEFT_INDEX"),
                end = bundle.getInt("RIGHT_INDEX")
            )
            mFirstSetTickCount = bundle.getBoolean("FIRST_SET_TICK_COUNT")
            setThumbIndices(tick.start, tick.end)
            isActivated = bundle.getBoolean("IS_ACTIVATED")

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        val height: Int

        // Get measureSpec mode and size values.
        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)

        // The RangeBar width should be as large as possible.
        width = if (measureWidthMode == MeasureSpec.AT_MOST) {
            measureWidth
        } else if (measureWidthMode == MeasureSpec.EXACTLY) {
            measureWidth
        } else {
            mDefaultWidth
        }

        // The RangeBar height should be as small as possible.
        height = if (measureHeightMode == MeasureSpec.AT_MOST) {
            Math.min(mDefaultHeight, measureHeight)
        } else if (measureHeightMode == MeasureSpec.EXACTLY) {
            measureHeight
        } else {
            mDefaultHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val ctx = context

        // This is the initial point at which we know the size of the View.

        // Create the two thumb objects.
        val yPos = h / 2f
        mLeftThumb = Thumb(
            ctx,
            yPos,
            mThumbColorNormal,
            mThumbColorPressed,
            mThumbRadiusDP,
            mThumbImageNormal,
            mThumbImagePressed
        )
        mRightThumb = Thumb(
            ctx,
            yPos,
            mThumbColorNormal,
            mThumbColorPressed,
            mThumbRadiusDP,
            mThumbImageNormal,
            mThumbImagePressed
        )

        // Create the underlying bar.
        val marginLeft = mLeftThumb!!.halfWidth / 2
        val barLength = w - 2 * marginLeft
        mBar =
            Bar(ctx, marginLeft, yPos, barLength, tick.range, mTickHeightDP, mBarWeight, mBarColor)

        // Initialize thumbs to the desired indices
        mLeftThumb!!.x = marginLeft + tick.start / (tick.range - 1).toFloat() * barLength
        mRightThumb!!.x = marginLeft + tick.end / (tick.range - 1).toFloat() * barLength

        // Set the thumb indices.
        val newTick = tick.copy(
            start = mBar!!.getNearestTickIndex(mLeftThumb!!),
            end = mBar!!.getNearestTickIndex(mRightThumb!!)
        )

        // Call the listener.
        if (newTick.start != tick.start || newTick.end != tick.end) {
            tick = newTick
            mListener?.invoke(tick)
        }

        // Create the line connecting the two thumbs.
        mConnectingLine = ConnectingLine(ctx, yPos, mConnectingLineWeight, mConnectingLineColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mBar!!.draw(canvas)
        mConnectingLine!!.draw(canvas, mLeftThumb!!, mRightThumb!!)
        mLeftThumb!!.draw(canvas)
        mRightThumb!!.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // If this View is not enabled, don't allow for touch interactions.
        return if (!isEnabled) {
            false
        } else when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onActionDown(event.x, event.y)
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.parent.requestDisallowInterceptTouchEvent(false)
                onActionUp(event.x, event.y)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                onActionMove(event.x)
                this.parent.requestDisallowInterceptTouchEvent(true)
                true
            }
            else -> false
        }
    }
    // Public Methods //////////////////////////////////////////////////////////
    /**
     * Sets a listener to receive notifications of changes to the RangeBar. This
     * will overwrite any existing set listeners.
     *
     * @param listener the RangeBar notification listener; null to remove any
     * existing listener
     */
    fun setOnRangeBarChangeListener(listener: OnRangeBarChangeListener) {
        mListener = listener
    }

    /**
     * Sets the number of ticks in the RangeBar.
     *
     * @param tickCount Integer specifying the number of ticks.
     */
    fun setTickCount(tickCount: Int) {
        if (isValidTickCount(tickCount)) {
            tick = tick.copy(range = tickCount)

            // Prevents resetting the indices when creating new activity, but
            // allows it on the first setting.
            if (mFirstSetTickCount) {
                tick = tick.copy(
                    start = 0,
                    end = tick.range - 1
                )
                mListener?.invoke(tick)
            }
            if (indexOutOfRange(tick.start, tick.end)) {
                tick = tick.copy(
                    start = 0,
                    end = tick.range - 1
                )
                mListener?.invoke(tick)
            }
            createBar()
            createThumbs()
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.")
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    /**
     * Sets the height of the ticks in the range bar.
     *
     * @param tickHeight Float specifying the height of each tick mark in dp.
     */
    fun setTickHeight(tickHeight: Float) {
        mTickHeightDP = tickHeight
        createBar()
    }

    /**
     * Set the weight of the bar line and the tick lines in the range bar.
     *
     * @param barWeight Float specifying the weight of the bar and tick lines in
     * px.
     */
    fun setBarWeight(barWeight: Float) {
        mBarWeight = barWeight
        createBar()
    }

    /**
     * Set the color of the bar line and the tick lines in the range bar.
     *
     * @param barColor Integer specifying the color of the bar line.
     */
    fun setBarColor(barColor: Int) {
        mBarColor = barColor
        createBar()
    }

    /**
     * Set the weight of the connecting line between the thumbs.
     *
     * @param connectingLineWeight Float specifying the weight of the connecting
     * line.
     */
    fun setConnectingLineWeight(connectingLineWeight: Float) {
        mConnectingLineWeight = connectingLineWeight
        createConnectingLine()
    }

    /**
     * Set the color of the connecting line between the thumbs.
     *
     * @param connectingLineColor Integer specifying the color of the connecting
     * line.
     */
    fun setConnectingLineColor(connectingLineColor: Int) {
        mConnectingLineColor = connectingLineColor
        createConnectingLine()
    }

    /**
     * If this is set, the thumb images will be replaced with a circle of the
     * specified radius. Default width = 20dp.
     *
     * @param thumbRadius Float specifying the radius of the thumbs to be drawn.
     */
    fun setThumbRadius(thumbRadius: Float) {
        mThumbRadiusDP = thumbRadius
        createThumbs()
    }

    /**
     * Sets the normal thumb picture by taking in a reference ID to an image.
     *
     * @param thumbNormalID Integer specifying the resource ID of the image to
     * be drawn as the normal thumb.
     */
    fun setThumbImageNormal(thumbImageNormalID: Int) {
        mThumbImageNormal = thumbImageNormalID
        createThumbs()
    }

    /**
     * Sets the pressed thumb picture by taking in a reference ID to an image.
     *
     * @param pressedThumbID Integer specifying the resource ID of the image to
     * be drawn as the pressed thumb.
     */
    fun setThumbImagePressed(thumbImagePressedID: Int) {
        mThumbImagePressed = thumbImagePressedID
        createThumbs()
    }

    /**
     * If this is set, the thumb images will be replaced with a circle. The
     * normal image will be of the specified color.
     *
     * @param thumbColorNormal Integer specifying the normal color of the circle
     * to be drawn.
     */
    fun setThumbColorNormal(thumbColorNormal: Int) {
        mThumbColorNormal = thumbColorNormal
        createThumbs()
    }

    /**
     * If this is set, the thumb images will be replaced with a circle. The
     * pressed image will be of the specified color.
     *
     * @param thumbColorPressed Integer specifying the pressed color of the
     * circle to be drawn.
     */
    fun setThumbColorPressed(thumbColorPressed: Int) {
        mThumbColorPressed = thumbColorPressed
        createThumbs()
    }

    /**
     * Sets the location of each thumb according to the developer's choice.
     * Numbered from 0 to tick.range - 1 from the left.
     *
     * @param leftThumbIndex Integer specifying the index of the left thumb
     * @param rightThumbIndex Integer specifying the index of the right thumb
     */
    fun setThumbIndices(leftThumbIndex: Int, rightThumbIndex: Int) {
        if (indexOutOfRange(leftThumbIndex, rightThumbIndex)) {
            Log.e(
                TAG,
                "A thumb index is out of bounds. Check that it is between 0 and tick.range - 1"
            )
            throw IllegalArgumentException("A thumb index is out of bounds. Check that it is between 0 and tick.range - 1")
        } else {
            if (mFirstSetTickCount) mFirstSetTickCount = false
            tick = tick.copy(
                start = leftThumbIndex,
                end = rightThumbIndex
            )
            createThumbs()
            mListener?.invoke(tick)
        }
        invalidate()
        requestLayout()
    }
    // Private Methods /////////////////////////////////////////////////////////
    /**
     * Does all the functions of the constructor for RangeBar. Called by both
     * RangeBar constructors in lieu of copying the code for each constructor.
     *
     * @param context Context from the constructor.
     * @param attrs AttributeSet from the constructor.
     * @return none
     */
    private fun rangeBarInit(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RangeBar, 0, 0)
        try {

            // Sets the values of the user-defined attributes based on the XML
            // attributes.
            val tickCount = ta.getInteger(R.styleable.RangeBar_tickCount, DEFAULT_TICK_COUNT)
            if (isValidTickCount(tickCount)) {

                // Similar functions performed above in setTickCount; make sure
                // you know how they interact
                tick = tick.copy(
                    range = tickCount,
                    start = 0,
                    end = tickCount - 1
                )
                mListener?.invoke(tick)
            } else {
                Log.e(TAG, "tickCount less than 2; invalid tickCount. XML input ignored.")
            }
            mTickHeightDP = ta.getDimension(R.styleable.RangeBar_tickHeight, DEFAULT_TICK_HEIGHT_DP)
            mBarWeight = ta.getDimension(R.styleable.RangeBar_barWeight, DEFAULT_BAR_WEIGHT_PX)
            mBarColor = ta.getColor(R.styleable.RangeBar_barColor, DEFAULT_BAR_COLOR)
            mConnectingLineWeight = ta.getDimension(
                R.styleable.RangeBar_connectingLineWeight,
                DEFAULT_CONNECTING_LINE_WEIGHT_PX
            )
            mConnectingLineColor = ta.getColor(
                R.styleable.RangeBar_connectingLineColor,
                DEFAULT_CONNECTING_LINE_COLOR
            )
            mThumbRadiusDP =
                ta.getDimension(R.styleable.RangeBar_thumbRadius, DEFAULT_THUMB_RADIUS_DP)
            mThumbImageNormal = ta.getResourceId(
                R.styleable.RangeBar_thumbImageNormal,
                DEFAULT_THUMB_IMAGE_NORMAL
            )
            mThumbImagePressed = ta.getResourceId(
                R.styleable.RangeBar_thumbImagePressed,
                DEFAULT_THUMB_IMAGE_PRESSED
            )
            mThumbColorNormal =
                ta.getColor(R.styleable.RangeBar_thumbColorNormal, DEFAULT_THUMB_COLOR_NORMAL)
            mThumbColorPressed = ta.getColor(
                R.styleable.RangeBar_thumbColorPressed,
                DEFAULT_THUMB_COLOR_PRESSED
            )
            deactivateAlpha  = ta.getFloat(
                R.styleable.RangeBar_deactivateAlpha,
                DEFAULT_DEACTIVATE_ALPHA
            )
        } finally {
            ta.recycle()
        }
    }

    /**
     * Creates a new mBar
     *
     * @param none
     */
    private fun createBar() {
        mBar = Bar(
            context,
            marginLeft,
            yPos,
            barLength,
            tick.range,
            mTickHeightDP,
            mBarWeight,
            mBarColor
        )
        invalidate()
    }

    /**
     * Creates a new ConnectingLine.
     *
     * @param none
     */
    private fun createConnectingLine() {
        mConnectingLine = ConnectingLine(
            context,
            yPos,
            mConnectingLineWeight,
            mConnectingLineColor
        )
        invalidate()
    }

    /**
     * Creates two new Thumbs.
     *
     * @param none
     */
    private fun createThumbs() {
        val ctx = context
        val yPos = yPos
        mLeftThumb = Thumb(
            ctx,
            yPos,
            mThumbColorNormal,
            mThumbColorPressed,
            mThumbRadiusDP,
            mThumbImageNormal,
            mThumbImagePressed
        )
        mRightThumb = Thumb(
            ctx,
            yPos,
            mThumbColorNormal,
            mThumbColorPressed,
            mThumbRadiusDP,
            mThumbImageNormal,
            mThumbImagePressed
        )
        val marginLeft = marginLeft
        val barLength = barLength

        // Initialize thumbs to the desired indices
        mLeftThumb!!.x = marginLeft / 2 + tick.start / (tick.range - 1).toFloat() * barLength
        mRightThumb!!.x =
            (marginLeft * 1.5).toFloat() + tick.end / (tick.range - 1).toFloat() * barLength
        invalidate()
    }

    /**
     * Get marginLeft in each of the public attribute methods.
     *
     * @param none
     * @return float marginLeft
     */
    private val marginLeft: Float
        private get() = if (mLeftThumb != null) mLeftThumb!!.halfWidth else 0f

    /**
     * Get yPos in each of the public attribute methods.
     *
     * @param none
     * @return float yPos
     */
    private val yPos: Float
        private get() = height / 2f

    /**
     * Get barLength in each of the public attribute methods.
     *
     * @param none
     * @return float barLength
     */
    private val barLength: Float
        private get() = width - 2 * marginLeft

    /**
     * Returns if either index is outside the range of the tickCount.
     *
     * @param leftThumbIndex Integer specifying the left thumb index.
     * @param rightThumbIndex Integer specifying the right thumb index.
     * @return boolean If the index is out of range.
     */
    private fun indexOutOfRange(leftThumbIndex: Int, rightThumbIndex: Int): Boolean {
        return leftThumbIndex < 0 || leftThumbIndex >= tick.range || rightThumbIndex < 0 || rightThumbIndex >= tick.range
    }

    /**
     * If is invalid tickCount, rejects. TickCount must be greater than 1
     *
     * @param tickCount Integer
     * @return boolean: whether tickCount > 1
     */
    private fun isValidTickCount(tickCount: Int): Boolean {
        return tickCount > 1
    }

    /**
     * Handles a [MotionEvent.ACTION_DOWN] event.
     *
     * @param x the x-coordinate of the down action
     * @param y the y-coordinate of the down action
     */
    private fun onActionDown(x: Float, y: Float) {
        if (!mLeftThumb!!.isPressed && mLeftThumb!!.isInTargetZone(x, y)) {
            pressThumb(mLeftThumb)
        } else if (!mLeftThumb!!.isPressed && mRightThumb!!.isInTargetZone(x, y)) {
            pressThumb(mRightThumb)
        }
    }

    /**
     * Handles a [MotionEvent.ACTION_UP] or
     * [MotionEvent.ACTION_CANCEL] event.
     *
     * @param x the x-coordinate of the up action
     * @param y the y-coordinate of the up action
     */
    private fun onActionUp(x: Float, y: Float) {
        if (!isActivated) return
        if (mLeftThumb!!.isPressed) {
            releaseThumb(mLeftThumb)
        } else if (mRightThumb!!.isPressed) {
            releaseThumb(mRightThumb)
        } else {
            val leftThumbXDistance = Math.abs(mLeftThumb!!.x - x)
            val rightThumbXDistance = Math.abs(mRightThumb!!.x - x)
            if (leftThumbXDistance < rightThumbXDistance) {
                mLeftThumb!!.x = x
                releaseThumb(mLeftThumb)
            } else {
                mRightThumb!!.x = x
                releaseThumb(mRightThumb)
            }
            val newTick = tick.copy(
                start = mBar!!.getNearestTickIndex(mLeftThumb!!),
                end = mBar!!.getNearestTickIndex(mRightThumb!!)
            )
            // If either of the indices have changed, update and call the listener.
            if (newTick.start != tick.start || newTick.end != tick.end) {
                tick = newTick
                mListener?.invoke(tick)
            }
        }
    }

    /**
     * Handles a [MotionEvent.ACTION_MOVE] event.
     *
     * @param x the x-coordinate of the move event
     */
    private fun onActionMove(x: Float) {
        if (!isActivated) return
        // Move the pressed thumb to the new x-position.
        if (mLeftThumb!!.isPressed) {
            moveThumb(mLeftThumb, x)
        } else if (mRightThumb!!.isPressed) {
            moveThumb(mRightThumb, x)
        }

        // If the thumbs have switched order, fix the references.
        if (mLeftThumb!!.x > mRightThumb!!.x) {
            val temp = mLeftThumb
            mLeftThumb = mRightThumb
            mRightThumb = temp
        }

        // Get the updated nearest tick marks for each thumb.

        val newTick = tick.copy(
            start = mBar!!.getNearestTickIndex(mLeftThumb!!),
            end = mBar!!.getNearestTickIndex(mRightThumb!!)
        )

        // If either of the indices have changed, update and call the listener.
        if (newTick.start != tick.start || newTick.end != tick.end) {
            tick = newTick
            mListener?.invoke(tick)
        }
    }

    /**
     * Set the thumb to be in the pressed state and calls invalidate() to redraw
     * the canvas to reflect the updated state.
     *
     * @param thumb the thumb to press
     */
    private fun pressThumb(thumb: Thumb?) {
        if (mFirstSetTickCount == true) mFirstSetTickCount = false
        thumb!!.press()
        invalidate()
    }

    /**
     * Set the thumb to be in the normal/un-pressed state and calls invalidate()
     * to redraw the canvas to reflect the updated state.
     *
     * @param thumb the thumb to release
     */
    private fun releaseThumb(thumb: Thumb?) {
        val nearestTickX = mBar!!.getNearestTickCoordinate(thumb!!)
        thumb.x = nearestTickX
        thumb.release()
        invalidate()
    }

    /**
     * Moves the thumb to the given x-coordinate.
     *
     * @param thumb the thumb to move
     * @param x the x-coordinate to move the thumb to
     */
    private fun moveThumb(thumb: Thumb?, x: Float) {

        // If the user has moved their finger outside the range of the bar,
        // do not move the thumbs past the edge.
        if (x < mBar!!.leftX || x > mBar!!.rightX) {
            // Do nothing.
        } else {
            thumb!!.x = x
            invalidate()
        }
    }

    fun reset() {

    }
    // Inner Classes ///////////////////////////////////////////////////////////
    /**
     * A callback that notifies clients when the RangeBar has changed. The
     * listener will only be called when either thumb's index has changed - not
     * for every movement of the thumb.
     */
    companion object {
        // Member Variables ////////////////////////////////////////////////////////
        private const val TAG = "RangeBar"

        // Default values for variables
        private const val DEFAULT_TICK_COUNT = 3
        private const val DEFAULT_TICK_HEIGHT_DP = 24f
        private const val DEFAULT_BAR_WEIGHT_PX = 2f
        private const val DEFAULT_BAR_COLOR = Color.LTGRAY
        private const val DEFAULT_CONNECTING_LINE_WEIGHT_PX = 4f
        private val DEFAULT_THUMB_IMAGE_NORMAL = R.drawable.seek_thumb_normal
        private val DEFAULT_THUMB_IMAGE_PRESSED = R.drawable.seek_thumb_pressed

        // Corresponds to android.R.color.holo_blue_light.
        private const val DEFAULT_CONNECTING_LINE_COLOR = -0xcc4a1b

        // Indicator value tells Thumb.java whether it should draw the circle or not
        private const val DEFAULT_THUMB_RADIUS_DP = -1f
        private const val DEFAULT_THUMB_COLOR_NORMAL = -1
        private const val DEFAULT_THUMB_COLOR_PRESSED = -1
        private const val DEFAULT_DEACTIVATE_ALPHA = 0.5f
    }
}
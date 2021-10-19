package com.github.alkurop.rangebar

import android.view.View

enum class MeasureSpecMode  // Constructor /////////////////////////////////////////////////////////////
    (
    /**
     * Gets the int value associated with this mode.
     *
     * @return the int value associated with this mode
     */
    // Member Variables ////////////////////////////////////////////////////////
    val modeValue: Int
) {
    AT_MOST(View.MeasureSpec.AT_MOST), EXACTLY(View.MeasureSpec.EXACTLY), UNSPECIFIED(View.MeasureSpec.UNSPECIFIED);

    // Public Methods //////////////////////////////////////////////////////////
    companion object {
        /**
         * Gets the MeasureSpecMode value that corresponds with the given
         * measureSpec int value.
         *
         * @param measureSpec the measure specification passed by the platform to
         * [View.onMeasure]
         * @return the MeasureSpecMode that matches with that measure spec
         */
        fun getMode(measureSpec: Int): MeasureSpecMode? {
            val modeValue = View.MeasureSpec.getMode(measureSpec)
            for (mode in values()) {
                if (mode.modeValue == modeValue) {
                    return mode
                }
            }
            return null
        }
    }
}

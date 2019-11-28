package com.plugable.mcommerceapp.cpmvp1.utils.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.plugable.mcommerceapp.cpmvp1.R


class CountDrawable(private val mContext: Context) : Drawable() {

    private val mBadgePaint: Paint
    private val mTextPaint: Paint
    private val mTxtRect = Rect()

    private var mCount = ""
    private var mWillDraw: Boolean = false

    init {
        val mTextSize = mContext.resources.getDimension(R.dimen._6ssp)

        mBadgePaint = Paint()
        mBadgePaint.color = ContextCompat.getColor(mContext.applicationContext, android.R.color.holo_red_light)
        mBadgePaint.isAntiAlias = true
        mBadgePaint.style = Paint.Style.FILL

        mTextPaint = Paint()
        mTextPaint.color = ContextCompat.getColor(mContext.applicationContext, R.color.colorWhite)
        mTextPaint.typeface = Typeface.DEFAULT
        mTextPaint.textSize = mTextSize
        mTextPaint.isAntiAlias = true
        mTextPaint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {

        if (!mWillDraw) {
            return
        }
        val bounds = bounds
        val width = (bounds.right - bounds.left).toFloat()
        val height = (bounds.bottom - bounds.top).toFloat()

        // Position the badge in the top-right quadrant of the icon.

        /*Using Math.max rather than Math.min */

        val radius = Math.max(width, height) / 2 / 2
        val centerX = width - radius - 1f + 5
        val centerY = radius - 5
        if (mCount.length <= 2) {
            // Draw badge circle.
            canvas.drawCircle(centerX, centerY, (radius + 5.5).toInt().toFloat(), mBadgePaint)
        } else {
            canvas.drawCircle(centerX, centerY, (radius + 6.5).toInt().toFloat(), mBadgePaint)
        }
        // Draw badge count text inside the circle.
        mTextPaint.getTextBounds(mCount, 0, mCount.length, mTxtRect)
        val textHeight = (mTxtRect.bottom - mTxtRect.top).toFloat()
        val textY = centerY + textHeight / 2f

            canvas.drawText(mCount, centerX, textY, mTextPaint)
    }

    /*
    Sets the count (i.e notifications) to display.
     */
    fun setCount(count: String) {
        mCount = count

        // Only draw a badge if there are notifications.
        mWillDraw = !count.equals("0", ignoreCase = true)
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        // do nothing
    }

    override fun setColorFilter(cf: ColorFilter?) {
        // do nothing
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }
}
package com.example.cpr_assist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var progressPercentage = 0f
    private var borderColor = ContextCompat.getColor(context, R.color.purple_200)
    private var borderWidth = resources.getDimensionPixelSize(R.dimen.circular_progress_width)

    private var text = "--%" // Initial text
    private var textColor = ContextCompat.getColor(context, R.color.purple_200)
    private var textSize = resources.getDimensionPixelSize(R.dimen.circular_progress_text_size).toFloat()


    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = borderColor
        strokeWidth = borderWidth.toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint().apply {
        color = textColor
        textSize = this@CircularProgressBar.textSize
        textAlign = Paint.Align.CENTER
    }

    fun setProgressPercentage(percentage: Float) {
        progressPercentage = percentage.coerceIn(0f, 100f)
        text = "${percentage.toInt()}%" // Update text with percentage value
        invalidate() // Request a redraw
    }


    // Method to reset the progress to default value
    fun resetProgress() {
        setProgressPercentage(0f)
        text = "--%"
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = kotlin.math.min(centerX, centerY)

        // Draw progress arc
        val startAngle = -90f
        val sweepAngle = 360 * progressPercentage / 100f
        canvas.drawArc(
            centerX - radius + borderWidth / 2f,
            centerY - radius + borderWidth / 2f,
            centerX + radius - borderWidth / 2f,
            centerY + radius - borderWidth / 2f,
            startAngle,
            sweepAngle,
            false,
            progressPaint
        )

        // Draw text in the center
        val textHeightOffset = (textPaint.descent() + textPaint.ascent()) / 2
        // Make the grade text bold
        val originalTypeface = textPaint.typeface
        textPaint.typeface = Typeface.create(originalTypeface, Typeface.BOLD)

        canvas.drawText(text, centerX, centerY - textHeightOffset, textPaint)

        textPaint.typeface = originalTypeface

    }
}

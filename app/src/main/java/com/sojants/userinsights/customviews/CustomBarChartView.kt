package com.sojants.userinsights.customviews

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.sojants.userinsights.R
import com.sojants.userinsights.database.SqliteDatabase

class CustomBarChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val barPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.darkblue)

        style = Paint.Style.FILL
    }
    private val labelPaint = Paint().apply {
        color = if (isDarkTheme()) Color.WHITE else Color.BLACK
        textSize = 30f
    }
    private val axisPaint = Paint().apply {
        color = if (isDarkTheme()) Color.WHITE else Color.BLACK
        strokeWidth = 3f
    }
    private val countPaint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }
    private val ageGroups = listOf("5-10", "11-20", "21-30", "31-40", "40+")
    private val ageRanges = listOf(Pair(5, 10), Pair(11, 20), Pair(21, 30), Pair(31, 40), Pair(41, 100))
    private var userCounts: List<Int> = listOf()

    init {
        val db = SqliteDatabase(context)
        userCounts = ageRanges.map { range ->
            db.getCountByAgeRange(range.first, range.second)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAxes(canvas)
        drawBars(canvas)
        drawYAxisLabels(canvas)
    }

    private fun drawAxes(canvas: Canvas) {
        // draw Y-axis
        canvas.drawLine(100f, 50f, 100f, height - 100f, axisPaint)
        // draw X-axis
        canvas.drawLine(100f, height - 100f, width.toFloat() - 50f, height - 100f, axisPaint)
    }

    private fun drawBars(canvas: Canvas) {
        val barWidth = 70f
        val maxBarHeight = height - 200f
        val maxValue = userCounts.maxOrNull()?.toFloat() ?: 1f

        userCounts.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * maxBarHeight
            val left = 150f + (index * (barWidth + 40f))
            val top = height - 100f - barHeight
            val right = left + barWidth
            val bottom = height - 100f
            canvas.drawRect(left, top, right, bottom, barPaint)
            canvas.drawText(ageGroups[index], left + (barWidth / 4), height - 50f, labelPaint)
            // draw count inside the bar
            canvas.drawText(value.toString(), left + barWidth / 2, top + barHeight / 2, countPaint)
        }
    }

    private fun drawYAxisLabels(canvas: Canvas) {
        val maxBarHeight = height - 200f
        val maxValue = userCounts.maxOrNull()?.toFloat() ?: 1f
        val intervalHeight = maxBarHeight / maxValue

        for (i in 0..maxValue.toInt() step 5) {
            val yPos = height - 100f - (i * intervalHeight)
            canvas.drawText(i.toString(), 50f, yPos + 10f, labelPaint)
        }
    }

    private fun isDarkTheme(): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
}


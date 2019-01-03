package com.anwesh.uiprojects.axisstepview

/**
 * Created by anweshmishra on 03/01/19.
 */

import android.content.Context
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.View
import android.view.MotionEvent

val nodes : Int = 5
val axises : Int = 2
val scDiv : Double = 0.51
val scGap : Float = 0.05f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.8f
val foreColor : Int = Color.parseColor("#43A047")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateScale(dir : Float, a : Int, b : Int) : Float =  mirrorValue(a, b) * dir * scGap

fun Canvas.drawASNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val sc11 : Float = sc1.divideScale(0, 2)
    val sc12 : Float = sc1.divideScale(1, 2)
    val arrowSize : Float = size / 5
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h / 2)
    translate(-size, -size)
    drawLine(0f, 0f, 2 * size * sc2, 2 * size * sc2, paint)
    for (j in 0..(axises - 1)) {
        val scj1 : Float = sc11.divideScale(j, axises)
        val scj2 : Float = sc12.divideScale(j, axises)
        save()
        rotate(90f * j)
        drawLine(0f, 0f, 0f, -2 * size * scj1, paint)
        save()
        translate(0f, -(2 * size - arrowSize))
        for (j in 0..(axises - 1)) {
            save()
            rotate(45f * (1 - 2 * j) * scj2)
            drawLine(0f, 0f, 0f, -arrowSize)
            restore()
        }
        restore()
        restore()
    }
    restore()
}
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
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
val DELAY : Long = 25
val sqSize : Float = 2f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateScale(dir : Float, a : Int, b : Int) : Float =  mirrorValue(a, b) * dir * scGap

fun Canvas.drawASNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val sc11 : Float = sc1.divideScale(0, 2)
    val sc12 : Float = sc1.divideScale(1, 2)
    val arrowSize : Float = size / 4
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, gap * (i + 1) + h/10)
    translate(-size, -size)
    drawLine(0f, 0f, 1.75f * size * sc2, -1.75f * size * sc2, paint)
    for (j in 0..(axises - 1)) {
        val scj1 : Float = sc11.divideScale(j, axises)
        val scj2 : Float = sc12.divideScale(j, axises)
        save()
        rotate(90f * j)
        drawLine(0f, 0f, 0f, -2 * size * scj1, paint)
        if (scj2 > 0) {
            save()
            translate(0f, -2 * size)
            for (j in 0..(axises - 1)) {
                save()
                rotate(60f * (1 - 2 * j) * scj2)
                drawLine(0f, 0f, 0f, arrowSize, paint)
                restore()
            }
            restore()
        }
        restore()
    }
    restore()
}

fun Canvas.drawSquare(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes  + 1)
    val size : Float = gap / sqSize
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = foreColor
    save()
    translate(w/2, gap * (i + 1))
    for (j in 0..3) {
        save()
        rotate(90f * j)
        drawLine(size, -size, size, -size + 2 * size * scale, paint)
        restore()
    }
    restore()
}

class AxisStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, axises * axises, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SineState(var scale : Float = 0f, var deg : Double = 0.0, var prevDeg : Double = 0.0, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            deg += Math.PI/18
            scale = Math.abs(Math.sin(deg).toFloat())
            if (Math.abs(deg - prevDeg) > Math.PI/2) {
                deg = prevDeg + Math.PI/2
                scale = Math.sin(deg).toFloat()
                dir = 0f
                prevDeg = deg
                cb(scale)
            }
        }

        fun startUpdating() {
            if (dir == 0f) {
                dir = 1f
            }
        }
    }

    data class SquareNode(var i : Int, val state : SineState = SineState()) {

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSquare(i, state.scale, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating() {
            state.startUpdating()
        }
    }

    data class ASNode(var i : Int, val state : State = State()) {

        private var next : ASNode? = null
        private var prev : ASNode? = null
        private var sqNode : SquareNode = SquareNode(i)

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = ASNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawASNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
            sqNode?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                sqNode.startUpdating()
            }
            if (state.scale == 0f || state.scale == 1f) {
                sqNode.update(cb)
            }

        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : ASNode {
            var curr : ASNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class AxisStep(var i : Int) {

        private var curr : ASNode = ASNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : AxisStepView) {
        private val animator : Animator = Animator(view)
        private val axisStep : AxisStep = AxisStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            axisStep.draw(canvas, paint)
            animator.animate {
                axisStep.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            axisStep.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : AxisStepView {
            val view : AxisStepView = AxisStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
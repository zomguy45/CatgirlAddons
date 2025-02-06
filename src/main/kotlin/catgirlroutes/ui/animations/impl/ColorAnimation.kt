package catgirlroutes.ui.animations.impl

import catgirlroutes.utils.render.Color
import java.awt.Color as JavaColor

// TODO: fix it
class ColorAnimation(duration: Long) {

    private val anim = LinearAnimation<Int>(duration) // temporary fix to weird colors

    fun start(bypass: Boolean = false): Boolean {
        return anim.start(bypass)
    }

    fun isAnimating(): Boolean {
        return anim.isAnimating()
    }

    fun percent(): Int {
        return anim.getPercent()
    }

    fun get(start: Color, end: Color, reverse: Boolean): JavaColor {
        return Color(
            anim.get(start.r, end.r, reverse),
            anim.get(start.g, end.g, reverse),
            anim.get(start.b, end.b, reverse),
            anim.get(start.a, end.a, reverse) / 255f,
        ).javaColor
    }
}
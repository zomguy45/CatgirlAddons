package catgirlroutes.ui.misc.elements

import java.awt.Color

abstract class MiscElement(open val style: MiscElementStyle) {
    var value: String // todo make better idk
        get() = style.value
        set(v) { updates.value = v }

    var x: Double
        get() = style.x
        set(v) { updates.x = v }

    var y: Double
        get() = style.y
        set(v) { updates.y = v }

    var width: Double
        get() = style.width
        set(v) { updates.width = v }

    var height: Double
        get() = style.height
        set(v) { updates.height = v }

    var thickness: Double
        get() = style.thickness
        set(v) { updates.thickness = v }

    var radius: Double
        get() = style.radius
        set(v) { updates.radius = v }

    var colour: Color
        get() = style.colour
        set(v) { updates.colour = v }

    var outlineColour: Color
        get() = style.outlineColour
        set(v) { updates.outlineColour = v }

    var outlineHoverColour: Color
        get() = style.outlineHoverColour
        set(v) { updates.outlineHoverColour = v }

    val updates: MiscElementStyle get() = style

    open fun render(mouseX: Int, mouseY: Int) {  }
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean = false
    open fun mouseClickMove(mouseX: Int, mouseY: Int, mouseButton: Int, timeSinceLastClick: Long) {  }
    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean = false
    open fun onScroll(amount: Int): Boolean = false

    open fun isHovered(mouseX: Int, mouseY: Int, xOff: Double = 0.0, yOff: Double = 0.0): Boolean {
        return mouseX >= x + xOff && mouseX <= x + width + xOff &&
                mouseY >= y + yOff && mouseY <= y + height + yOff
    }

    companion object {
        var currentlyFocused: MiscElement? = null
    }
}

abstract class ElementDSL<T : MiscElement> {
    val _style = MiscElementStyle()

    var _x by _style::x
    var _y by _style::y
    var width by _style::width
    var height by _style::height
    var thickness by _style::thickness
    var radius by _style::radius
    var colour by _style::colour
    var outlineColour by _style::outlineColour
    var outlineHoverColour by _style::outlineHoverColour
    var alignment by _style::alignment
    var vAlignment by _style::vAlignment
    var textPadding by _style::textPadding

    fun at(x: Number, y: Number) {
        this._x = x.toDouble()
        this._y = y.toDouble()
    }

    fun size(width: Number, height: Number) {
        this.width = width.toDouble()
        this.height = height.toDouble()
    }

    protected open fun createStyle(): MiscElementStyle = _style
    protected abstract fun buildElement(): T
    fun build(): T = buildElement()
}

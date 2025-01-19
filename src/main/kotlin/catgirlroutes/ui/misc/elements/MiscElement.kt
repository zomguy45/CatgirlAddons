package catgirlroutes.ui.misc.elements

abstract class MiscElement(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 100.0,
    var height: Double = 20.0
) { // todo rewrite a bit

    abstract fun render(mouseX: Int, mouseY: Int)

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean { return false }

    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {  }

    open fun otherComponentClick() {  }

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }

    open fun isHovered(mouseX: Int, mouseY: Int, xOff: Int = 0, yOff: Int = 0): Boolean {
        return mouseX >= x + xOff && mouseX <= x + width + xOff &&
                mouseY >= y + yOff && mouseY <= y + height + yOff
    }
}
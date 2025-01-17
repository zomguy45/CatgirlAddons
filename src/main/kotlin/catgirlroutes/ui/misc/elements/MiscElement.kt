package catgirlroutes.ui.misc.elements

abstract class MiscElement(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 100.0,
    var height: Double = 20.0
) { // todo rewrite a bit

    abstract fun render(x: Double = this.x, y: Double = this.y)

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean { return false }

    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {  }

    open fun otherComponentClick() {  }

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }
}
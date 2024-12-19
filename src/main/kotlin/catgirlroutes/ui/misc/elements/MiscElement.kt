package catgirlroutes.ui.misc.elements

import net.minecraft.client.gui.Gui

abstract class MiscElement : Gui() {

    abstract fun render(x: Int, y: Int)

    abstract fun getWidth(): Int

    abstract fun getHeight(): Int

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean { return false }

    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {  }

    open fun otherComponentClick() {  }

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }
}
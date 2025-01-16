package catgirlroutes.ui.misc.elements

import net.minecraft.client.gui.Gui

abstract class MiscElement : Gui() { // todo rewrite a bit

    abstract fun render(x: Int = 0, y: Int = 0)

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean { return false }

    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {  }

    open fun otherComponentClick() {  }

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }
}
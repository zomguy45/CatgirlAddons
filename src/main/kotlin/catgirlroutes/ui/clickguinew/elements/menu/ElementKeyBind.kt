package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton

class ElementKeyBind(parent: ModuleButton, setting: KeyBindSetting) :
    Element<KeyBindSetting>(parent, setting, ElementType.KEY_BIND) {

    private val keyBlackList = intArrayOf()

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return super.keyTyped(typedChar, keyCode)
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute + width - 13 && mouseX <= xAbsolute + width - 1 && mouseY >= yAbsolute && mouseY <= yAbsolute + height - 2
    }
}
package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton

class ElementDropdown(parent: ModuleButton, setting: DropdownSetting) :
    Element<DropdownSetting>(parent, setting, ElementType.DROPDOWN) {

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute + width - 13 && mouseX <= xAbsolute + width - 1 && mouseY >= yAbsolute && mouseY <= yAbsolute + height - 2
    }
}
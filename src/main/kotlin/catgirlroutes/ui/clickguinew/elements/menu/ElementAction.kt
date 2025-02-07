package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.ui.misc.elements.impl.MiscElementButton

class ElementAction(parent: ModuleButton, setting: ActionSetting) :
    Element<ActionSetting>(parent, setting, ElementType.ACTION) {

    private val actionButton = MiscElementButton(
        displayName.capitalizeOnlyFirst(),
        width = width,
        height = height,
        thickness = 1.0,
        radius = 3.0
    ) {
        this.setting.doAction()
    }

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        this.actionButton.render(mouseX - xAbsolute.toInt(), mouseY - yAbsolute.toInt())
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return this.actionButton.mouseClicked(mouseX - xAbsolute.toInt(), mouseY - yAbsolute.toInt(), mouseButton)
    }
}
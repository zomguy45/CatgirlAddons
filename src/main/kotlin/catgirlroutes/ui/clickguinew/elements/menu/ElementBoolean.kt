package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.ui.misc.elements.impl.boolean
import catgirlroutes.ui.misc.elements.util.update
import java.awt.Color

class ElementBoolean(parent: ModuleButton, setting: BooleanSetting) :
    Element<BooleanSetting>(parent, setting, ElementType.BOOLEAN) {

    private val booleanElement = boolean {
        text = displayName
        enabled = setting.enabled
        width = 10.0
        height = 10.0
        thickness = 1.0
        radius = 3.0
        gap = 0.0
    } onChange {
        this.setting.toggle()
    }

    override fun renderElement(): Double {
        this.booleanElement.update { // FIXME
            outlineColour = Color(ColorUtil.outlineColor)
            outlineHoverColour = ColorUtil.clickGUIColor
        }.render(mouseXRel, mouseYRel)
        return super.renderElement()
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        return this.booleanElement.mouseClicked(mouseXRel, mouseYRel, mouseButton)
    }
}
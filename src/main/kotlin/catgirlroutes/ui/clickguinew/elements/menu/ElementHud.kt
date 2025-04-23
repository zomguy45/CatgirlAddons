package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.HudSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.ui.misc.elements.impl.boolean
import catgirlroutes.ui.misc.elements.util.update
import java.awt.Color

class ElementHud(parent: ModuleButton, setting: HudSetting) :
    Element<HudSetting>(parent, setting, ElementType.HUD) {

    private val booleanElement = boolean {
        size(10.0, 10.0)
        text = displayName
        enabled = setting.enabled
        gap = 0.0
    } onChange {
        this.setting.enabled = !this.setting.enabled
    }

    override fun renderElement(): Double {
        if (height == -5.0) return super.renderElement()
        this.booleanElement.update { // FIXME
            outlineColour = Color(ColorUtil.outlineColor)
            outlineHoverColour = ColorUtil.clickGUIColor
        }.render(mouseXRel, mouseYRel)
        return super.renderElement()
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (height == -5.0) return false
        return this.booleanElement.mouseClicked(mouseXRel, mouseYRel, mouseButton)
    }
}
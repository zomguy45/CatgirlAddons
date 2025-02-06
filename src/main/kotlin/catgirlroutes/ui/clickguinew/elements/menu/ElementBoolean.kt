package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import java.awt.Color

class ElementBoolean(parent: ModuleButton, setting: BooleanSetting) :
    Element<BooleanSetting>(parent, setting, ElementType.BOOLEAN) {

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        val buttonColor = if (this.setting.enabled) ColorUtil.clickGUIColor else Color(ColorUtil.buttonColor)
        drawRoundedBorderedRect(
            width - 10.0, 0.0, 10.0, 10.0, 3.0, 1.0,
            Color(ColorUtil.elementColor), if (this.isHovered(mouseX, mouseY)) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )

        if (this.setting.enabled) drawRoundedRect(width - 10.0, 0.0, 10.0, 10.0, 3.0, ColorUtil.clickGUIColor)

        FontUtil.drawString(displayName, 0.0, 0.0)
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && this.isHovered(mouseX, mouseY)) {
            this.setting.toggle()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute + width - 10 && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + 10.0
    }

}
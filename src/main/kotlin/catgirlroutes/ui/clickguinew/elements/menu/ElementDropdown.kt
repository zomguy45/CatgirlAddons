package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.ui.animations.impl.EaseOutQuadAnimation
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import java.awt.Color

class ElementDropdown(parent: ModuleButton, setting: DropdownSetting) : // todo anim, icon
    Element<DropdownSetting>(parent, setting, ElementType.DROPDOWN) {

    private var extraHeight = 0.0
    private var extendAnimation = EaseOutQuadAnimation(500)

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
//        val height = if (this.setting.enabled)
        drawRoundedBorderedRect(-3.0, 0.0, width + 6.0, height + this.extraHeight, 3.0, 1.0, Color(ColorUtil.outlineColor).darker(), ColorUtil.clickGUIColor)

        if (this.setting.enabled) {
            FontUtil.drawString("⬇", width - 11, 3.0)
        } else {
            FontUtil.drawString("⬅", width - 13, 3.0)
        }
        FontUtil.drawString(displayName, 0.0, 3.0)
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            this.setting.enabled = !this.setting.enabled
            this.parent.updateElements()
            this.extraHeight = if (this.setting.enabled)
                this.parent.elementsHeight - this.parent.prevHeight
            else 0.0
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }
}
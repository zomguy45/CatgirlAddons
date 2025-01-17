package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.render.HUDRenderUtils
import java.awt.Color

class MiscElementButton(
    var name: String = "",
    x: Double,
    y: Double,
    width: Double = 80.0,
    height: Double = 20.0,
    var thickness: Double = 2.0,
    var action: () -> Unit
) : MiscElement(x, y, width, height) {

    var hovering = false

    override fun render(x: Double, y: Double) {
        HUDRenderUtils.drawRoundedBorderedRect(
            this.x, this.y, this.width, this.height, 5.0, thickness,
            Color(ColorUtil.elementColor), if (this.hovering) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        FontUtil.drawString(name, x + width / 2.0 - FontUtil.getStringWidth(name) / 2.0, y + height / 2.0 - FontUtil.fontHeight / 2.0)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            action()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun isHovered(mouseX: Int, mouseY: Int, xOff: Int = 0, yOff: Int = 0): Boolean {
        hovering = mouseX >= x + xOff && mouseX <= x + width + xOff &&
                mouseY >= y + yOff && mouseY <= y + height + yOff

        return hovering
    }
}
package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.render.HUDRenderUtils
import java.awt.Color

class MiscElementButton(
    var name: String = "",
    var x: Double,
    var y: Double,
    var width: Double = 10.0,
    var height: Double = 10.0,
    var action: () -> Unit
) : MiscElement() {

    private val hovering = false

    override fun render(x: Int, y: Int) {
        HUDRenderUtils.drawRoundedBorderedRect(
            this.x, this.y, this.width, this.height, 5.0, 1.0,
            Color(ColorUtil.elementColor), if (this.hovering) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        FontUtil.drawString(name, x, y)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            action()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun isHovering(mouseX: Int, mouseY: Int, xOff: Int = 0, yOff: Int = 0): Boolean {
        return mouseX >= x + xOff && mouseX <= x + width + xOff &&
                mouseY >= y + yOff && mouseY <= y + height + yOff
    }
}
package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import java.awt.Color

/**
 * A [MiscElement] representing a boolean toggle with customizable text and style.
 *
 * @param x The x-coordinate position (default is 0.0).
 * @param y The y-coordinate position (default is 0.0).
 * @param width The width of the element (default is 15.0).
 * @param height The height of the element (default is 15.0).
 * @param text The text displayed on the element (default is none).
 * @param enabled The initial state of the boolean toggle (default is false).
 * @param thickness The border thickness (default is 2.0).
 * @param radius The radius of the corners (default is 5.0).
 */
class MiscElementBoolean(
    x: Double = 0.0,
    y: Double = 0.0,
    width: Double = 15.0,
    height: Double = 15.0,
    var text: String = "",
    var enabled: Boolean = false,
    var thickness: Double = 2.0,
    var radius: Double = 5.0
): MiscElement(x, y, width, height) {

    override fun render(mouseX: Int, mouseY: Int) {
        drawRoundedBorderedRect(
            this.x, this.y, this.width, this.height, this.radius, this.thickness,
            Color(ColorUtil.elementColor), if (this.isHovered(mouseX, mouseY)) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        if (this.enabled) drawRoundedRect(this.x + 1.0, this.y + 1.0, this.width - 2.0, this.height - 2.0, this.radius, ColorUtil.clickGUIColor)

        FontUtil.drawString(this.text, this.x + this.width + 5.0, this.y + this.height / 2 - FontUtil.fontHeight / 2)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (this.isHovered(mouseX, mouseY) && mouseButton == 0) this.enabled = !this.enabled
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

}
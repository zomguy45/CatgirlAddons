package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class MiscElementButton(
    var name: String,
    x: Double = 0.0,
    y: Double = 0.0,
    width: Double = 80.0,
    height: Double = 20.0,
    var thickness: Double = 2.0,
    var radius: Double = 5.0,
    var action: () -> Unit
) : MiscElement(x, y, width, height) {

    override fun render(mouseX: Int, mouseY: Int) {
        GlStateManager.pushMatrix()
        HUDRenderUtils.drawRoundedBorderedRect(
            this.x, this.y, this.width, this.height, this.radius, this.thickness,
            Color(ColorUtil.elementColor), if (this.isHovered(mouseX, mouseY)) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        FontUtil.drawTotalCenteredString(name, x + width / 2.0, y + height / 2.0)
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            action()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
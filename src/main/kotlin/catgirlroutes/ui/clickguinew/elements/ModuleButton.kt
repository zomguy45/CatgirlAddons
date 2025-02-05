package catgirlroutes.ui.clickguinew.elements

import catgirlroutes.module.Module
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickguinew.Window
import catgirlroutes.utils.render.HUDRenderUtils.drawOutlinedRectBorder
import catgirlroutes.utils.wrapText
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class ModuleButton(val module: Module, val window: Window) {
    val menuElements: ArrayList<Element<*>> = ArrayList()

    var x = 0.0
    var y = 0.0

    val width = window.width / 2
    val height: Double
        get() = 25.0 + fontHeight * description.size

    var extended = false

    val xAbsolute: Double
        get() = x + window.x
    val yAbsolute: Double
        get() = y + window.y

    private val description: List<String> = wrapText(this.module.description, this.width - 50.0)

    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) : Double {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0)
        drawOutlinedRectBorder(0.0, 0.0, width, height, 3.0, 1.0, Color(ColorUtil.outlineColor))
        FontUtil.drawStringWithShadow(module.name, 5.0, 3.0)
        this.description.forEachIndexed { i, it ->
            FontUtil.drawStringWithShadow(it, 7.0, 5.0 + fontHeight * (i + 1), Color.LIGHT_GRAY.rgb)
        }


        var offs = this.height + 5.0
        if (extended && menuElements.isNotEmpty()) {
            for (menuElement in menuElements) {
                menuElement.y = offs
                menuElement.update()

                offs += menuElement.drawScreen(mouseX, mouseY, partialTicks)
            }
        }

        GlStateManager.popMatrix()

        return offs
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {

    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return false
    }

    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }

    private fun isMouseUnderButton(mouseX: Int, mouseY: Int): Boolean {
        if (!extended) return false
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY > yAbsolute + height
    }


}
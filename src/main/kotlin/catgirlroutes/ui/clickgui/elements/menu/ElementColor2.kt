package catgirlroutes.ui.clickgui.elements.menu

import catgirlroutes.module.settings.impl.ColorSetting2
import catgirlroutes.ui.clickgui.elements.Element
import catgirlroutes.ui.clickgui.elements.ElementType
import catgirlroutes.ui.clickgui.elements.ModuleButton
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.Gui

class ElementColor2(parent: ModuleButton, setting: ColorSetting2) :
    Element<ColorSetting2>(parent, setting, ElementType.COLOR2) {
    var dragging: Int? = null

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Int {

        val colorValue = setting.value.rgb

        FontUtil.drawString(displayName, 1, 2)

        /** Render the color preview */
        Gui.drawRect(width - 26, 2, width - 1, 11, colorValue)

        /** Render the tab indicating the drop-down */
//        Gui.drawRect(0,  13, width, 15, ColorUtil.tabColorBg)
//        Gui.drawRect((width * 0.4).toInt(), 12, (width * 0.6).toInt(), 15, ColorUtil.tabColor)

        if (extended) {
            HUDRenderUtils.drawSBBox(0, 15, width, height, this.setting.value.rgb) // todo: hue instead
        }

        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isButtonHovered(mouseX, mouseY)) {
                extended = !extended
                return true
            }

            if (!extended) return false
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return super.keyTyped(typedChar, keyCode)
    }

    /**
     * Checks whether the mouse is hovering the selector
     */
    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + 15
    }

}
package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import net.minecraft.util.MathHelper
import java.awt.Color
import kotlin.math.roundToInt

class ElementSlider(parent: ModuleButton, setting: NumberSetting) :
    Element<NumberSetting>(parent, setting, ElementType.SLIDER) {
    var dragging: Boolean = false

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        val displayValue = "" + (this.setting.value * 100.0).roundToInt() / 100.0
        val percentBar = (this.setting.value - this.setting.min) / (this.setting.max - this.setting.min)

        FontUtil.drawString("$displayName: $displayValue", 0.0, 0.0)

        drawRoundedBorderedRect(0.0, fontHeight + 5.0, width, 2.0, 2.0, 1.0, ColorUtil.clickGUIColor,  ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(percentBar * width - 3.0, fontHeight + 3.0, 6.0, 6.0, 3.0, 1.0, Color(ColorUtil.buttonColor),  ColorUtil.clickGUIColor)

        if (this.dragging) {
            val diff = this.setting.max - this.setting.min
            val newVal = this.setting.min + MathHelper.clamp_double(((mouseX - xAbsolute) / width), 0.0, 1.0) * diff
            this.setting.value = newVal
        }

        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            this.dragging = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        this.dragging = false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return super.keyTyped(typedChar, keyCode)
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute + fontHeight + 3.0 && mouseY <= yAbsolute + height - fontHeight + 2.0
    }
}
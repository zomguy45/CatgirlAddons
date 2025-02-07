package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import java.awt.Color

class ElementSelector(parent: ModuleButton, setting: StringSelectorSetting) :
    Element<StringSelectorSetting>(parent, setting, ElementType.SELECTOR) {

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        val displayValue = "$displayName: ${this.setting.selected}"

        drawRoundedBorderedRect(0.0, 0.0, width, height, 3.0, 1.0, Color(ColorUtil.buttonColor),  ColorUtil.clickGUIColor)
        FontUtil.drawTotalCenteredString(displayValue, width / 2.0, height / 2.0)

//        if (extended) {
//            this.setting.options.forEachIndexed { i, option ->
//                if (this.setting.isSelected(option) || this.isHovered(mouseX, mouseY, yOff = height.toInt() * (i + 1))) {
//                    drawRoundedBorderedRect(0.0, height * (i + 1), width, height, 5.0, 1.0, ColorUtil.clickGUIColor, ColorUtil.clickGUIColor)
//                }
//
//                FontUtil.drawTotalCenteredString(option.capitalizeOnlyFirst(), width / 2.0, height / 2.0 + (height / 2.0 * (i + 1)))
//            }
//        }


        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (this.isHovered(mouseX, mouseY)) {
                this.setting.index += 1
                return true
            }

            if (!extended) return false

            this.setting.options.forEachIndexed { i, option ->
                val yOff = i * height.toInt()
                if (this.isHovered(mouseX, mouseY, yOff = yOff)) {
                    this.setting.selected = option
                    return true
                }
            }
        } else if (mouseButton == 1 && this.isHovered(mouseX, mouseY)) {
//            extended = !extended
//            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun isHovered(mouseX: Int, mouseY: Int, xOff: Int = 0, yOff: Int = 0): Boolean {
        return mouseX >= xAbsolute + xOff && mouseX <= xAbsolute + width + xOff &&
                mouseY >= yAbsolute + yOff && mouseY <= yAbsolute + height + yOff
    }
}
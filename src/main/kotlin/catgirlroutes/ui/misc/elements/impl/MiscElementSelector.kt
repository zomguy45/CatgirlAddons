package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.util.MathHelper
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

/**
 * @see StringSelectorSetting
 */
class MiscElementSelector(
    var name: String,
    var defaultSelected: String,
    var options: ArrayList<String>,
    x: Double,
    y: Double,
    width: Double = 80.0,
    height: Double = 20.0,
    var thickness: Double = 2.0
) : MiscElement(x, y, width, height) {

    var extended = false

    var index: Int = optionIndex(defaultSelected)
        set(newVal) {
            field = if (newVal > options.size - 1)  0 else if ( newVal < 0) options.size - 1 else newVal
        }
    var selected: String
        set(value) {
            index = optionIndex(value)
        }
        get() {
            return options[index]
        }

    private fun optionIndex(string: String): Int {
        return MathHelper.clamp_int(this.options.map { it.lowercase() }.indexOf(string.lowercase()), 0, options.size - 1)
    }

    fun isSelected(string: String): Boolean {
        return this.selected.equals(string, ignoreCase = true)
    }


    override fun render(mouseX: Int, mouseY: Int, x: Double, y: Double) { // todo add some indicator it's a selector
        HUDRenderUtils.drawRoundedBorderedRect(
            this.x, this.y, this.width, if (extended) this.height * (this.options.size + 1) else this.height, 5.0, this.thickness,
            Color(ColorUtil.elementColor), if (extended || isHovered(mouseX, mouseY)) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        FontUtil.drawTotalCenteredString(this.selected, x + width / 2.0, y + height / 2.0)

        if (extended) {
            this.options.forEachIndexed { i, option ->

                if (this.isSelected(option)) {
                    HUDRenderUtils.drawRoundedRect(this.x, this.y + (this.width / 4.0 * (i + 1).toDouble()), this.width, this.height, 5.0, ColorUtil.clickGUIColor)
                }

                if (this.isHovered(mouseX, mouseY, yOff = this.width.toInt() / 4 * (i + 1))) {
                    HUDRenderUtils.drawRoundedRect(this.x, this.y + (this.width / 4.0 * (i + 1).toDouble()), this.width, this.height, 5.0, ColorUtil.clickGUIColor)
                }

                val optionName = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                FontUtil.drawTotalCenteredString(optionName, this.x + width / 2.0, this.y + height / 2.0 + (this.width / 4.0 * (i + 1).toDouble()))
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isHovered(mouseX, mouseY)) {
                this.index += 1
                return true
            }

            if (!extended) return false

            this.options.forEachIndexed { i, option ->
                if (this.isHovered(mouseX, mouseY, yOff = this.width.toInt() / 4 * (i + 1))) {
                    this.selected = option
                    return true
                }
            }
        } else if (mouseButton == 1 && isHovered(mouseX, mouseY)) {
            this.extended = !extended
            return true
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
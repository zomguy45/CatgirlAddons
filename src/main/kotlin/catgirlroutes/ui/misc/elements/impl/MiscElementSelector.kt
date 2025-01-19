package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.ChatUtils.debugMessage
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
    x: Double = 0.0,
    y: Double = 0.0,
    private val vertical: Boolean = true,
    width: Double = 80.0,
    height: Double = 20.0,
    var thickness: Double = 2.0,
    val gap: Int = 5
) : MiscElement(x, y, width, height) {

    private var extended = false

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


    override fun render(mouseX: Int, mouseY: Int) { // todo add some indicator it's a selector
        if (vertical) {
            HUDRenderUtils.drawRoundedBorderedRect(
                this.x, this.y, this.width, if (extended) this.height * (this.options.size + 1) else this.height, 5.0, this.thickness,
                Color(ColorUtil.elementColor), if (extended || isHovered(mouseX, mouseY)) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
            )
            FontUtil.drawTotalCenteredString(this.selected, x + this.width / 2.0, y + this.height / 2.0)

            if (!extended) return
            this.options.forEachIndexed { i, option ->

                if (this.isSelected(option) || this.isHovered(mouseX, mouseY, yOff = this.height.toInt() * (i + 1))) {
                    HUDRenderUtils.drawRoundedRect(this.x, this.y + (this.height * (i + 1)), this.width, this.height, 5.0, ColorUtil.clickGUIColor)
                }

                val optionName = option.substring(0, 1).uppercase(Locale.getDefault()) + option.substring(1, option.length)
                FontUtil.drawTotalCenteredString(optionName, this.x + this.width / 2.0, this.y + height / 2.0 + (this.width / 4.0 * (i + 1)))
            }
            return
        }

        this.options.forEachIndexed { i, option ->
            val xPos = this.x + i * this.width + i * this.gap
            HUDRenderUtils.drawRoundedBorderedRect(
                xPos, this.y, this.width, this.height, 5.0, this.thickness,
                Color(ColorUtil.elementColor), if (this.isSelected(option)) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
            )

            if (this.isSelected(option) || this.isHovered(mouseX, mouseY, xOff = (xPos - this.x).toInt())) {
                HUDRenderUtils.drawOutlinedRectBorder(xPos, this.y, this.width, this.height, 5.0, this.thickness, ColorUtil.clickGUIColor)
            }

            FontUtil.drawTotalCenteredString(option.capitalizeOnlyFirst(), this.x + this.width / 2.0 + xPos - this.x, this.y + height / 2.0)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0) {
            if (isHovered(mouseX, mouseY) && this.vertical) {
                this.index += 1
                return true
            }

            if (!extended && this.vertical) return false

            this.options.forEachIndexed { i, option ->
                val isOptionHovered = if (this.vertical)
                    this.isHovered(mouseX, mouseY, yOff = this.height.toInt() * (i + 1))
                else
                    this.isHovered(mouseX, mouseY, xOff = i * this.width.toInt() + i * this.gap)

                if (isOptionHovered) {
                    this.selected = option
                    return true
                }
            }
        } else if (mouseButton == 1 && isHovered(mouseX, mouseY) && this.vertical) {
            this.extended = !extended
            return true
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.ui.clickgui.util.ColorUtil.hex
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.Utils.equalsOneOf
import java.awt.Color

class ElementColor(parent: ModuleButton, setting: ColorSetting) :
    Element<ColorSetting>(parent, setting, ElementType.COLOR) {

    var dragging: Int? = null

    private inline val colorValue: Color
        get() = this.setting.value

    private var hexString = "#${colorValue.hex}"
    private var hexPrev = hexString
    private var listeningHex = false

    private val hMultiplier = if (this.setting.allowAlpha) 8 else 7 // for hex

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return super.keyTyped(typedChar, keyCode)
    }

    private fun completeHexString() {
        if (hexString.isEmpty()) return
        val stringWithoutHash = hexString.removePrefix("#")
        if (stringWithoutHash.length.equalsOneOf(6, 8)) {
            try {
                val alpha = if (stringWithoutHash.length == 8) stringWithoutHash.substring(6, 8).toInt(16) / 255f else 1f
                val red = stringWithoutHash.substring(0, 2).toInt(16) / 255f
                val green = stringWithoutHash.substring(2, 4).toInt(16) / 255f
                val blue = stringWithoutHash.substring(4, 6).toInt(16) / 255f
                setting.value = Color(red, green, blue, alpha)
                val hsb = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

                // tbh should it probably should be in ColorSetting .value but I cba todo: refactor some day
                setting.hue = hsb[0]
                setting.saturation = hsb[1]
                setting.brightness = hsb[2]

                hexPrev = hexString
            } catch (e: Exception) {
                debugMessage(e.toString())
                hexString = hexPrev
            }
        } else {
            hexString = hexPrev
        }
    }

    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + 15
    }

    private fun isHovered(mouseX: Int, mouseY: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
        return mouseX >= xAbsolute + x && mouseX <= xAbsolute + width && mouseY >= yAbsolute + y && mouseY <= yAbsolute + height
    }

    private fun Color.hsbMax(setting: ColorSetting): Color { // the dumbest fix ever
        val hsb = Color.RGBtoHSB(this.red, this.green, this.blue, null)
        if (hsb[1] == 0.0f || hsb[2] == 0.0f) hsb[0] = setting.hue // when saturation or brightness are 0 hue is 0 too for some weird reason... took me a few hours to realise
        return Color.getHSBColor(hsb[0], 1f, 1f)
    }

}
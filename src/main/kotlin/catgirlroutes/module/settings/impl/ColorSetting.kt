package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility
import net.minecraft.util.MathHelper
import java.awt.Color

class ColorSetting(
    name: String,
    override val default: Color,
    var allowAlpha: Boolean = true,
    description: String? = null,
    val collapsible: Boolean = true,
    visibility: Visibility = Visibility.VISIBLE,
) : Setting<Color>(name, description, visibility) {

    override var value: Color = default
        set(value) {
            field = processInput(value)
        }

    private var hsbvals: FloatArray = Color.RGBtoHSB(default.red, default.green, default.blue, null)

    var red: Int
        get() = value.red
        set(input) {
            value = Color(MathHelper.clamp_int(input,0,255), green, blue, (alpha * 255).toInt())
        }

    var green: Int
        get() = value.green
        set(input) {
            value = Color(red, MathHelper.clamp_int(input,0,255), blue, (alpha * 255).toInt())
        }

    var blue: Int
        get() = value.blue
        set(input) {
            value = Color(red, green, MathHelper.clamp_int(input,0,255), (alpha * 255).toInt())
        }

    var hue: Float
        get() {
            return hsbvals[0]
        }
        set(input) {
            hsbvals[0] = input
            updateColor()
        }

    var saturation: Float
        get() {
            return hsbvals[1]
        }
        set(input) {
            hsbvals[1] = input
            updateColor()
        }

    var brightness: Float
        get() {
            return hsbvals[2]
        }
        set(input) {
            hsbvals[2] = input
            updateColor()
        }

    var alpha: Float
        get() = value.alpha / 255f
        set(input) {
            // prevents changing the alpha if not allowed
            if (!allowAlpha) return
            value = Color(red, green, blue, MathHelper.clamp_int((input * 255).toInt(),0,255))
        }

    /**
     * Updates the color stored in value from the hsb values stored in hsbvals
     */
    private fun updateColor() {
        val tempColor =  Color(Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]))
        value = Color(tempColor.red, tempColor.green, tempColor.blue, (alpha * 255).toInt())
    }
}
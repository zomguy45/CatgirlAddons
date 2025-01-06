package catgirlroutes.ui.clickgui.util

import catgirlroutes.module.impl.render.ClickGui
import java.awt.Color

/**
 * Provides color for the click gui.
 *
 * @author Aton
 */
object ColorUtil {
    val clickGUIColor: Color
        get() = ClickGui.color.value

    val elementColor: Int
     get() = if (ClickGui.design.isSelected("New"))
             newColor
         else if (ClickGui.design.isSelected("JellyLike"))
             jellyColor
         else
             0

    val bgColor: Int
        get() = if (ClickGui.design.isSelected("New"))
            newColor
        else if (ClickGui.design.isSelected("JellyLike"))
            Color(255,255,255,50).rgb
        else
            0

    val outlineColor : Int
        get() = clickGUIColor.darker().rgb

    val hoverColor: Int
        get() {
            val temp = clickGUIColor.darker()
            val scale = 0.5
            return Color(((temp.red*scale).toInt()), (temp.green*scale).toInt(), (temp.blue*scale).toInt()).rgb
        }

    val tabColor: Int
        get() = clickGUIColor.withAlpha(150).rgb

    fun sliderColor(dragging: Boolean): Int = clickGUIColor.withAlpha(if (dragging) 250 else 200).rgb

    fun sliderKnobColor(dragging: Boolean): Int = clickGUIColor.withAlpha(if (dragging) 255 else 230).rgb

    fun Color.withAlpha(alpha: Int): Color {
        return Color(red, green, blue, alpha)
    }

    fun Color.withAlpha(alpha: Float): Color {
        return Color(red, green, blue, (alpha / 255).toInt())
    }

    fun Color.hsbMax(): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(this.red, this.green, this.blue, hsb)

        return Color.getHSBColor(hsb[0], 1f, 1f)
    }

    val Color.hex: String
        get() {
            val rgba = (red shl 24) or (green shl 16) or (blue shl 8) or alpha
            return String.format("%08X", rgba)
        }

    const val jellyColor = -0x44eaeaeb
    const val newColor = -0xdcdcdd
    const val moduleButtonColor = -0xe5e5e6
    const val textcolor = -0x101011

    const val jellyPanelColor = -0x555556

    const val tabColorBg = 0x77000000
    const val dropDownColor = -0x55ededee
    const val boxHoverColor = 0x55111111
    const val sliderBackground = -0xefeff0

    const val buttonColor = -0x1000000

}
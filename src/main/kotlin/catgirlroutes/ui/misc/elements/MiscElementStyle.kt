package catgirlroutes.ui.misc.elements

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.Alignment
import catgirlroutes.ui.clickgui.util.VAlignment
import java.awt.Color

data class MiscElementStyle(
    var value: String = "",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 80.0,
    var height: Double = 20.0,
    var radius: Double = 3.0,
    var thickness: Double = 1.0,
    var textColour: Color = Color(ColorUtil.textcolor),
    var colour: Color = ColorUtil.buttonColor,
    var outlineColour: Color = ColorUtil.outlineColor,
    var outlineHoverColour: Color = ColorUtil.clickGUIColor,
    var alignment: Alignment = Alignment.CENTRE,
    var vAlignment: VAlignment = VAlignment.CENTRE,
    var textPadding: Double = 5.0,
    var vTextPadding: Double = 0.0
) {
    fun apply(block: MiscElementStyle.() -> Unit): MiscElementStyle {
        this.block()
        return this
    }
}
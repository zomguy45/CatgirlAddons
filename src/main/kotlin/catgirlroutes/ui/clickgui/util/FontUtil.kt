package catgirlroutes.ui.clickgui.util

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.StringUtils
import java.util.*
import kotlin.math.min


/**
 * Provides methods for rending text.
 *
 * @author Aton
 */
object FontUtil {
    private var fontRenderer: FontRenderer? = null
    fun setupFontUtils() {
        fontRenderer = mc.fontRendererObj
    }

    fun getStringWidth(text: String?, scale: Double = 1.0): Int {
        return (fontRenderer!!.getStringWidth(StringUtils.stripControlCodes(text)) * scale).toInt()
    }

    fun getSplitHeight(text: String, wrapWidth: Int): Int {
        var dy = 0
        for (s in mc.fontRendererObj.listFormattedStringToWidth(text, wrapWidth)) {
            dy += mc.fontRendererObj.FONT_HEIGHT
        }
        return dy
    }

    val fontHeight: Int
        get() = fontRenderer!!.FONT_HEIGHT

    fun getScaledFontHeight(scale: Double = 1.0): Int {
        return (fontHeight * scale).toInt()
    }

    fun drawString(text: String, x: Double, y: Double, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        drawString(text, x.toInt(), y.toInt(), color, scale)
    }

    fun drawString(text: String, x: Int, y: Int, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toDouble(), y.toDouble(), 0.0)
        GlStateManager.scale(scale.toFloat(), scale.toFloat(), 1.0f)
        fontRenderer!!.drawString(text, 0, 0, color)
        GlStateManager.popMatrix()
    }

    fun drawStringWithShadow(text: String, x: Double, y: Double, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0)
        GlStateManager.scale(scale.toFloat(), scale.toFloat(), 1.0f)
        fontRenderer?.drawStringWithShadow(text, 0f, 0f, color)
        GlStateManager.popMatrix()
    }

    fun drawCenteredString(text: String, x: Double, y: Double, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        val scaledX = x - (fontRenderer!!.getStringWidth(text) * scale) / 2
        val scaledY = y
        drawString(text, scaledX, scaledY, color, scale)
    }

    fun drawCenteredStringWithShadow(text: String, x: Double, y: Double, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        val scaledX = x - (fontRenderer!!.getStringWidth(text) * scale) / 2
        val scaledY = y
        drawStringWithShadow(text, scaledX, scaledY, color, scale)
    }

    fun drawTotalCenteredString(text: String, x: Double, y: Double, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        val scaledX = x - (fontRenderer!!.getStringWidth(text) * scale) / 2
        val scaledY = y - (fontRenderer!!.FONT_HEIGHT * scale) / 2
        drawString(text, scaledX, scaledY, color, scale)
    }

    fun drawTotalCenteredStringWithShadow(text: String, x: Double, y: Double, color: Int = ColorUtil.textcolor, scale: Double = 1.0) {
        val scaledX = x - (fontRenderer!!.getStringWidth(text) * scale) / 2
        val scaledY = y - (fontRenderer!!.FONT_HEIGHT * scale) / 2
        drawStringWithShadow(text, scaledX, scaledY, color, scale)
    }

    /**
     * Draws a string with line wrapping.
     */
    fun drawSplitString(text: String, x: Int, y: Int, wrapWidth: Int, color: Int = ColorUtil.textcolor) {
        fontRenderer?.drawSplitString(text, x, y, wrapWidth, color)
    }

    /**
     * Returns a copy of the String where the first letter is capitalized.
     */
    fun String.forceCapitalize(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length)
    }

    /**
     * Returns a copy of the String where the only first letter is capitalized.
     */
    fun String.capitalizeOnlyFirst(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length).lowercase()
    }
}
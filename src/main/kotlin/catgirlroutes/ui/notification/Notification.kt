package catgirlroutes.ui.notification

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class Notification( // todo: add icons for module toggle notifications (maybe?)
    val title: String,
    val description: String,
    val duration: Double,
    val type: NotificationType,
) {
    val startTime: Long = System.currentTimeMillis()
    val endTime: Long = startTime + duration.toLong()

    fun draw(x: Double, y: Double, width: Double, height: Double) {
        GlStateManager.pushMatrix()

        HUDRenderUtils.renderRect(x, y, width, height, Color(ColorUtil.bgColor)) // bg
        HUDRenderUtils.renderRectBorder(x, y, width, height, 1.0, this.type.getColour()) // border
        HUDRenderUtils.renderRect(x, y, width * (System.currentTimeMillis() - startTime) / this.duration, height, this.type.getColour().darker()) // progress

        mc.fontRendererObj.drawStringWithShadow(this.title, x.toFloat() + 3f, y.toFloat() + 5f, Color(255, 255, 255).rgb) // title

        // description
        val lines = wrapText(this.description, width - 12)
        var lineY = y + 10f + mc.fontRendererObj.FONT_HEIGHT
        for (line in lines) {
            mc.fontRendererObj.drawStringWithShadow(line, x.toFloat() + 6f, lineY.toFloat(), Color(255, 255, 255).rgb)
            lineY += mc.fontRendererObj.FONT_HEIGHT + 2
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix()
    }

    fun wrapText(text: String, maxWidth: Double): List<String> { // shit to split long description
        val lines = mutableListOf<String>()
        var currentLine = ""

        text.split(" ").forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (mc.fontRendererObj.getStringWidth(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }

        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
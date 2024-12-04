package catgirlroutes.ui.notification

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class Notification(
    val title: String,
    val description: String,
    val duration: Double,
    val type: NotificationType,
) {
    val time: Long = System.currentTimeMillis()
    val end: Long = time + duration.toLong()

    fun getCurrentTime(): Long {
        return System.currentTimeMillis() - time
    }

    fun draw(x: Double, y: Double, width: Double, height: Double) { // todo: colours and maybe redesign
        GlStateManager.pushMatrix()

        HUDRenderUtils.renderRectBorder(x, y, width, height, 1.0, Color(255, 255, 255))
        HUDRenderUtils.renderRect(x, y, width * this.getCurrentTime() / this.duration, height, Color(0, 0, 0))

        mc.fontRendererObj.drawStringWithShadow(this.title, x.toFloat() + 3f, y.toFloat() + 5f, Color(255, 255, 255).rgb)
        mc.fontRendererObj.drawStringWithShadow(this.description, x.toFloat() + 6f, y.toFloat() + 10f + mc.fontRendererObj.FONT_HEIGHT , Color(255, 255, 255).rgb)

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix()
    }
}
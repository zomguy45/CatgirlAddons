package catgirlroutes.utils.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


/**
 * ## A Collection of methods for rendering 2D Objects in orthographic projection for the HUD or for a gui.
 *
 * ### Coordinate space
 * The coordinate space used by the methods here sees the top left corner of your window as the origin 0,0.
 * The x-axis is pointing towards the right of the screen. and the y-axis is pointing **downwards**.
 *
 *
 * Heavily based on the rendering for [Funny Map by Harry282](https://github.com/Harry282/FunnyMap/blob/master/src/main/kotlin/funnymap/utils/RenderUtils.kt).
 *
 * @author Aton
 */
object HUDRenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.translate(0f, 0f, -10f)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.translate(0f, 0f, 0f)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        GlStateManager.shadeModel(GL11.GL_FLAT)

        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    fun drawTexturedModalRect(x: Int, y: Int, width: Int, height: Int) {
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }

    /**
     * Sets up a GL scissor test for the specified region of the screen.
     *
     * Uses the same coordinate system as all the rendering methods.
     * The native OpenGL method [GL11.glScissor] uses a different coordinate system.
     * This method takes care of the coordinate transform for you.
     * @see setUpScissor
     * @see endScissor
     */
    fun setUpScissorAbsolute(left: Int, top: Int, right: Int, bottom: Int) {
        setUpScissor(left, top, (right - left).coerceAtLeast(0), (bottom - top).coerceAtLeast(0))
    }

    /**
     * Sets up a GL scissor test for the specified region of the screen.
     *
     * Uses the same coordinate system as all the rendering methods.
     * The native OpenGL method [GL11.glScissor] uses a different coordinate system.
     * This method takes care of the coordinate transform for you.
     * @see setUpScissorAbsolute
     * @see endScissor
     */
    fun setUpScissor(x: Int, y: Int, width: Int, height: Int) {
        /*
        glScissor uses different coordinates than all the rendering methods.
        It uses absolute window coordinates starting with 0,0 in the bottom left corner of the window.
        The coordinates directly relate to pixels.
        It is not affected by things such as glTanslate and glScale.

        In contrast, all other hud rendering methods use the top left corner as 0,0
         */
        val scale = mc.displayHeight / ScaledResolution(mc).scaledHeight.toDouble()
        GL11.glScissor(
            (x * scale).toInt(),
            (mc.displayHeight - (height + y) *scale).toInt() ,
            (width*scale).toInt(),
            (height * scale).toInt()
        )
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
    }

    /**
     * Disables the GL scissor test.
     * @see setUpScissor
     * @see setUpScissorAbsolute
     */
    fun endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    fun drawTexturedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        uMin: Float,
        uMax: Float,
        vMin: Float,
        vMax: Float,
        filter: Int
    ) {
        GlStateManager.enableBlend()
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA
        )

        drawTexturedRectNoBlend(x, y, width, height, uMin, uMax, vMin, vMax, filter)

        GlStateManager.disableBlend()
    }

    fun drawTexturedRectNoBlend(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        uMin: Float,
        uMax: Float,
        vMin: Float,
        vMax: Float,
        filter: Int
    ) {
        GlStateManager.enableTexture2D()

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)

        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldRenderer
            .pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex(uMin.toDouble(), vMax.toDouble()).endVertex()
        worldRenderer
            .pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(uMax.toDouble(), vMax.toDouble()).endVertex()
        worldRenderer
            .pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(uMax.toDouble(), vMin.toDouble()).endVertex()
        worldRenderer
            .pos(x.toDouble(), y.toDouble(), 0.0)
            .tex(uMin.toDouble(), vMin.toDouble()).endVertex()
        tessellator.draw()

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
    }
}

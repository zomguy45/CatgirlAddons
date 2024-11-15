package catgirlroutes.utils.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Color.*


/**
 * ## A Collection of Methods for Rendering within the 3D World.
 *
 * This class provides methods for rendering shapes in the 3D in-game world.
 *
 *
 *  ### The **phase** Parameter
 * To control whether objects should be visible through walls you can use the **phase** parameter.
 * This will disable the depth test.
 *
 *
 * ### The *relocate* Parameter
 * Depending on when methods in here are called in the rendering process, coordinates may or may not be already translated by the camera position.
 * To account for this most methods have a **relocate** parameter.
 * In general, like when using the [RenderWorldLastEvent][net.minecraftforge.client.event.RenderWorldLastEvent],
 * this should be set to true for the expected behaviour.
 *
 *
 * @author Aton
 * @author Stivais
 */
object WorldRenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    private val renderManager = mc.renderManager

    /**
     * Draws a line connecting the points [start] and [finish].
     *
     * @param phase Determines whether the box should be visible through walls (disables the depth test).
     */
    fun drawLine(start: Vec3, finish: Vec3, color: Color, thickness: Float = 3f, phase: Boolean = true) {
        drawLine(start.xCoord, start.yCoord, start.zCoord, finish.xCoord, finish.yCoord, finish.zCoord, color, thickness, phase)
    }

    /**
     * Draws a line connecting the points ([x], [y], [z]) and ([x2], [y2], [z2]).
     *
     * @param phase Determines whether the box should be visible through walls (disables the depth test).
     */
    fun drawLine (x: Double, y: Double, z: Double, x2: Double, y2: Double, z2:Double, color: Color, thickness: Float = 3f, phase: Boolean = true) {
        GlStateManager.disableLighting()
        GL11.glBlendFunc(770, 771)
        GlStateManager.enableBlend()
        GL11.glLineWidth(thickness)
        if (phase) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.pushMatrix()

        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        GlStateManager.color(color.red.toFloat() / 255f, color.green.toFloat() / 255f,
            color.blue.toFloat() / 255f, 1f)

        worldRenderer.pos(x, y, z).endVertex()
        worldRenderer.pos(x2, y2, z2).endVertex()

        tessellator.draw()

        GlStateManager.popMatrix()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }

    /**
     * Draws a cube outline for the block at the given [blockPos].
     *
     * This outline will be visible through walls. The depth test is disabled.
     *
     * @param relocate Translates the coordinates to account for the camera position. See [WorldRenderUtils] for more information.
     */
    fun drawBoxAtBlock (blockPos: BlockPos, color: Color, thickness: Float = 3f, relocate: Boolean = true) {
        drawBoxAtBlock(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), color, thickness, relocate)
    }

    /**
     * Draws a cube outline of size 1 starting at [x], [y], [z] which extends by 1 along the axes in positive direction.
     *
     * This outline will be visible through walls. The depth test is disabled.
     *
     * @param relocate Translates the coordinates to account for the camera position. See [WorldRenderUtils] for more information.
     */
    fun drawBoxAtBlock (x: Double, y: Double, z: Double, color: Color, thickness: Float = 3f, relocate: Boolean = true) {
        drawCustomSizedBoxAt(x, y, z, 1.0, 1.0, 1.0, color, thickness, true, relocate)
    }

    /**
     * Draws a rectangular cuboid outline (box) around the [entity].
     *
     * This box is centered horizontally around the entity with the given [width].
     * Vertically the box is aligned with the bottom of the entities hit-box and extends upwards by [height].
     * The box can be offset from this default alignment through the use of [xOffset], [yOffset], [zOffset].
     *
     * @param phase Determines whether the box should be visible through walls (disables the depth test).
     * @param partialTicks Used for predicting the [entity]'s position so that the box smoothly moves with the entity.
     */
    fun drawBoxByEntity (entity: Entity, color: Color, width: Double, height: Double, partialTicks: Float = 0f,
                         lineWidth: Double = 2.0, phase: Boolean = false,
                         xOffset: Double = 0.0, yOffset: Double = 0.0, zOffset: Double = 0.0
    ) {
        drawBoxByEntity(entity, color, width.toFloat(), height.toFloat(), partialTicks, lineWidth.toFloat(),phase,xOffset, yOffset, zOffset)
    }

    /**
     * Draws a rectangular cuboid outline (box) around the [entity].
     *
     * This box is centered horizontally around the entity with the given [width].
     * Vertically the box is aligned with the bottom of the entities hit-box and extends upwards by [height].
     * The box can be offset from this default alignment through the use of [xOffset], [yOffset], [zOffset].
     *
     * @param phase Determines whether the box should be visible through walls (disables the depth test).
     * @param partialTicks Used for predicting the [entity]'s position so that the box smoothly moves with the entity.
     */
    fun drawBoxByEntity (entity: Entity, color: Color, width: Float, height: Float, partialTicks: Float = 0f,
                         lineWidth: Float = 2f, phase: Boolean = false,
                         xOffset: Double = 0.0, yOffset: Double = 0.0, zOffset: Double = 0.0
    ){
        val x = entity.posX + ((entity.posX-entity.lastTickPosX)*partialTicks) + xOffset - width / 2.0
        val y = entity.posY + ((entity.posY-entity.lastTickPosY)*partialTicks) + yOffset
        val z = entity.posZ + ((entity.posZ-entity.lastTickPosZ)*partialTicks) + zOffset - width / 2.0

        drawCustomSizedBoxAt(x, y, z, width.toDouble(), height.toDouble(), width.toDouble(), color, lineWidth, phase)
    }

    fun draw2DBoxByEntity(entity: Entity, color: Color, width: Double, height: Double, partialTicks: Float = 0f, lineWidth: Double = 2.0, phase: Boolean = false, xOffset: Double = 0.0, yOffset: Double = 0.0, zOffset: Double = 0.0) {
        val x = entity.posX + ((entity.posX-entity.lastTickPosX)*partialTicks) + xOffset - width / 2.0
        val y = entity.posY + ((entity.posY-entity.lastTickPosY)*partialTicks) + yOffset
        val z = entity.posZ + ((entity.posZ-entity.lastTickPosZ)*partialTicks) + zOffset - width / 2.0


        GlStateManager.pushMatrix()

        GlStateManager.color(color.red.toFloat() / 255f, color.green.toFloat() / 255f,
            color.blue.toFloat() / 255f, 1f)

        GL11.glTranslated(x, y - 0.2, z)
        GL11.glRotated((-mc.renderManager.playerViewY).toDouble(), 0.0, 1.0, 0.0)

        if (phase) GlStateManager.disableDepth()
        val outline = Color.black.rgb
        Gui.drawRect(-20, -1, -26, 75, outline)
        Gui.drawRect(20, -1, 26, 75, outline)
        Gui.drawRect(-20, -1, 21, 5, outline)
        Gui.drawRect(-20, 70, 21, 75, outline)
        Gui.drawRect(-21, 0, -25, 74, 1)
        Gui.drawRect(21, 0, 25, 74, 1)
        Gui.drawRect(-21, 0, 24, 4, 1)
        Gui.drawRect(-21, 71, 25, 74, 1)

        GlStateManager.enableDepth()

        GlStateManager.popMatrix()
    }

    fun drawCustomSizedBoxAt(x: Double, y: Double, z: Double, xWidth: Double, yHeight: Double, zWidth: Double, color: Color, thickness: Float = 3f, phase: Boolean = true, relocate: Boolean = true) {
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glLineWidth(thickness)
        if (phase) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()

        GlStateManager.pushMatrix()

        if (relocate) GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        GlStateManager.color(color.red.toFloat() / 255f, color.green.toFloat() / 255f,
            color.blue.toFloat() / 255f, 1f)


        worldRenderer.pos(x+xWidth,y+yHeight,z+zWidth).endVertex()
        worldRenderer.pos(x+xWidth,y+yHeight,z).endVertex()
        worldRenderer.pos(x,y+yHeight,z).endVertex()
        worldRenderer.pos(x,y+yHeight,z+zWidth).endVertex()
        worldRenderer.pos(x+xWidth,y+yHeight,z+zWidth).endVertex()
        worldRenderer.pos(x+xWidth,y,z+zWidth).endVertex()
        worldRenderer.pos(x+xWidth,y,z).endVertex()
        worldRenderer.pos(x,y,z).endVertex()
        worldRenderer.pos(x,y,z+zWidth).endVertex()
        worldRenderer.pos(x,y,z).endVertex()
        worldRenderer.pos(x,y+yHeight,z).endVertex()
        worldRenderer.pos(x,y,z).endVertex()
        worldRenderer.pos(x+xWidth,y,z).endVertex()
        worldRenderer.pos(x+xWidth,y+yHeight,z).endVertex()
        worldRenderer.pos(x+xWidth,y,z).endVertex()
        worldRenderer.pos(x+xWidth,y,z+zWidth).endVertex()
        worldRenderer.pos(x,y,z+zWidth).endVertex()
        worldRenderer.pos(x,y+yHeight,z+zWidth).endVertex()
        worldRenderer.pos(x+xWidth,y+yHeight,z+zWidth).endVertex()

        tessellator.draw()

        GlStateManager.popMatrix()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }
    fun drawP3box(x: Double, y: Double, z: Double, xWidth: Double, yHeight: Double, zWidth: Double, color: Color, thickness: Float = 3f, phase: Boolean = true, relocate: Boolean = true) {

        val yMiddle = (y + yHeight / 2)
        drawSquare(x, y + yHeight, z, xWidth, zWidth, color, thickness, phase, relocate)
        drawSquare(x, yMiddle, z, xWidth, zWidth, color, thickness, phase, relocate)
        drawSquare(x, y + 0.02, z, xWidth, zWidth, color, thickness, phase, relocate)
    }
    private fun drawSquare(x: Double, y: Double, z: Double, xWidth: Double, zWidth: Double, color: Color, thickness: Float = 3f, phase: Boolean = true, relocate: Boolean = true) {
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glLineWidth(thickness)
        if (phase) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()

        GlStateManager.pushMatrix()

        if (relocate) GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        GlStateManager.color(color.red.toFloat() / 255f, color.green.toFloat() / 255f,
            color.blue.toFloat() / 255f, 1f)

        worldRenderer.pos(x + xWidth, y, z + zWidth).endVertex()
        worldRenderer.pos(x + xWidth, y, z).endVertex()
        worldRenderer.pos(x, y, z).endVertex()
        worldRenderer.pos(x, y, z + zWidth).endVertex()
        worldRenderer.pos(x + xWidth, y, z + zWidth).endVertex()

        tessellator.draw()

        GlStateManager.popMatrix()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }

    fun renderTransFlag(
        x: Double,
        y: Double,
        z: Double,
        Width: Float,
        Height: Float,
    ){
        drawSquareTwo(x, y + 0.01, z, Width, Width, cyan, 4f, false)
        drawSquareTwo(x, y + Height * 0.25, z, Width, Width, pink, 4f, false)
        drawSquareTwo(x, y + Height * 0.5, z, Width, Width, white, 4f, false)
        drawSquareTwo(x, y + Height * 0.75, z, Width, Width, pink, 4f, false)
        drawSquareTwo(x, y + Height, z, Width, Width, cyan, 4f, false)
    }

    fun renderGayFlag(
        x: Double,
        y: Double,
        z: Double,
        Width: Float,
        Height: Float,
    ){
        drawSquareTwo(x, y + 0.01, z, Width, Width, red, 4f, false)
        drawSquareTwo(x, y + Height * 0.2, z, Width, Width, orange, 4f, false)
        drawSquareTwo(x, y + Height * 0.4, z, Width, Width, yellow, 4f, false)
        drawSquareTwo(x, y + Height * 0.6, z, Width, Width, green, 4f, false)
        drawSquareTwo(x, y + Height * 0.8, z, Width, Width, blue, 4f, false)
        drawSquareTwo(x, y + Height, z, Width, Width, pink, 4f, false)
    }


    fun drawSquareTwo(
        x: Double,
        y: Double,
        z: Double,
        xWidth: Float,
        zWidth: Float,
        color: Color,
        thickness: Float = 3f,
        phase: Boolean = true,
        relocate: Boolean = true
    ) {
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glLineWidth(thickness)

        if (phase) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()

        GlStateManager.pushMatrix()

        if (relocate) {
            GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        }

        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        GlStateManager.color(color.red.toFloat() / 255f, color.green.toFloat() / 255f,
            color.blue.toFloat() / 255f, 1f)

        val halfXWidth = xWidth / 2
        val halfZWidth = zWidth / 2

        worldRenderer.pos(x + halfXWidth, y, z + halfZWidth).endVertex()
        worldRenderer.pos(x + halfXWidth, y, z - halfZWidth).endVertex()
        worldRenderer.pos(x - halfXWidth, y, z - halfZWidth).endVertex()
        worldRenderer.pos(x - halfXWidth, y, z + halfZWidth).endVertex()
        worldRenderer.pos(x + halfXWidth, y, z + halfZWidth).endVertex()

        tessellator.draw()

        GlStateManager.popMatrix()

        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }
}
package catgirlroutes.ui.misc

import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class InventoryButtonEditor : GuiScreen() {

    private val invWidth: Float = 176f;
    private val invHeight: Float = 166f;

    private var invX: Float = 0f
    private var invY: Float = 0f

    override fun initGui() {
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        invX = width / 2 - invWidth / 2;
        invY = height / 2 - invHeight / 2;

        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        GlStateManager.color(1f, 1f, 1f, 1f)

        mc.textureManager.bindTexture(INVENTORY)

        HUDRenderUtils.drawTexturedRect(invX, invY, invWidth, invHeight, 0f, invWidth / 256f, 0f, invHeight / 256f, GL11.GL_NEAREST)

        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        val INVENTORY: ResourceLocation = ResourceLocation("minecraft:textures/gui/container/inventory.png")
    }
}
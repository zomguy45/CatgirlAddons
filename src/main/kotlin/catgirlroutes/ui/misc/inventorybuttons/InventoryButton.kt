package catgirlroutes.ui.misc.inventorybuttons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Tuple
import org.lwjgl.opengl.GL11
import java.awt.Color


class InventoryButton(
    val x: Int,
    val y: Int,
    var command: String = "",
    var icon: String = "",
    val isEquipment: Boolean = false
) {

    inline val isActive: Boolean
        get() = command.isNotEmpty()

    inline val action: Unit
        get() = ChatUtils.commandAny(command)

    fun render(xOff: Int, yOff: Int, c: Color = colour, bC: Color = borderColour) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()

        HUDRenderUtils.drawRoundedBorderedRect((x + xOff).toDouble(), (y + yOff).toDouble(), 16.0, 16.0, 3.0, 2.0, c, bC)

        if (icon.isNotEmpty()) {
            Item.getByNameOrId(icon)?.let {
                mc.renderItem.renderItemAndEffectIntoGUI(ItemStack(it), x + xOff, y + yOff)
            }
        }

        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return (mouseX >= x && mouseX <= (x + 16) && mouseY >= y && mouseY <= (y + 16))
    }

    companion object {
        val colour: Color = Color(139, 139, 139, 155)
        val borderColour: Color = Color(250, 250, 250, 155)
    }

}
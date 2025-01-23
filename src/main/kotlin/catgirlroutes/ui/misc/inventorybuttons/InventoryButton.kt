package catgirlroutes.ui.misc.inventorybuttons

import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.NeuRepo
import catgirlroutes.utils.NeuRepo.toStack
import catgirlroutes.utils.RepoItem
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
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

    fun render(xOff: Double, yOff: Double, c: Color = colour, bC: Color = borderColour) {
        GlStateManager.pushMatrix()

        HUDRenderUtils.drawRoundedBorderedRect(x + xOff, y + yOff, 16.0, 16.0, 3.0, 2.0, c, bC)

        if (icon.isNotEmpty()) {
            val item = Item.getByNameOrId(icon.lowercase()) ?: NeuRepo.getItemFromID(icon.uppercase())

            item?.let {
                val itemStack = when (it) {
                    is Item -> ItemStack(it)
                    else -> (it as RepoItem).toStack()
                }
                HUDRenderUtils.drawItemStackWithText(itemStack, x + xOff, y + yOff)
            }
        }

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
package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.config.InventoryButtonsConfig
import catgirlroutes.mixins.accessors.AccessorGuiContainer
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.ui.misc.inventorybuttons.InventoryButtonEditor
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.render.HUDRenderUtils
import com.google.common.collect.Lists
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse

object InventoryButtons : Module(
    "Inventory buttons",
    Category.MISC,
    tag = TagType.WHIP
) {
    val equipmentOverlay: BooleanSetting = BooleanSetting("Equipment Overlay", false)

    val editMode: ActionSetting = ActionSetting("Edit") { display = InventoryButtonEditor() }

    init {
        addSettings(this.equipmentOverlay, this.editMode)
    }

    @SubscribeEvent
    fun onDrawScreenPost(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (mc.currentScreen !is GuiInventory) return
        val accessor = event.gui as AccessorGuiContainer
        InventoryButtonsConfig.allButtons.forEach {
            if (!it.isActive()) return@forEach
            if (it.isEquipment && !equipmentOverlay.enabled) return@forEach

            GlStateManager.pushMatrix()
            GlStateManager.disableLighting()

            it.render(accessor.guiLeft, accessor.guiTop)

            if (it.isHovered(event.mouseX - accessor.guiLeft, event.mouseY - accessor.guiTop)) {
                GuiUtils.drawHoveringText(
                    Lists.newArrayList("§7/${it.command.replace("/", "")}"),
                    event.mouseX, event.mouseY,
                    event.gui.width, event.gui.height,
                    -1, mc.fontRendererObj
                )
            }

            GlStateManager.enableLighting()
            GlStateManager.popMatrix()
        }
    }

    @SubscribeEvent
    fun onMouseInputPre(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (mc.currentScreen !is GuiInventory) return

        val sr = ScaledResolution(mc)

        val mouseX: Int = Mouse.getX() * sr.scaledWidth / mc.displayWidth
        val mouseY: Int = sr.scaledHeight - (Mouse.getY() * sr.scaledHeight / mc.displayHeight)

        val accessor = event.gui as AccessorGuiContainer

        InventoryButtonsConfig.allButtons.forEach { button ->
            if (!button.isActive()) return@forEach
            if (button.isHovered(mouseX - accessor.guiLeft, mouseY - accessor.guiTop) && Mouse.getEventButtonState()) {
                ChatUtils.commandAny(button.command)
            }
        }
    }
}
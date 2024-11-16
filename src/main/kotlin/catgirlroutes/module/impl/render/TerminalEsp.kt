package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object TerminalEsp : Module (
    name = "Terminal ESP",
    category = Category.RENDER,
    description = "Shows undone terminals."
){
    private val color = ColorSetting("Terminal ESP color", Color(0,0,255), description = "Color for the Terminal ESP")

    init{
        this.addSettings(
            color
        )
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (mc.theWorld != null) {
            val terminals = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
            for (terminal in terminals) {
                if (arrayOf("Inactive Terminal", "Inactive Device", "Not Activated").contains(terminal.name)) {
                    drawBoxByEntity(terminal, color.value, terminal.width.toDouble(), terminal.height.toDouble(), 0f)
                }
            }
        }
    }
}
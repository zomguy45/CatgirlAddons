package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.ModuleManager.modules
import catgirlroutes.module.settings.impl.ColorSetting
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

//TODO: Maybe box idk. I would personally not. Maybe add a switch to modules for showing in list.

object ModuleList : Module(
    name = "Array List",
    category = Category.RENDER,
    description = "ArrayList"
) {
    val colorText = ColorSetting("String color", Color.PINK, true)
    init {
        this.addSettings(colorText)
    }
    var activeModuleList = mutableListOf<String>()

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        activeModuleList.clear()
        for (module in modules) {
            if (module.enabled)
                //if (module.name == "Array List") return
                activeModuleList.add(module.name)
        }
        activeModuleList.sortByDescending { mc.fontRendererObj.getStringWidth(it) }
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !this.enabled) return
        val sr = ScaledResolution(mc)
        var y = 10
        val screenWidth = sr.scaledWidth
            for (active in activeModuleList) {
            val stringWidth = mc.fontRendererObj.getStringWidth(active)
            val x = screenWidth - stringWidth - 10
            mc.fontRendererObj.drawStringWithShadow(active, x.toFloat(), y.toFloat(), colorText.value.rgb)
            y += 11
        }
    }
}

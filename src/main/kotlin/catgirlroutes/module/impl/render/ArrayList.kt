package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.ModuleManager.modules
import catgirlroutes.module.settings.NoShowInList
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

//TODO: Maybe box idk. I would personally not. Maybe add a switch to modules for showing in list.

@NoShowInList
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

    @RegisterHudElement
    object ArrayHud : HudElement(
        this,
        6,
        12
    ) {
        override fun renderHud() {
            activeModuleList.clear()
            for (module in modules) {
                module.settings.filterIsInstance<BooleanSetting>()
                    .filter { it.value && it.name == "Show in List" && module.enabled }
                    .forEach { activeModuleList.add(module.name) }
            }
            activeModuleList.sortByDescending { mc.fontRendererObj.getStringWidth(it) }
            val isLeft = this.x < ScaledResolution(mc).scaledWidth / 2
            val isTop = this.y < ScaledResolution(mc).scaledHeight / 2
            if (activeModuleList.isEmpty()) return
            var y = 0.0
            for (active in activeModuleList) {
                val startLine = if (isLeft) 5.0 else 3.0
                val startBox = if (isLeft) 5.0 else -mc.fontRendererObj.getStringWidth(active) - 1.0
                val startText = if (isLeft) 9.0 else -mc.fontRendererObj.getStringWidth(active) + 1.0
                HUDRenderUtils.renderRect(startLine, y - 2.0, 2.0, 11.0, colorText.value, z = -10f)
                mc.fontRendererObj.drawStringWithShadow(active, (startText).toFloat(), y.toFloat(), colorText.value.rgb)
                HUDRenderUtils.renderRect(startBox, y - 2.0, mc.fontRendererObj.getStringWidth(active) + 4.0, 11.0, Color(0, 0, 0, 128), z = -10f)
            y += if (isTop) 11.0 else -11.0
            }
        }
    }
}

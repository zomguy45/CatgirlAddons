package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.ModuleManager.modules
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

object ModuleList : Module(
    name = "Array List",
    category = Category.RENDER,
    description = "ArrayList"
) {
    val colorText = ColorSetting("String color", Color.PINK, true)
    //Testing!!! Dont delete!!!
    val xSetting = NumberSetting("X", 0.0, -20.0, 20.0, 1.0)
    val ySetting = NumberSetting("Y", 0.0, -20.0, 20.0, 1.0)
    val widthSetting = NumberSetting("W", 0.0, -20.0, 20.0, 1.0)
    val heightSetting = NumberSetting("H", 0.0, -20.0, 20.0, 1.0)

    init {
        this.addSettings(colorText/*,xSetting,ySetting,widthSetting,heightSetting*/)
    }
    var activeModuleList = mutableListOf<String>()

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        return
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !this.enabled) return
        activeModuleList.clear()
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is BooleanSetting && setting.value && setting.name == "Show in List" && module.enabled) {
                    activeModuleList.add(module.name)
                }
            }
        }
        activeModuleList.sortByDescending { mc.fontRendererObj.getStringWidth(it) }
        if (activeModuleList.isEmpty()) return

        val sr = ScaledResolution(mc)
        var y = 10
        val screenWidth = sr.scaledWidth

            for (active in activeModuleList) {
            val stringWidth = mc.fontRendererObj.getStringWidth(active)
            val x = screenWidth - stringWidth - 10
            mc.fontRendererObj.drawStringWithShadow(active, x.toFloat(), y.toFloat(), colorText.value.rgb)
                HUDRenderUtils.renderRect(screenWidth - 8.0, y.toDouble() - 3.0, 2.0, 13.0, colorText.value)
                HUDRenderUtils.renderRect(x.toDouble() - 4.0, y.toDouble() - 3.0, stringWidth.toDouble() + 6.0, 13.0, Color(0, 0, 0, 128))
            y += 11
        }
    }

    @RegisterHudElement
    object ArrayHud : HudElement(
        this,
        ScaledResolution(mc).scaledWidth, //FUCK NIGGERS
        0
    ) {
        override fun renderHud() {
            activeModuleList.clear()
            for (module in modules) {
                for (setting in module.settings) {
                    if (setting is BooleanSetting && setting.value && setting.name == "Show in List" && module.enabled) {
                        activeModuleList.add(module.name)
                    }
                }
            }
            activeModuleList.sortByDescending { mc.fontRendererObj.getStringWidth(it) }
            if (activeModuleList.isEmpty()) return

            var y = 10

            for (active in activeModuleList) {
                val stringWidth = mc.fontRendererObj.getStringWidth(active)
                val x = -stringWidth - 10
                mc.fontRendererObj.drawStringWithShadow(active, x.toFloat(), y.toFloat(), colorText.value.rgb)
                HUDRenderUtils.renderRect(-8.0, y.toDouble() - 3.0, 2.0, 13.0, colorText.value)
                HUDRenderUtils.renderRect(x.toDouble() - 4.0, y.toDouble() - 3.0, stringWidth.toDouble() + 6.0, 13.0, Color(0, 0, 0, 128))
                y += 11
            }
        }
    }
}

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
                for (setting in module.settings) {
                    if (setting is BooleanSetting && setting.value && setting.name == "Show in List" && module.enabled) {
                        activeModuleList.add(module.name)
                    }
                }
            }
            var isLeft = false
            if (this.x < ScaledResolution(mc).scaledWidth / 2) {
                isLeft = true
            }
            var isTop = false
            if (this.y < ScaledResolution(mc).scaledHeight / 2) {
                isTop = true
            }
            activeModuleList.sortByDescending { mc.fontRendererObj.getStringWidth(it) }
            if (activeModuleList.isEmpty()) return
            var startLine = 0.0
            var startBox = 0.0
            var startText = 0.0
            var y = 0.0
            for (active in activeModuleList) {
                if (isLeft) {
                    startLine = 5.0
                    startBox = 5.0
                    startText = 9.0
                } else {
                    startLine = 3.0
                    startBox = -mc.fontRendererObj.getStringWidth(active) - 1.0
                    startText = -mc.fontRendererObj.getStringWidth(active) + 1.0
                }
                HUDRenderUtils.renderRect(startLine, y - 3.0, 2.0, 13.0, colorText.value)
                mc.fontRendererObj.drawStringWithShadow(active, (startText).toFloat(), y.toFloat(), colorText.value.rgb)
                HUDRenderUtils.renderRect(startBox, y - 3.0, mc.fontRendererObj.getStringWidth(active) + 6.0, 13.0, Color(0, 0, 0, 128))
            y += if (isTop) {
                11.0
            } else {
                -11.0
            }
            }
        }
    }
}

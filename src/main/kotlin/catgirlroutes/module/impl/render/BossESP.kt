package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.utils.render.WorldRenderUtils.draw2DBoxByEntity
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.entity.boss.EntityWither
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BossESP : Module( // todo: outline
    name = "Boss ESP",
    category = Category.RENDER,
    description = "ESP for the Withers."
) {
    private val style = StringSelectorSetting("Style","Box", arrayListOf("Box", "Outline", "2D"), "Esp render style to be used.")
    private val lineWidth = NumberSetting("Line width", 4.0, 1.0, 8.0, 1.0)
    private val color = ColorSetting("Boss ESP color", Color(0,0,255), description = "Color for the Boss ESP")

    init{
        this.addSettings(
            style,
            lineWidth,
            color
        )
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        mc.theWorld?.let { world ->
            world.loadedEntityList.filterIsInstance<EntityWither>().forEach { wither ->
                if (wither.isInvisible || wither.renderSizeModifier != 1f) return@forEach
                when (style.selected) {
                    "Box" -> drawBoxByEntity(wither, color.value, wither.width.toDouble(), wither.height.toDouble(), event.partialTicks, lineWidth.value, true)
                    "2D" -> draw2DBoxByEntity(wither, color.value, wither.width.toDouble(), wither.height.toDouble(), event.partialTicks, lineWidth.value, true)
                }
            }
        }
    }
}
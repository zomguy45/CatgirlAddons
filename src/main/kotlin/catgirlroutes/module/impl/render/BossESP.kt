package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.entity.boss.EntityWither
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BossESP : Module( // todo: add esp modes, line width
    name = "Boss ESP",
    category = Category.RENDER,
    description = "ESP for the Withers."
) {
    private val color = ColorSetting("Boss ESP color", Color(0,0,255), description = "Color for the Boss ESP")

    init{
        this.addSettings(
            color
        )
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        mc.theWorld?.let { world ->
            world.loadedEntityList.filterIsInstance<EntityWither>().forEach { wither ->
                if (wither.isInvisible || wither.renderSizeModifier != 1f) return@forEach
                drawBoxByEntity(wither, color.value, wither.width.toDouble(), wither.height.toDouble(), 0f, 4.0, true)
            }
        }
    }
}
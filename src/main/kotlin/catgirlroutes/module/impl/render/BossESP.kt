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

object BossESP : Module(
    name = "Boss ESp",
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
        if (mc.theWorld != null) {
            val withers = mc.theWorld.loadedEntityList.filterIsInstance<EntityWither>()
            for (wither in withers) {
                if (wither.isInvisible) return
                drawBoxByEntity(wither, color.value, wither.width.toDouble(), wither.height.toDouble(), 0f)
            }
        }
    }
}
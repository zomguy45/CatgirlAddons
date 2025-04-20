package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RenderEntityModelEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.dungeon.M7Phases
import catgirlroutes.utils.render.OutlineUtils
import catgirlroutes.utils.render.WorldRenderUtils.draw2DBoxByEntity
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.entity.boss.EntityWither
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object BossESP : Module( // todo stop rendering mini withers
    "Boss ESP",
    Category.RENDER,
    "ESP for the Withers."
) {
    private val style by SelectorSetting("Style","Box", arrayListOf("Box", "Outline", "2D"), "Esp render style to be used.")
    private val lineWidth by NumberSetting("Line width", 4.0, 1.0, 8.0, 1.0)
    private val color by ColorSetting("Boss ESP color", Color(0,0,255), collapsible = false, description = "Color for the Boss ESP")

    private var wither: EntityWither? = null;

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() == M7Phases.Unknown) return
        mc.theWorld.loadedEntityList
            .filterIsInstance<EntityWither>()
            .filter { !it.isInvisible && it.renderSizeModifier == 1f }
            .forEach {
                wither = it
                when (style.selected) {
                    "Box" -> drawBoxByEntity(it, color, it.width.toDouble(), it.height.toDouble(), event.partialTicks, lineWidth, true)
                    "2D" -> draw2DBoxByEntity(it, color, event.partialTicks, lineWidth.toFloat(), true)
                }
            }
    }

    @SubscribeEvent
    fun onRenderEntityModel (event: RenderEntityModelEvent) {
        if (style.selected == "Outline" && event.entity == wither) {
            OutlineUtils.outlineESP(event, lineWidth.toFloat(), color, true)
        }
    }
}
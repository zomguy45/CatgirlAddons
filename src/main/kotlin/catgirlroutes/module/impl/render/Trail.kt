package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.AlwaysActive
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.render.WorldRenderUtils.drawLine
import catgirlroutes.utils.render.WorldRenderUtils.renderManager
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@AlwaysActive
object Trail: Module(
    "Trail",
    Category.RENDER
){

    private var trailTicks = NumberSetting("Ticks", 20.0, 1.0, 400.0)

    private var trailColor = ColorSetting("Color", java.awt.Color.PINK)
    private var trailThickness = NumberSetting("Thickness", 3.0, 1.0, 5.0)
    private var trailStay = BooleanSetting("Stay", true)
    private var trailPhase = BooleanSetting("Phase", false)

    init {
        addSettings(
            trailTicks,
            trailColor,
            trailThickness,
            trailStay,
            trailPhase
        )
    }

    private var posToRender = mutableListOf<Vec3>()

    @SubscribeEvent
    fun onLoad(event: WorldEvent.Load) {
        posToRender.clear()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.thePlayer == null || !this.enabled) return
        if (!hasMoved() && trailStay.value) return
        posToRender.add(Vec3(renderManager.viewerPosX, renderManager.viewerPosY + 0.05, renderManager.viewerPosZ))
        if (posToRender.size.toDouble() == trailTicks.value * 2) posToRender.removeFirst()
        if (posToRender.size > trailTicks.value * 2) posToRender.clear()
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        for (i in 0 until posToRender.size - 1) {
            drawLine(posToRender[i], posToRender[i+1], trailColor.value, trailThickness.value.toFloat(), trailPhase.value)
        }
    }

    private fun hasMoved(): Boolean {
        return mc.thePlayer.posZ != mc.thePlayer.prevPosZ || mc.thePlayer.posY != mc.thePlayer.prevPosY || mc.thePlayer.posX != mc.thePlayer.prevPosX
    }
}
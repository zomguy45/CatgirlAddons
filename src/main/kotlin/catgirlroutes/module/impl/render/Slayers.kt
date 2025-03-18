package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.commodore
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.clock.Executor
import catgirlroutes.utils.clock.Executor.Companion.register
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

//TODO: Track Slayer Progress and which slayer. Display spawn time, kill time, avg time, total time. Improve render?

object Slayers : Module(
    name = "Slayers",
    category = Category.RENDER //Render Category for now
) {

    init {
        percentageAdjust()
    }

    var percentage = 0.0
    var targetPercentage = 0.0

    @RegisterHudElement
    object BarRender: HudElement(
        this, 0, 0, width = 0, height = 0
    ) {
        override fun renderHud() {
            val sr = ScaledResolution(mc)
            val x = sr.scaledWidth / 2 - 49.0
            val y = sr.scaledHeight / 2 + 10.0
            if (percentage < 0.0) percentage = 0.0
            if (percentage > 100.0) percentage = 100.0
            HUDRenderUtils.renderRect(x, y, percentage, 2.0, Color.PINK)
            HUDRenderUtils.renderRect(x + percentage, y, 100.0 - percentage, 2.0, Color.BLACK)
        }
    }

    private fun percentageAdjust() {
        Executor(10) {
            if (percentage != targetPercentage) {
                percentage += (targetPercentage - percentage) * 0.1
            }
        }.register()
    }

    val barSetter = commodore("bar") {
        literal("set").runs{
            x: Double ->
                targetPercentage = x
            modMessage("Set Bar to" + x)
        }
    }
}
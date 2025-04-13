package catgirlroutes.module.impl.dungeons

import catgirlroutes.events.impl.EntityRemovedEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.render.WorldRenderUtils.displayTitle
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Watcher : Module(
    name = "Watcher",
    category = Category.DUNGEON
) {
    val killTitle = BooleanSetting("Display title", false)
    val displayTime = NumberSetting("Display time", 40.0, 20.0, 100.0)

    init {
        this.addSettings(killTitle, displayTime)
    }

    @SubscribeEvent
    fun onEntityLeave(event: EntityRemovedEvent) {
        if (event.entity.name.contains("Let's see how you can handle this.") && killTitle.value) {
            displayTitle("KILL NOW!", displayTime.value.toInt())
        }
    }
}
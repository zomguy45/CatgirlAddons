package catgirlroutes.module.impl.dungeons.puzzlesolvers

import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.module.settings.impl.NumberSetting
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Puzzles : Module ( // todo: auto icefill
    "Puzzles",
    Category.DUNGEON,
    "Puzzle solvers"
) {
    private val iceFillSettings: DropdownSetting = DropdownSetting("Ice fill", false)
    private var iceFill: BooleanSetting = BooleanSetting("Toggle", false).withDependency { iceFillSettings.value }
    var iceFillAuto: BooleanSetting = BooleanSetting("Auto", false).withDependency { iceFillSettings.value }
    var iceFillDelay: NumberSetting = NumberSetting("Auto delay", 2.0, 1.0, 10.0, 1.0).withDependency { iceFillSettings.value }
    init {
        addSettings(
            iceFillSettings,
            iceFill,
            iceFillAuto,
            iceFillDelay,
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        IceFillSolver.reset()
    }

    @SubscribeEvent
    fun onWorldLast(event: RenderWorldLastEvent) {
        if (iceFill.enabled) IceFillSolver.onRenderWorld(Color.GREEN)
    }

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        IceFillSolver.onRoomEnter(event)
    }
}
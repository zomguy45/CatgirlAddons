package catgirlroutes.module.impl.dungeons.puzzlesolvers

import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Puzzles : Module ( // todo: auto icefill
    "Puzzles",
    Category.DUNGEON,
    "Puzzle solvers"
) {
    private var iceFill: BooleanSetting = BooleanSetting("Ice Fill", false)

    init {
        addSettings(
            iceFill,
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
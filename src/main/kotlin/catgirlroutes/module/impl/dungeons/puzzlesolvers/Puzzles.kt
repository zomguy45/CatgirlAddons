package catgirlroutes.module.impl.dungeons.puzzlesolvers

import catgirlroutes.events.impl.MotionUpdateEvent
import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.module.settings.impl.NumberSetting
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
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

    private val tttSettings: DropdownSetting = DropdownSetting("Tic tac toe", false)
    private var ttt: BooleanSetting = BooleanSetting("Toggle", false).withDependency { tttSettings.value }
    val renderNext = BooleanSetting("Show Next Move", true, description = "Shows which move is next.").withDependency { tttSettings.value }
    var tttAuto: BooleanSetting = BooleanSetting("Auto", false).withDependency { tttSettings.value }
    var tttReach: NumberSetting = NumberSetting("Auto reach", 4.5, 1.0, 6.0, 0.1).withDependency { tttSettings.value }

    init {
        addSettings(
            iceFillSettings,
            iceFill,
            iceFillAuto,
            iceFillDelay,

            tttSettings,
            ttt,
            renderNext,
            tttAuto,
            tttReach,
        )
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        IceFillSolver.reset()
        TicTacToeSolver.onWorldLoad()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onMotionUpdate(event: MotionUpdateEvent.Post) {
        TicTacToeSolver.onMotion()
    }

    @SubscribeEvent
    fun onWorldLast(event: RenderWorldLastEvent) {
        if (iceFill.enabled) IceFillSolver.onRenderWorld(Color.GREEN)
        if (ttt.enabled) TicTacToeSolver.onRenderWorld()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        TicTacToeSolver.onTick()
    }

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        IceFillSolver.onRoomEnter(event)
    }
}
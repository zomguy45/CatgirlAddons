package catgirlroutes.module.impl.dungeons.puzzlesolvers

import catgirlroutes.events.impl.MotionUpdateEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.dungeons.puzzlesolvers.WaterSolver.waterInteract
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.clock.Executor
import catgirlroutes.utils.clock.Executor.Companion.register
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.client.event.ClientChatReceivedEvent
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
    var iceFill: BooleanSetting = BooleanSetting("Ice fill solver", false).withDependency { iceFillSettings.value }
    var iceFillAuto: BooleanSetting = BooleanSetting("Auto ice fill", false).withDependency { iceFillSettings.value }
    var iceFillDelay: NumberSetting = NumberSetting("Auto ice fill delay", 2.0, 1.0, 10.0, 1.0).withDependency { iceFillSettings.value }

    private val tttSettings: DropdownSetting = DropdownSetting("Tic tac toe", false)
    var ttt: BooleanSetting = BooleanSetting("Tic tac toe solver", false).withDependency { tttSettings.value }
    val renderNext = BooleanSetting("Show Next Move", true, description = "Shows which move is next.").withDependency { tttSettings.value }
    var tttAuto: BooleanSetting = BooleanSetting("Auto TTT", false).withDependency { tttSettings.value }
    var tttReach: NumberSetting = NumberSetting("Auto TTT reach", 4.5, 1.0, 6.0, 0.1).withDependency { tttSettings.value }

    private val waterSettings: DropdownSetting = DropdownSetting("Water board", false)
    val waterSolver: BooleanSetting = BooleanSetting("Water Board Solver", false, description = "Shows you the solution to the water puzzle.").withDependency { waterSettings.value }
    val showTracer: BooleanSetting = BooleanSetting("Show Tracer", true, description = "Shows a tracer to the next lever.").withDependency { waterSettings.value }

    private val weirdosSettings: DropdownSetting = DropdownSetting("Three weirdos", false)
    val weirdoSolver: BooleanSetting = BooleanSetting("Weirdo solver", false).withDependency { weirdosSettings.value }
    val autoWeirdos = BooleanSetting("Auto weirdos", false).withDependency { weirdosSettings.value }

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

            waterSettings,
            waterSolver,
            showTracer,

            weirdosSettings,
            weirdoSolver,
            autoWeirdos
        )

        Executor(500) {
            if (!inDungeons || inBoss) return@Executor
            if (waterSolver.value) WaterSolver.scan()
        }.register()
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        IceFillSolver.reset()
        TicTacToeSolver.onWorldLoad()
        WaterSolver.reset()
        AutoWeirdos.onWorldChange()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onMotionUpdate(event: MotionUpdateEvent.Post) {
        TicTacToeSolver.onMotion()
    }

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet is C08PacketPlayerBlockPlacement) {
            if (waterSolver.value) waterInteract(event.packet)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        AutoWeirdos.onChat(event)
    }

    @SubscribeEvent
    fun onWorldLast(event: RenderWorldLastEvent) {
        if (iceFill.enabled) IceFillSolver.onRenderWorld(Color.GREEN)
        if (ttt.enabled) TicTacToeSolver.onRenderWorld()
        if (waterSolver.enabled)   WaterSolver.onRenderWorld(showTracer.value)
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        TicTacToeSolver.onTick()
        WaterSolver.onTick()
        AutoWeirdos.onTick()
    }

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        IceFillSolver.onRoomEnter(event)
    }
}
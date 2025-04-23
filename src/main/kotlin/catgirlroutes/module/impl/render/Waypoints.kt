package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.commodore
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ConfigSystem
import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager.currentArea
import catgirlroutes.utils.render.WorldRenderUtils.blockPosToAABB
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxAtBlock
import com.google.gson.reflect.TypeToken
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.io.File

object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER
) {
    val waypointColorInner = ColorSetting("Inner Color", Color.PINK)
    val waypointColorOuter = ColorSetting("Outer Color", Color.PINK)

    init {
        this.addSettings(waypointColorInner, waypointColorOuter)
    }

    override fun onKeyBind() {
        waypoints.add(Waypoint(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, currentArea))
    }

    private val waypointsFile = File("config/catgirlroutes/waypoints.json")

    var waypoints: MutableList<Waypoint> = loadWaypoints()

    private fun loadWaypoints(): MutableList<Waypoint> {
        return ConfigSystem.loadConfig(waypointsFile, object : TypeToken<MutableList<Waypoint>>() {}.type) ?: mutableListOf()
    }

    private fun saveWaypoints() {
        ConfigSystem.saveConfig(waypointsFile, waypoints)
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        waypoints.forEach { p ->
            if (p.area != currentArea) return@forEach
            drawBoxAtBlock(BlockPos(p.x, p.y, p.z), color = if (intersects().contains(BlockPos(p.x, p.y, p.z))) Color(255, 255, 255, waypointColorInner.value.alpha) else waypointColorInner.value, filled = true)
            drawBoxAtBlock(BlockPos(p.x, p.y, p.z), color = if (intersects().contains(BlockPos(p.x, p.y, p.z))) Color(255, 255, 255, waypointColorOuter.value.alpha) else waypointColorOuter.value)
        }
    }

    @SubscribeEvent
    fun onMouse(event: MouseEvent) {
        if (!event.buttonstate) return
        if (event.button != 0) return

        waypoints.removeAll{ p ->
            intersects().contains(BlockPos(p.x, p.y, p.z))
        }
    }

    private fun intersects() : List<BlockPos> {
        var list = ArrayList<BlockPos>()
        waypoints.forEach {p ->
            val startVec = mc.thePlayer.getPositionEyes(1.0f)
            val box = blockPosToAABB(BlockPos(p.x, p.y, p.z), 1, 1, 1)
            val ray = mc.thePlayer.rayTrace(10.0, 1f).hitVec
            if (box.calculateIntercept(startVec, ray) != null) {
                list.add(BlockPos(p.x, p.y, p.z))
            }
        }
        return list
    }

    val waypointCommands = commodore("wp") {
        literal("add").runs {
            x: Double, y: Double, z: Double ->
            val area = currentArea
            waypoints.add(Waypoint(x, y, z, area))
            modMessage("Added waypoint at x: ${x}, y: ${y},z: ${z} in the ${area}!")
            saveWaypoints()
        }
        literal("undo").runs {
            waypoints.removeLast()
            saveWaypoints()
        }
        literal("clear").runs {
            waypoints.clear()
            saveWaypoints()
        }
        literal("remove").runs {
            val toRemove = intersects()
            waypoints.removeAll { p ->
                toRemove.contains(BlockPos(p.x, p.y, p.z))
            }
            saveWaypoints()
        }
        literal("load").runs { loadWaypoints() }
    }

    data class Waypoint (val x: Double, val y: Double, val z: Double, val area: Island)
}
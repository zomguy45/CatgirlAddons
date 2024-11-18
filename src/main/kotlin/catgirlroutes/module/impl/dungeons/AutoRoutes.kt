package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.Node
import catgirlroutes.commands.NodeManager
import catgirlroutes.commands.nodeeditmode
import catgirlroutes.commands.ringsActive
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.misc.PearlClip
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.FakeRotater
import catgirlroutes.utils.MovementUtils
import catgirlroutes.utils.Utils
import catgirlroutes.utils.Utils.swapFromName
import catgirlroutes.utils.render.WorldRenderUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.currentRoom
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.floor

object AutoRoutes : Module(
    "Auto Routes",
    category = Category.DUNGEON,
    description = "A module that allows you to place down nodes that execute various actions."
){
    private val preset = StringSelectorSetting("Node style","Trans", arrayListOf("Trans", "Normal", "LGBTQIA+"), description = "Ring render style to be used.")

    init {
        this.addSettings(
            preset
        )
    }

    @SubscribeEvent
    fun onEnterRoom(event: DungeonEvents.RoomEnterEvent) {
        NodeManager.loadNodes()
    }

    private val cooldownMap = mutableMapOf<String, Boolean>()

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!inDungeons || !this.enabled || nodeeditmode) return
        NodeManager.nodes.forEach { node ->
            val key = "${node.location.xCoord},${node.location.yCoord},${node.location.zCoord},${node.type}"
            val cooldown: Boolean = cooldownMap[key] == true
            if(inNode(node)) {
                if (cooldown) return@forEach
                cooldownMap[key] = true
                GlobalScope.launch {
                    executeAction(node)
                }
            } else if (cooldown) {
                cooldownMap[key] = false
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!ringsActive || !this.enabled) return
        NodeManager.nodes.forEach { node ->
            val room = currentRoom ?: return
            val realLocation = room.getRealCoords(node.location)
            val x: Double = realLocation.xCoord
            val y: Double = realLocation.yCoord
            val z: Double = realLocation.zCoord

            val cooldown: Boolean = cooldownMap["$x,$y,$z,${node.type}"] == true
            val color = if (cooldown) Color.WHITE else Color.black

            when(preset.selected) {
                "Trans" -> WorldRenderUtils.renderTransFlag(x, y, z, node.width, node.height)
                "Normal" -> WorldRenderUtils.drawP3box(
                    x - node.width / 2,
                    y,
                    z - node.width / 2,
                    node.width.toDouble(),
                    node.height.toDouble(),
                    node.width.toDouble(),
                    color,
                    4F,
                    false
                )
                "LGBTQIA+" -> WorldRenderUtils.renderGayFlag(x, y, z, node.width, node.height)
            }
        }
    }

    private fun inNode(node: Node): Boolean {
        val viewerPos = mc.renderManager
        val room = currentRoom ?: return false
        val realLocation = room.getRealCoords(node.location)
        val distanceX = abs(viewerPos.viewerPosX - realLocation.xCoord)
        val distanceY = abs(viewerPos.viewerPosY - realLocation.yCoord)
        val distanceZ = abs(viewerPos.viewerPosZ - realLocation.zCoord)

        return distanceX < (node.width / 2) &&
                distanceY < node.height &&
                distanceY >= -0.5 &&
                distanceZ < (node.width / 2);
    }

    private suspend fun executeAction(node: Node) {
        val actionDelay: Int = if (node.delay == null) 0 else node.delay!!
        delay(actionDelay.toLong())
        val room = currentRoom ?: return
        val realLocation = room.getRealCoords(node.location)
        val realLookLocation = room.getRealCoords(node.lookBlock)
        val(yaw, pitch) = Utils.getYawAndPitch(
            node.lookBlock.xCoord,
            node.lookBlock.yCoord,
            node.lookBlock.zCoord
        )
        node.arguments?.let {
            if ("stop" in it) MovementUtils.stopVelo()
            if ("walk" in it) MovementUtils.setKey("w", true)
            if ("look" in it) Utils.snapTo(yaw, pitch)
        }
        when(node.type) {
            "warp" -> {
                swapFromName("aspect of the void")
                MovementUtils.setKey("shift", true)
                FakeRotater.rotate(yaw, pitch)
            }
            "walk" -> {
                modMessage("Walking!")
                MovementUtils.setKey("w", true)
            }
            "jump" -> {
                modMessage("Jumping!")
                MovementUtils.jump()
            }
            "stop" -> {
                modMessage("Stopping!")
                MovementUtils.stopMovement()
                MovementUtils.stopVelo()
            }
            "boom" -> {
                modMessage("Bomb denmark!")
                swapFromName("infinityboom tnt")
                scheduleTask(1) { Utils.leftClick() }
            }
            "pearlclip" -> {
                modMessage("Pearl clipping!")
                if (node.depth == 0F) {
                    PearlClip.pearlClip()
                } else {
                    PearlClip.pearlClip(node.depth!!.toDouble())
                }
            }
            "look" -> {
                modMessage("Looking!")
                Utils.snapTo(yaw, pitch)
            }
            "align" -> {
                modMessage("Aligning!")
                mc.thePlayer.setPosition(floor(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, floor(mc.thePlayer.posZ) + 0.5)
            }
        }
    }
}
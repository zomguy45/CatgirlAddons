package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.Node
import catgirlroutes.commands.NodeManager
import catgirlroutes.commands.NodesActive.nodesActive
import catgirlroutes.commands.editmodetoggled
import catgirlroutes.events.ClickEvent
import catgirlroutes.events.DungeonEvents
import catgirlroutes.events.DungeonSecretEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.*
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.getRealCoords
import catgirlroutes.utils.dungeon.DungeonUtils.getRealYaw
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeCoords
import catgirlroutes.utils.render.WorldRenderUtils
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor

object AutoRoutes : Module(
    "Auto routes",
    category = Category.DUNGEON,
    description = "A module that allows auto routing in dungeons."
){

    init {
        this.addSettings(
            //AutoP3.selectedRoute
        )
    }

    @SubscribeEvent
    fun onRoom(event: DungeonEvents.RoomEnterEvent) {
        NodeManager.loadNodes()
    }

    private val cooldownMap = mutableMapOf<String, Boolean>()

    @SubscribeEvent
    fun onClick(event: ClickEvent.LeftClickEvent) {
        cooldownMap.keys.forEach { key ->
            cooldownMap[key] = false
            modMessage("cooldown reset")
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!nodesActive || !this.enabled || editmodetoggled) return
        NodeManager.nodes.forEach { node ->
            val key = "${node.x},${node.y},${node.z},${node.type},${node.roomname}"
            val cooldown: Boolean = cooldownMap[key] == true
            if(inNode(node)) {
                if (cooldown) return@forEach
                if (node.awaitsecret && !secretCollected) return@forEach
                cooldownMap[key] = true
                executeAction(node)
            } else if (cooldown) {
                cooldownMap[key] = false
            }
        }
    }

    private var secretCollected = false

    @SubscribeEvent
    fun onSecret(event: DungeonSecretEvent) {
        secretCollected = true
        scheduleTask(1) {secretCollected = false}
        modMessage("secret collected!!!!!!!!!")
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!nodesActive || !this.enabled ) return
        NodeManager.nodes.forEach { node ->
            val key = "${node.x},${node.y},${node.z},${node.type},${node.roomname}"
            val cooldown: Boolean = cooldownMap[key] == true
            val color = if (cooldown) {
                Color.white
            } else {
                Color.black
            }
            val coords = Vec3(node.x, node.y, node.z)
            if (currentFullRoom == null) return
            val rcoords = currentFullRoom!!.getRealCoords(coords)
            WorldRenderUtils.drawP3box(
                rcoords.xCoord - node.width / 2,
                rcoords.yCoord,
                rcoords.zCoord - node.width / 2,
                node.width.toDouble(),
                node.height.toDouble(),
                node.width.toDouble(),
                color,
                4F,
                false
            )
        }
    }

    private fun inNode(node: Node): Boolean {
        val coords = Vec3(mc.renderManager.viewerPosX, mc.renderManager.viewerPosY, mc.renderManager.viewerPosZ)
        if (currentFullRoom == null) return false
        val rcoords = currentFullRoom!!.getRelativeCoords(coords)
        val distanceX = abs(rcoords.xCoord - node.x)
        val distanceY = abs(rcoords.yCoord - node.y)
        val distanceZ = abs(rcoords.zCoord - node.z)

        return distanceX < (node.width / 2) && distanceY < node.height && distanceY >= -0.5 && distanceZ < (node.width / 2);
    }

    private fun executeAction(node: Node) {
        val yaw = currentFullRoom!!.getRealYaw(node.yaw)
        when(node.type) {
            "walk" -> {
                ChatUtils.modMessage("Walking!")
                MovementUtils.stopVelo()
                MovementUtils.setKey("w", true)
                Utils.snapTo(yaw, node.pitch)
            }
            "walk2" -> {
                ChatUtils.modMessage("Walking!")
                MovementUtils.setKey("w", true)
                Utils.snapTo(yaw, node.pitch)
            }
            "jump" -> {
                ChatUtils.modMessage("Jumping!")
                Utils.snapTo(yaw, node.pitch)
                MovementUtils.jump()
            }
            "jump2" -> {
                ChatUtils.modMessage("Jumping!")
                MovementUtils.jump()
            }
            "stop" -> {
                ChatUtils.modMessage("Stopping!")
                MovementUtils.setKey("w", false)
                MovementUtils.stopVelo()
            }
            "boom" -> {
                ChatUtils.modMessage("Bomb denmark!")
                Utils.swapFromName("infinityboom tnt")
                Utils.snapTo(yaw, node.pitch)
                ClientListener.scheduleTask(1) { Utils.leftClick() }
            }
            "pearlclip" -> {
                ChatUtils.modMessage("Pearl clipping!")
                LavaClip.lavaClipToggle(node.depth!!.toDouble(), true)
            }
            "look" -> {
                ChatUtils.modMessage("Looking!")
                Utils.snapTo(yaw, node.pitch)
            }
            "command" -> {
                ChatUtils.modMessage("Commanding!")
                ChatUtils.modMessage(node.command!!)
                ChatUtils.sendChat(node.command)
            }
            "align" -> {
                ChatUtils.modMessage("Aligning!")
                MovementUtils.stopVelo()
                CatgirlRoutes.mc.thePlayer.setPosition(floor(CatgirlRoutes.mc.thePlayer.posX) + 0.5, CatgirlRoutes.mc.thePlayer.posY, floor(CatgirlRoutes.mc.thePlayer.posZ) + 0.5)
            }
            "useitem" -> {
                ChatUtils.modMessage("Iteming!")
                Utils.swapFromName(node.item!!)
                FakeRotater.rotate(yaw, node.pitch)
            }
        }
    }
}
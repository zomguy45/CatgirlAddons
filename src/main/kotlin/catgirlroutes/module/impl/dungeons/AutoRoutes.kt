package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.Node
import catgirlroutes.commands.impl.NodeManager
import catgirlroutes.commands.impl.NodeManager.nodes
import catgirlroutes.commands.impl.nodeEditMode
import catgirlroutes.events.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.player.PearlClip
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.MovementUtils
import catgirlroutes.utils.Utils
import catgirlroutes.utils.Utils.renderText
import catgirlroutes.utils.Utils.swapFromName
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.getRealYaw
import catgirlroutes.utils.render.WorldRenderUtils
import catgirlroutes.utils.render.WorldRenderUtils.drawP3box
import catgirlroutes.utils.render.WorldRenderUtils.drawP3boxWithLayers
import catgirlroutes.utils.render.WorldRenderUtils.renderGayFlag
import catgirlroutes.utils.render.WorldRenderUtils.renderTransFlag
import catgirlroutes.utils.rotation.FakeRotater
import catgirlroutes.utils.rotation.RotationUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.currentRoom
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color.WHITE
import java.awt.Color.black
import kotlin.collections.set
import kotlin.math.floor

object AutoRoutes : Module(
    "Auto Routes",
    category = Category.DUNGEON,
    description = "A module that allows you to place down nodes that execute various actions."
){
    private val editTitle = BooleanSetting("EditMode title", false)
    private val preset = StringSelectorSetting("Node style","Trans", arrayListOf("Trans", "Normal", "LGBTQIA+"), description = "Ring render style to be used.")
    private val layers = NumberSetting("Ring layers amount", 3.0, 3.0, 5.0, 1.0, "Amount of ring layers to render").withDependency { preset.selected == "Normal" }
    private val colour = ColorSetting("Ring colour", black, false, "Colour of Normal ring style").withDependency { preset.selected == "Normal" }

    init {
        this.addSettings(
            editTitle,
            preset,
            layers,
            colour
        )
    }

    @SubscribeEvent
    fun onEnterRoom(event: DungeonEvents.RoomEnterEvent) {
        NodeManager.loadNodes()
    }

    private var secretPickedUp = false

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        modMessage("secret!?!?!?!??!?!?!!??!")
        secretPickedUp = true
        scheduleTask (1) {secretPickedUp = false}
    }

    private val cooldownMap = mutableMapOf<String, Boolean>()

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!inDungeons || nodeEditMode || event.phase != TickEvent.Phase.START) return
        nodes.forEach { node ->
            val key = "${node.location.xCoord},${node.location.yCoord},${node.location.zCoord},${node.type}"
            val cooldown: Boolean = cooldownMap[key] == true
            if(inNode(node)) {
                if (node.arguments?.contains("await") == true && !secretPickedUp) return
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
        if (!inDungeons) return
        nodes.forEach { node ->
            val room = currentRoom ?: return
            val realLocation = room.getRealCoords(node.location)
            val x: Double = realLocation.xCoord + 0.5
            val y: Double = realLocation.yCoord
            val z: Double = realLocation.zCoord + 0.5

            val cooldown: Boolean = cooldownMap["$x,$y,$z,${node.type}"] == true
            val color = if (cooldown) WHITE else colour.value

            when(preset.selected) {
                "Trans"     -> renderTransFlag(x, y, z, node.width, node.height)
                "Normal"    -> drawP3boxWithLayers(x, y, z, node.width, node.height, color, layers.value.toInt())
                "LGBTQIA+"  -> renderGayFlag(x, y, z, node.width, node.height)
            }
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (editTitle.enabled && nodeEditMode && inDungeons) {
            val sr = ScaledResolution(mc)
            val t = "Edit Mode"
            renderText(t, sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(t) / 2, sr.scaledHeight / 2 + mc.fontRendererObj.FONT_HEIGHT)
        }
    }

    /*
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
     */
    private fun inNode(node: Node): Boolean {
        val viewerPos = mc.renderManager
        val room = currentRoom ?: return false
        val realLocation = room.getRealCoords(node.location)

        // Calculate the bounds of the node based on its dimensions and position
        val minX = realLocation.xCoord
        val maxX = realLocation.xCoord + node.width
        val minY = realLocation.yCoord
        val maxY = realLocation.yCoord + node.height
        val minZ = realLocation.zCoord
        val maxZ = realLocation.zCoord + node.width

        // Check if the viewer's position is within these bounds
        val viewerX = viewerPos.viewerPosX
        val viewerY = viewerPos.viewerPosY
        val viewerZ = viewerPos.viewerPosZ

        return viewerX in minX..maxX &&
                viewerY in minY..maxY &&
                viewerZ in minZ..maxZ
    }

    private suspend fun executeAction(node: Node) {
        // Check next block for a node and return the x y z yaw pitch

        val newNode = getNextNode(node)
        devMessage(newNode.x)
        devMessage(newNode.y)
        devMessage(newNode.z)
        devMessage(newNode.yaw)
        devMessage(newNode.pitch)
        devMessage(newNode.hasNode)

        val actionDelay: Int = if (node.delay == null) 0 else node.delay!!
        delay(actionDelay.toLong())
        val room2 = currentFullRoom ?: return
        val yaw = room2.getRealYaw(node.yaw)
        node.arguments?.let {
            if ("stop" in it) MovementUtils.stopVelo()
            if ("walk" in it) MovementUtils.setKey("w", true)
            if ("look" in it) RotationUtils.snapTo(yaw, node.pitch)
        }
        when(node.type) {
            "warp" -> {
                swapFromName("aspect of the void")
                MovementUtils.setKey("shift", true)
                FakeRotater.rotate(yaw, node.pitch)
                scheduleTask(0) {
                    MovementUtils.setKey("shift", false)
                }
            }
            "aotv" -> {
                swapFromName("aspect of the void")
                MovementUtils.setKey("shift", false)
                scheduleTask(0) { FakeRotater.rotate(yaw, node.pitch) }
            }
            "hype" -> {
                swapFromName("hyperion")
                MovementUtils.setKey("shift", false)
                scheduleTask(0) { FakeRotater.rotate(yaw, node.pitch) }
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
            "pearl" -> {
                swapFromName("ender pearl")
                MovementUtils.setKey("shift", false)
                scheduleTask(0) { FakeRotater.rotate(yaw, node.pitch) }
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
                RotationUtils.snapTo(yaw, node.pitch)
            }
            "align" -> {
                modMessage("Aligning!")
                mc.thePlayer.setPosition(floor(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, floor(mc.thePlayer.posZ) + 0.5)
            }
            "command" -> {
                modMessage("Sexecuting!")
                commandAny(node.command!!)
            }
        }
    }

    private var lastX: Double = 0.0
    private var lastY: Double = 0.0
    private var lastZ: Double = 0.0

    @SubscribeEvent
    fun onC03(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        val x = event.packet.positionX
        val y = event.packet.positionY
        val z = event.packet.positionZ

        if (event.packet.isMoving) {
            lastX = x
            lastY = y
            lastZ = z
        }
    }
    // Todo: Test this shit
    private fun getNextNode(node: Node): nextNode {
        val room = currentRoom ?: return nextNode(null, null, null, null, null, false)
        val cYaw = node.yaw
        val cPitch = node.pitch
        val nextBlock = EtherWarpHelper.getEtherPos(Vec3(lastX, lastY, lastZ), cYaw, cPitch)
        if (!nextBlock.succeeded) return nextNode(null, null, null, null, null, false)
        val pos: BlockPos = nextBlock.pos!!
        var nextYaw = 9999f
        var nextPitch = 9999f
        var hasNode = false
        nodes.forEach { _ ->
            val realLocation = room.getRealCoords(node.location)
            if (realLocation.xCoord == pos.x.toDouble() && realLocation.yCoord == pos.y.toDouble() + 1.0 && realLocation.zCoord == pos.z.toDouble()) {
                val room2 = currentFullRoom ?: return nextNode(null, null, null, null, null, false)
                nextYaw = room2.getRealYaw(node.yaw)
                nextPitch = node.pitch
                hasNode = true
            }
        }
        if(!hasNode) return nextNode(null, null, null, null, null, false)
        return nextNode(pos.x, pos.y + 1, pos.z, nextYaw, nextPitch, true)
    }

    data class nextNode(
        var x: Int?,
        var y: Int?,
        var z: Int?,
        val yaw: Float?,
        val pitch: Float?,
        val hasNode: Boolean
    )
}
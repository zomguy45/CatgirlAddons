package catgirlroutes.commands

import catgirlroutes.CatgirlRoutes
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.NodeManager.allnodes
import catgirlroutes.commands.NodeManager.loadNodes
import catgirlroutes.commands.NodeManager.saveNodes
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.currentRoomName
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeCoords
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeYaw
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class AutoRoutesCommands : CommandBase() {

    override fun getCommandName(): String = "node"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/node <type> [height] [width]"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.modMessage("Usage: /node §5<§dtype§5> [§dheight§5] [§dwidth§5]")
            return
        }

        val type = args[0].lowercase()
        val h = args.getOrNull(1)?.toFloat() ?: 1F
        val w = args.getOrNull(2)?.toFloat() ?: 1F
        var depth: Int? = null
        var command: String? = null
        var item: String? = null
        val roomname: String = currentRoomName

        val validTypes = setOf("warp", "walk", "walk2", "jump", "jump2", "stop", "boom", "look", "pearlclip", "command", "align", "useitem")

        if (!validTypes.contains(type)) {
            ChatUtils.modMessage("Invalid node type! Use: warp, walk, jump, stop, boom, look, pearlclip, command, align, useitem!")
            return
        }

        when (type) {
            "pearlclip" -> {
                if (args.size != 4) {
                    ChatUtils.modMessage("Usage: /node §5<§dtype§5> [§dheight§5] [§dwidth§5] [§ddepth§5]")
                    return
                }
                depth = args[3].toInt()
            }
            "useitem" -> {
                if (args.size < 4) {
                    ChatUtils.modMessage("Usage: /node §5<§dtype§5> [§dheight§5] [§dwidth§5] [§ditem§5]")
                    return
                }
                item = args.drop(3).joinToString(" ")
            }
            "command" -> {
                if (args.size < 4) {
                    ChatUtils.modMessage("Usage: /node §5<§dtype§5> [§dheight§5] [§dwidth§5] [§dcommand§5]")
                    return
                }
                command = args.drop(3).joinToString(" ")
            }
        }

        val room = currentFullRoom
        val coords = Vec3(Math.round(CatgirlRoutes.mc.thePlayer.posX * 2) / 2.0, Math.round(CatgirlRoutes.mc.thePlayer.posY * 2) / 2.0, Math.round(CatgirlRoutes.mc.thePlayer.posZ * 2) / 2.0)
        val rcoords = room?.getRelativeCoords(coords)
        val yaw = room?.getRelativeYaw(mc.thePlayer.rotationYaw)
        val pitch = mc.thePlayer.rotationPitch

        val node = Node(type, rcoords!!.xCoord, rcoords.yCoord, rcoords.zCoord, w, h, yaw!!, pitch, command, item, depth, roomname, false)

        allnodes.add(node)

        saveNodes()
        loadNodes()

        ChatUtils.modMessage("$type node placed!")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class AutoRoutesAwait : CommandBase() {
    override fun getCommandName(): String = "awaitsecret"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/awaitsecret"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val coords = Vec3(round(mc.thePlayer.posX * 2) / 2.0, round(mc.thePlayer.posY * 2) / 2.0, round(mc.thePlayer.posZ * 2) / 2.0)
        val rcoords = currentFullRoom?.getRelativeCoords(coords)
        val x = rcoords!!.xCoord
        val y = rcoords.yCoord
        val z = rcoords.zCoord
        loadNodes()
        NodeManager.allnodes.forEach { node ->
            if (node.roomname != currentRoomName) return@forEach
            val distance = sqrt(
                (x - node.x).pow(2) + (y - node.y).pow(2) + (z - node.z).pow(2)
            )
            if (distance <= 2.5) {
                node.awaitsecret = true
            }
        }
        saveNodes()
        loadNodes()
        ChatUtils.modMessage("Added await secret!")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class AutoRoutesRemove : CommandBase() {
    override fun getCommandName(): String = "removenode"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/removenode"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val coords = Vec3(round(mc.thePlayer.posX * 2) / 2.0, round(mc.thePlayer.posY * 2) / 2.0, round(mc.thePlayer.posZ * 2) / 2.0)
        val rcoords = currentFullRoom?.getRelativeCoords(coords)
        val x = rcoords!!.xCoord
        val y = rcoords.yCoord
        val z = rcoords.zCoord
        loadNodes()
        allnodes = allnodes.filter { node ->
            if (node.roomname != currentRoomName) return@filter true

            val distance = sqrt(
                (x - node.x).pow(2) + (y - node.y).pow(2) + (z - node.z).pow(2)
            )
            distance >= 2.5
        }.toMutableList()
        saveNodes()
        loadNodes()
        ChatUtils.modMessage("removed nodes!")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

object NodesActive {
    var nodesActive = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.inDungeons) nodesActive = true
        if (!DungeonUtils.inDungeons) nodesActive = false
    }
}

object NodeManager {
    var nodes: MutableList<Node> = mutableListOf()
    var allnodes: MutableList<Node> = mutableListOf()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("config/catgirlroutes/nodes_data.json")

    fun loadNodes() {
        if (file.exists()) {
            allnodes = gson.fromJson(file.readText(), object : TypeToken<List<Node>>() {}.type)
            nodes = allnodes.filter { it.roomname == currentRoomName }.toMutableList()
        }
    }

    fun saveNodes() {
        file.writeText(gson.toJson(allnodes))
    }
}

data class Node(
    val type: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val width: Float,
    val height: Float,
    val yaw: Float,
    val pitch: Float,
    val command: String?,
    val item: String?,
    val depth: Int?,
    val roomname: String,
    var awaitsecret: Boolean,
)
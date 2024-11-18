package catgirlroutes.commands

import catgirlroutes.CatgirlRoutes
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.NodeManager.allnodes
import catgirlroutes.commands.NodeManager.loadNodes
import catgirlroutes.commands.NodeManager.saveNodes
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.Utils
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.currentRoomName
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeYaw
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.Vec3
import java.io.File
import kotlin.math.floor

var nodeeditmode: Boolean = false

class AutoRoutesCommands : CommandBase() {
    override fun getCommandName(): String {
        return "node"
    }

    override fun getCommandAliases(): List<String> {
        return listOf()
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender?, args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.modMessage("No argument specified!")
            return
        }
        when (args[0]) {
            "add" -> {
                val type = args[1].lowercase()
                var depth: Float? = null
                var height = 1F
                var width = 1F
                var delay: Int? = null
                val arguments = mutableListOf<String>()

                if (!arrayListOf("warp", "walk", "look", "stop", "boom", "pearlclip", "pearl", "jump", "align").contains((type))) {
                    ChatUtils.modMessage("Invalid node!")
                    return
                }

                args.drop(2).forEachIndexed { _, arg ->
                    when {
                        arg.startsWith("h") -> height = arg.slice(1 until arg.length).toFloat()
                        arg.startsWith("w") && arg != "walk" -> width = arg.slice(1 until arg.length).toFloat()
                        arg == "stop" -> arguments.add(arg)
                        arg == "look" -> arguments.add(arg)
                        arg == "walk" -> arguments.add(arg)
                        arg == "await" -> arguments.add(arg)
                        arg.startsWith("delay:") -> {
                            delay = arg.slice(6 until arg.length).toInt()
                        }
                    }
                }

                when (type) {
                    "pearlclip" -> {
                        if (args.size < 2) {
                            ChatUtils.modMessage("Usage: /node §5<§dtype§5> [§dheight§5] [§dwidth§5] [§ddepth§5]")
                            return
                        }
                        depth = args[2].toFloat()
                    }
                }

                val room = DungeonUtils.currentRoom ?: return
                val room2 = currentFullRoom ?: return
                val x = floor(mc.thePlayer.posX)
                val y = floor(mc.thePlayer.posY)
                val z = floor(mc.thePlayer.posZ)
                val location = room.getRelativeCoords(Vec3(x, y, z))
                val yaw = room2.getRelativeYaw(mc.thePlayer.rotationYaw)
                val pitch = mc.thePlayer.rotationPitch

                val node = Node(type, location, height, width, yaw, pitch, depth, arguments, delay, currentRoomName)

                allnodes.add(node)

                saveNodes()
                loadNodes()

                ChatUtils.modMessage("$type placed!")
            }
            "edit" -> {
                nodeeditmode = !nodeeditmode
                ChatUtils.modMessage("Editmode toggled!")
            }
            "remove" -> {
                val range = args.getOrNull(1)?.toDoubleOrNull() ?: 2.0 // Default range to 2 if not provided
                allnodes = allnodes.filter { node ->
                    if (node.room != currentRoomName) return@filter true
                    val room = DungeonUtils.currentRoom ?: return
                    val realLocation = room.getRealCoords(node.location)
                    val distance =
                        Utils.distanceToPlayer(realLocation.xCoord, realLocation.yCoord, realLocation.zCoord)
                    distance >= range
                }.toMutableList()
                saveNodes()
                loadNodes()
            }
            "undo" -> {
                allnodes.removeLast()
                saveNodes()
                loadNodes()
            }
            "clear" -> {
                sender?.addChatMessage(
                    ChatComponentText("${CatgirlRoutes.CHAT_PREFIX} Are you sure?")
                    .apply {
                        chatStyle = ChatStyle().apply {
                            chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/node clearconfirm")
                            chatHoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("${CatgirlRoutes.CHAT_PREFIX} Click to clear ALL rooms!")
                            )
                        }
                    }
                )
            }
            "clearroom" -> {
                sender?.addChatMessage(
                    ChatComponentText("${CatgirlRoutes.CHAT_PREFIX} Are you sure?")
                        .apply {
                            chatStyle = ChatStyle().apply {
                                chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/node clearroomconfirm")
                                chatHoverEvent = HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("${CatgirlRoutes.CHAT_PREFIX} Click to clear CURRENT room!")
                                )
                            }
                        }
                )
            }
            "clearroomconfirm" -> {
                allnodes = allnodes.filter { node ->
                    node.room != currentRoomName
                }.toMutableList()
                ChatUtils.modMessage("$currentRoomName cleared!")
                saveNodes()
                loadNodes()
            }
            "clearconfirm" -> {
                allnodes = mutableListOf()
                ChatUtils.modMessage("All routes cleared!")
                saveNodes()
                loadNodes()
            }
            "load" -> {
                ChatUtils.modMessage("Loaded $currentRoomName")
                loadNodes()
            }
            "save" -> saveNodes()
            else -> ChatUtils.modMessage("Invalid command!")
        }
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
            nodes = allnodes.filter { it.room == currentRoomName }.toMutableList()
        }
    }

    fun saveNodes() {
        file.writeText(gson.toJson(allnodes))
    }
}

data class Node(
    val type: String,
    var location: Vec3,
    var height: Float,
    var width: Float,
    var yaw: Float,
    var pitch: Float,
    var depth: Float?,
    var arguments: List<String>?,
    var delay: Int?,
    var room: String,
)
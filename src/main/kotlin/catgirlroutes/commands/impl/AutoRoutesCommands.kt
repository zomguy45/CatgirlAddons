package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.NodeManager.allNodes
import catgirlroutes.commands.impl.NodeManager.loadNodes
import catgirlroutes.commands.impl.NodeManager.saveNodes
import catgirlroutes.commands.commodore
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.ChatUtils.getPrefix
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.distanceToPlayer
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.currentRoomName
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeYaw
import com.github.stivais.commodore.utils.GreedyString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.Vec3
import java.io.File
import kotlin.math.floor

object NodeManager {
    var nodes: MutableList<Node> = mutableListOf()
    var allNodes: MutableList<Node> = mutableListOf()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("config/catgirlroutes/nodes_data.json")

    fun loadNodes() {
        if (file.exists()) {
            allNodes = gson.fromJson(file.readText(), object : TypeToken<List<Node>>() {}.type)
            nodes = allNodes.filter { it.room == currentRoomName }.toMutableList()
        }
    }

    fun saveNodes() {
        file.writeText(gson.toJson(allNodes))
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
    var command: String?,
    var room: String,
)

var nodeEditMode: Boolean = false
var nodeTypes: List<String> = listOf("warp", "walk", "look", "stop", "boom", "pearlclip", "pearl", "jump", "align", "command", "aotv", "hype")

val autoRoutesCommands = commodore("node") {

    literal("help").runs {
        modMessage("""
            List of AutoRoutes commands:
              §7/node add §5<§dtype§5> [§ddepth§5] [§dargs..§5] §8: (§7/node add §rfor info)
              §7/node edit (em) §8: §rmakes nodes inactive
              §7/node remove §5[§drange§5]§r §8: §rremoves nodes in range (default value - 2)
              §7/node undo §8: §rremoves last placed node
              §7/node clear §8: §rclears ALL nodes
              §7/node clearroom §8: §rclears nodes in current room
              §7/node load §8: §rloads routes
              §7/node save §8: §rsaves routes
              §7/node help §8: §rshows this message
        """.trimIndent())
    }

    literal("add") {
        runs {
            modMessage("""
                Usage: §7/node add §5<§dtype§5> [§dargs..§5] 
                  List of node types: §7${nodeTypes.joinToString()}
                    §7- warp §8: §retherwarp
                    §7- walk §8: §rmakes the player walk
                    §7- look §8: §rturns player's head
                    §7- stop §8: §rstops the player from moving
                    §7- boom §8: §ruses boom tnt
                    §7- pearlclip §8: §rpearl clips with a specified depth
                    §7- pearl §8: §ruses ender pearl
                    §7- jump §8: §rmakes the player jump
                    §7- align §8: §raligns the player with the centre of a block
                    §7- command §8: §rexecutes a specified command
                    §7- aotv §8: §ruses AOTV
                    §7- hype §8: §ruses hyperion
                  List of args: §7w_, h_, delay_, look, walk, await, stop
                    §7- w §8: §rnode width (w1 - default)
                    §7- h §8: §rnode height (h1 - default)
                    §7- delay §8: §rnode action delay (delay0 - default)
                    §7- look §8: §rturns player's head
                    §7- walk §8: §rmakes the player walk
                    §7- await §8: §rawait for secret
                    §7- stop §8: §rsets your velocity to 0
            """.trimIndent())
        }

        runs { type: String, text: GreedyString? ->
            var depth: Float? = null
            var height = 1F
            var width = 1F
            var delay: Int? = null
            var command: String? = null
            val arguments = mutableListOf<String>()

            if (!nodeTypes.contains(type)) {
                return@runs modMessage("""
                    §cInvalid node type!
                      Types: §7${nodeTypes.joinToString()}
                """.trimIndent())
            }

            /**
             * regex shit is for commands (/node add command <"cmd"> [args])
             */
            val args = text?.string?.let { Regex("\"[^\"]*\"|\\S+").findAll(it).map { m -> m.value }.toList() } ?: emptyList()
            debugMessage(args)

            when (type) {
                "pearlclip" -> {
                    depth = args.firstOrNull { it.toFloatOrNull() != null }?.toFloat()
                        ?: return@runs modMessage("Usage: §7/node add §dpearlclip §5<§ddepth§5> [§dargs..§5]")
                }
                "command" -> {
                    command = args.firstOrNull { it.startsWith('"') && it.endsWith('"') }?.replace("\"", "")
                        ?: return@runs modMessage("Usage: §7/node add §dcommand §5<§d\"cmd\"§5> [§dargs..§5]")
                }
            }

            args.forEach { arg ->
                when {
                    arg.startsWith("w") && arg != "walk" -> width = arg.slice(1 until arg.length).toFloat()
                    arg.startsWith("h") -> height = arg.slice(1 until arg.length).toFloat()
                    arg.startsWith("delay") -> { delay = arg.substring(5).toIntOrNull() ?: return@runs modMessage("§cInvalid delay!") }
                    arg in listOf("stop", "look", "walk", "await") -> arguments.add(arg)
                }
            }

            val room = DungeonUtils.currentRoom ?: return@runs
            val room2 = currentFullRoom ?: return@runs
            val x = floor(mc.thePlayer.posX)
            val y = floor(mc.thePlayer.posY)
            val z = floor(mc.thePlayer.posZ)
            val location = room.getRelativeCoords(Vec3(x, y, z))
            val yaw = room2.getRelativeYaw(mc.thePlayer.rotationYaw)
            val pitch = mc.thePlayer.rotationPitch

            val node = Node(type, location, height, width, yaw, pitch, depth, arguments, delay, command, currentRoomName)

            allNodes.add(node)

            modMessage("${type.capitalize()} placed!")
            saveNodes()
            loadNodes()
        }.suggests("type", nodeTypes)
    }


    literal("edit").runs {
        nodeEditMode = !nodeEditMode
        modMessage("EditMode ${if (nodeEditMode) "§aenabled" else "§cdisabled"}§r!")
    }

    literal("em").runs {
        nodeEditMode = !nodeEditMode
        modMessage("EditMode ${if (nodeEditMode) "§aenabled" else "§cdisabled"}§r!")
    }

    literal("remove").runs { range: Double? ->
        allNodes = allNodes.filter { node -> node.room != currentRoomName || (DungeonUtils.currentRoom
            ?.let { room ->
                val realLocation = room.getRealCoords(node.location)
                distanceToPlayer(realLocation.xCoord, realLocation.yCoord, realLocation.zCoord) >= (range ?: 2.0) } ?: false)
        }.toMutableList()
        modMessage("Removed")
        saveNodes()
        loadNodes()
    }

    literal("undo").runs {
        modMessage("Undone " + allNodes.last().type)
        allNodes.removeLast()
        saveNodes()
        loadNodes()
    }

    literal("clearroom").runs {
        mc.thePlayer?.addChatMessage(
            ChatComponentText("${getPrefix()} §8»§r Are you sure you want to clear §nCURRENT§r room?")
                .apply {
                    chatStyle = ChatStyle().apply {
                        chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/node clearroomconfirm")
                        chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("Click to clear §nCURRENT§r route!")
                        )
                    }
                }
            )
    }

    literal("clearroomconfirm").runs {
        allNodes = allNodes.filter { node -> node.room != currentRoomName }.toMutableList()
        modMessage("$currentRoomName cleared!")
        saveNodes()
        loadNodes()
    }

    literal("clear").runs {
        mc.thePlayer?.addChatMessage(
            ChatComponentText("${getPrefix()} §8»§r Are you sure you want to clear §nALL§r rooms?")
                .apply {
                    chatStyle = ChatStyle().apply {
                        chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/node clearconfirm")
                        chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("Click to clear §nALL§r rooms!")
                        )
                    }
                }
            )
    }

    literal("clearconfirm").runs {
        allNodes = mutableListOf()
        modMessage("All routes cleared!")
        saveNodes()
        loadNodes()
    }

    literal("load").runs {
        modMessage("Loaded $currentRoomName")
        loadNodes()
    }

    literal("save").runs {
        modMessage("Saved")
        saveNodes()
    }
    
}


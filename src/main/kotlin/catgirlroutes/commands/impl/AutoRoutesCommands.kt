package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.NodeManager.allNodes
import catgirlroutes.commands.impl.NodeManager.loadNodes
import catgirlroutes.commands.impl.NodeManager.saveNodes
import catgirlroutes.commands.commodore
import catgirlroutes.utils.ChatUtils.getPrefix
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.distanceToPlayer
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.currentRoomName
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeYaw
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
    var room: String,
)

var nodeEditMode: Boolean = false

val autoRoutesCommands = commodore("node") {
    
    runs {
        modMessage("""
            §cInvalid usage!
              §7Run /node help for help.
        """.trimIndent())
    }

    literal("help").runs {
        modMessage("""
            List of AutoRoutes commands:
              §7/node add §5<§dtype§5> [§ddepth§5] [§dargs..?§5]
              §7/node edit
              §7/node remove §5<§drange§5>§r
              §7/node undo
              §7/node clear
              §7/node clearroom
              §7/node load
              §7/node save
              §7/node help
        """.trimIndent())
    }

    literal("add") {
        runs {
            modMessage("""
                Usage: §7/node add §5<§dtype§5> [§dargs..?§5] 
                  §7List of args: w_, h_, delay_, look, walk, await, stop
            """.trimIndent())
        }

        runs { type: String, w: String?, h: String?, dep: String?, del: String?, a1: String?, a2: String?, a3: String?, a4: String? ->
            var depth: Float? = null
            var height = 1F
            var width = 1F
            var delay: Int? = null
            val arguments = mutableListOf<String>()

            if (!arrayListOf("warp", "walk", "look", "stop", "boom", "pearlclip", "pearl", "jump", "align", "aotv", "hype").contains(type)) {
                modMessage("""
                    §cInvalid node type!
                      Types: §7warp, walk, look, stop, boom, pearlclip, pearl, jump, align, aotv, hype
                """.trimIndent())
                return@runs
            }

            val args: List<String> = listOfNotNull(w, h, dep, del, a1, a2, a3, a4)


            args.forEach { arg ->
                when {
                    arg.startsWith("w") && arg != "walk" -> width = arg.slice(1 until arg.length).toFloat()
                    arg.startsWith("h") -> height = arg.slice(1 until arg.length).toFloat()
                    arg.startsWith("delay") -> { delay = arg.substring(5).toIntOrNull() ?: return@runs modMessage("§cInvalid delay!") }
                    arg in listOf("stop", "look", "walk", "await") -> arguments.add(arg)
                }
            }

            when (type) {
                "pearlclip" -> {
                    depth = args.firstOrNull { it.toFloatOrNull() != null }?.toFloat()
                        ?: return@runs modMessage("Usage: §7/node add §dpearlclip §5<§ddepth§5> [§dargs..§5]")
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

            val node = Node(type, location, height, width, yaw, pitch, depth, arguments, delay, currentRoomName)

            allNodes.add(node)

            modMessage("${type.capitalize()} placed!")
            saveNodes()
            loadNodes()
        }
    }


    literal("edit").runs {
        nodeEditMode = !nodeEditMode
        modMessage("EditMode " + if (nodeEditMode) "§aenabled" else "§cdisabled" + "§r!")
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


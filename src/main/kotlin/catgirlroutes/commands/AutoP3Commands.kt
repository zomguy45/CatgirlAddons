package catgirlroutes.commands

import catgirlroutes.CatgirlRoutes.Companion.CHAT_PREFIX
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.RingManager.allrings
import catgirlroutes.commands.RingManager.loadRings
import catgirlroutes.commands.RingManager.saveRings
import catgirlroutes.module.impl.dungeons.AutoP3.selectedRoute
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.distanceToPlayer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.Vec3
import java.io.File

var route: String = selectedRoute.value
var ringsActive: Boolean = false
var ringeditmode: Boolean = false

class AutoP3Commands : CommandBase() {
    override fun getCommandName(): String {
        return "p3"
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
            modMessage("No argument specified!")
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
                var lookblock: Vec3? = null

                if (!arrayListOf("walk", "look", "stop", "bonzo", "boom", "hclip", "block", "edge", "vclip", "jump", "term", "align").contains((type))) {
                    modMessage("Invalid ring!")
                    return
                }

                args.drop(2).forEachIndexed { _, arg ->
                    when {
                        arg.startsWith("h") -> height = arg.slice(1 until arg.length).toFloat()
                        arg.startsWith("w") && arg != "walk" -> width = arg.slice(1 until arg.length).toFloat()
                        arg == "stop" -> arguments.add(arg)
                        arg == "look" -> arguments.add(arg)
                        arg == "walk" -> arguments.add(arg)
                        arg == "term" -> arguments.add(arg)
                        arg.startsWith("delay:") -> {
                            delay = arg.slice(6 until arg.length).toInt()
                        }
                    }
                }

                when (type) {
                    "vclip" -> {
                        if (args.size < 2) {
                            modMessage("Usage: /ring §5<§dtype§5> [§dheight§5] [§dwidth§5] [§ddepth§5]")
                            return
                        }
                        depth = args[2].toFloat()
                    }
                    "block" -> {
                        lookblock = mc.thePlayer.rayTrace(40.0, 1F).hitVec
                    }
                }

                val x = Math.round(mc.thePlayer.posX * 2) / 2.0
                val y = Math.round(mc.thePlayer.posY * 2) / 2.0
                val z = Math.round(mc.thePlayer.posZ * 2) / 2.0
                val location = Vec3(x, y, z)
                val yaw = mc.thePlayer.rotationYaw
                val pitch = mc.thePlayer.rotationPitch

                val ring = Ring(type, location, yaw, pitch, height, width, lookblock, depth, arguments, delay, route)

                allrings.add(ring)

                saveRings()
                loadRings()

                modMessage("$type placed!")
            }
            "edit" -> {
                ringeditmode = !ringeditmode
                modMessage("Editmode toggled!")
            }
            "toggle" -> {
                ringsActive = !ringsActive
                modMessage("Rings toggled!")
            }
            "remove" -> {
                val range = args.getOrNull(1)?.toDoubleOrNull() ?: 2.0 // Default range to 2 if not provided
                allrings = allrings.filter { ring ->
                    if (ring.route != route) return@filter true
                    val distance = distanceToPlayer(ring.location.xCoord, ring.location.yCoord, ring.location.zCoord)
                    distance >= range
                }.toMutableList()
                saveRings()
                loadRings()
            }
            "undo" -> {
                allrings.removeLast()
                saveRings()
                loadRings()
            }
            "clear" -> {
                sender?.addChatMessage(ChatComponentText("$CHAT_PREFIX Are you sure?")
                    .apply {
                        chatStyle = ChatStyle().apply {
                            chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p3 clearconfirm")
                            chatHoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("$CHAT_PREFIX Click to clear ALL routes!")
                            )
                        }
                    }
                )
            }
            "clearroute" -> {
                sender?.addChatMessage(
                    ChatComponentText("$CHAT_PREFIX Are you sure?")
                    .apply {
                        chatStyle = ChatStyle().apply {
                            chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p3 clearrouteconfirm")
                            chatHoverEvent = HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("$CHAT_PREFIX Click to clear CURRENT route!")
                            )
                        }
                    }
                )
            }
            "clearrouteconfirm" -> {
                allrings = allrings.filter { ring ->
                    // Filter rings based on the route and distance criteria
                    ring.route != route
                }.toMutableList()
                modMessage("$route cleared!")
                saveRings()
                loadRings()
            }
            "clearconfirm" -> {
                allrings = mutableListOf()
                modMessage("All routes cleared!")
                saveRings()
                loadRings()
            }
            "load" -> {
                if (args.size < 2) {
                    route = selectedRoute.value
                } else {
                    route = args[1]
                }
                modMessage("Loaded $route")
                loadRings()
            }
            "save" -> saveRings()
            else -> modMessage("Invalid command!")
        }
    }
}

object RingManager {
    var rings: MutableList<Ring> = mutableListOf()
    var allrings: MutableList<Ring> = mutableListOf()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("config/catgirlroutes/rings_data.json")

    fun loadRings() {
        if (file.exists()) {
            allrings = gson.fromJson(file.readText(), object : TypeToken<List<Ring>>() {}.type)
            rings = allrings.filter { it.route == route }.toMutableList()
        }
    }

    fun saveRings() {
        file.writeText(gson.toJson(allrings))
    }
}

data class Ring(
    val type: String,
    var location: Vec3,
    var yaw: Float,
    var pitch: Float,
    var height: Float,
    var width: Float,
    var lookBlock: Vec3?,
    var depth: Float?,
    var arguments: List<String>?,
    var delay: Int?,
    var route: String,
)
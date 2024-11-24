package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.*
import catgirlroutes.commands.impl.RingManager.allRings
import catgirlroutes.commands.impl.RingManager.loadRings
import catgirlroutes.commands.impl.RingManager.saveRings
import catgirlroutes.module.impl.dungeons.AutoP3
import catgirlroutes.module.impl.dungeons.AutoP3.selectedRoute
import catgirlroutes.utils.ChatUtils.getPrefix
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.distanceToPlayer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.Vec3
import java.io.File

object RingManager {
    var rings: MutableList<Ring> = mutableListOf()
    var allRings: MutableList<Ring> = mutableListOf()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("config/catgirlroutes/rings_data.json")

    fun loadRings() {
        if (file.exists()) {
            allRings = gson.fromJson(file.readText(), object : TypeToken<List<Ring>>() {}.type)
            rings = allRings.filter { it.route == route }.toMutableList()
        }
    }

    fun saveRings() {
        file.writeText(gson.toJson(allRings))
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

var route: String = selectedRoute.value
var ringEditMode: Boolean = false


val autoP3Commands = commodore("p3") {

    literal("help").runs {
        modMessage("""
            List of AutoP3 commands:
              §7/p3 add §5<§dtype§5> [§ddepth§5] [§dargs..§5] §8: §radds a ring (§7/p3 add §rfor info)
              §7/p3 edit §8: §rmakes rings inactive
              §7/p3 toggle §8: §rtoggles the module
              §7/p3 remove §5<§drange§5>§r §8: §rremoves rings in range (default value - 2)
              §7/p3 undo §8: §rremoves last placed node
              §7/p3 clearroute §8: §rclears current route
              §7/p3 clear §8: §rclears ALL routes
              §7/p3 load §5[§droute§5]§r §8: §rloads selected route
              §7/p3 save §8: §rsaves current route
              §7/p3 help §8: §rshows this message
        """.trimIndent())
    }

    literal("add") {
        runs {
            modMessage("""
                Usage: §7/p3 add §5<§dtype§5> [§ddepth§5] [§dargs..§5]
                  List of types:§7 walk, look, stop, bonzo, boom, hclip, block, edge, vclip, jump, align
                    §7- walk §8: §rmakes the player walk
                    §7- look §8: §rturns player's head
                    §7- stop §8: §rstops the player from moving
                    §7- bonzo §8: §ruses bonzo staff
                    §7- boom §8: §ruses boom tnt
                    §7- edge §8: §rjumps from block's edge
                    §7- vclip §8: §ruses lava clip (needs depth to be specified)
                    §7- jump §8: §rmakes the player jump
                    §7- align §8: §raligns the player with the centre of a block 
                  List of args: §7w_, h_, delay_, look, walk, term, stop
                    §7- w §8: §rring width (w1 - default)
                    §7- h §8: §rring height (h1 - default)
                    §7- delay §8: §rring action delay (delay0 - default)
                    §7- look §8: §rturns player's head
                    §7- walk §8: §rmakes the player walk
                    §7- term §8: §ractivates the node when terminal opens
                    §7- stop §8: §rsets your velocity to 0
            """.trimIndent())
        }

        runs { type: String, w: String?, h: String?, dep: String?, del: String?, a1: String?, a2: String?, a3: String?, a4: String? ->
            var depth: Float? = null
            var height = 1F
            var width = 1F
            var delay: Int? = null
            val arguments = mutableListOf<String>()
            var lookBlock: Vec3? = null

            if (!arrayListOf("walk", "look", "stop", "bonzo", "boom", "hclip", "block", "edge", "vclip", "jump", "align").contains(type)) {
                modMessage("""
                    §cInvalid ring type!
                      §rTypes: §7walk, look, stop, bonzo, boom, hclip, block, edge, vclip, jump, term, align
                """.trimIndent())
                return@runs
            }

            val args: List<String> = listOfNotNull(w, h, dep, del, a1, a2, a3, a4)

            args.forEach { arg ->
                when {
                    arg.startsWith("w") && arg != "walk" -> width = arg.slice(1 until arg.length).toFloat()
                    arg.startsWith("h") && arg != "hclip" -> height = arg.slice(1 until arg.length).toFloat()
                    arg.startsWith("delay") -> { delay = arg.substring(5).toIntOrNull() ?: return@runs modMessage("§cInvalid delay!") }
                    arg in listOf("stop", "look", "walk", "term") -> arguments.add(arg)
                }
            }

            when (type) {
                "block" -> {
                    lookBlock = mc.thePlayer.rayTrace(40.0, 1F).hitVec
                }
                "vclip" -> {
                    depth = args.firstOrNull { it.toFloatOrNull() != null }?.toFloat()
                        ?: return@runs modMessage("Usage: §7/p3 add §dvclip §5<§ddepth§5> [§dargs..§5]")
                }
            }

            val x = Math.round(mc.thePlayer.posX * 2) / 2.0
            val y = Math.round(mc.thePlayer.posY * 2) / 2.0
            val z = Math.round(mc.thePlayer.posZ * 2) / 2.0
            val location = Vec3(x, y, z)
            val yaw = mc.thePlayer.rotationYaw
            val pitch = mc.thePlayer.rotationPitch

            val ring = Ring(type, location, yaw, pitch, height, width, lookBlock, depth, arguments, delay, route)

            allRings.add(ring)

            modMessage("${type.capitalize()} ring placed!")
            saveRings()
            loadRings()

        }.suggests("type", listOf("walk", "look", "stop", "bonzo", "boom", "hclip", "block", "edge", "vclip", "jump", "align"))
    }

    literal("edit").runs {
        ringEditMode = !ringEditMode
        modMessage("EditMode ${if (ringEditMode) "§aenabled" else "§cdisabled"}")
    }

    literal("toggle").runs {
        AutoP3.onKeyBind()
    }

    literal("remove").runs { range: Double? ->
        allRings = allRings.filter { ring -> ring.route != route || distanceToPlayer(ring.location.xCoord, ring.location.yCoord, ring.location.zCoord) >= (range ?: 2.0) }.toMutableList()
        modMessage("Removed")
        saveRings()
        loadRings()
    }

    literal("undo").runs {
        modMessage("Undone " + allRings.last().type)
        allRings.removeLast()
        saveRings()
        loadRings()
    }

    literal("clearroute").runs {
        mc.thePlayer?.addChatMessage(
            ChatComponentText("${getPrefix()} §8»§r Are you sure you want to clear §nCURRENT§r route?")
                .apply {
                    chatStyle = ChatStyle().apply {
                        chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p3 clearrouteconfirm")
                        chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("Click to clear §nCURRENT§r route!")
                        )
                    }
                }
        )
    }

    literal("clearrouteconfirm").runs {
        allRings = allRings.filter { ring -> ring.route != route }.toMutableList()
        modMessage("$route cleared!")
        saveRings()
        loadRings()
    }

    literal("clear").runs {
        mc.thePlayer?.addChatMessage(
            ChatComponentText("${getPrefix()} §8»§r Are you sure you want to clear §nALL§r routes?")
                .apply {
                    chatStyle = ChatStyle().apply {
                        chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p3 clearconfirm")
                        chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("Click to clear §nALL§r routes!")
                        )
                    }
                }
        )
    }

    literal("clearconfirm").runs {
        allRings = mutableListOf()
        modMessage("All routes cleared!")
        saveRings()
        loadRings()
    }

    literal("load").runs { routeNum: String? ->
        route = routeNum ?: selectedRoute.value
        modMessage("Loaded $route")
        loadRings()
    }

    literal("save").runs {
        modMessage("Saved")
        saveRings()
    }

}
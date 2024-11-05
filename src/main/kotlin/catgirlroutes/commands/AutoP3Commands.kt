package catgirlroutes.commands

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.RingManager.allrings
import catgirlroutes.commands.RingManager.loadRings
import catgirlroutes.commands.RingManager.saveRings
import catgirlroutes.module.impl.dungeons.AutoP3.selectedRoute
import catgirlroutes.utils.AutoRouteUtils.cancelRotate
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.airClick
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

var route: String = selectedRoute.value
var editmodetoggled: Boolean = false

class AutoP3Commands : CommandBase() {

    override fun getCommandName(): String = "ring"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/ring <type> [height] [width]"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            modMessage("Usage: /ring §5<§dtype§5> [§dheight§5] [§dwidth§5]")
            return
        }

        val type = args[0].lowercase()
        var h = args.getOrNull(1)?.toFloat() ?: 1F
        var w = args.getOrNull(2)?.toFloat() ?: 1F
        var depth: Int? = null
        var command: String? = null
        var item: String? = null

        val validTypes = setOf("walk", "walk2", "jump", "jump2", "stop", "boom", "hclip", "bonzo", "look", "aura", "bonzo2", "vclip", "command", "align", "useitem")

        if (!validTypes.contains(type)) {
            modMessage("Invalid ring type! Use: walk, jump, stop, boom, hclip, bonzo, look, aura, vclip, command, align, useitem!")
            return
        }

        when (type) {
            "vclip" -> {
                if (args.size != 4) {
                    modMessage("Usage: /ring §5<§dtype§5> [§dheight§5] [§dwidth§5] [§ddepth§5]")
                    return
                }
                depth = args[3].toInt()
            }
            "align" -> {
                h = 1F
                w = 1F
            }
            "useitem" -> {
                if (args.size < 4) {
                    modMessage("Usage: /ring §5<§dtype§5> [§dheight§5] [§dwidth§5] [§ditem§5]")
                    return
                }
                item = args.drop(3).joinToString(" ")
            }
            "command" -> {
                if (args.size < 4) {
                    modMessage("Usage: /ring §5<§dtype§5> [§dheight§5] [§dwidth§5] [§dcommand§5]")
                    return
                }
                command = args.drop(3).joinToString(" ")
            }
        }

        val x = Math.round(mc.thePlayer.posX * 2) / 2.0
        val y = Math.round(mc.thePlayer.posY * 2) / 2.0
        val z = Math.round(mc.thePlayer.posZ * 2) / 2.0
        val yaw = mc.thePlayer.rotationYaw
        val pitch = mc.thePlayer.rotationPitch

        val ring = Ring(type, x, y, z, w, h, yaw, pitch, depth, null, command, item, route)

        allrings.add(ring)

        saveRings()
        loadRings()

        modMessage("$type placed!")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class AutoP3Remove : CommandBase() {
    override fun getCommandName(): String = "removering"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/removering"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val x = round(mc.thePlayer.posX * 2) / 2.0
        val y = round(mc.thePlayer.posY * 2) / 2.0
        val z = round(mc.thePlayer.posZ * 2) / 2.0
        loadRings()
        allrings = allrings.filter { ring ->
            if (ring.route != route) return@filter true

            val distance = sqrt(
                (x - ring.x).pow(2) + (y - ring.y).pow(2) + (z - ring.z).pow(2)
            )
            distance >= 2.5
        }.toMutableList()
        saveRings()
        loadRings()
        modMessage("removed rings!")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class AutoP3Await : CommandBase() {
    override fun getCommandName(): String = "awaitmessage"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/awaitmessage"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val x = round(mc.thePlayer.posX * 2) / 2.0
        val y = round(mc.thePlayer.posY * 2) / 2.0
        val z = round(mc.thePlayer.posZ * 2) / 2.0
        val message = args.joinToString(" ")
        loadRings()
        allrings.forEach { ring ->
            if (ring.route != route) return@forEach
            val distance = sqrt(
                (x - ring.x).pow(2) + (y - ring.y).pow(2) + (z - ring.z).pow(2)
            )
            if (distance <= 2.5) {
                ring.message = message
            }
        }
        saveRings()
        loadRings()
        modMessage("Added await message!")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class AutoP3EditMode : CommandBase() {
    override fun getCommandName(): String = "editmode"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/editmode"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (editmodetoggled) {
            editmodetoggled = false
            modMessage("Edit mode disabled!")
        } else {
            editmodetoggled = true
            modMessage("Edit mode enabled!")
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

var ringsActive = false

class AutoP3Force : CommandBase() {
    override fun getCommandName(): String = "ringtoggle"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/ringstoggle"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (ringsActive) {
            ringsActive = false
            modMessage("Rings disabled!")
        } else {
            ringsActive = true
            modMessage("Rings enabled!")
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        if (message == "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!") {
            ringsActive = true
        }
        if (message == "[BOSS] Necron: All this, for nothing...") {
            ringsActive = false
        }
    }
}

class AutoP3Undo : CommandBase() {
    override fun getCommandName(): String = "ringundo"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/ringundo"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        loadRings()
        allrings.removeLast()
        saveRings()
        loadRings()
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class AutoP3Load : CommandBase() {
    override fun getCommandName(): String = "load"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/load"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        route = if (args.isEmpty()) {
            selectedRoute.value
        }
        else {
            args[0]
        }
        loadRings()
        modMessage("Loaded $route")
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class Test : CommandBase() {
    override fun getCommandName(): String = "canceltest"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/canceltest"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.size != 2) {
            return
        }
        cancelRotate(args[0].toFloat(), args[1].toFloat())
        airClick()
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
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
    val x: Double,
    val y: Double,
    val z: Double,
    val width: Float,
    val height: Float,
    val yaw: Float,
    val pitch: Float,
    val depth: Int?,
    var message: String?,
    val command: String?,
    val item: String?,
    val route: String
)
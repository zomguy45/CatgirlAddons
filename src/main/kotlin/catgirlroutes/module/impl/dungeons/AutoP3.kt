package catgirlroutes.module.impl.dungeons

import Hclip.hclip
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.Ring
import catgirlroutes.commands.RingManager.loadRings
import catgirlroutes.commands.RingManager.rings
import catgirlroutes.commands.editmodetoggled
import catgirlroutes.commands.ringsActive
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.misc.LavaClip.lavaClipToggle
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ChatUtils.sendChat
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.MovementUtils.jump
import catgirlroutes.utils.MovementUtils.setKey
import catgirlroutes.utils.MovementUtils.stopVelo
import catgirlroutes.utils.ServerRotateUtils.resetRotations
import catgirlroutes.utils.ServerRotateUtils.set
import catgirlroutes.utils.Utils.airClick
import catgirlroutes.utils.Utils.leftClick
import catgirlroutes.utils.Utils.snapTo
import catgirlroutes.utils.Utils.swapFromName
import catgirlroutes.utils.render.WorldRenderUtils.drawP3box
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color.black
import java.awt.Color.white
import kotlin.math.abs
import kotlin.math.floor


object AutoP3 : Module(
    "Auto P3",
    category = Category.DUNGEON,
    description = "A module that allows you to place down rings that execute various actions."
){
    val selectedRoute = StringSetting("Selected route", "1", description = "Name of the selected route for auto p3.")

    init {
        this.addSettings(
            AutoP3.selectedRoute
        )
    }

    @SubscribeEvent
    fun onLoad(event: WorldEvent.Load) {
        loadRings()
    }

    private val registeredmessages: MutableList<String> = ArrayList()

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val message = event.message.unformattedText
        rings.forEach{ ring ->
            if (ring.message == null) return@forEach
            if (message.contains(ring.message!!) && registeredmessages.indexOf(message) == -1) {
                registeredmessages.add(message)
            }
        }
    }


    private val cooldownMap = mutableMapOf<String, Boolean>()

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!ringsActive || !this.enabled || editmodetoggled) return
        rings.forEach { ring ->
            //if(registeredmessages.indexOf(ring.message) == -1 && ring.message != null) {
                //return
            //} else if (registeredmessages.indexOf(ring.message) != -1) {
                //val index = registeredmessages.indexOf(ring.message)
                //scheduleTask(1) {if(index != -1) registeredmessages.removeAt(index)}
            //}
            val key = "${ring.x},${ring.y},${ring.z},${ring.type}"
            val cooldown: Boolean = cooldownMap[key] == true
            if(inRing(ring)) {
                if (cooldown) return@forEach
                cooldownMap[key] = true
                executeAction(ring)
            } else if (cooldown) {
                cooldownMap[key] = false
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!ringsActive || !this.enabled ) return
        rings.forEach { ring ->
            val key = "${ring.x},${ring.y},${ring.z},${ring.type}"
            val cooldown: Boolean = cooldownMap[key] == true
            val color = if (cooldown) {
                white
            } else {
                black
            }
            drawP3box(ring.x - ring.width / 2, ring.y, ring.z - ring.width / 2, ring.width.toDouble(), ring.height.toDouble(), ring.width.toDouble(), color, 4F, false)
        }
    }

    private fun inRing(ring: Ring): Boolean {
        val distanceX = abs(mc.renderManager.viewerPosX - ring.x)
        val distanceY = abs(mc.renderManager.viewerPosY - ring.y)
        val distanceZ = abs(mc.renderManager.viewerPosZ - ring.z)

        return distanceX < (ring.width / 2) && distanceY < ring.height && distanceY >= -0.5 && distanceZ < (ring.width / 2);
    }

    private fun executeAction(ring: Ring) {
        when(ring.type) {
            "walk" -> {
                modMessage("Walking!")
                stopVelo()
                setKey("w", true)
                snapTo(ring.yaw, ring.pitch)
            }
            "walk2" -> {
                modMessage("Walking!")
                setKey("w", true)
                snapTo(ring.yaw, ring.pitch)
            }
            "jump" -> {
                modMessage("Jumping!")
                snapTo(ring.yaw, ring.pitch)
                jump()
            }
            "jump2" -> {
                modMessage("Jumping!")
                jump()
            }
            "stop" -> {
                modMessage("Stopping!")
                setKey("w", false)
                stopVelo()
            }
            "boom" -> {
                modMessage("Bomb denmark!")
                swapFromName("infinityboom tnt")
                snapTo(ring.yaw, ring.pitch)
                scheduleTask(1) {leftClick()}
            }
            "hclip" -> {
                modMessage("Hclipping!")
                hclip(ring.yaw)
            }
            "vclip" -> {
                modMessage("Vclipping!")
                lavaClipToggle(ring.depth!!.toDouble(), true)
            }
            "bonzo" -> {
                modMessage("Bonzoing!")
                stopVelo()
                swapFromName("bonzo's staff")
                set(ring.yaw, ring.pitch)
                scheduleTask(1) {
                    airClick()
                    resetRotations()
                }
            }
            "look" -> {
                modMessage("Looking!")
                snapTo(ring.yaw, ring.pitch)
            }
            "command" -> {
                modMessage("Commanding!")
                modMessage(ring.command!!)
                sendChat(ring.command!!)
            }
            "align" -> {
                modMessage("Aligning!")
                stopVelo()
                mc.thePlayer.setPosition(floor(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, floor(mc.thePlayer.posZ) + 0.5)
            }
            "useitem" -> {
                modMessage("Iteming!")
                swapFromName(ring.item!!)
                set(ring.yaw, ring.pitch)
                scheduleTask(1) {
                    airClick()
                    resetRotations()
                }
            }
        }
    }
}
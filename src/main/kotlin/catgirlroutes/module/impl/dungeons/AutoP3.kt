package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.Ring
import catgirlroutes.commands.impl.RingManager.loadRings
import catgirlroutes.commands.impl.RingManager.rings
import catgirlroutes.commands.impl.ringEditMode
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.dungeons.Blink.packetArray
import catgirlroutes.module.impl.dungeons.LavaClip.lavaClipToggle
import catgirlroutes.module.impl.player.HClip.hClip
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.*
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.MovementUtils.edge
import catgirlroutes.utils.MovementUtils.jump
import catgirlroutes.utils.MovementUtils.setKey
import catgirlroutes.utils.MovementUtils.stopMovement
import catgirlroutes.utils.MovementUtils.stopVelo
import catgirlroutes.utils.PlayerUtils.leftClick
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.Utils.renderText
import catgirlroutes.utils.dungeon.DungeonUtils.floorNumber
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.termGuiTitles
import catgirlroutes.utils.render.WorldRenderUtils
import catgirlroutes.utils.render.WorldRenderUtils.drawP3boxWithLayers
import catgirlroutes.utils.render.WorldRenderUtils.renderGayFlag
import catgirlroutes.utils.render.WorldRenderUtils.renderTransFlag
import catgirlroutes.utils.rotation.FakeRotater.rotate
import catgirlroutes.utils.rotation.RotationUtils.getYawAndPitch
import catgirlroutes.utils.rotation.RotationUtils.snapTo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.awt.Color.WHITE
import java.awt.Color.black
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.floor


object AutoP3 : Module(
    "Auto P3",
    category = Category.DUNGEON,
    description = "A module that allows you to place down rings that execute various actions."
){
    val selectedRoute = StringSetting("Selected route", "1", description = "Name of the selected route for auto p3.")
    private val inBossOnly = BooleanSetting("Boss only", true)
    private val editTitle = BooleanSetting("EditMode title", false)
    private val boomType = StringSelectorSetting("Boom type","Regular", arrayListOf("Regular", "Infinity"), "Superboom TNT type to use for BOOM ring")
    private val style = StringSelectorSetting("Ring style","Trans", arrayListOf("Trans", "Normal", "LGBTQIA+"), "Ring render style to be used.")
    private val layers = NumberSetting("Ring layers amount", 3.0, 3.0, 5.0, 1.0, "Amount of ring layers to render").withDependency { style.selected == "Normal" }
    private val colour = ColorSetting("Ring colour", black, false, "Colour of Normal ring style").withDependency { style.selected == "Normal" }

    init {
        this.addSettings(
            selectedRoute,
            inBossOnly,
            editTitle,
            boomType,
            style,
            layers,
            colour
        )
    }

    var termFound = false
    var termListener = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        loadRings()
    }

    val cooldownMap = mutableMapOf<String, Boolean>()

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (ringEditMode || (inBossOnly.enabled && floorNumber != 7 && !inBoss)) return
        rings.forEach { ring ->
            val key = "${ring.location.xCoord},${ring.location.yCoord},${ring.location.zCoord},${ring.type}"
            val cooldown: Boolean = cooldownMap[key] == true
            if(inRing(ring)) {
                if (ring.arguments!!.contains("term") && !termFound) {
                    termListener = true
                    return
                } else  scheduleTask (1) { termFound = false }
                if (cooldown) return@forEach
                cooldownMap[key] = true
                GlobalScope.launch {
                    executeAction(ring)
                }
            } else if (cooldown) {
                cooldownMap[key] = false
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (inBossOnly.enabled && floorNumber != 7 && !inBoss) return
        rings.forEach { ring ->
            val x: Double = ring.location.xCoord
            val y: Double = ring.location.yCoord
            val z: Double = ring.location.zCoord

            val cooldown: Boolean = cooldownMap["$x,$y,$z,${ring.type}"] == true
            val color = if (cooldown) WHITE else colour.value

            when(style.selected) {
                "Trans"     -> renderTransFlag(x, y, z, ring.width, ring.height)
                "Normal"    -> drawP3boxWithLayers(x, y, z, ring.width, ring.height, color, layers.value.toInt())
                "LGBTQIA+"  -> renderGayFlag(x, y, z, ring.width, ring.height)
            }
            if (ring.type == "blink" && ring.packets.size != 0) {
                for (i in 0 until ring.packets.size - 1) {
                    val p1 = ring.packets[i]
                    val p2 = ring.packets[i + 1]
                    WorldRenderUtils.drawLine(
                        p1.x, p1.y + 0.1, p1.z,
                        p2.x, p2.y + 0.1, p2.z,
                        Color.PINK, 4.0f, false
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (!editTitle.enabled) return
        if (!ringEditMode || (inBossOnly.enabled && floorNumber != 7)) return
        val sr = ScaledResolution(mc)
        val t = "Edit Mode"
        renderText(t, sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(t) / 2, sr.scaledHeight / 2 + mc.fontRendererObj.FONT_HEIGHT)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) // idk if you actually need this. but jic for inv walk
    fun onTerm(event: PacketReceiveEvent) {
        if (!termListener || (inBossOnly.enabled && floorNumber != 7 && !inBoss)) return
        if (event.packet !is S2DPacketOpenWindow) return
        val windowTitle = event.packet.windowTitle
        if (windowTitle != null && termGuiTitles.any { windowTitle.unformattedText.startsWith(it) }) {
            modMessage("Term found")
            termFound = true
            termListener = false
        }
    }

    fun inRing(ring: Ring): Boolean {
        val viewerPos = mc.renderManager
        val distanceX = abs(viewerPos.viewerPosX - ring.location.xCoord)
        val distanceY = abs(viewerPos.viewerPosY - ring.location.yCoord)
        val distanceZ = abs(viewerPos.viewerPosZ - ring.location.zCoord)

        return distanceX < (ring.width / 2) &&
               distanceY < ring.height &&
               distanceY >= -0.5 &&
               distanceZ < (ring.width / 2);
    }

    private suspend fun executeAction(ring: Ring) {
        val actionDelay: Int = if (ring.delay == null) 0 else ring.delay!!
        delay(actionDelay.toLong())
        ring.arguments?.let {
            if ("stop" in it) stopVelo()
            if ("walk" in it) setKey("w", true)
            if ("look" in it) snapTo(ring.yaw, ring.pitch)
        }
        when(ring.type) {
            "walk" -> {
                modMessage("Walking!")
                setKey("w", true)
            }
            "jump" -> {
                modMessage("Jumping!")
                jump()
            }
            "stop" -> {
                modMessage("Stopping!")
                stopMovement()
                stopVelo()
            }
            "boom" -> {
                modMessage("Bomb denmark!")
                if (boomType.selected == "Regular") swapFromName("superboom tnt") else swapFromName("infinityboom tnt")
                //modMessage(boomType.selected)
                scheduleTask(0) {leftClick()}
            }
            "hclip" -> {
                modMessage("Hclipping!")
                hClip(ring.yaw)
            }
            "vclip" -> {
                modMessage("Vclipping!")
                lavaClipToggle(ring.depth!!.toDouble(), true)
            }
            "bonzo" -> {
                modMessage("Bonzoing!")
                swapFromName("bonzo's staff")
                scheduleTask(0) {
                    rotate(ring.yaw, ring.pitch)
                }
            }
            "look" -> {
                modMessage("Looking!")
                snapTo(ring.yaw, ring.pitch)
            }
            "align" -> {
                modMessage("Aligning!")
                mc.thePlayer.setPosition(floor(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, floor(mc.thePlayer.posZ) + 0.5)
            }
            "block" -> {
                modMessage("Snaping to [${ring.lookBlock!!.xCoord}, ${ring.lookBlock!!.yCoord}, ${ring.lookBlock!!.zCoord}]! ")
                val(yaw, pitch) = getYawAndPitch(ring.lookBlock!!.xCoord, ring.lookBlock!!.yCoord, ring.lookBlock!!.zCoord)
                snapTo(yaw, pitch)
            }
            "edge" -> {
                modMessage("Edging!")
                edge()
            }
            "command" -> {
                modMessage("Sexecuting!")
                commandAny(ring.command!!)
            }
            "blink" -> {
                if (ring.packets.size == 0) return
                if (packetArray.size > ring.packets.size) {
                    ring.packets.forEach{ packet ->
                        mc.netHandler.networkManager.sendPacket(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, packet.onGround))
                    }
                    mc.thePlayer.setPosition(ring.packets.last().x, ring.packets.last().y, ring.packets.last().z)
                }
            }
        }
    }
}
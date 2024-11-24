package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.Ring
import catgirlroutes.commands.impl.RingManager.loadRings
import catgirlroutes.commands.impl.RingManager.rings
import catgirlroutes.commands.impl.ringEditMode
import catgirlroutes.commands.impl.ringsActive
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.dungeons.LavaClip.lavaClipToggle
import catgirlroutes.module.impl.player.HClip.hClip
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.MovementUtils.edge
import catgirlroutes.utils.MovementUtils.jump
import catgirlroutes.utils.MovementUtils.setKey
import catgirlroutes.utils.MovementUtils.stopMovement
import catgirlroutes.utils.MovementUtils.stopVelo
import catgirlroutes.utils.rotation.ServerRotateUtils.resetRotations
import catgirlroutes.utils.rotation.ServerRotateUtils.set
import catgirlroutes.utils.Utils.airClick
import catgirlroutes.utils.rotation.RotationUtils.getYawAndPitch
import catgirlroutes.utils.Utils.leftClick
import catgirlroutes.utils.Utils.renderText
import catgirlroutes.utils.Utils.swapFromName
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.render.WorldRenderUtils.drawP3box
import catgirlroutes.utils.render.WorldRenderUtils.renderGayFlag
import catgirlroutes.utils.render.WorldRenderUtils.renderTransFlag
import catgirlroutes.utils.rotation.RotationUtils.snapTo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color.WHITE
import java.awt.Color.black
import kotlin.math.abs
import kotlin.math.floor


object AutoP3 : Module(
    "Auto P3",
    category = Category.DUNGEON,
    description = "A module that allows you to place down rings that execute various actions."
){
    val selectedRoute = StringSetting("Selected route", "1", description = "Name of the selected route for auto p3.")
    private val preset = StringSelectorSetting("Ring style","Trans", arrayListOf("Trans", "Normal", "LGBTQIA+"), description = "Ring render style to be used.")
    private val colour = ColorSetting("Ring colour", black, false, "Colour of Normal ring style").withDependency { preset.selected == "Normal" }

    init {
        this.addSettings(
            selectedRoute,
            preset,
            colour
        )
    }

    private var termFound = false
    private var termListener = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        loadRings()
    }

    private val cooldownMap = mutableMapOf<String, Boolean>()

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!ringsActive || !this.enabled || ringEditMode) return
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
        if (!ringsActive) return
        rings.forEach { ring ->
            val x: Double = ring.location.xCoord
            val y: Double = ring.location.yCoord
            val z: Double = ring.location.zCoord

            val cooldown: Boolean = cooldownMap["$x,$y,$z,${ring.type}"] == true
            val color = if (cooldown) WHITE else colour.value

            when(preset.selected) {
                "Trans" -> renderTransFlag(x, y, z, ring.width, ring.height)
                "Normal" -> drawP3box(x - ring.width / 2, y, z - ring.width / 2, ring.width.toDouble(), ring.height.toDouble(), ring.width.toDouble(), color, 4F, false)
                "LGBTQIA+" -> renderGayFlag(x, y, z, ring.width, ring.height)
            }
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (ringsActive && ringEditMode) {
            val sr = ScaledResolution(mc)
            val t = "Edit Mode"
            renderText(t, sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(t) / 2, sr.scaledHeight / 2 + mc.fontRendererObj.FONT_HEIGHT)
        }
    }

    @SubscribeEvent
    fun onTerm(event: ReceivePacketEvent) {
        if (!termListener) return
        if (event.packet !is S2DPacketOpenWindow) return
        if (event.packet.windowTitle?.unformattedText in DungeonUtils.termGuiTitles) {
            modMessage("Term found")
            termFound = true
            termListener = false
        }
    }

    private fun inRing(ring: Ring): Boolean {
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
                if (!swapFromName("infinityboom tnt")) swapFromName("superboom tnt")
                scheduleTask(1) {leftClick()}
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
        }
    }
}
package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.Ring
import catgirlroutes.commands.impl.RingManager
import catgirlroutes.commands.impl.RingManager.saveRings
import catgirlroutes.commands.impl.ringEditMode
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object Blink : Module(
    "Blink",
    category = Category.DUNGEON
) {

    val packetArray = mutableListOf<Long>()
    private var recorderActive = false
    private var currentRing: Ring? = null

    override fun onKeyBind() {
        if (!ringEditMode) return
        RingManager.rings.forEach { ring ->
            if (AutoP3.inRing(ring) && ring.type == "blink") {
                modMessage("Started recording")
                mc.thePlayer.setPosition(floor(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, floor(mc.thePlayer.posZ) + 0.5)
                recorderActive = true
                currentRing = ring
                currentRing!!.packets = mutableListOf<BlinkC06>()
            }
        }
    }

    @SubscribeEvent
    fun onPacketRecorder(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !recorderActive) return
        if (currentRing!!.packets.size == 28) {
            recorderActive = false
            saveRings()
            modMessage("Done recording")
        }
        if (event.packet is C06PacketPlayerPosLook || event.packet is C04PacketPlayerPosition) {
            currentRing!!.packets.add(BlinkC06(event.packet.yaw, event.packet.pitch, event.packet.positionX, event.packet.positionY, event.packet.positionZ, event.packet.isOnGround))
        }
    }

    data class BlinkC06(
        val yaw: Float,
        val pitch: Float,
        val x: Double,
        val y: Double,
        val z: Double,
        val onGround: Boolean = mc.thePlayer.onGround
    )

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        packetArray.clear()
    }

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        val currentTime = System.currentTimeMillis()
        if (event.packet is C05PacketPlayerLook) {
            packetArray.add(currentTime)
            //modMessage("Added")
            event.isCanceled = true
        } else if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && mc.thePlayer.isCollidedVertically) {
            packetArray.add(currentTime)
            //modMessage("Added")
            event.isCanceled = true
        } else {
            if (packetArray.isNotEmpty()) {
                packetArray.removeFirst()
                //modMessage("Removed (walking)")
            }
        }
        //val originalSize = packetArray.size
        packetArray.removeIf { currentTime - it > 20000 }

        /*
        if (packetArray.size < originalSize) {
            modMessage("Removed (timeout)")
        }
        */
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || mc.ingameGUI == null) return

        val text = packetArray.size.toString()

        val sr = ScaledResolution(mc)
        val x = sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(text) / 2
        val y = sr.scaledHeight / 2 + -20
        Utils.renderText(text, x, y)
    }
}
package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.Ring
import catgirlroutes.commands.impl.RingManager
import catgirlroutes.commands.impl.RingManager.saveRings
import catgirlroutes.commands.impl.blinkEditMode
import catgirlroutes.commands.impl.ringEditMode
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.AlwaysActive
import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.math.floor

@AlwaysActive
object Blink : Module(
    "Blink",
    category = Category.DUNGEON
) {

    private val recordLength = NumberSetting("Recording length", 28.0, 1.0, 50.0)
    private val recordBind: KeyBindSetting = KeyBindSetting("Blink record", Keyboard.KEY_NONE, "Starts recording a blink if you are on a blink ring and in editmode")
        .onPress {
            if (recorderActive) {
                recorderActive = false
                modMessage("Done recording")
                return@onPress
            }
            if (!ringEditMode && !blinkEditMode) return@onPress
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

    init {
        this.addSettings(this.recordBind, recordLength)
    }

    val packetArray = mutableListOf<Long>()
    private var recorderActive = false
    private var currentRing: Ring? = null

    @SubscribeEvent
    fun onPacketRecorder(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !recorderActive) return
        if (currentRing!!.packets.size == recordLength.value.toInt()) {
            recorderActive = false
            saveRings()
            modMessage("Done recording")
        }
        if (event.packet is C06PacketPlayerPosLook || event.packet is C04PacketPlayerPosition) {
            if (currentRing!!.packets.isNotEmpty() && currentRing!!.packets.last() == BlinkC06(event.packet.yaw, event.packet.pitch, event.packet.positionX, event.packet.positionY, event.packet.positionZ, event.packet.isOnGround)) return
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

    var lastX = 0.0
    var lastY = 0.0
    var lastZ = 0.0

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        val currentTime = System.currentTimeMillis()
        if ((event.packet is C04PacketPlayerPosition || event.packet is C06PacketPlayerPosLook)) {
            if (packetArray.isNotEmpty()) {
                packetArray.removeFirst()
            }
        } else if (this.enabled) {
            packetArray.add(currentTime)
            event.isCanceled = true
        } else {
            if (packetArray.isNotEmpty()) {
                packetArray.removeFirst()
            }
        }

        packetArray.removeIf { currentTime - it > 20000 }
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || mc.ingameGUI == null || packetArray.size == 0) return

        val text = packetArray.size.toString()

        val sr = ScaledResolution(mc)
        val x = sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(text) / 2
        val y = sr.scaledHeight / 2 + -20
        Utils.renderText(text, x + 1, y)
    }
}
package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.PlayerUtils.findDistanceToAirBlocks
import catgirlroutes.utils.PlayerUtils.relativeClip
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.rotation.FakeRotater.rotate
import catgirlroutes.utils.rotation.Rotater.Companion.shouldClick
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.round

object PearlClip : Module(
    "Pearl Clip",
    category = Category.PLAYER,
    description = "Clips you down selected blocks using an ender pearl."
){
    private val pearlClipDistance: NumberSetting = NumberSetting("Pearl Clip distance", 20.0, 0.0, 80.0, 1.0, description = "Distance to clip down")

    init {
        this.addSettings(
            pearlClipDistance
        )
    }

    private var active = false
    override fun onKeyBind() {
        if (!this.enabled) return
        modMessage("Pearl clipping!")
        pearlClip((pearlClipDistance.value * -1))
    }

    private var clipDepth: Double? = 0.0

    fun pearlClip(depth: Double? = findDistanceToAirBlocks()) { // todo: move to ClipUtils
        clipDepth = if (depth == 0.0) findDistanceToAirBlocks() else depth; // fuck k*tlin
        if (clipDepth == null) return

        val swapResult = swapFromName("ender pearl")
        if (swapResult != "NOT_FOUND") {
            active = true
            rotate(mc.thePlayer.rotationYaw, 90F)
            scheduleTask(0) {
                shouldClick = true
            }
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !active) return
        active = false
        scheduleTask(0) {
            val x = (round(mc.thePlayer.posX))
            val z = (round(mc.thePlayer.posZ))
            relativeClip(x + 0.5, -abs(clipDepth!!), z + 0.5)
        }
    }
}
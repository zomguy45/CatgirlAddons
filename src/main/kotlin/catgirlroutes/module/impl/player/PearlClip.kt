package catgirlroutes.module.impl.player

import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.rotation.ServerRotateUtils.resetRotations
import catgirlroutes.utils.rotation.ServerRotateUtils.set
import catgirlroutes.utils.Utils.airClick
import catgirlroutes.utils.Utils.findDistanceToAirBlocks
import catgirlroutes.utils.Utils.relativeClip
import catgirlroutes.utils.Utils.swapFromName
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

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
        pearlClip((pearlClipDistance.value))
    }

    private var clipDepth: Double? = 0.0

    fun pearlClip(depth: Double? = findDistanceToAirBlocks()) {
        clipDepth = if (depth == 0.0) findDistanceToAirBlocks() else depth
        if (clipDepth == null) return

        val swapResult = swapFromName("ender pearl")
        if (swapResult) {
            active = true
            set(0F, 90F)
            scheduleTask(1) {
                airClick()
                resetRotations()
            }
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !active) return
        active = false
        scheduleTask(0) {
            relativeClip(0.0, -abs(clipDepth!!), 0.0)
        }
    }
}
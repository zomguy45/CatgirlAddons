package catgirlroutes.module.impl.misc

import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.ServerRotateUtils.resetRotations
import catgirlroutes.utils.ServerRotateUtils.set
import catgirlroutes.utils.Utils.airClick
import catgirlroutes.utils.Utils.findDistanceToAirBlocks
import catgirlroutes.utils.Utils.relativeClip
import catgirlroutes.utils.Utils.swapFromName
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PearlClip : Module(
    "Pearl Clip",
    category = Category.MISC,
    description = "Clips you down selected blocks using an ender pearl."
){
    private val pearlclipdistance: NumberSetting = NumberSetting("Pearl Clip distance", 20.0, 0.0, 80.0, 1.0, description = "Distance to clip down")
    init {
        this.addSettings(
            PearlClip.pearlclipdistance
        )
    }
    private var active = false
    override fun onKeyBind() {
        if (!this.enabled) return
        modMessage("Pearl clipping!")
        pearlClip((pearlclipdistance.value * -1))
    }

    private var clipdepth: Double? = 0.0

    fun pearlClip(depth: Double? = findDistanceToAirBlocks()) {
        clipdepth = depth
        if (depth == 0.0) clipdepth = findDistanceToAirBlocks()
        if (clipdepth == null) return
        val swapresult = swapFromName("ender pearl")
        if (!swapresult) return
        active = true
        set(0F, 90F)
        scheduleTask(1) {
            airClick()
            resetRotations()
        }
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !active) return
        active = false
        scheduleTask(0) {
            relativeClip(0.0, clipdepth!!, 0.0)
        }
    }
}
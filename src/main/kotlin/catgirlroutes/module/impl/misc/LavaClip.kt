package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.Utils.findDistanceToAirBlocks
import catgirlroutes.utils.Utils.relativeClip
import catgirlroutes.utils.Utils.renderText
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LavaClip : Module(
    "Lava Clip",
    category = Category.MISC,
    description = "Clips you x blocks down when jumping into lava."
){
    private val lavaclipDistance: NumberSetting = NumberSetting("Lava Clip distance", 15.0, 0.0, 50.0, 1.0, description = "Distance to clip down")
    init {
        this.addSettings(
            LavaClip.lavaclipDistance
        )
    }

    private var lavaclipping = false
    private var velocancelled = true

    override fun onKeyBind() {
        if (this.enabled) lavaClipToggle(lavaclipDistance.value * -1)
    }

    private var adjustedDistance: Double? = lavaclipDistance.value * -1

    fun lavaClipToggle(distance: Double = 0.0, onlyToggle: Boolean = false) {
        if (!lavaclipping || onlyToggle) {
            lavaclipping = true
            velocancelled = false
            adjustedDistance = distance
        } else {
            lavaclipping = false
            velocancelled = true
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!mc.thePlayer.isInLava || !lavaclipping) return
        lavaclipping = false
        if (adjustedDistance == 0.0) adjustedDistance = findDistanceToAirBlocks()
        if (adjustedDistance == null) {
            velocancelled = true
            return
        }
        relativeClip(0.0, adjustedDistance!!, 0.0)
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !lavaclipping || mc.ingameGUI == null) return
        val sr = ScaledResolution(mc)
        var text = "Lava clipping $adjustedDistance"
        if (adjustedDistance == 0.0) text = "Lava clipping"
        val width = sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(text) / 2
        renderText(
            text = text,
            x = width,
            y = sr.scaledHeight / 2 + 10
        )
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (velocancelled) return
        if (event.packet !is S12PacketEntityVelocity) return
        if (event.packet.entityID != mc.thePlayer.entityId) return
        if (event.packet.motionY == 28000) {
            event.isCanceled = true
            velocancelled = true
        }
    }
}
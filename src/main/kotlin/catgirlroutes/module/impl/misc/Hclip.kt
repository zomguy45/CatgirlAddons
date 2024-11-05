
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.MovementUpdateEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.MovementUtils.restartMovement
import catgirlroutes.utils.MovementUtils.stopMovement
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object Hclip : Module(
    "Hclip",
    category = Category.MISC,
    description = "Boosts you forward when keybind is pressed"
){
    private val forward = mc.gameSettings.keyBindForward.keyCode
    private var pendingHclip = false
    private var yawtouse: Float? = null

    override fun onKeyBind() {
        modMessage("Hclipping!")
        if (!this.enabled) return
        hclip()
    }

    fun hclip(yaw: Float = mc.thePlayer.rotationYaw) {
        stopMovement()
        yawtouse = yaw
        mc.thePlayer.setVelocity(0.0, mc.thePlayer.motionY, 0.0)
        pendingHclip = true
    }

    @SubscribeEvent
    fun onMovementUpdate(event: MovementUpdateEvent.Pre) {
        if (pendingHclip) {
            val speed = mc.thePlayer.capabilities.walkSpeed * 2.806
            val radians = yawtouse!! * Math.PI / 180
            val x = -sin(radians) * speed
            val z = cos(radians) * speed

            mc.thePlayer.motionX = x
            mc.thePlayer.motionZ = z
            restartMovement()

            pendingHclip = false
        }
    }
}
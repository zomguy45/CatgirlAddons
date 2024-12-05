package catgirlroutes.module.impl.player
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.MovementUpdateEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.MovementUtils.jump
import catgirlroutes.utils.MovementUtils.restartMovement
import catgirlroutes.utils.MovementUtils.stopMovement
import catgirlroutes.utils.Notifications
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object HClip : Module(
    "HClip",
    category = Category.PLAYER,
    description = "Boosts you forward when key bind is pressed"
){
    private val shouldJump = BooleanSetting("Auto jump", false, "Makes hclip automatically jump if on ground.")
    private val shouldNotify = BooleanSetting("Notifications", false, "Makes hclip send notification on activation.")

    init {
        addSettings(shouldJump, shouldNotify)
    }

    private var pendingHClip = false
    private var yawToUse: Float? = null

    override fun onKeyBind() {
        if (!this.enabled) return // todo: do something about it idk?!
        if (shouldNotify.value) Notifications.send("Hclipping", "") else modMessage("Hclipping")
        hClip()
    }

    fun hClip(yaw: Float = mc.thePlayer.rotationYaw) {
        stopMovement()
        if (mc.thePlayer.onGround && shouldJump.value) jump()
        yawToUse = yaw
        mc.thePlayer.setVelocity(0.0, mc.thePlayer.motionY, 0.0)
        pendingHClip = true
    }

    @SubscribeEvent
    fun onMovementUpdate(event: MovementUpdateEvent.Pre) {
        if (pendingHClip) {
            val speed = mc.thePlayer.capabilities.walkSpeed * 2.806
            val radians = yawToUse!! * Math.PI / 180 // todo: MathUtils?
            val x = -sin(radians) * speed
            val z = cos(radians) * speed

            mc.thePlayer.motionX = x
            mc.thePlayer.motionZ = z
            restartMovement()

            pendingHClip = false
        }
    }
}
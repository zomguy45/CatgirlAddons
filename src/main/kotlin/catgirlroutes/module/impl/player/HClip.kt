package catgirlroutes.module.impl.player
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.MovementUpdateEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.MovementUtils.jump
import catgirlroutes.utils.MovementUtils.restartMovement
import catgirlroutes.utils.MovementUtils.stopMovement
import catgirlroutes.utils.Notifications
import catgirlroutes.utils.PlayerUtils
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

    private val dingdingding: BooleanSetting = BooleanSetting("Play sound", false)

    private val soundOptions = arrayListOf(
        "note.pling",
        "mob.blaze.hit",
        "fire.ignite",
        "random.orb",
        "random.break",
        "mob.guardian.land.hit",
        "Custom"
    )
    private val soundSelector = StringSelectorSetting("Sound", soundOptions[0], soundOptions, "Sound Selection").withDependency { dingdingding.enabled }
    private val customSound: StringSetting = StringSetting("Custom Sound", soundOptions[0], placeholder = "Sound name", description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.").withDependency { dingdingding.enabled && soundSelector.selected == "Custom" }
    private val pitch: NumberSetting = NumberSetting("Pitch", 1.0, 0.1, 2.0, 0.1).withDependency { dingdingding.enabled }

    init {
        addSettings(shouldJump, shouldNotify, dingdingding, soundSelector, customSound, pitch)
    }

    private var pendingHClip = false
    private var yawToUse: Float? = null

    override fun onKeyBind() {
        if (!this.enabled) return // todo: do something about it idk?!
        if (shouldNotify.value) Notifications.send("Hclipping") else modMessage("Hclipping")
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

            if (dingdingding.enabled) PlayerUtils.playLoudSound(getSound(), 100f, pitch.value.toFloat())
            mc.thePlayer.motionX = x
            mc.thePlayer.motionZ = z
            restartMovement()

            pendingHClip = false
        }
    }
    /**
    * Returns the sound from the selector setting, or the custom sound when the last element is selected
    */
    private fun getSound(): String {
        return if (soundSelector.index < soundSelector.options.size - 1)
            soundSelector.selected
        else
            customSound.text
    }

}
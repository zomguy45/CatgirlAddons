package catgirlroutes.module.impl.dungeons

import catgirlroutes.events.impl.SecretPickupEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.PlayerUtils.playLoudSound
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Module to play a sound when a secret is collected.
 * @author Aton
 */
object SecretChime : Module( // todo: use secretevent
    "Secret Chime",
    category = Category.DUNGEON,
    description = "Plays a sound whenever you click a secret. Also plays this sound for aura clicks. \n" +
            "§4Do not use the bat death sound or your game will freeze!"
) {
    private val soundOptions = arrayListOf(
        "mob.blaze.hit",
        "fire.ignite",
        "random.orb",
        "random.break",
        "mob.guardian.land.hit",
        "Custom"
    )

    private val sound = StringSelectorSetting("Sound", "mob.blaze.hit", soundOptions, "Sound selection.")
    private val customSound = StringSetting("Custom Sound", "mob.blaze.hit", description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.")

    private val dropSound = StringSelectorSetting("Drop Sound", "mob.blaze.hit", soundOptions, "Sound selection for item pickups.")
    private val customDropSound = StringSetting("Custom Drop Sound", "mob.blaze.hit", description = "Name of a custom sound to play for item pickups. This is used when Custom is selected in the DropSound setting.")

    private val volume = NumberSetting("Volume", 1.0, 0.0, 1.0, 0.01, "Volume of the sound.")
    private val pitch = NumberSetting("Pitch", 2.0, 0.0, 2.0, 0.01, "Pitch of the sound.")

    private var lastPlayed = System.currentTimeMillis()

    /*
    List of good sound effects:

    fire.ignite - pitch 1
    mob.blaze.hit - pitch 2
    random.orb - pitch 1
    random.break - 2
    mob.guardian.land.hit - 2

     */
    init {
        this.addSettings(
            sound,
            customSound,
            dropSound,
            customDropSound,
            volume,
            pitch
        )
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent) {
        if (!inDungeons) return
        playSecretSound()
    }

    /**
     * Returns the sound from the selector setting, or the custom sound when the last element is selected
     */
    private fun getSound(isItemDrop: Boolean = false): String {
        return if(isItemDrop)
            if ( dropSound.index < sound.options.size - 1)
                dropSound.selected
            else
                customDropSound.text
        else if (sound.index < sound.options.size - 1)
            sound.selected
        else
            customSound.text
    }

    private fun playSecretSound(sound: String = getSound()) {
        if (System.currentTimeMillis() - lastPlayed > 10) {
            playLoudSound(sound, volume.value.toFloat(), pitch.value.toFloat())
            lastPlayed = System.currentTimeMillis()
        }
    }
}
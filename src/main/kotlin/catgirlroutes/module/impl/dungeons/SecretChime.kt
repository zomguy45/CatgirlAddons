package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.DungeonSecretEvent
import catgirlroutes.events.impl.EntityRemovedEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.dungeon.DungeonUtils
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
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
){

    private val sound: StringSelectorSetting
    private val customSound = StringSetting("Custom Sound", "mob.blaze.hit", description = "Name of a custom sound to play. This is used when Custom is selected in the Sound setting.")
    private val dropSound: StringSelectorSetting
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
        val soundOptions = arrayListOf(
            "mob.blaze.hit",
            "fire.ignite",
            "random.orb",
            "random.break",
            "mob.guardian.land.hit",
            "Custom"
        )
        sound = StringSelectorSetting("Sound", "mob.blaze.hit", soundOptions, "Sound selection.")
        dropSound = StringSelectorSetting("Drop Sound", "mob.blaze.hit", soundOptions, "Sound selection for item pickups.")

        this.addSettings(
            sound,
            customSound,
            dropSound,
            customDropSound,
            volume,
            pitch
        )
    }

    /**
     * Registers right clicking a secret.
     */
    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!inDungeons) return
        val blockPos = event.pos
        try { // for some reason getBlockState can throw null pointer exception
            val block = mc.theWorld?.getBlockState(blockPos)?.block ?: return

            if (block == Blocks.chest || block == Blocks.lever || // todo: replace with DungeonUtils.isSecret
                block == Blocks.trapped_chest
            ) {
                playSecretSound()
                devMessage("clicked secret!!!")
            } else if (block == Blocks.skull) {
                val tileEntity: TileEntitySkull = mc.theWorld.getTileEntity(blockPos) as TileEntitySkull
                if (tileEntity.playerProfile.id.toString() == "e0f3e929-869e-3dca-9504-54c666ee6f23") {
                    playSecretSound()
                    devMessage("clicked secret!!!")
                }
            }
        } catch (_: Exception) { }
    }

    /**
     * For bat death detection
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onSoundPlay(event: PlaySoundSourceEvent) {
        if (!inDungeons) return
        when(event.name) {
            "mob.bat.death"-> {
                playSecretSound()
                devMessage("bat ded!!!")
            }
        }
    }

    /**
     * For item pickup detection. The forge event for item pickups cant be used, because item pickups are handled server side.
     */
    @SubscribeEvent
    fun onRemoveEntity(event: EntityRemovedEvent) {
        if(!inDungeons) return
        if(event.entity !is EntityItem) return
        if(mc.thePlayer.getDistanceToEntity(event.entity) > 6) return
        // Check the item name to filter for secrets.
        if (event.entity.entityItem.displayName.run isSecret@ {
                DungeonUtils.dungeonItemDrops.any { this.contains(it) }
            }) {
            devMessage("picked up item!!!")
            playSecretSound(getSound(true))
        }
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
        MinecraftForge.EVENT_BUS.post(DungeonSecretEvent())
        if (System.currentTimeMillis() - lastPlayed > 10) {
            playLoudSound(sound, volume.value.toFloat(), pitch.value.toFloat())
            lastPlayed = System.currentTimeMillis()
        }
    }

    private var shouldBypassVolume: Boolean = false

    private fun playLoudSound(sound: String?, volume: Float, pitch: Float) {
        shouldBypassVolume = true
        mc.thePlayer?.playSound(sound, volume, pitch)
        shouldBypassVolume = false
    }
}
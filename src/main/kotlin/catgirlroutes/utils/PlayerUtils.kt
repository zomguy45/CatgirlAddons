package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.unformattedName
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import kotlin.math.sqrt

object PlayerUtils {

    inline val posX get() = mc.thePlayer.posX
    inline val posY get() = mc.thePlayer.posY
    inline val posZ get() = mc.thePlayer.posZ

    private var shouldBypassVolume: Boolean = false
    fun playLoudSound(sound: String?, volume: Float, pitch: Float) {
        shouldBypassVolume = true
        mc.thePlayer?.playSound(sound, volume, pitch)
        shouldBypassVolume = false
    }

    fun getItemSlot(item: String, ignoreCase: Boolean = true): Int? =
        mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it?.unformattedName?.contains(item, ignoreCase) == true }.takeIf { it != -1 }

    fun swapFromName(name: String): Boolean {
        for (i in 0..8) {
            val stack: ItemStack? = mc.thePlayer.inventory.getStackInSlot(i)
            val itemName = stack?.displayName
            if (itemName != null) {
                if (itemName.contains(name, ignoreCase = true)) {
                    mc.thePlayer.inventory.currentItem = i
                    return true
                }
            }
        }
        modMessage("$name not found.")
        return false
    }

    fun rightClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
    }

    fun leftClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)
    }

    fun airClick() {
        PacketUtils.sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
    }

    fun relativeClip(x: Double, y: Double, z: Double) {
        modMessage("clipping2")
        mc.thePlayer.setPosition(posX + x, posY + y,posZ + z)
    }

    fun findDistanceToAirBlocks(): Double? {
        val world = mc.theWorld

        if (mc.thePlayer == null || world == null) return null

        val startPos = BlockPos(posX, posY, posZ)
        var airCount = 0

        for ((distance, y) in (startPos.y downTo 0).withIndex()) {
            val pos = BlockPos(startPos.x, y, startPos.z)
            val block = world.getBlockState(pos).block

            if (block is BlockAir) {
                airCount++
                if (airCount == 2) return (distance * -1).toDouble() - 1.0
            } else {
                airCount = 0
            }
        }
        return null
    }


}
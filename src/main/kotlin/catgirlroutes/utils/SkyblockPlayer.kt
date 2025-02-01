package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// taken from kp which is inspired by odon clint
object SkyblockPlayer { // todo account for absorption
    val health: Int
        get() = if (mc.thePlayer != null) (this.maxHealth * mc.thePlayer.health / mc.thePlayer.maxHealth).toInt() else 0
    var maxHealth = 0

    var defence = 0

    var mana = 0
    var maxMana = 0

    var overflowMana: Int = 0
    var stacks: String = ""
    var salvation: Int = 0

    var effectiveHealth: Int = 0
    val speed: Int
        get() = if (mc.thePlayer != null) (mc.thePlayer.capabilities.walkSpeed * 1000).toInt() else 0

    val HP_REGEX = "([\\d,]+)/([\\d,]+)❤".toRegex()
    val DEF_REGEX = "([\\d|,]+)❈ Defense".toRegex()
    val MANA_REGEX = "([\\d,]+)/([\\d,]+)✎( Mana)?".toRegex()

    val OVERFLOW_REGEX = "([\\d,]+)ʬ".toRegex()
    val STACKS_REGEX = "[0-9]+([ᝐ⁑Ѫ])".toRegex()
    val SALVATION_REGEX = "T([1-3])!".toRegex()

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: PacketReceiveEvent) {
        if (event.packet !is S02PacketChat || event.packet.type.toInt() != 2) return
        val message = StringUtils.stripControlCodes(event.packet.chatComponent.unformattedText).replace(",", "")

        val hpMatch = HP_REGEX.find(message)
        if (hpMatch != null) {
            val groups = hpMatch.groupValues
            this.maxHealth = groups[2].toInt()
            this.effectiveHealth = (this.maxHealth * (1 + this.defence / 100))
        }

        val defMatch = DEF_REGEX.find(message)
        if (defMatch != null) {
            val groups = defMatch.groupValues
            this.defence = groups[1].toInt()
        }

        val manaMatch = MANA_REGEX.find(message)
        if (manaMatch != null) {
            val groups = manaMatch.groupValues
            this.mana = groups[1].toInt()
            this.maxMana = groups[2].toInt()
        }

        val overflowMatch = OVERFLOW_REGEX.find(message)
        if (overflowMatch != null) {
            val groups = overflowMatch.groupValues
            this.overflowMana = groups[2].toInt()
        }

        val stacksMatch = STACKS_REGEX.find(message)
        if (stacksMatch != null) {
            val groups = stacksMatch.groupValues
            this.stacks = groups[1]
        }

        val salvationMatch = SALVATION_REGEX.find(message)
        if (salvationMatch != null) {
            val groups = salvationMatch.groupValues
            this.salvation = groups[1].toInt()
        }

    }
}
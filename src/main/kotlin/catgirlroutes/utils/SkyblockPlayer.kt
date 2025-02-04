package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import jdk.nashorn.internal.ir.annotations.Ignore
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// taken from kp which is inspired by odon clint
object SkyblockPlayer { // todo account for absorption
    val health: Int
        get() = if (mc.thePlayer != null) (this.maxHealth * mc.thePlayer.health / mc.thePlayer.maxHealth).toInt() else 0
    var maxHealth: Int = 0

    var defence: Int = 0

    var mana: Int = 0
    var maxMana: Int = 0

    var overflowMana: Int = 0
    var stacks: String = ""
    var salvation: Int = 0

    var effectiveHealth: Int = 0
    val speed: Int
        get() = if (mc.thePlayer != null) (mc.thePlayer.capabilities.walkSpeed * 1000).toInt() else 0

    var manaUsage: String = ""

    var currentSecrets: Int = -1
    var maxSecrets: Int = -1

    val HP_REGEX = "([\\d,]+)/([\\d,]+)❤".toRegex()
    val DEF_REGEX = "([\\d|,]+)❈ Defense".toRegex()
    val MANA_REGEX = "([\\d,]+)/([\\d,]+)✎( Mana)?".toRegex()

    val OVERFLOW_REGEX = "([\\d,]+)ʬ".toRegex()
    val STACKS_REGEX = "[0-9]+([ᝐ⁑Ѫ])".toRegex()
    val SALVATION_REGEX = "T([1-3])!".toRegex()

    val MANA_USAGE_REGEX = "-\\d+ Mana (.+)".toRegex()
    val SECRETS_REGEX = "(\\d+)/(\\d+) Secrets".toRegex()

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: PacketReceiveEvent) {
        if (event.packet !is S02PacketChat || event.packet.type.toInt() != 2) return
        val message = StringUtils.stripControlCodes(event.packet.chatComponent.unformattedText).replace(",", "")

        try { // temp
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

            val manaUsageMatch = MANA_USAGE_REGEX.find(message)
            if (manaUsageMatch != null) {
                val groups = manaUsageMatch.groupValues
                this.manaUsage = groups[1]
            }

            val secretsMatch = SECRETS_REGEX.find(message)
            if (secretsMatch != null) {
                val groups = secretsMatch.groupValues
                this.currentSecrets = groups[1].toInt()
                this.maxSecrets = groups[2].toInt()
            } else {
                this.currentSecrets = -1
                this.maxSecrets = -1
            }
        } catch (e: IndexOutOfBoundsException) {}
    }
}
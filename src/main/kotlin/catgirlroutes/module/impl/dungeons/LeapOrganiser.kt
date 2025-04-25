package catgirlroutes.module.impl.dungeons

import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.OrderSetting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.Party

object LeapOrganiser : Module(
    "Leap Organiser",
    Category.DUNGEON,
) {
    val leapOrder by OrderSetting("Leap order", mapOf("1" to "None", "2" to "None", "3" to "None", "4" to "None")) {
        values = Party.members
    }

    private val updateParty by ActionSetting("Apply") {
        val order = leapOrder.values.joinToString(" ") { it.ifBlank { "_" } }
        when (leapMenu.selected) {
            "SA" -> commandAny("/sa leap $order")
            "Odin" -> commandAny("/od leaporder $order")
        }
    }

    private val leapMenu by SelectorSetting("Leap menu", "SA", arrayListOf("SA", "Odin"))

    override fun onEnable() {
        toggle()
        super.onEnable()
    }
}
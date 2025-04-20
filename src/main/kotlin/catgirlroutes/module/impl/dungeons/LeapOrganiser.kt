package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Visibility
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.ui.misc.LeapOrganiser

object LeapOrganiser : Module(
    "Leap Organiser",
    Category.DUNGEON,
    tag = TagType.WHIP
) {
    var leapOrder by StringSetting("Leap order", "_ _ _ _", visibility = Visibility.HIDDEN)
    val player1Note by StringSetting("Player1 note", "ee2", description = "Player 1 note")
    val player2Note by StringSetting("Player2 note", "core", description = "Player 2 note")
    val player3Note by StringSetting("Player3 note", "ee3", description = "Player 3 note")
    val player4Note by StringSetting("Player4 note", "i4", description = "Player 4 note")
    val leapMenu by SelectorSetting("Leap menu", "SA", arrayListOf("SA", "Odin"))

    override fun onKeyBind() {
        this.toggle()
    }

    override fun onEnable() {
        display = LeapOrganiser()
        toggle()
        super.onEnable()
    }
}
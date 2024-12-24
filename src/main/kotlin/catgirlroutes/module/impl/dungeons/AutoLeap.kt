package catgirlroutes.module.impl.dungeons

import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.utils.dungeon.LeapUtils.leap

//TODO: Change the lines that are being checked to the right ones.
//      Add a way to select the player names.
//      Make the triggers for the auto leap.
//      Add S2D cancel.
//      Add fail safes.

object AutoLeap : Module(
    name = "Auto Leap",
    category = Category.DUNGEON,
    tag = TagType.WHIP
) {
    private val target = StringSetting("target", "", description = "Target for leap!")

    init {
        this.addSettings(
            target
        )
    }
    override fun onKeyBind() {
        leap(target.value)
    }
}
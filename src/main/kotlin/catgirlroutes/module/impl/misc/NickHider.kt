package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.StringSetting

object NickHider: Module(
    "Nick hider",
    Category.MISC
){
    private var nameInput = StringSetting("Name", "meow")

    init {
        addSettings(nameInput)
    }

    @JvmStatic
    fun replaceText(text: String?): String? {
        if (text == null || mc.thePlayer == null || !this.enabled) return text
        val replacement = nameInput.value
        return text.replace(Regex(mc.thePlayer.name), replacement)
    }
}
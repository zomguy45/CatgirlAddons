package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.clickGUI
import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.commands.commodore
import catgirlroutes.utils.ChatUtils

val catgirlAddonsCommands = commodore("catgirladdons", "cataddons", "cga") {
    runs {
        display = clickGUI
    }
}
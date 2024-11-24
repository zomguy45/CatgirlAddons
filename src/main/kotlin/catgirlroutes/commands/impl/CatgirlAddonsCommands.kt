package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.clickGUI
import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.commands.commodore
import catgirlroutes.utils.ChatUtils.modMessage

val catgirlAddonsCommands = commodore("catgirladdons", "cataddons", "cga") {
    runs {
        display = clickGUI
    }

    literal("help").run { // todo: add description
        modMessage("""
            List of commands:
              §7/cga
              §7/pearlclip §5[§ddepth§5]
              §7/lavaclip §5[§ddepth§5]
              §7/node
              §7/p3
              §7/dev
        """.trimIndent())
    }
}
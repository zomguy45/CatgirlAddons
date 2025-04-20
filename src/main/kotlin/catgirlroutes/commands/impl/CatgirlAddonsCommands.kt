package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.commands.commodore
import catgirlroutes.module.ModuleManager
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.ui.misc.searchoverlay.AuctionOverlay
import catgirlroutes.ui.misc.searchoverlay.BazaarOverlay
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.LocationManager.inSkyblock
import catgirlroutes.utils.Notifications
import com.github.stivais.commodore.utils.GreedyString

val catgirlAddonsCommands = commodore("catgirladdons", "cataddons", "cga") {
    runs {
        ClickGui.onEnable()
    }

    literal("help").runs { // todo: add description
        modMessage("""
            List of commands:
              §7/cga
              §7/pearlclip §5[§ddepth§5]
              §7/lavaclip §5[§ddepth§5]
              §7/blockclip §5[§ddistance§5]
              §7/node
              §7/p3
              §7/dev
              §7/cgaaura
              §7/cgaac
              §7/cga ah
              §7/cga bz
              §7/cga bz
        """.trimIndent())
    }

    literal("ah").runs {
        if (inSkyblock) {
            display = AuctionOverlay()
        } else modMessage("You're not in skyblock")
    }

    literal("bz").runs {
        if (inSkyblock) {
            display = BazaarOverlay()
        } else modMessage("You're not in skyblock")
    }

    literal("toggle").runs { moduleName: GreedyString ->
        val module = ModuleManager.getModuleByName(moduleName.toString()) ?: return@runs
        module.toggle()
        if (ClickGui.notifications) Notifications.send("${if (module.enabled) "Enabled" else "Disabled"} ${module.name}", "", icon = if (module.enabled) "check.png" else "x.png")
        else modMessage("${module.name} ${if (module.enabled) "§aenabled" else "§cdisabled"}.")
    }
}
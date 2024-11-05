package catgirlroutes.commands

import catgirlroutes.CatgirlRoutes.Companion.clickGUI
import catgirlroutes.CatgirlRoutes.Companion.display
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class CatgirlAddonsCommands : CommandBase() {
    override fun getCommandName(): String {
        return "catgirladdons"
    }

    override fun getCommandAliases(): List<String> {
        return listOf(
            "cataddons",
            "cga",
        )
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        display = clickGUI
        return
    }
}
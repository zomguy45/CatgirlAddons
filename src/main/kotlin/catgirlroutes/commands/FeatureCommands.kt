package catgirlroutes.commands

import catgirlroutes.module.impl.dungeons.LavaClip.lavaClipToggle
import catgirlroutes.module.impl.misc.PearlClip.pearlClip
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class PearlClipCommand : CommandBase() {
    override fun getCommandName(): String = "pearlclip"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/pearlclip [depth]"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            pearlClip()
        } else {
            pearlClip(args[0].toDouble())
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

class LavaClipCommand : CommandBase() {
    override fun getCommandName(): String = "lavaclip"

    override fun getCommandUsage(sender: ICommandSender?): String =
        "/lavaclip [depth]"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) return lavaClipToggle(0.0)
        lavaClipToggle(args[0].toDouble())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}


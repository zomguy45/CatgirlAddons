package catgirlroutes.commands

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.dungeon.DungeonUtils.currentFullRoom
import catgirlroutes.utils.dungeon.DungeonUtils.getRealYaw
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeYaw
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import me.odinmain.utils.toVec3
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class DevCommands : CommandBase() {
    override fun getCommandName(): String {
        return "dev"
    }

    override fun getCommandAliases(): List<String> {
        return listOf()
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender?, args: Array<String>) {
        if (args.isEmpty()) {
            modMessage("No argument specified!")
            return
        }

        when(args[0]) {
            "relativecoords" -> {
                val block = mc.objectMouseOver?.blockPos ?: return
                ChatUtils.chatMessage("""
                    ---------
                    Middle: $block
                    Relative Coords: ${DungeonUtils.currentRoom?.getRelativeCoords(block.toVec3())?.toString()}
                    --------
                """.trimIndent())
            }
            "relativeyaw" -> {
                val room = currentFullRoom ?: return
                ChatUtils.chatMessage("""
                    ---------
                    Player Yaw: ${mc.thePlayer.rotationYaw}
                    Relative Yaw: ${room.getRelativeYaw(mc.thePlayer.rotationYaw)}
                    --------
                """.trimIndent())
            }
            "realyaw" -> {
                val room = currentFullRoom ?: return
                ChatUtils.chatMessage("""
                    ---------
                    Player Yaw: ${mc.thePlayer.rotationYaw}
                    Relative Yaw: ${room.getRealYaw(mc.thePlayer.rotationYaw)}
                    --------
                """.trimIndent())
            }
        }
    }
}
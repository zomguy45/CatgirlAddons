package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.commodore
import catgirlroutes.module.impl.dungeons.puzzlesolvers.IceFillSolver
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.EntityAura.entityArray
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.dungeon.DungeonUtils.currentRoom
import catgirlroutes.utils.dungeon.DungeonUtils.getRealYaw
import catgirlroutes.utils.dungeon.DungeonUtils.getRelativeYaw
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getRelativeCoords
import me.odinmain.utils.toVec3
import net.minecraft.entity.passive.EntityVillager


val devCommands = commodore("dev") {

    literal("help").run { // todo: add description
        modMessage("""
            List of commands:
              §7/relativecoords
              §7/relativeyaw
              §7/realyaw
        """.trimIndent())
    }

    literal("relativecoords").runs {
        val block = mc.objectMouseOver?.blockPos ?: return@runs
        ChatUtils.chatMessage(
            """
            ---------
            Middle: $block
            Relative Coords: ${DungeonUtils.currentRoom?.getRelativeCoords(block.toVec3())?.toString()}
            --------
            """.trimIndent())
    }

    literal("relativeyaw").runs {
        val room = currentRoom ?: return@runs
        ChatUtils.chatMessage(
            """
            ---------
            Player Yaw: ${mc.thePlayer.rotationYaw}
            Relative Yaw: ${room.getRelativeYaw(mc.thePlayer.rotationYaw)}
            --------
            """.trimIndent())
    }

    literal("realyaw").runs {
        val room = currentRoom ?: return@runs
        ChatUtils.chatMessage(
            """
            ---------
            Player Yaw: ${mc.thePlayer.rotationYaw}
            Relative Yaw: ${room.getRealYaw(mc.thePlayer.rotationYaw)}
            --------
            """.trimIndent())
    }

    literal("icefill").runs {
        ChatUtils.chatMessage(
            """
            ---------
            IceFill solution: ${IceFillSolver.currentPatterns}
            --------
            """.trimIndent())
    }
    literal("swaptest").runs {
        swapFromName("orange")
        swapFromName("magenta")
        swapFromName("light blue")
        swapFromName("yellow")
        swapFromName("lime")
        scheduleTask(0) {
            swapFromName("pink")
            swapFromName("gray")
            swapFromName("light gray")
            swapFromName("cyan")
        }
    }
    literal("entityaura").runs {
        mc.theWorld.loadedEntityList.forEach{entity ->
            if (entity is EntityVillager) entityArray.add(entity)
        }
    }
}
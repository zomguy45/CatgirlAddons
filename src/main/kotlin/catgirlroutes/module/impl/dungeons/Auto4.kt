package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.PlayerUtils.isHolding
import catgirlroutes.utils.Utils.removeFormatting
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.utils.skyblock.lore
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Auto4: Module(
    "Auto 4",
    category = Category.DUNGEON,
    description = "Automatically does fourth dev."
){

    data class BowStats (
        var isTerm: Boolean,
        var cooldown: Int
    )

    private val devBlocks = listOf(
        BlockPos(64.0, 126.0, 50.0),
        BlockPos(66.0, 126.0, 50.0),
        BlockPos(68.0, 126.0, 50.0),
        BlockPos(64.0, 128.0, 50.0),
        BlockPos(66.0, 128.0, 50.0),
        BlockPos(68.0, 128.0, 50.0),
        BlockPos(64.0, 130.0, 50.0),
        BlockPos(66.0, 130.0, 50.0),
        BlockPos(68.0, 130.0, 50.0)
    )

    fun onDev(): Boolean {
        return mc.thePlayer.posX in 62.0..65.0 && mc.thePlayer.posY == 127.0 && mc.thePlayer.posZ in 34.0..37.0
    }

    fun getBow(): BowStats {
        val isTerm = isHolding("TERMINATOR")
        var shotSpeed = 300
        val lore = mc.thePlayer.heldItem.lore
        lore.forEach{line ->
            val regex = Regex("^Shot Cooldown: (d+(?:.d+)?)s\$")
            val regexMatch = regex.find(removeFormatting(line))
            if (regexMatch != null) shotSpeed = regexMatch.value.toInt() * 1000
        }
        return BowStats(isTerm, shotSpeed)
    }

    fun getPrefire() {

    }

    fun findEmerald(): BlockPos? {
        var target: BlockPos? = null
        devBlocks.forEach{block ->
            if (mc.theWorld.getBlockState(block).block == Blocks.emerald_block) target = block
        }
        return target
    }

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent) {
        if (event.packet is S23PacketBlockChange) {
            return
        } else if (event.packet is S22PacketMultiBlockChange) {
            return
        }
    }
}
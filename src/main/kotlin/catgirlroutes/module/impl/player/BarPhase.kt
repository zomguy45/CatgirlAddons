package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.MotionUpdateEvent
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ClientListener.scheduleTask
import net.minecraft.block.BlockPane
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

object BarPhase: Module(
    "Bar phase",
    Category.PLAYER
){
    private var lastX = Double.MAX_VALUE
    private var lastZ = Double.MAX_VALUE
    private var lagbackRegister = false
    private var lagBacked = false

    @SubscribeEvent
    fun onMotion(event: MotionUpdateEvent.Post) {
        if (!mc.thePlayer.onGround) return
        val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ)
        val blockState = mc.theWorld.getBlockState(blockPos)
        if (blockState.block != Blocks.iron_bars || panesConnected(blockPos, blockState)) return

        blockState.block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)
        val aabb = blockState.block.getCollisionBoundingBox(mc.theWorld, blockPos, blockState)

        adjustPos(aabb.minX, aabb.maxX, lastX, mc.thePlayer.posX)
        adjustPos(aabb.minZ, aabb.maxZ, lastZ, mc.thePlayer.posZ)
    }

    @SubscribeEvent
    fun onS08(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !lagbackRegister) return
        lagbackRegister = false
        lagBacked = true
        scheduleTask(10) { lagBacked = false}
    }

    private fun adjustPos(min: Double, max: Double, lastValue: Double, pos: Double) {
        if (abs(min - max) > 0.3) return

        val playerWidth = mc.thePlayer.width / 2.0
        val closerToMin = abs(pos - min) < abs(pos - max)
        val boundaryAdjustment = if (closerToMin) max + playerWidth + 0.1 else min - playerWidth + 0.1
        val isStupid = closerToMin && pos - 0.3 < min || !closerToMin && pos + 0.3 > max

        if (isStupid && !lagBacked && mc.thePlayer.isCollidedHorizontally) {
            mc.thePlayer.setPosition(
                if (pos == mc.thePlayer.posX) boundaryAdjustment else mc.thePlayer.posX,
                mc.thePlayer.posY,
                if (pos == mc.thePlayer.posZ) boundaryAdjustment else mc.thePlayer.posZ
            )
            lagbackRegister = true
            scheduleTask(2) { lagbackRegister = false }
            if (pos == mc.thePlayer.posX) lastX = boundaryAdjustment else lastZ = boundaryAdjustment
        } else if (lastValue != boundaryAdjustment && lastValue != Double.MAX_VALUE) {
            if (pos == mc.thePlayer.posX) lastX = Double.MAX_VALUE else lastZ = Double.MAX_VALUE
        }
    }

    private fun panesConnected(pos: BlockPos?, state: IBlockState): Boolean {
        val block = state.block as BlockPane
        val ns = block.canPaneConnectTo(mc.theWorld, pos, EnumFacing.NORTH) ||
                block.canPaneConnectTo(mc.theWorld, pos, EnumFacing.SOUTH)
        val we = block.canPaneConnectTo(mc.theWorld, pos, EnumFacing.WEST) ||
                block.canPaneConnectTo(mc.theWorld, pos, EnumFacing.EAST)
        return ns && we || !ns && !we
    }
}
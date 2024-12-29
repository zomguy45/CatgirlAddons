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
        if (blockState.block == Blocks.iron_bars) {
            if (panesConnected(blockPos, blockState)) return
            blockState.block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)
            val aabb = blockState.block.getCollisionBoundingBox(mc.theWorld, blockPos, blockState)
            if (abs(aabb.minX - aabb.maxX) <= 0.3) {
                val playerWidth = mc.thePlayer.width / 2.0
                val closerToMin = abs(mc.thePlayer.posX - aabb.minX) < abs(mc.thePlayer.posX - aabb.maxX)
                val boundaryAdjustment = if (closerToMin) aabb.maxX + playerWidth + 0.1 else aabb.minX - playerWidth + 0.1
                if ((closerToMin && mc.thePlayer.posX - 0.3 < aabb.minX || !closerToMin && mc.thePlayer.posX + 0.3 > aabb.maxX) && !lagBacked && mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.setPosition(boundaryAdjustment, mc.thePlayer.posY, mc.thePlayer.posZ)
                    lagbackRegister = true
                    scheduleTask(2) { lagbackRegister = false }
                    lastX = boundaryAdjustment
                } else if (lastX != boundaryAdjustment && lastX != Double.MAX_VALUE) {
                    lastX = Double.MAX_VALUE
                }
            }
            if (abs(aabb.minZ - aabb.maxZ) <= 0.3) {
                val playerWidth = mc.thePlayer.width / 2.0
                val closerToMin = abs(mc.thePlayer.posZ - aabb.minZ) < abs(mc.thePlayer.posZ - aabb.maxZ)
                val boundaryAdjustment = if (closerToMin) aabb.maxZ + playerWidth + 0.1 else aabb.minZ - playerWidth + 0.1
                if ((closerToMin && mc.thePlayer.posZ - 0.3 < aabb.minZ || !closerToMin && mc.thePlayer.posZ + 0.3 > aabb.maxZ) && !lagBacked && mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, boundaryAdjustment)
                    lagbackRegister = true
                    scheduleTask(2) { lagbackRegister = false }
                    lastZ = boundaryAdjustment
                } else if (lastZ != boundaryAdjustment && lastZ != Double.MAX_VALUE) {
                    lastZ = Double.MAX_VALUE
                }
            }
        }
    }

    @SubscribeEvent
    fun onS08(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !lagbackRegister) return
        lagbackRegister = false
        lagBacked = true
        scheduleTask(10) { lagBacked = false}
    }

    private fun panesConnected(var1: BlockPos?, var2: IBlockState): Boolean {
        val var3 = var2.block as BlockPane
        return if (!var3.canPaneConnectTo(
                mc.theWorld,
                var1,
                EnumFacing.NORTH
            ) && !var3.canPaneConnectTo(
                mc.theWorld,
                var1,
                EnumFacing.SOUTH
            ) || !var3.canPaneConnectTo(
                mc.theWorld,
                var1,
                EnumFacing.WEST
            ) && !var3.canPaneConnectTo(mc.theWorld, var1, EnumFacing.EAST)
        ) {
            !var3.canPaneConnectTo(mc.theWorld, var1, EnumFacing.NORTH) && !var3.canPaneConnectTo(
                mc.theWorld,
                var1,
                EnumFacing.SOUTH
            ) && !var3.canPaneConnectTo(
                mc.theWorld,
                var1,
                EnumFacing.WEST
            ) && !var3.canPaneConnectTo(mc.theWorld, var1, EnumFacing.EAST)
        } else {
            true
        }
    }
}
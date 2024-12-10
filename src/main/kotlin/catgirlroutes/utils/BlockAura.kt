package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.render.WorldRenderUtils.drawCustomSizedBoxAt
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


object BlockAura {

    val blockArray = mutableListOf<BlockPos>()
    val breakArray = mutableListOf<BlockPos>()
    private val recentClicks = mutableListOf<MovingObjectPosition>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || blockArray.isEmpty()) return
        blockArray.forEach{block ->
            if (Utils.distanceToPlayer(block.x, block.y, block.z) < 4) {
                val blockState = mc.theWorld.getBlockState(block)
                blockState.block.setBlockBoundsBasedOnState(mc.theWorld, block)
                val aabb = aabbConvert(blockState.block.getSelectedBoundingBox(mc.theWorld, block), block)
                val eyePos = mc.thePlayer.getPositionEyes(0f)
                val centerPos = Vec3(block).addVector(
                    (aabb.minX + aabb.maxX) / 2,
                    (aabb.minY + aabb.maxY) / 2,
                    (aabb.minZ + aabb.maxZ) / 2
                )
                //modMessage(block)
                val movingObjectPosition: MovingObjectPosition = BlockUtils.collisionRayTrace(
                    block,
                    aabb,
                    eyePos,
                    centerPos
                ) ?: return@forEach
                mc.netHandler.networkManager.sendPacket(
                    C08PacketPlayerBlockPlacement(
                        block,
                        movingObjectPosition.sideHit.index,
                        mc.thePlayer.heldItem,
                        movingObjectPosition.hitVec.xCoord.toFloat(),
                        movingObjectPosition.hitVec.yCoord.toFloat(),
                        movingObjectPosition.hitVec.zCoord.toFloat()
                    )
                )
                blockArray.remove(block)
                recentClicks.add(movingObjectPosition)
                scheduleTask(10) {recentClicks.remove(movingObjectPosition)}
                return
            }
        }
    }

    @SubscribeEvent
    fun onTick2(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || breakArray.isEmpty()) return
        breakArray.forEach{block ->
            if (Utils.distanceToPlayer(block.x, block.y, block.z) < 4) {
                val blockState = mc.theWorld.getBlockState(block)
                blockState.block.setBlockBoundsBasedOnState(mc.theWorld, block)
                val aabb = aabbConvert(blockState.block.getSelectedBoundingBox(mc.theWorld, block), block)
                val eyePos = mc.thePlayer.getPositionEyes(0f)
                val centerPos = Vec3(block).addVector(
                    (aabb.minX + aabb.maxX) / 2,
                    (aabb.minY + aabb.maxY) / 2,
                    (aabb.minZ + aabb.maxZ) / 2
                )
                //modMessage(block)
                val movingObjectPosition: MovingObjectPosition = BlockUtils.collisionRayTrace(
                    block,
                    aabb,
                    eyePos,
                    centerPos
                ) ?: return@forEach
                mc.netHandler.networkManager.sendPacket(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                        block,
                        movingObjectPosition.sideHit,
                    )
                )
                breakArray.remove(block)
                recentClicks.add(movingObjectPosition)
                scheduleTask(10) {recentClicks.remove(movingObjectPosition)}
                //PlayerControllerMP
                return
            }
        }
    }

    private fun aabbConvert(aabb: AxisAlignedBB, block: BlockPos): AxisAlignedBB {
        val minX: Double = aabb.minX - block.x
        val minY: Double = aabb.minY - block.y
        val minZ: Double = aabb.minZ - block.z

        val maxX: Double = aabb.maxX - block.x
        val maxY: Double = aabb.maxY - block.y
        val maxZ: Double = aabb.maxZ - block.z

        return AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        recentClicks.forEach{pos ->
            drawCustomSizedBoxAt(pos.blockPos.x + pos.hitVec.xCoord,pos.blockPos.y + pos.hitVec.yCoord,pos.blockPos.z + pos.hitVec.zCoord, 0.1, 0.1, 0.1, java.awt.Color.BLUE)
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        blockArray.clear()
    }
}
package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.render.WorldRenderUtils.drawCustomSizedBoxAt
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
    private val recentClicks = mutableListOf<MovingObjectPosition>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || blockArray.isEmpty()) return
        blockArray.forEach{block ->
            if (Utils.distanceToPlayer(block.x, block.y, block.z) < 4) {
                val aabb = aabbConvert(mc.theWorld.getBlockState(block).block.getSelectedBoundingBox(mc.theWorld, block), block)
                modMessage(aabb)
                val eyePos = mc.thePlayer.getPositionEyes(0f)
                val centerPos = Vec3(block.x + 0.5, block.y + 0.4375, block.z + 0.5)
                val movingObjectPosition: MovingObjectPosition? = BlockUtils.collisionRayTrace(
                    block,
                    aabb,
                    //AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375),
                    eyePos,
                    centerPos
                )
                if (movingObjectPosition == null) {
                    modMessage("raytrace is null nga")
                    return@forEach
                }
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

    fun aabbConvert(aabb: AxisAlignedBB, block: BlockPos): AxisAlignedBB {
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
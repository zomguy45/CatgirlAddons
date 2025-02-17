package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.dungeon.tiles.Rotations
import net.minecraft.entity.Entity
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.*

data class PositionLook(val pos: Vec3, val yaw: Float, val pitch: Float)

object VecUtils {
    fun getPositionEyes(pos: Vec3 = mc.thePlayer.positionVector): Vec3 {
        return Vec3(
            pos.xCoord,
            pos.yCoord + mc.thePlayer.eyeHeight,
            pos.zCoord
        )
    }

    fun getLook(yaw: Float = mc.thePlayer.rotationYaw, pitch: Float = mc.thePlayer.rotationPitch): Vec3 {
        val f2 = -cos(-pitch * 0.017453292f).toDouble()
        return Vec3(
            sin(-yaw * 0.017453292f - 3.1415927f) * f2,
            sin(-pitch * 0.017453292f).toDouble(),
            cos(-yaw * 0.017453292f - 3.1415927f) * f2
        )
    }

    fun Vec3.multiply(factor: Number): Vec3 {
        return Vec3(this.xCoord * factor.toDouble(), this.yCoord * factor.toDouble(), this.zCoord * factor.toDouble())
    }

    fun Vec3.equal(other: Vec3): Boolean =
        this.xCoord == other.xCoord && this.yCoord == other.yCoord && this.zCoord == other.zCoord

    var partialTicks = 0f

    val Entity.renderX: Double
        get() = lastTickPosX + (posX - lastTickPosX) * partialTicks


    val Entity.renderY: Double
        get() = lastTickPosY + (posY - lastTickPosY) * partialTicks

    val Entity.renderZ: Double
        get() = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks

    val Entity.renderVec: Vec3
        get() = Vec3(renderX, renderY, renderZ)

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderWorld(event: RenderWorldLastEvent) {
        partialTicks = event.partialTicks
    }

    fun Vec3.toBlockPos(add: Double = 0.0): BlockPos {
        return BlockPos(this.xCoord + add, this.yCoord + add, this.zCoord + add)
    }

    fun BlockPos.toVec3(): Vec3 =
        Vec3(x.toDouble(), y.toDouble(), z.toDouble())

    fun fastEyeHeight(): Float =
        if (mc.thePlayer?.isSneaking == true) 1.54f else 1.62f

    fun BlockPos.addRotationCoords(rotation: Rotations, x: Int, z: Int): BlockPos =
        when (rotation) {
            Rotations.NORTH -> BlockPos(this.x + x, this.y, this.z + z)
            Rotations.SOUTH -> BlockPos(this.x - x, this.y, this.z - z)
            Rotations.WEST ->  BlockPos(this.x + z, this.y, this.z - x)
            Rotations.EAST ->  BlockPos(this.x - z, this.y, this.z + x)
            else -> this
        }

    fun Vec3.get(index: Int): Double {
        return when (index) {
            0 -> this.xCoord
            1 -> this.yCoord
            2 -> this.zCoord
            else -> throw IndexOutOfBoundsException("Index: $index, Size: 3")
        }
    }
}
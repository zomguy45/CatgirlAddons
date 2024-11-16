package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraft.entity.Entity
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.*

data class PositionLook(val pos: Vec3, val yaw: Float, val pitch: Float)

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

val partialTicks = 0

val Entity.renderX: Double
    get() = lastTickPosX + (posX - lastTickPosX) * partialTicks


val Entity.renderY: Double
    get() = lastTickPosY + (posY - lastTickPosY) * partialTicks

val Entity.renderZ: Double
    get() = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks

val Entity.renderVec: Vec3
    get() = Vec3(renderX, renderY, renderZ)

fun Vec3.toBlockPos(add: Double = 0.0): BlockPos {
    return BlockPos(this.xCoord + add, this.yCoord + add, this.zCoord + add)
}

fun Vec3.get(index: Int): Double {
    return when (index) {
        0 -> this.xCoord
        1 -> this.yCoord
        2 -> this.zCoord
        else -> throw IndexOutOfBoundsException("Index: $index, Size: 3")
    }
}
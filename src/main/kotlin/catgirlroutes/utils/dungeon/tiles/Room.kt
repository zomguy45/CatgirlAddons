package catgirlroutes.utils.dungeon.tiles

import catgirlroutes.utils.Utils
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

class Room(override val x: Int, override val z: Int, var data: RoomData, var core: Int = 0) : Tile {
    var vec2 = Utils.Vec2(x, z)
    var vec3 = Vec3(x.toDouble(), 70.0, z.toDouble())
    var rotation = Rotations.NONE
    override var state: RoomState = RoomState.UNDISCOVERED
}

data class FullRoom(val room: Room, var clayPos: BlockPos, val components: ArrayList<ExtraRoom>)
data class ExtraRoom(val x: Int, val z: Int, val core: Int) {
    val vec2 = Utils.Vec2(x, z)
}
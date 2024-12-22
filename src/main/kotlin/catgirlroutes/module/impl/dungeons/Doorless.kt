package catgirlroutes.module.impl.dungeons

import catgirlroutes.module.Category
import catgirlroutes.module.Module

object Doorless: Module(
    "Doorless",
    Category.DUNGEON
){
    /*
    private var doorClip = BooleanSetting("Clip", false)
    private var doorMotion = BooleanSetting("Motion", false)

    private var inDoor = false
    private var lastUse = System.currentTimeMillis()
    private val validBlocks = listOf(Blocks.coal_block, Blocks.stained_hardened_clay)
    private val skullIds = listOf("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2JjYmJmOTRkNjAzNzQzYTFlNzE0NzAyNmUxYzEyNDBiZDk4ZmU4N2NjNGVmMDRkY2FiNTFhMzFjMzA5MTRmZCJ9fX0=", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5ZDgwYjc5NDQyY2YxYTNhZmVhYTIzN2JkNmFkYWFhY2FiMGMyODgzMGZiMzZiNTcwNGNmNGQ5ZjU5MzdjNCJ9fX0=")

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        if (!inDungeons || inDoor || !mc.thePlayer.isCollidedVertically) return
        if (mc.thePlayer.heldItem.item != Item.getItemById(368)) return
        if (System.currentTimeMillis() - lastUse < 1000) return
        if (!event.packet.isMoving) return
        val posX = event.packet.positionX
        val posY = event.packet.positionY
        val posZ = event.packet.positionZ
        if (posX > 0 || posZ > 0 || posX < -200 || posZ < -200) return
        val xDec = (posX + 200) % 1
        val zDec = (posZ + 200) % 1
        var yaw = -1F
        var pitch = -1F
        var xOffset = 0
        var zOffset = 0

        when {
            isWithinTolerence(zDec, 0.7) && xDec > 0.3 && xDec < 0.7 && (validBlocks.contains(getBlockFloor(posX + 1, posY, posZ + 2)) || validBlocks.contains(getBlockFloor(posX - 1, posY, posZ + 2))) -> {
                yaw = 0F
                pitch = 77F
                ++zOffset
            }
            isWithinTolerence(xDec, 0.3) && zDec > 0.3 && zDec < 0.7 && (validBlocks.contains(getBlockFloor(posX - 2, posY, posZ + 1)) || validBlocks.contains(getBlockFloor(posX - 2, posY, posZ - 1))) -> {
                yaw = 90F
                pitch = 77F
                --xOffset
            }
            isWithinTolerence(zDec, 0.3) && xDec > 0.3 && xDec < 0.7 && (validBlocks.contains(getBlockFloor(posX - 1, posY, posZ - 2)) || validBlocks.contains(getBlockFloor(posX + 1, posY, posZ - 2))) -> {
                yaw = 180F
                pitch = 77F
                --zOffset
            }
            isWithinTolerence(xDec, 0.7) && zDec > 0.3 && zDec < 0.7 && (validBlocks.contains(getBlockFloor(posX + 2, posY, posZ - 1)) || validBlocks.contains(getBlockFloor(posX + 2, posY, posZ + 1))) -> {
                yaw = 270F
                pitch = 77F
                ++xOffset
            }
            isWithinTolerence(zDec, 0.95) && xDec > 0.3 && xDec < 0.7 && (validBlocks.contains(getBlockFloor(posX + 1, posY, posZ + 2)) || validBlocks.contains(getBlockFloor(posX - 1, posY, posZ + 2))) -> {
                yaw = 0F
                pitch = 84F
                ++zOffset
            }
            isWithinTolerence(xDec, 0.05) && zDec > 0.3 && zDec < 0.7 && (validBlocks.contains(getBlockFloor(posX - 2, posY, posZ + 1)) || validBlocks.contains(getBlockFloor(posX - 2, posY, posZ - 1))) -> {
                yaw = 90F
                pitch = 84F
                --xOffset
            }
            isWithinTolerence(zDec, 0.05) && xDec > 0.3 && xDec < 0.7 && (validBlocks.contains(getBlockFloor(posX - 1, posY, posZ - 2)) || validBlocks.contains(getBlockFloor(posX + 1, posY, posZ - 2))) -> {
                yaw = 180F
                pitch = 84F
                --zOffset
            }
            isWithinTolerence(xDec, 0.95) && zDec > 0.3 && zDec < 0.7 && (validBlocks.contains(getBlockFloor(posX + 2, posY, posZ - 1)) || validBlocks.contains(getBlockFloor(posX + 2, posY, posZ + 1))) -> {
                yaw = 270F
                pitch = 84F
                ++xOffset
            }
        }
        if (yaw < 0F || pitch < 0F) return
        val tileEntity = mc.theWorld?.getTileEntity(getBlockPosFloor(posX + xOffset, posX + 1, posZ + zOffset)) as? TileEntitySkull ?: return
        if (tileEntity.playerProfile == null) return
        val skullId = tileEntity.playerProfile.properties.get("textures").first().value
        if (!skullIds.contains(skullId)) return
        inDoor = true
        val initialYaw = mc.thePlayer.rotationYaw
        val initialPitch = mc.thePlayer.rotationPitch
    }

    private fun isWithinTolerence(n1: Double, n2: Double): Boolean {
        return abs(n1 - n2) < 1e-4;
    }

    private fun getBlockPosFloor(x: Double, y: Double, z: Double): BlockPos {
        return BlockPos(floor(x), floor(y), floor(z));
    }

    private fun getBlockFloor(x: Double, y: Double, z: Double): Block {
        return mc.theWorld.getBlockState(BlockPos(floor(x), floor(y), floor(z))).block;
    }

    private fun setBlockAt(x: Double, y: Double, z: Double) {
        val blockPos = getBlockPosFloor(x, y, z).bl
    }

     */
}
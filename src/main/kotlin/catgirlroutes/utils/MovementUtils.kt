package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.commodore
import catgirlroutes.events.impl.MotionUpdateEvent
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.MovementUtils.addBlock
import catgirlroutes.utils.MovementUtils.clearBlocks
import catgirlroutes.utils.MovementUtils.moveToBlock
import catgirlroutes.utils.rotation.RotationUtils.getYawAndPitch
import net.minecraft.block.Block
import net.minecraft.client.settings.GameSettings.isKeyDown
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object MovementUtils {
    fun setKey(key: String, down: Boolean) {
        when(key) {
            "w" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, down)
            "a" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.keyCode, down)
            "s" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.keyCode, down)
            "d" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.keyCode, down)
            "jump" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, down)
            "shift" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, down)
            "sprint" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, down)
        }
    }

    fun stopVelo() {
        mc.thePlayer.setVelocity(0.0, mc.thePlayer.motionY, 0.0)
    }

    fun stopMovement() {
        setKey("w", false)
        setKey("a", false)
        setKey("s", false)
        setKey("d", false)
    }

    fun restartMovement() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, isKeyDown(mc.gameSettings.keyBindForward))
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.keyCode, isKeyDown(mc.gameSettings.keyBindLeft))
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.keyCode, isKeyDown(mc.gameSettings.keyBindBack))
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.keyCode, isKeyDown(mc.gameSettings.keyBindRight))
    }

    fun jump() {
        scheduleTask(0) {setKey("jump", true)}
        scheduleTask(2) {setKey("jump", false)}
    }

    private var shouldEdge = false

    fun edge() {
        shouldEdge = true
    }

    @SubscribeEvent
    fun checkForEdging(event: RenderWorldLastEvent) {
        if (!shouldEdge) return
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)
        val blockID = Block.getIdFromBlock(mc.theWorld.getBlockState(playerPos).block)
        if (blockID == 0) {
            jump()
            shouldEdge = false
        }
    }

    var targetBlocks = arrayListOf<Vec3>()

    fun clearBlocks() {
        targetBlocks.clear()
    }

    fun moveToBlock(x: Double, z: Double) {
        targetBlocks.add(0, Vec3(x, 0.0, z)) // y coord doesn't matter anyway
    }

    fun addBlock(x: Double, z: Double) {
        targetBlocks.add(Vec3(x, 0.0, z))
    }

    private var wasOnGround = false

    @SubscribeEvent
    fun onTick(event: MotionUpdateEvent.Pre) {
        if (targetBlocks.isEmpty()) return
        val targetBlock = targetBlocks[0]
        if (
            abs(mc.thePlayer.posX - targetBlock.xCoord) <= 1.3 * mc.thePlayer.capabilities.walkSpeed &&
            abs(mc.thePlayer.posZ - targetBlock.zCoord) <= 1.3 * mc.thePlayer.capabilities.walkSpeed
        ) {
            mc.thePlayer.setVelocity(0.0, mc.thePlayer.motionY, 0.0)
            mc.thePlayer.setPosition(targetBlock.xCoord, mc.thePlayer.posY, targetBlock.zCoord)
            targetBlocks.removeFirst()
            return
        }
        val(yaw, pitch) = getYawAndPitch(targetBlock.xCoord, targetBlock.yCoord, targetBlock.zCoord)
        val radians = yaw * Math.PI / 180
        if (!mc.thePlayer.onGround && !wasOnGround) {
            val speedX = mc.thePlayer.motionX * 1 * 0.91 + 0.02 * 1.3
            val speedZ = mc.thePlayer.motionZ * 1 * 0.91 + 0.02 * 1.3
            mc.thePlayer.motionX = speedX * -sin(radians)
            mc.thePlayer.motionZ = speedZ * cos(radians)
            wasOnGround = false
        } else if (!mc.thePlayer.onGround && wasOnGround) {
            modMessage("jumping velo")
            val speedX = mc.thePlayer.motionX * 1 * 0.91 + 0.1 * 1.3 * (0.6 / 0.6).pow(3.0)
            val speedZ = mc.thePlayer.motionZ * 1 * 0.91 + 0.1 * 1.3 * (0.6 / 0.6).pow(3.0)
            mc.thePlayer.motionX = speedX * -sin(radians) + 0.2 * -sin(radians)
            mc.thePlayer.motionZ = speedZ * cos(radians) + 0.2 * cos(radians)
            wasOnGround = false
        } else {
            val speed = if (!mc.thePlayer.isSneaking) {mc.thePlayer.capabilities.walkSpeed * 2.806} else {
                mc.thePlayer.capabilities.walkSpeed * 0.64753846153
            }
            mc.thePlayer.motionX = speed * -sin(radians)
            mc.thePlayer.motionZ = speed * cos(radians)
            wasOnGround = true
        }
    }
}

val thisshit = commodore("mottar") { //motiontarget
    literal("set").runs{
            x: Double, z: Double ->
        moveToBlock(x, z)
    }
    literal("add").runs {
            x: Double, z: Double ->
        addBlock(x, z)
    }
    literal("stop").runs{
        clearBlocks()
    }
    literal("test").runs{
        addBlock(145.5, 160.5)
        addBlock(145.5, 161.5)
        addBlock(144.5, 161.5)
        addBlock(143.5, 161.5)
        addBlock(143.5, 162.5)
        addBlock(143.5, 163.5)
        addBlock(144.5, 163.5)
        addBlock(145.5, 163.5)
        addBlock(145.5, 164.5)
        addBlock(145.5, 165.5)
    }
}
package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.ClientListener.scheduleTask
import net.minecraft.block.Block
import net.minecraft.client.settings.GameSettings.isKeyDown
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
        val blockID = Block.getIdFromBlock(mc.theWorld.getBlockState(playerPos).block)
        if (blockID == 0) {
            jump()
            shouldEdge = false
        }
    }
}
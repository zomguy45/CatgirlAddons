package catgirlroutes.module.impl.misc


import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object GhostBlocks : Module(
    "Ghost Blocks",
    category = Category.MISC,
    description = "Creates ghost blocks where you are looking when the key bind is pressed."
){
    override fun onKeyBind() { }
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (this.keyCode > 0 && !Keyboard.isKeyDown(this.keyCode)) return
        if (this.keyCode < 0 && !Mouse.isButtonDown(this.keyCode + 100)) return
        toAir(mc.objectMouseOver.blockPos)
    }
    private fun toAir(blockPos: BlockPos?): Boolean {
        if (blockPos != null) {
            mc.theWorld.setBlockToAir(mc.objectMouseOver.blockPos)
            return true
        }
        return false
    }
}

package catgirlroutes.module.impl.dungeons


import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object GhostBlocks : Module(  // todo: add delay, range, option to gkey skulls maybeidk
    "Ghost Blocks",
    category = Category.DUNGEON,
    description = "Creates ghost blocks where you are looking when the key bind is pressed."
){
    private val swingHand = BooleanSetting("Swing Hand", true, description = "Swings the player's hand.")

    init {
        this.addSettings(swingHand)
    }

    override fun onKeyBind() { } // todo: fix when it workie if keybind nono

    private val ignoredBlockTypes: List<Block> = listOf(Blocks.skull, Blocks.chest, Blocks.lever) // todo: use DungeonUtils.isSecret maybeidk

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        if (this.keyCode > 0 && !Keyboard.isKeyDown(this.keyCode)) return
        if (this.keyCode < 0 && !Mouse.isButtonDown(this.keyCode + 100)) return

        val blockPos = mc.objectMouseOver.blockPos
        val block = mc.theWorld.getBlockState(blockPos).block

        if (ignoredBlockTypes.contains(block)) return
        if (swingHand.value) mc.thePlayer.swingItem()
        toAir(blockPos)
    }

    private fun toAir(blockPos: BlockPos?): Boolean {
        if (blockPos != null) {
            mc.theWorld.setBlockToAir(mc.objectMouseOver.blockPos)
            return true
        }
        return false
    }
}

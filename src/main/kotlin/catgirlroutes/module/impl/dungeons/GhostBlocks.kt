package catgirlroutes.module.impl.dungeons


import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.PlayerUtils.leftClick
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.clock.Executor
import catgirlroutes.utils.clock.Executor.Companion.register
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
) {
    private val gKey: BooleanSetting = BooleanSetting("GKey", false)
    private val swingHand = BooleanSetting("Swing Hand", true, "Swings the player's hand.").withDependency { gKey.enabled }
    private val gKeyKeyBind: KeyBindSetting = KeyBindSetting("GKey Key", Keyboard.KEY_NONE, "Creates ghost blocks where you are looking.").withDependency { gKey.enabled }

    private val swapStonk: BooleanSetting = BooleanSetting("Swap Stonk", false);

    private var running = false
    private var prevSlot = -1
    private val swapKeyBind: KeyBindSetting = KeyBindSetting("Swap Key", Keyboard.KEY_NONE, "Automatically makes a ghost block using a swap to pickaxe.").withDependency { swapStonk.enabled }
        .onPress {
            if (!this.swapStonk.enabled || !this.enabled) return@onPress
            if (this.running) return@onPress;

            this.running = true;
            this.prevSlot = mc.thePlayer.inventory.currentItem;

            val swapResult = swapFromName("pickaxe")
            if (!swapResult) {
                this.running = false
                return@onPress
            }

            scheduleTask(1) { leftClick() }
            scheduleTask(2) {
                mc.thePlayer.inventory.currentItem = this.prevSlot
                this.running = false
            }
        }

    init {
        Executor(50) {
            if (!gKeyKeyBind.value.isDown() || !gKey.enabled || !this.enabled) return@Executor

            val blockPos = mc.objectMouseOver.blockPos
            val block = mc.theWorld.getBlockState(blockPos).block

            if (ignoredBlockTypes.contains(block)) return@Executor
            if (swingHand.value) mc.thePlayer.swingItem() // I think it'd swing in guis (do not get pringleclient'd)
            toAir(blockPos)
        }.register()

        this.addSettings(this.gKey, this.swingHand, this.gKeyKeyBind, this.swapStonk, this.swapKeyBind)
    }

    private val ignoredBlockTypes: List<Block> = listOf(Blocks.skull, Blocks.chest, Blocks.lever) // todo: use DungeonUtils.isSecret maybeidk

    private fun toAir(blockPos: BlockPos?): Boolean {
        if (blockPos != null) {
            mc.theWorld.setBlockToAir(mc.objectMouseOver.blockPos)
            return true
        }
        return false
    }
}

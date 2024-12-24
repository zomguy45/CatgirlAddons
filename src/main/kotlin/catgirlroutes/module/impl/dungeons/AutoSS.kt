package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.LocationManager
import catgirlroutes.utils.PlayerUtils.rightClick
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.block.Block
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.random.Random

/** @Author Kaze.0707**/

//Sponsored by chinese spyware

object AutoSS : Module(
    name = "AutoSS",
){
    val delay: NumberSetting = NumberSetting("Delay", 200.0, 0.0, 500.0, 50.0)

    init {
        this.addSettings(delay)
    }

    var next = false
    var progress = 0
    var delayTick = 0
    var doneFirst = false
    var doingSS = false
    var clicked = false
    var clicks = ArrayList<BlockPos>()

    fun reset() {
        clicks.clear()
        progress = 0
        delayTick = 0
        doneFirst = false
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        reset()
        doingSS = false
        clicked = false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!this.enabled) return
        if (LocationManager.inSkyblock || mc.objectMouseOver == null) return
        val detect: Block = mc.theWorld.getBlockState(BlockPos(110, 123, 92)).block
        val startButton: BlockPos = BlockPos(110, 121, 91)

        if (mc.thePlayer.getDistanceSqToCenter(startButton) > 25) return

        var device = false

        mc.theWorld.loadedEntityList
            .filterIsInstance<EntityArmorStand>()
            .filter { it.getDistanceToEntity(mc.thePlayer) < 6 && it.displayName.unformattedText.contains("Device") }
            .forEach {
                device = true
            }

        if (!device) return

        if (!clicked) {
            clicked = true
            doingSS = true
            reset()
            Thread{
                try {
                    for (i in 0 until 2) {
                        rightClick() //TODO: Click start button with packets
                        Thread.sleep(Random.nextInt(125, 141).toLong())
                    }
                    rightClick() //TODO: Click start button with packets
                } catch (e: Exception) {
                    modMessage("NIGGER")
                }
            }.start()
        } else {
            clicked = false
        }
        if (detect == Blocks.air) {
            progress = 0
        } else if (detect == Blocks.stone_button) {
            if (delayTick > 0) {
                delayTick--
            } else {
                if (!doneFirst) {
                    if (clicks.size == 3) {
                        clicks.removeAt(0)
                    }
                    doneFirst = true
                }
                if (progress < clicks.size) {
                    val next: BlockPos = clicks.get(progress)
                    if (mc.theWorld.getBlockState(next).block == Blocks.stone_button) {
                        rightClick() //TODO: Click next with packets
                        progress++
                        delayTick = delay.value.toInt() / 50
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val mop: MovingObjectPosition = mc.objectMouseOver
        if (mop == null) return

        val startButton: BlockPos = BlockPos(110, 121, 91)
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && startButton == event.pos && startButton == mop.blockPos && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            clicked = false
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: PacketReceiveEvent) {
        if (event.packet !is S23PacketBlockChange) return
        if (event.packet.blockPosition.x == 111 && event.packet.blockPosition.y >= 120 && event.packet.blockPosition.y <= 123 && event.packet.blockPosition.z >= 92 && event.packet.blockPosition.z <= 95) {
            val button: BlockPos = BlockPos(110, event.packet.blockPosition.y, event.packet.blockPosition.z)
            if (event.packet.blockState.block == Blocks.sea_lantern) {
                if (!clicks.contains(button) || !doneFirst) {
                    clicks.add(button)
                }
            }
        }
    }
}
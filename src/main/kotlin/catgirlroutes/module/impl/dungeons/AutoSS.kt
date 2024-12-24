package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.BlockChangeEvent
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.BlockAura.BlockAuraAction
import catgirlroutes.utils.BlockAura.blockArray
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.LocationManager
import net.minecraft.block.Block
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
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
    Category.DUNGEON
){
    val delay: NumberSetting = NumberSetting("Delay", 200.0, 0.0, 500.0, 50.0)
    val forceDevice: BooleanSetting = BooleanSetting("Force Device", false)
    var resetSS: ActionSetting = ActionSetting("Reset") {reset2()}

    init {
        this.addSettings(delay, forceDevice, resetSS)
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
        next = false
        progress = 0
        delayTick = 0
        doneFirst = false
        debugMessage("Reset!")
    }

    fun reset2() {
        doingSS = false
        clicked = false
        clicks.clear()
        next = false
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
        if (event.phase != TickEvent.Phase.START) return
        //if (!LocationManager.inSkyblock) return
        if (mc.theWorld == null) return
        val detect: Block = mc.theWorld.getBlockState(BlockPos(110, 123, 92)).block
        val startButton: BlockPos = BlockPos(110, 121, 91)

        if (mc.thePlayer.getDistanceSqToCenter(startButton) > 25) return

        var device = false

        if (forceDevice.value) {
            device = true
        }

        mc.theWorld.loadedEntityList
            .filterIsInstance<EntityArmorStand>()
            .filter { it.getDistanceToEntity(mc.thePlayer) < 6 && it.displayName.unformattedText.contains("Device") }
            .forEach { _ ->
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
                        clickButton(startButton.x, startButton.y, startButton.z)
                        Thread.sleep(Random.nextInt(125, 140).toLong())
                    }
                    clickButton(startButton.x, startButton.y, startButton.z)
                    debugMessage("Starting SS")
                } catch (e: Exception) {
                    modMessage("NIGGER")
                }
            }.start()
            return
        }

        if (detect == Blocks.air) {
            progress = 0
        } else if (detect == Blocks.stone_button && doingSS) {
            if (delayTick > 0) {
                delayTick--
            } else {
                if (!doneFirst) {
                    if (clicks.size == 3) {
                        clicks.removeAt(0)
                    }
                    doneFirst = true
                    debugMessage("First Phase")
                }
                if (progress < clicks.size) {
                    val next: BlockPos = clicks[progress]
                    if (mc.theWorld.getBlockState(next).block == Blocks.stone_button) {
                        clickButton(next.x, next.y, next.z)
                        progress++
                        delayTick = delay.value.toInt() / 50
                        debugMessage("Clicked at: x: ${next.x}, y: ${next.y}, z: ${next.z}")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val mop: MovingObjectPosition = mc.objectMouseOver ?: return

        val startButton: BlockPos = BlockPos(110, 121, 91)
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && startButton == event.pos && startButton == mop.blockPos && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            clicked = false
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (event.pos.x == 111 && event.pos.y >= 120 && event.pos.y <= 123 && event.pos.z >= 92 && event.pos.z <= 95) {
            val button: BlockPos = BlockPos(110, event.pos.y, event.pos.z)
            if (event.update.block == Blocks.sea_lantern) {
                if (!clicks.contains(button)) {
                    debugMessage("Added to clicks: x: ${event.pos.x}, y: ${event.pos.y}, z: ${event.pos.z}")
                    clicks.add(button)
                }
            }
        }
    }

    fun clickButton(x: Int, y: Int, z: Int) {
        if (mc.thePlayer.getDistanceSqToCenter(BlockPos(x, y, z)) > 25) return
        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(x, y, z), 4, mc.thePlayer.heldItem, 0.875f, 0.5f, 0.5f))
    }
}
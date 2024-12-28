package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.BlockChangeEvent
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.ReceiveChatPacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Visibility
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.BlockAura.BlockAuraAction
import catgirlroutes.utils.BlockAura.blockArray
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.LocationManager
import catgirlroutes.utils.render.WorldRenderUtils.drawCustomSizedBoxAt
import catgirlroutes.utils.render.WorldRenderUtils.drawStringInWorld
import net.minecraft.block.Block
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.random.Random

/** @Author Kaze.0707**/

//Sponsored by chinese spyware

object AutoSS : Module(
    name = "AutoSS",
    Category.DUNGEON
){
    val delay: NumberSetting = NumberSetting("Delay", 200.0, 50.0, 500.0, 10.0)
    private val forceDevice: BooleanSetting = BooleanSetting("Force Device", false, visibility = Visibility.ADVANCED_ONLY)
    private val resetSS: ActionSetting = ActionSetting("Reset SS") {reset(); doingSS = false; clicked = false}
    private val autoStart: NumberSetting = NumberSetting("Autostart delay", 125.0, 50.0, 200.0, 1.0)


    init {
        this.addSettings(delay, forceDevice, resetSS, autoStart)
    }

    var next = false
    var progress = 0
    var delayTick = 0
    var doneFirst = false
    var doingSS = false
    var clicked = false
    var clicks = ArrayList<BlockPos>()
    var lastClick = System.currentTimeMillis()
    var wtflip = System.currentTimeMillis()
    var clickedButton: Vec3? = null
    var allButtons = ArrayList<Vec3>()
    var lastClickAdded = System.currentTimeMillis()

    fun reset() {
        allButtons.clear()
        clicks.clear()
        next = false
        progress = 0
        delayTick = 0
        doneFirst = false
        debugMessage("Reset!")
    }

    override fun onKeyBind() {
        clicked = false
        doingSS = false
        //start()
        //&lingling()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        reset()
        allButtons.clear()
    }

    fun start() {
        allButtons.clear()
        val startButton: BlockPos = BlockPos(110, 121, 91)
        if (mc.thePlayer.getDistanceSqToCenter(startButton) > 25) return
        if (!clicked) {
            debugMessage("Starting SS")
            modMessage(System.currentTimeMillis())
            clicked = true
            doingSS = true
            reset()
            Thread{
                try {
                    for (i in 0 until 2) {
                        clickButton(startButton.x, startButton.y, startButton.z)
                        Thread.sleep(Random.nextInt(delay.value.toInt(), delay.value.toInt() * 1136 / 1000).toLong())
                    }
                    clickButton(startButton.x, startButton.y, startButton.z)
                } catch (e: Exception) {
                    modMessage("NIGGER")
                }
            }.start()
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val msg = event.message.unformattedText
        val startButton: BlockPos = BlockPos(110, 121, 91)
        if (mc.thePlayer.getDistanceSqToCenter(startButton) > 25) return
        if (msg.contains("Device")) {
            modMessage(System.currentTimeMillis())
        }
        if (!msg.contains("[BOSS] Goldor: Who dares trespass into my domain?")) return
        start()
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!this.enabled) return
        if (!LocationManager.inSkyblock && !forceDevice.value) return
        if (mc.theWorld == null) return
        val detect: Block = mc.theWorld.getBlockState(BlockPos(110, 123, 92)).block
        val startButton: BlockPos = BlockPos(110, 121, 91)

        if (System.currentTimeMillis() - lastClickAdded > delay.value) {
            clickedButton = null
        }

        if (mc.thePlayer.getDistanceSqToCenter(startButton) < 1600) {
            if (clickedButton != null) {
                drawCustomSizedBoxAt(clickedButton!!.xCoord + 0.875, clickedButton!!.yCoord + 0.375, clickedButton!!.zCoord + 0.3125, 0.125, 0.25, 0.375, Color.PINK, filled = true)
            }
            allButtons.forEachIndexed{index, location ->
                drawStringInWorld((index + 1).toString(), Vec3(location.xCoord - 0.0625, location.yCoord + 0.5625, location.zCoord + 0.5), scale = 0.02f, shadow = true, depthTest = false)
            }
        }

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

        if (!device) {
            clicked = false
            return
        }

        /*if (!clicked) {
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
        }*/

        if (!clicked) return

        if (detect == Blocks.air) {
            progress = 0
        } else if (detect == Blocks.stone_button && doingSS) {
            if (System.currentTimeMillis() - lastClick < delay.value) {
                return
            } else {
                lastClick = System.currentTimeMillis()
                if (!doneFirst) {
                    if (clicks.size == 3) {
                        clicks.removeAt(0)
                        allButtons.removeAt(0)
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
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val mop: MovingObjectPosition = mc.objectMouseOver ?: return
        if (System.currentTimeMillis() - wtflip < 2500) return
        wtflip = System.currentTimeMillis()
        val startButton: BlockPos = BlockPos(110, 121, 91)
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && startButton == event.pos && startButton == mop.blockPos && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            clicked = false
            doingSS = false
            start()
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (event.pos.x == 111 && event.pos.y >= 120 && event.pos.y <= 123 && event.pos.z >= 92 && event.pos.z <= 95) {
            val button: BlockPos = BlockPos(110, event.pos.y, event.pos.z)
            if (event.update.block == Blocks.sea_lantern) {
                if (!clicks.contains(button)) {
                    debugMessage("Added to clicks: x: ${event.pos.x}, y: ${event.pos.y}, z: ${event.pos.z}")
                    progress = 0
                    clicks.add(button)
                    allButtons.add(Vec3(event.pos.x.toDouble(), event.pos.y.toDouble(), event.pos.z.toDouble()))
                }
            }
        }
    }

    fun clickButton(x: Int, y: Int, z: Int) {
        if (mc.thePlayer.getDistanceSqToCenter(BlockPos(x, y, z)) > 25) return
        debugMessage("Clicked at: x: ${x}, y: ${y}, z: ${z}. Time: ${System.currentTimeMillis()}")
        clickedButton = Vec3(x.toDouble(), y.toDouble(), z.toDouble())
        lastClickAdded = System.currentTimeMillis()
        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(x, y, z), 4, mc.thePlayer.heldItem, 0.875f, 0.5f, 0.5f))
    }

    fun lingling() {
        Thread{
            try {
                for (i in 0 until 7) {
                    val startButton: BlockPos = BlockPos(151, 64, 105)
                    clickButton(startButton.x, startButton.y, startButton.z)
                    Thread.sleep(50)
                }
            } catch (e: Exception) {
                modMessage("...")
            }
        }.start()
    }
}

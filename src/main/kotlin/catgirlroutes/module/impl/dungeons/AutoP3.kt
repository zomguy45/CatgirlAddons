package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.impl.Ring
import catgirlroutes.commands.impl.RingManager
import catgirlroutes.commands.impl.RingManager.loadRings
import catgirlroutes.commands.impl.RingManager.rings
import catgirlroutes.commands.impl.ringEditMode
import catgirlroutes.events.impl.MotionUpdateEvent
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.dungeons.Blink.packetArray
import catgirlroutes.module.impl.dungeons.LavaClip.lavaClipToggle
import catgirlroutes.module.impl.player.HClip.hClip
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.*
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.MovementUtils.edge
import catgirlroutes.utils.MovementUtils.jump
import catgirlroutes.utils.MovementUtils.setKey
import catgirlroutes.utils.MovementUtils.stopMovement
import catgirlroutes.utils.MovementUtils.stopVelo
import catgirlroutes.utils.PlayerUtils.leftClick
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.Utils.renderText
import catgirlroutes.utils.dungeon.DungeonUtils.floorNumber
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.termGuiTitles
import catgirlroutes.utils.render.WorldRenderUtils
import catgirlroutes.utils.render.WorldRenderUtils.drawP3boxWithLayers
import catgirlroutes.utils.render.WorldRenderUtils.drawStringInWorld
import catgirlroutes.utils.render.WorldRenderUtils.renderGayFlag
import catgirlroutes.utils.render.WorldRenderUtils.renderTransFlag
import catgirlroutes.utils.rotation.FakeRotater.clickAt
import catgirlroutes.utils.rotation.RotationUtils.getYawAndPitch
import catgirlroutes.utils.rotation.RotationUtils.snapTo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Color.WHITE
import java.awt.Color.black
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


object AutoP3 : Module(
    "Auto P3",
    category = Category.DUNGEON,
    description = "A module that allows you to place down rings that execute various actions."
) {
    val selectedRoute = StringSetting("Selected route", "1", description = "Name of the selected route for auto p3.")
    private val inBossOnly = BooleanSetting("Boss only", true)
    private val editTitle = BooleanSetting("EditMode title", false)
    private val boomType = StringSelectorSetting(
        "Boom type",
        "Regular",
        arrayListOf("Regular", "Infinity"),
        "Superboom TNT type to use for BOOM ring"
    )
    private val style = StringSelectorSetting(
        "Ring style",
        "Trans",
        arrayListOf("Trans", "Normal", "LGBTQIA+"),
        "Ring render style to be used."
    )
    private val layers = NumberSetting(
        "Ring layers amount",
        3.0,
        3.0,
        5.0,
        1.0,
        "Amount of ring layers to render"
    ).withDependency { style.selected == "Normal" }
    private val colour = ColorSetting(
        "Ring colour",
        black,
        false,
        "Colour of Normal ring style"
    ).withDependency { style.selected == "Normal" }
    private val disableLength = NumberSetting("Recording length", 50.0, 1.0, 100.0)
    private val recordLength = NumberSetting("Recording length", 50.0, 1.0, 999.0)
    private val recordBind: KeyBindSetting = KeyBindSetting(
        "Movement record",
        Keyboard.KEY_NONE,
        "Starts recording a movement replay if you are on a movement ring and in editmode"
    )
        .onPress {
            if (movementRecord) {
                movementRecord = false
                modMessage("Done recording")
                return@onPress
            }
            if (!ringEditMode) return@onPress
            rings.forEach { ring ->
                if (inRing(ring) && ring.type == "movement") {
                    modMessage("Started recording")
                    mc.thePlayer.setPosition(
                        floor(mc.thePlayer.posX) + 0.5,
                        mc.thePlayer.posY,
                        floor(mc.thePlayer.posZ) + 0.5
                    )
                    movementRecord = true
                    movementCurrentRing = ring
                    movementCurrentRing!!.packets = mutableListOf<Blink.BlinkC06>()
                }
            }
        }


    init {
        this.addSettings(
            selectedRoute,
            inBossOnly,
            editTitle,
            boomType,
            style,
            layers,
            colour,
            disableLength,
            recordLength,
            recordBind,
        )
    }

    var termFound = false
    var termListener = false
    var dir: Double? = null
    var airTicks = 0
    var lastX = 0.0
    var lastZ = 0.0

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        loadRings()
    }

    val cooldownMap = mutableMapOf<String, Boolean>()

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (ringEditMode || (inBossOnly.enabled && floorNumber != 7 && !inBoss)) return
        rings.forEach { ring ->
            val key = "${ring.location.xCoord},${ring.location.yCoord},${ring.location.zCoord},${ring.type}"
            val cooldown: Boolean = cooldownMap[key] == true
            if (inRing(ring)) {
                if (ring.arguments!!.contains("term") && !termFound) {
                    termListener = true
                    return
                } else scheduleTask(1) { termFound = false }
                if (cooldown) return@forEach
                cooldownMap[key] = true
                GlobalScope.launch {
                    executeAction(ring)
                }
            } else if (cooldown) {
                cooldownMap[key] = false
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (inBossOnly.enabled && floorNumber != 7 && !inBoss) return
        rings.forEach { ring ->
            val x: Double = ring.location.xCoord
            val y: Double = ring.location.yCoord
            val z: Double = ring.location.zCoord

            val cooldown: Boolean = cooldownMap["$x,$y,$z,${ring.type}"] == true
            val color = if (cooldown) WHITE else colour.value

            when (style.selected) {
                "Trans" -> renderTransFlag(x, y, z, ring.width, ring.height)
                "Normal" -> drawP3boxWithLayers(x, y, z, ring.width, ring.height, color, layers.value.toInt())
                "LGBTQIA+" -> renderGayFlag(x, y, z, ring.width, ring.height)
            }
            if ((ring.type == "blink" || ring.type == "movement") && ring.packets.size != 0) {
                for (i in 0 until ring.packets.size - 1) {
                    val p1 = ring.packets[i]
                    val p2 = ring.packets[i + 1]
                    WorldRenderUtils.drawLine(
                        p1.x, p1.y + 0.1, p1.z,
                        p2.x, p2.y + 0.1, p2.z,
                        Color.PINK, 4.0f, false
                    )
                }
                drawStringInWorld((ring.packets.size - 1).toString(), Vec3(x, y + ring.height, z), scale = 0.035F)
            }
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (!editTitle.enabled) return
        if (!ringEditMode || (inBossOnly.enabled && floorNumber != 7)) return
        val sr = ScaledResolution(mc)
        val t = "Edit Mode"
        renderText(
            t,
            sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(t) / 2,
            sr.scaledHeight / 2 + mc.fontRendererObj.FONT_HEIGHT
        )
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) // idk if you actually need this. but jic for inv walk
    fun onTerm(event: PacketReceiveEvent) {
        if (!termListener || (inBossOnly.enabled && floorNumber != 7 && !inBoss)) return
        if (event.packet !is S2DPacketOpenWindow) return
        val windowTitle = event.packet.windowTitle
        if (windowTitle != null && termGuiTitles.any { windowTitle.unformattedText.startsWith(it) }) {
            modMessage("Term found")
            termFound = true
            termListener = false
        }
    }

    fun inRing(ring: Ring): Boolean {
        val viewerPos = mc.renderManager
        val distanceX = abs(viewerPos.viewerPosX - ring.location.xCoord)
        val distanceY = abs(viewerPos.viewerPosY - ring.location.yCoord)
        val distanceZ = abs(viewerPos.viewerPosZ - ring.location.zCoord)

        return distanceX < (ring.width / 2) &&
                distanceY < ring.height &&
                distanceY >= -0.5 &&
                distanceZ < (ring.width / 2);
    }

    private suspend fun executeAction(ring: Ring) {
        val actionDelay: Int = if (ring.delay == null) 0 else ring.delay!!
        delay(actionDelay.toLong())
        ring.arguments?.let {
            if ("stop" in it) {
                dir = null
                stopVelo()
            }
            if ("walk" in it) setKey("w", true)
            if ("look" in it) snapTo(ring.yaw, ring.pitch)
            if ("fullstop" in it) {
                stopMovement()
                stopVelo()
            }
            if ("block" in it) {
                val (yaw, pitch) = getYawAndPitch(
                    ring.lookBlock!!.xCoord,
                    ring.lookBlock!!.yCoord,
                    ring.lookBlock!!.zCoord
                )
                snapTo(yaw, pitch)
            }
        }
        when (ring.type) {
            "walk" -> {
                dir = null
                modMessage("Walking!")
                setKey("w", true)
            }

            "jump" -> {
                modMessage("Jumping!")
                jump()
            }

            "stop" -> {
                dir = null
                modMessage("Stopping!")
                stopMovement()
                stopVelo()
            }

            "boom" -> {
                dir = null
                modMessage("Bomb denmark!")
                if (boomType.selected == "Regular") swapFromName("superboom tnt") else swapFromName("infinityboom tnt")
                //modMessage(boomType.selected)
                scheduleTask(0) { leftClick() }
            }

            "hclip" -> {
                dir = null
                modMessage("Hclipping!")
                hClip(ring.yaw)
                ring.arguments?.let {
                    if ("walk" in it) {
                        scheduleTask(1) {
                            setKey("w", true)
                        }
                    }
                }
            }

            "vclip" -> {
                dir = null
                modMessage("Vclipping!")
                lavaClipToggle(ring.depth!!.toDouble(), true)
            }

            "bonzo" -> {
                dir = null
                modMessage("Bonzoing!")
                swapFromName("bonzo's staff")
                scheduleTask(0) {
                    clickAt(ring.yaw, ring.pitch)
                }
            }

            "look" -> {
                dir = null
                modMessage("Looking!")
                snapTo(ring.yaw, ring.pitch)
            }

            "align" -> {
                dir = null
                modMessage("Aligning!")
                mc.thePlayer.setPosition(
                    floor(mc.thePlayer.posX) + 0.5,
                    mc.thePlayer.posY,
                    floor(mc.thePlayer.posZ) + 0.5
                )
            }

            "block" -> {
                dir = null
                modMessage("Snaping to [${ring.lookBlock!!.xCoord}, ${ring.lookBlock!!.yCoord}, ${ring.lookBlock!!.zCoord}]! ")
                val (yaw, pitch) = getYawAndPitch(
                    ring.lookBlock!!.xCoord,
                    ring.lookBlock!!.yCoord,
                    ring.lookBlock!!.zCoord
                )
                snapTo(yaw, pitch)
            }

            "edge" -> {
                dir = null
                modMessage("Edging!")
                edge()
            }

            "command" -> {
                modMessage("Sexecuting!")
                commandAny(ring.command!!)
            }

            "blink" -> {
                if (ring.packets.size == 0) return
                if (packetArray.size > ring.packets.size) {
                    scheduleTask(0) {
                        ring.packets.forEach { packet ->
                            mc.netHandler.networkManager.sendPacket(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    packet.x,
                                    packet.y,
                                    packet.z,
                                    packet.onGround
                                )
                            )
                        }
                        mc.thePlayer.setPosition(ring.packets.last().x, ring.packets.last().y, ring.packets.last().z)
                    }
                }
            }

            "movement" -> {
                dir = null
                if (ring.packets.size == 0) return
                movementList = ring.packets.toMutableList()
                movementOn = true
            }

            "velo" -> {
                lastX = 0.0
                lastZ = 0.0
                airTicks = 0
                modMessage("Motion")
                if (mc.thePlayer.onGround) {
                    stopMovement()
                    dir = ring.yaw.toDouble()
                } else {
                    stopMovement()
                    stopVelo()
                    scheduleTask(0) {
                        dir = ring.yaw.toDouble()
                    }
                }
            }
        }
    }

    private var movementRecord = false
    private var movementCurrentRing: Ring? = null

    @SubscribeEvent
    fun onMovementRecorder(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !movementRecord) return
        if (movementCurrentRing!!.packets.size == recordLength.value.toInt()) {
            movementRecord = false
            RingManager.saveRings()
            modMessage("Done recording")
        }
        if (event.packet is C03PacketPlayer.C06PacketPlayerPosLook || event.packet is C03PacketPlayer.C04PacketPlayerPosition) {
            movementCurrentRing!!.packets.add(
                Blink.BlinkC06(
                    event.packet.yaw,
                    event.packet.pitch,
                    event.packet.positionX,
                    event.packet.positionY,
                    event.packet.positionZ,
                    event.packet.isOnGround
                )
            )
        }
    }

    private var movementList = mutableListOf<Blink.BlinkC06>()
    private var movementOn = false
    private var onlyHorizontal = false

    @SubscribeEvent
    fun onTickMovement(event: MotionUpdateEvent.Pre) {
        if (!movementOn) return
        if (movementList.isEmpty()) {
            movementOn = false
            return
        }
        val move = movementList.first()
        val x = move.x - mc.thePlayer.posX
        var y = move.y - mc.thePlayer.posY
        val z = move.z - mc.thePlayer.posZ
        if (onlyHorizontal) y = 0.0
        movementList.removeFirst()
        mc.thePlayer.moveEntity(x, y, z)
    }

    @SubscribeEvent
    fun onS12(event: PacketReceiveEvent) {
        if (event.packet !is S12PacketEntityVelocity || event.packet.entityID != mc.thePlayer.entityId) return
        if (event.packet.motionY == 28000) {
            onlyHorizontal = true
            scheduleTask((disableLength.value - 1).toInt()) { onlyHorizontal = false }
        }
    }


    @SubscribeEvent
    fun onS08(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook) return
        movementOn = false
        dir = null
        movementList = mutableListOf()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase !== TickEvent.Phase.START) return
        if (dir == null) {
            return
        }

        if (mc.thePlayer?.onGround == true) {
            airTicks = 0
        } else {
            airTicks += 1
        }

        val speed = if (!mc.thePlayer.isSneaking) {
            mc.thePlayer.capabilities.walkSpeed
        } else {
            mc.thePlayer.capabilities.walkSpeed * 3 / 10 /// doesnt let me do 0.3???
        }

        val radians = dir!! * Math.PI / 180 // todo: MathUtils?
        val x = -sin(radians) * speed * 2.806
        val z = cos(radians) * speed * 2.806

        if (airTicks < 2) {
            mc.thePlayer.motionX = x
            mc.thePlayer.motionZ = z
            lastX = x
            lastZ = z
        } else {
            //assume max acceleration
            mc.thePlayer.motionX = lastX * 0.91 + 0.0512 * speed * -sin(radians)
            mc.thePlayer.motionZ = lastZ * 0.91 + 0.0512 * speed * cos(radians)
            lastX = lastX * 0.91 + 0.0512 * speed * -sin(radians)
            lastZ = lastZ * 0.91 + 0.0512 * speed * cos(radians)
        }
    }

    @SubscribeEvent
    fun stupid(event: InputEvent.KeyInputEvent) {
        var keycode = Keyboard.getEventKey()
        if (keycode == mc.gameSettings.keyBindBack.keyCode) {
            dir = null
        }
    }
}
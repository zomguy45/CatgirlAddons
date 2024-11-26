package catgirlroutes.events.impl

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.Entity
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

open class ClickEvent : Event() { // todo: clean this bad boy
    @Cancelable
    class LeftClickEvent : ClickEvent()

    @Cancelable
    class RightClickEvent : ClickEvent()

    @Cancelable
    class MiddleClickEvent : ClickEvent()
}

open class GuiContainerEvent(val container: Container, val gui: GuiContainer) : Event() {
    @Cancelable
    class DrawSlotEvent(container: Container, gui: GuiContainer, var slot: Slot) :
        GuiContainerEvent(container, gui)

    @Cancelable
    class SlotClickEvent(container: Container, gui: GuiContainer, var slot: Slot?, var slotId: Int) :
        GuiContainerEvent(container, gui)
}

open class MovementUpdateEvent : Event() {
    @Cancelable
    class Pre : MovementUpdateEvent()

    @Cancelable
    class Post : MovementUpdateEvent()
}

open class MotionUpdateEvent(
    @JvmField var x: Double,
    @JvmField var y: Double,
    @JvmField var z: Double,
    @JvmField var motionX: Double,
    @JvmField var motionY: Double,
    @JvmField var motionZ: Double,
    @JvmField var yaw: Float,
    @JvmField var pitch: Float,
    @JvmField var onGround: Boolean
) : Event() {

    class PreMotionUpdateEvent(
        x: Double,
        y: Double,
        z: Double,
        motionX: Double,
        motionY: Double,
        motionZ: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ) : MotionUpdateEvent(x, y, z, motionX, motionY, motionZ, yaw, pitch, onGround)

    class PostMotionUpdateEvent(
        x: Double,
        y: Double,
        z: Double,
        motionX: Double,
        motionY: Double,
        motionZ: Double,
        yaw: Float,
        pitch: Float,
        onGround: Boolean
    ) : MotionUpdateEvent(x, y, z, motionX, motionY, motionZ, yaw, pitch, onGround)
}

class EntityRemovedEvent(val entity: Entity) : Event()

class DungeonSecretEvent : Event()

class ReceiveChatPacketEvent(val packet: S02PacketChat) : Event()

@Cancelable
class TeleportEventPre(val packet: S08PacketPlayerPosLook) : Event()

@Cancelable
class PacketSentEvent(val packet: Packet<*>) : Event()

@Cancelable
class PacketReceiveEvent(val packet: Packet<*>) : Event()

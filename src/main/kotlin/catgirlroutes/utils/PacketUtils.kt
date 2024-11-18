package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraft.network.Packet

object PacketUtils {

    fun sendPacket(packet: Packet<*>?) {
        mc.netHandler.networkManager.sendPacket(packet)
    }
}
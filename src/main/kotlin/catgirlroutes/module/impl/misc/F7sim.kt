package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager
import catgirlroutes.utils.Utils.equalsOneOf
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object F7sim : Module(
    "F7 sim",
    category = Category.MISC,
    description = "Simulates 500 speed and lava bounce on single player."
){
    var coolDown = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!LocationManager.currentArea.isArea(Island.SinglePlayer) || event.phase != TickEvent.Phase.START) return

        mc.thePlayer.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).baseValue = 0.50000000745
        mc.thePlayer.capabilities?.setPlayerWalkSpeed(0.5f)

        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
        val block = mc.theWorld.getBlockState(playerPos).block
        if (mc.thePlayer.isInLava && !coolDown) {
            coolDown = true
            modMessage("Lavaing")
            scheduleTask(20) {coolDown = false}
            mc.thePlayer.setVelocity(mc.thePlayer.motionX, 3.5, mc.thePlayer.motionZ)
        } else if (block.equalsOneOf(Blocks.rail) && !coolDown) {
            coolDown = true
            modMessage("Railing")
            scheduleTask(20) {coolDown = false}
            mc.thePlayer.setVelocity(mc.thePlayer.motionX, 7.0, mc.thePlayer.motionZ)
        }
        //else if (block.equalsOneOf(Blocks.stone_slab, Blocks.stone_slab2, Blocks.wooden_slab)) mc.thePlayer.setVelocity(mc.thePlayer.motionX, 7.0, mc.thePlayer.motionZ)
    }
}
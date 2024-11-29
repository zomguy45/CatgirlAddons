package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.render.WorldRenderUtils.draw2DBoxByEntity
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object DungeonESP: Module(
    "Dungeon ESP",
    category = Category.RENDER,
    description = "Esp for anything in dungeons."
){
    private val boxWidth = NumberSetting("Box Width",0.9,0.1,2.0,0.05, "Width of the esp box in units of blocks.")
    private val defaultLineWidth = NumberSetting("Default LW",1.0,0.1,10.0,0.1, "Default line width of the esp box.")
    private val specialLineWidth = NumberSetting("Special Mob LW",2.0,0.1,10.0,0.1, "Line width of the esp box for special mobs like Fel and Withermancer.")
    private val miniLineWidth = NumberSetting("Mini Boss LW",3.0,0.1,10.0,0.1, "Line width of the esp box for Mini Bosses.")

    private val espStyle = StringSelectorSetting("Esp style","3D", arrayListOf("3D", "2D"), "Esp render style to be used.")

    private val showStarMobs = BooleanSetting("Star Mobs", false, "Render star mob ESP.")
    private val showFelHead = BooleanSetting("Fel Head", false,"Render a box around Fel heads. This box can not be seen through walls.")
    private val showBat = BooleanSetting("Bat ESP", false, "Render the bat ESP")
    private val showKey = BooleanSetting("Key ESP", false, "Render key ESP")

    private val colorStar = ColorSetting("Star Mob Color", Color(255, 0, 0), false, "ESP color for star mobs.").withDependency { showStarMobs.enabled }
    private val colorMini = ColorSetting("Mini Boss Color", Color(255, 255, 0), false, "ESP color for all Mini Bosses except Shadow Assassins.").withDependency { showStarMobs.enabled }
    private val colorShadowAssassin = ColorSetting("SA Color", Color(255, 0, 255), false, "ESP color for Shadow Assassins.").withDependency { showStarMobs.enabled }
    private val colorFel = ColorSetting("Fel Color", Color(0, 255, 255), false, "ESP color for star Fel.").withDependency { showStarMobs.enabled }
    private val colorWithermancer = ColorSetting("Withermancer Color", Color(255, 255, 0), false, "ESP color for star Withermancer.").withDependency { showStarMobs.enabled }

    private val colorFelHead = ColorSetting("Fel Head Color", Color(0, 0, 255), false, "ESP color for Fel heads on the floor.").withDependency { showFelHead.enabled }
    private val colorBat = ColorSetting("Bat Color", Color(0, 255, 0), false, "ESP color for bats.").withDependency { showBat.enabled }
    private val colorKey = ColorSetting("Key Color", Color(0, 0, 0), false, "ESP color for wither and blood key.").withDependency { showKey.enabled }

    init {
        this.addSettings(
            boxWidth,
            defaultLineWidth,
            specialLineWidth,
            miniLineWidth,

            espStyle,

            showStarMobs,
            showFelHead,
            showBat,
            showKey,

            colorStar,
            colorMini,
            colorShadowAssassin,
            colorFel,
            colorWithermancer,

            colorFelHead,
            colorBat,
            colorKey,
        )
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!this.enabled || !inDungeons) return
        mc.theWorld.loadedEntityList.stream()
            .forEach { entity ->
                val entityName = entity.customNameTag?.let { StringUtils.stripControlCodes(it) } ?: return@forEach
                when (entity) {
                    is EntityArmorStand -> handleArmorStand(entity, entityName, event.partialTicks)
                    is EntityEnderman -> handleEnderman(entity, event.partialTicks)
                    is EntityOtherPlayerMP -> handleOtherPlayer(entity, event.partialTicks)
                    is EntityBat -> handleBat(entity, event.partialTicks)
                }
            }
    }

    private fun handleArmorStand(entity: EntityArmorStand, entityName: String, partialTicks: Float) {
        if (showStarMobs.enabled && entityName.contains("✯") && !isExcludedMob(entityName)) {
            val correspondingMob = getCorrespondingMob(entity) ?: return
            drawMobBox(correspondingMob, entityName, partialTicks)
        } else if (showKey.enabled && (entityName == "Wither Key" || entityName == "Blood Key")) {
            if (espStyle.selected == "2D") {
                draw2DBoxByEntity(entity, colorKey.value, boxWidth.value, 1.0, partialTicks, miniLineWidth.value, true)
            } else {
                drawBoxByEntity(entity, colorKey.value, boxWidth.value, 1.0, partialTicks, miniLineWidth.value, true, 0.0, 1.0, 0.0)
            }
        }
    }

    private fun handleEnderman(entity: EntityEnderman, partialTicks: Float) {
        if (showFelHead.enabled) {
            if (espStyle.selected == "2D") {
                draw2DBoxByEntity(entity, colorFelHead.value, boxWidth.value, 1.0, partialTicks, specialLineWidth.value, false)
            } else {
                drawBoxByEntity(entity, colorFelHead.value, boxWidth.value, 1.0, partialTicks, specialLineWidth.value, false)
            }
        }
    }

    private fun handleOtherPlayer(entity: EntityOtherPlayerMP, partialTicks: Float) {
        if (entity.name.contains("Shadow Assassin")) {
            if (espStyle.selected == "2D") {
                draw2DBoxByEntity(entity, colorKey.value, boxWidth.value, 2.0, partialTicks, miniLineWidth.value, true)
            } else {
                drawBoxByEntity(entity, colorShadowAssassin.value, boxWidth.value, 2.0, partialTicks, miniLineWidth.value, true)
            }
        } else if (entity.name == "Diamond Guy" || entity.name == "Lost Adventurer") {
            if (espStyle.selected == "2D") {
                draw2DBoxByEntity(entity, colorKey.value, boxWidth.value, 2.0, partialTicks, miniLineWidth.value, true)
            } else {
                drawBoxByEntity(entity, colorMini.value, boxWidth.value, 2.0, partialTicks, miniLineWidth.value, true)
            }
        }
    }

    private fun handleBat(entity: EntityBat, partialTicks: Float) {
        if (showBat.enabled && !entity.isInvisible) {
            if (espStyle.selected == "2D") {
                //modMessage("2D")
                draw2DBoxByEntity(entity, colorKey.value, entity.width.toDouble(), entity.height.toDouble(), partialTicks, defaultLineWidth.value, true)
            } else {
                drawBoxByEntity(entity, colorBat.value, entity.width, entity.height, partialTicks, defaultLineWidth.value.toFloat(), true)
            }
        }
    }

    private fun isExcludedMob(entityName: String): Boolean {
        return entityName.contains("Angry Archeologist") ||
                entityName.contains("Frozen Adventurer") ||
                entityName.contains("Lost Adventurer")
    }

    private fun drawMobBox(mob: Entity, entityName: String, partialTicks: Float) {
        val color = when {
            entityName.contains("Fel") -> colorFel.value
            entityName.contains("Withermancer") -> colorWithermancer.value
            else -> colorStar.value
        }
        val lineWidth = when {
            entityName.contains("Fel") || entityName.contains("Withermancer") -> specialLineWidth.value
            else -> defaultLineWidth.value
        }
        val size = when {
            entityName.contains("Fel") -> 3.0
            entityName.contains("Withermancer") -> 2.4
            else -> 2.0
        }
        if (espStyle.selected == "2D") {
            draw2DBoxByEntity(mob, color, boxWidth.value, size, partialTicks, lineWidth, true)
        } else {
            drawBoxByEntity(mob, color, boxWidth.value, size, partialTicks, lineWidth, true)
        }
    }

    private fun getCorrespondingMob(entity: Entity): Entity? {
        val possibleEntities = entity.entityWorld.getEntitiesInAABBexcluding(
            entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0)
        ) { it !is EntityArmorStand }

        return possibleEntities.find {
            when (it) {
                is EntityPlayer -> !it.isInvisible() && it.getUniqueID()
                    .version() == 2 && it != mc.thePlayer
                is EntityWither -> false
                else -> true
            }
        }
    }
}
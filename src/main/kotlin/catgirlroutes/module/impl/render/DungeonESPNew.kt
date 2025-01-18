package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RenderEntityModelEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.utils.clock.Executor
import catgirlroutes.utils.clock.Executor.Companion.register
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.render.OutlineUtils.outlineESP
import catgirlroutes.utils.render.WorldRenderUtils.draw2DBoxByEntity
import catgirlroutes.utils.render.WorldRenderUtils.drawEntityBox
import me.odinmain.utils.isOtherPlayer
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.passive.EntityBat
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object DungeonESPNew: Module(
    "Dungeon ESP 2",
    Category.RENDER
) {

    private val espStyle = StringSelectorSetting("Esp style","3D", arrayListOf("3D", "2D", "Outline", "Trans"), "Esp render style to be used.")
    private val espFill = BooleanSetting("Esp fill", false).withDependency { espStyle.selected == "3D"}
    private val lineWidth = NumberSetting("Line width", 4.0, 0.0, 8.0, 1.0)

    private val colorStar = ColorSetting("Star Mob Color", Color(255, 0, 0), true, "ESP color for star mobs.")
    private val colorStarFill = ColorSetting("Star Fill Mob Color", Color(255, 0, 0), true, "ESP color for star mobs.").withDependency { espFill.value }
    private val colorMini = ColorSetting("Mini Boss Color", Color(255, 0, 0), true, "ESP color for mini bosses.")
    private val colorMiniFill = ColorSetting("Mini Fill Boss Color", Color(255, 0, 0), true, "ESP color for mini bosses.").withDependency { espFill.value }
    private val colorSA = ColorSetting("Shadow Assassin Color", Color(255, 0, 0), true, "ESP color for shadow assassins.")
    private val colorSAFill = ColorSetting("Shadow Fill Assassin Color", Color(255, 0, 0), true, "ESP color for shadow assassins.").withDependency { espFill.value }
    private val colorBat = ColorSetting("Bat Color", Color(255, 0, 0), true, "ESP color for bats.")
    private val colorBatFill = ColorSetting("Bat Fill Color", Color(255, 0, 0), true, "ESP color for bats.").withDependency { espFill.value }
    private val colorKey = ColorSetting("Key Color", Color(255, 0, 0), true, "ESP color for keys.")
    private val colorKeyFill = ColorSetting("Key Fill Color", Color(255, 0, 0), true, "ESP color for keys.").withDependency { espFill.value }

    private var currentEntities = mutableSetOf<ESPEntity>()

    init {
        Executor(500) {
            if (!inDungeons || !this.enabled) return@Executor
            currentEntities.clear()
            getEntities()
        }.register()

        addSettings(
            espStyle,
            espFill,
            lineWidth,

            colorStar,
            colorStarFill,
            colorMini,
            colorMiniFill,
            colorSA,
            colorSAFill,
            colorBat,
            colorBatFill,
            colorKey,
            colorKeyFill,
        )
    }

    data class ESPEntity (
        val entity: Entity,
        val color: Color,
        val fillcolor: Color
    )

    val transColors = mutableListOf(Color.CYAN.withAlpha(90), Color.PINK.withAlpha(90), Color.WHITE.withAlpha(90), Color.PINK.withAlpha(90), Color.CYAN.withAlpha(90))

    //fun drawEntityBox(entity: Entity, colors: List<Color>, outline: Boolean, fill: Boolean, partialTicks: Float, lineWidth: Float) {

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!inDungeons || !this.enabled) return
        currentEntities.forEach{espEntity ->
            if (espStyle.selected == "Outline" && espEntity.color != colorKey.value) return@forEach
            when (espStyle.selected) {
                "2D" -> draw2DBoxByEntity(espEntity.entity, espEntity.color, event.partialTicks, lineWidth.value.toFloat(), true)
                "3D" -> drawEntityBox(espEntity.entity, espEntity.color, espEntity.fillcolor, true, espFill.value, event.partialTicks, lineWidth.value.toFloat())
                "Trans" -> return//drawEntityBox(espEntity.entity, espEntity.color, transColors.toList(), true, fill = true, event.partialTicks, lineWidth.value.toFloat())
            }
        }
    }

    @SubscribeEvent
    fun onRenderModel(event: RenderEntityModelEvent) {
        if (!inDungeons || !this.enabled || espStyle.selected != "Outline") return
        currentEntities.forEach{espEntity ->
            if (event.entity != espEntity.entity) return@forEach
            outlineESP(event, lineWidth.value.toFloat(), espEntity.color, true)
        }
    }

    private fun getEntities() {
        mc.theWorld.loadedEntityList.stream().forEach {entity ->
            when (entity) {
                is EntityArmorStand -> handleStands(entity)
                is EntityOtherPlayerMP -> handlePlayer(entity)
                is EntityBat -> handleBat(entity)
            }
        }
    }

    private fun handleStands(entity: Entity) {
        val entityName = entity.customNameTag?.let { StringUtils.stripControlCodes(it) } ?: return
        if (entity.name.startsWith("§6✯ ") && entity.name.endsWith("§c❤")) {
            val correspondingEntity = getMobEntity(entity) ?: return
            currentEntities.add(ESPEntity(correspondingEntity, colorStar.value, colorStarFill.value))
        }
        if ((entityName == "Wither Key" || entityName == "Blood Key")) {
            currentEntities.add(ESPEntity(entity, colorKey.value, colorKeyFill.value))
        }
    }

    private fun handlePlayer(entity: Entity) {
        if (entity.name.contains("Shadow Assassin")) {
            currentEntities.add(ESPEntity(entity, colorSA.value, colorSAFill.value))
        } else if (entity.name == "Diamond Guy" || entity.name == "Lost Adventurer") {
            currentEntities.add(ESPEntity(entity, colorMini.value, colorMiniFill.value))
        }
    }

    private fun handleBat(entity: Entity) {
        currentEntities.add(ESPEntity(entity, colorBat.value, colorBatFill.value))
    }

    private fun getMobEntity(entity: Entity): Entity? {
        return mc.theWorld?.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.offset(0.0, -1.0, 0.0))
            ?.filter { it !is EntityArmorStand && mc.thePlayer != it && !(it is EntityWither && it.isInvisible) && !(it is EntityOtherPlayerMP && it.isOtherPlayer()) }
            ?.minByOrNull { entity.getDistanceToEntity(it) }
    }
}
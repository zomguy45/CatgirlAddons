package catgirlroutes.module.impl.render

import catgirlroutes.events.impl.ScoreboardRenderEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil.drawString
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickgui.util.FontUtil.getWidth
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedOutline
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object CustomScoreboard : Module(
    "Custom Scoreboard",
    Category.RENDER
) {
    private val footerText by StringSetting("Footer text", "§dCGA", 0)
    private val hideLobby by BooleanSetting("Hide lobby", "Hides lobby ID.")
    private val background by BooleanSetting("Background", "Draws scoreboard background.")
    private val backgroundColour by ColorSetting("Background colour", Color(21, 21, 21, 100), true).withDependency { background }
    private val outline by BooleanSetting("Outline", "Draws outline.")
    private val outlineColour by ColorSetting("Outline colour", ColorUtil.clickGUIColor, true).withDependency { outline }
    private val outlineThickness by NumberSetting("Outline thickness", 2.5, 0.5, 15.0, 0.5, "Changes outline thickness.").withDependency { outline }
    private val cornerRadius by NumberSetting("Corner radius", 3.0, 0.0, 15.0, 0.5, "Changes background and outline corner radius.").withDependency { background || outline }

    @SubscribeEvent
    fun onScoreboardRender(event: ScoreboardRenderEvent) {
        event.isCanceled = true

        GlStateManager.pushMatrix()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()

        val scoreboard = event.objective.scoreboard
        val collection = scoreboard.getSortedScores(event.objective)
        val list = collection.filter { score ->
            score.playerName != null && !score.playerName.startsWith("#")
        }

        val filteredScores = if (list.size > 15) list.takeLast(15) else list

        var stringWidth = event.objective.displayName.getWidth()

        for (score in filteredScores) {
            val scorePlayerTeam = scoreboard.getPlayersTeam(score.playerName)
            val s = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.playerName) +
                    ": " + EnumChatFormatting.RED + score.scorePoints
            stringWidth = maxOf(stringWidth, s.getWidth())
        }

        val linesHeight = filteredScores.size * fontHeight
        val x = event.resolution.scaledWidth - stringWidth - 3.0
        val y = event.resolution.scaledHeight / 2.0 + linesHeight / 3.0
        val w = event.resolution.scaledWidth - 5.0 + 2.0

        val bgX = x - 2.0
        val bgY = y - filteredScores.size * fontHeight - fontHeight - 4.0
        val bgW = w - (bgX - 2.0)
        val bgH = fontHeight * (filteredScores.size + 1) + 4.0


        if (background) drawRoundedRect(bgX, bgY, bgW, bgH, cornerRadius, backgroundColour)
        if (outline) drawRoundedOutline(bgX, bgY, bgW, bgH, cornerRadius, outlineThickness, outlineColour)


        collection.forEachIndexed { i, score2 ->
            val scorePlayerTeam2 = scoreboard.getPlayersTeam(score2.playerName)
            var s2 = ScorePlayerTeam.formatPlayerName(scorePlayerTeam2, score2.playerName)
                .replace("§ewww.hypixel.ne\ud83c\udf82§et", footerText)

            val y2 = bgH + bgY - (i + 1) * fontHeight

            val matcher = Regex("\\d\\d/\\d\\d/\\d\\d").find(s2)
            if (hideLobby && matcher != null) {
                s2 = "§7${matcher.value}"
            }

            val isFooter = s2 == footerText
            val xPos = if (isFooter) {
                x + stringWidth / 2.0 - s2.getWidth() / 2.0
            } else {
                x
            }
            val color = if (isFooter) {
                ColorUtil.textcolor
            } else {
                553648127
            }

            drawString(s2, xPos, y2, color)

            if (i == collection.size - 1) {
                val s3 = event.objective.displayName
                drawString(
                    s3,
                    x + stringWidth / 2.0 - s3.getWidth() / 2.0,
                    y2 - fontHeight,
                )
            }
        }

        GlStateManager.enableBlend()
        GlStateManager.popMatrix()
    }
}
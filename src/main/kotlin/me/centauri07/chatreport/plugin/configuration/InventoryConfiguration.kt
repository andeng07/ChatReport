package me.centauri07.chatreport.plugin.configuration

import com.charleskorn.kaml.YamlComment
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import kotlinx.serialization.Serializable
import me.centauri07.chatreport.discord.ChatReportBot
import me.centauri07.chatreport.plugin.chat.ChatHistory
import me.centauri07.chatreport.util.extensions.applyPlaceholders
import me.centauri07.chatreport.util.extensions.component
import org.bukkit.Material
import org.bukkit.entity.Player
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

@Serializable
class InventoryConfiguration(
    val title: String,
    @YamlComment("the layout of the inventory, every line is equivalent to a row.")
    val layout: List<String>,
    @YamlComment("the list of items pre-defined that can be used for the layout.")
    val items: List<Item>
) {

    fun inventory(executor: Player, reportedPlayer: Player, chatHistory: ChatHistory): PaginatedGui {

        if (layout.any { it.length != 9 } || layout.isEmpty()) throw IllegalArgumentException("Invalid inventory format.")

        val placeholders =
            mutableMapOf(
                "%executor_name" to executor.name,
                "%executor_uuid" to executor.uniqueId.toString(),
                "%target_name%" to reportedPlayer.name,
                "%target_uuid%" to reportedPlayer.uniqueId.toString()
            )

        val gui = Gui.paginated()
            .rows(layout.size)
            .title(title.applyPlaceholders(placeholders).component())
            .disableAllInteractions()
            .create()

        val layoutJoined = layout.joinToString("")

        for (charIndex in layout.joinToString("").indices) {
            val char = layoutJoined[charIndex]

            if (char == '*') continue

            val item = items.firstOrNull { it.identifier == char } ?: continue

            gui.setItem(charIndex, item.item(placeholders).also {
                it.setAction {
                    when (item.action) {
                        "next" -> gui.next()
                        "previous" -> gui.previous()
                    }
                }
            })
        }

        chatHistory.chats.forEachIndexed { index, chat ->
            run {

                val chatHistoryItem = items.firstOrNull { item -> item.identifier == '*' }
                    ?: throw NullPointerException("Chat history item configuration not found. (identifier: \'*\')")

                placeholders["%index%"] = (index + 1).toString()
                placeholders["%content%"] = chat.content
                placeholders["%date%"] = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date.from(chat.date))
                placeholders["%is_reported%"] = chat.isReported.toString()

                gui.addItem(chatHistoryItem.item(placeholders).also { guiItem ->
                    guiItem.setAction { _ ->
                        run {
                            if (!chat.isReported) {
                                if (ChatReportBot.report(executor, reportedPlayer, chat)) {
                                    executor.sendMessage("<green>Message has been successfully reported.".component())
                                } else {
                                    executor.sendMessage("<red>Fail to report message.".component())
                                }
                            } else {
                                executor.sendMessage("<red>Message has been already reported.".component())
                            }


                            gui.close(executor)
                        }
                    }
                })
            }
        }

        return gui
    }

}

@Serializable
data class Item(
    val identifier: Char,
    val item: Material,
    val displayName: String,
    val lore: List<String>,
    val action: String?
) {
    fun item(placeholder: Map<String, String>): GuiItem = ItemBuilder.from(item)
        .name(displayName.applyPlaceholders(placeholder).component())
        .lore(lore.map { it.applyPlaceholders(placeholder).component() })
        .asGuiItem()

}
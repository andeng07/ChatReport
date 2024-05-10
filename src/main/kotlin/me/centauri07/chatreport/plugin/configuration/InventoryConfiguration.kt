package me.centauri07.chatreport.plugin.configuration

import com.charleskorn.kaml.YamlComment
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import kotlinx.serialization.Serializable
import me.centauri07.chatreport.plugin.chat.ChatHistory
import me.centauri07.chatreport.util.extensions.applyPlaceholders
import me.centauri07.chatreport.util.extensions.component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
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

    fun inventory(executor: Player, chatHistory: ChatHistory): PaginatedGui {

        val player: OfflinePlayer = Bukkit.getOfflinePlayer(chatHistory.uuid)

        if (layout.any { it.length != 9 }) throw IllegalArgumentException("Invalid inventory format.")

        val gui = Gui.paginated()
            .rows(layout.size)
            .title(title.applyPlaceholders(mapOf("%player%" to player.name!!)).component())
            .disableAllInteractions()
            .create()

        val layoutJoined = layout.joinToString("")

        val placeholders = mutableMapOf("%target_name%" to player.name!!, "%target_uuid%" to player.uniqueId.toString())

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

        chatHistory.chats.forEach {

            val chatHistoryItem = items.firstOrNull { item -> item.identifier == '*' }
                ?: throw NullPointerException("Chat history item configuration not found. (identifier: \'*\')")

            placeholders["%content%"] = it.content
            placeholders["%date%"] = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date.from(it.date))
            placeholders["%is_reported%"] = it.isReported.toString()

            gui.addItem(chatHistoryItem.item(placeholders))

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
package me.centauri07.chatreport.plugin

import io.papermc.paper.event.player.AsyncChatEvent
import me.centauri07.chatreport.discord.ChatReportBot
import me.centauri07.chatreport.plugin.chat.Chat
import me.centauri07.chatreport.plugin.chat.ChatHistory
import me.centauri07.chatreport.plugin.chat.ChatHistoryRepository
import me.centauri07.chatreport.plugin.configuration.InventoryConfiguration
import me.centauri07.chatreport.plugin.configuration.Item
import me.centauri07.chatreport.plugin.configuration.PluginConfiguration
import me.centauri07.chatreport.util.extensions.component
import me.centauri07.chatreport.util.extensions.toPlainText
import me.centauri07.chatreport.util.serializer.YamlFileSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.time.Instant
import java.util.*

class ChatReportPlugin : JavaPlugin(), Listener {

    private val pluginConfiguration: PluginConfiguration = YamlFileSerializer(
        PluginConfiguration::class.java,
        PluginConfiguration(
            "mongodb://localhost:27017/",
            10,
            InventoryConfiguration(
                "<dark_green>%target_name%'s Chat History",
                listOf(
                    "#########",
                    "#       #",
                    "#       #",
                    "#       #",
                    "#       #",
                    "##<###>##"
                ),
                listOf(
                    Item(
                        '*', Material.PAPER, "<gold>Message #%index%", listOf(
                            "<gray>Content: <gold>%content%",
                            "<gray>Date: <gold>%date%",
                            "<gray>Is reported: <gold>%is_reported%",
                            "",
                            "<green>Click to report!"
                        ), null
                    ),
                    Item(
                        '#', Material.LIME_STAINED_GLASS_PANE, "", listOf(), null
                    ),
                    Item(
                        '>', Material.ARROW, "<gold>Next page", listOf("", "<green>Click to next page!"), "next"
                    ),
                    Item(
                        '<', Material.ARROW, "<gold>Previous page", listOf("", "<green>Click to previous page!"), "previous"
                    )
                )
            )
        ),
        File(dataFolder, "config.yml")
    ).value

    override fun onEnable() {
        ChatReportBot.dataFolder = dataFolder

        ChatHistoryRepository.repositoryFolder = dataFolder

        Bukkit.getPluginManager().registerEvents(this, this)

        getCommand("chatreport")?.setExecutor(this)

        ChatReportBot.enable()
    }

    @EventHandler
    fun on(e: AsyncChatEvent) {

        Bukkit.getScheduler().runTaskAsynchronously(
            this, Runnable {
                val chatHistory = ChatHistoryRepository.find(e.player.uniqueId) ?: ChatHistory(
                    e.player.uniqueId,
                    mutableListOf()
                ).also {
                    ChatHistoryRepository.insert(e.player.uniqueId, it)
                }

                chatHistory.addChat(
                    pluginConfiguration.chatHistoryLength,
                    Chat(e.message().toPlainText(), Instant.now(), false)
                )

                ChatHistoryRepository.save(e.player.uniqueId)
            })

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender !is Player) {
            sender.sendMessage("<red>Only players can execute this command.".component())
            return true
        }

        if (args.size != 1) {
            sender.sendMessage("<red>Usage: /chatreport <player>".component())
            return true
        }

        val player =
            Bukkit.getOfflinePlayer(args[0]).player ?: Bukkit.getOfflinePlayer(UUID.fromString(args[0])).player ?: run {
                sender.sendMessage("<red>Unable to find player. (${args[0]})")

                return true
            }

        if (player.hasPermission("chatreport.exempt")) {
            sender.sendMessage("<red>You can't view ${player.name}'s chat history.")
            return false
        }

        ChatHistoryRepository.find(player.uniqueId)
            ?.let { pluginConfiguration.inventoryConfiguration.inventory(sender, player, it).open(player) }
            ?: sender.sendMessage("<red>Player doesn't have a chat history.")


        return true
    }

}
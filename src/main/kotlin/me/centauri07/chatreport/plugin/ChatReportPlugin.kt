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
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.time.Instant

class ChatReportPlugin : JavaPlugin(), Listener {


    companion object {
        lateinit var instance: JavaPlugin
            private set
    }

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
        File(dataFolder, "plugin-config.yml")
    ).value

    override fun onEnable() {
        instance = this

        ChatReportBot.dataFolder = dataFolder

        ChatReportBot.enable()

        ChatHistoryRepository.repositoryFolder = File(dataFolder, "chat-history")

        Bukkit.getPluginManager().registerEvents(this, this)

        getCommand("chatreport")?.setExecutor(this)
    }

    @EventHandler
    fun on(e: AsyncChatEvent) {
        val start = System.currentTimeMillis()

        val chatHistory = ChatHistoryRepository.find(e.player.uniqueId) ?: ChatHistory(
            mutableListOf()
        ).also {
            ChatHistoryRepository.insert(e.player.uniqueId, it)
        }

        chatHistory.addChat(
            pluginConfiguration.chatHistoryLength,
            Chat(e.message().toPlainText(), Instant.now().toEpochMilli(), false)
        )

        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            ChatHistoryRepository.save(e.player.uniqueId)
        })

        println("Time elapsed: ${System.currentTimeMillis() - start}")
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        ChatHistoryRepository.invalidateCache(e.player.uniqueId)
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
            Bukkit.getOfflinePlayer(args[0])

        ChatHistoryRepository.find(player.uniqueId)
            ?.let { pluginConfiguration.inventoryConfiguration.inventory(sender, player, it).open(sender) }
            ?: sender.sendMessage("<red>Player doesn't have a chat history.")


        return true
    }

}
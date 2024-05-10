package me.centauri07.chatreport.plugin

import io.papermc.paper.event.player.AsyncChatEvent
import me.centauri07.chatreport.discord.ChatReportBot
import me.centauri07.chatreport.plugin.chat.Chat
import me.centauri07.chatreport.plugin.chat.ChatHistoryRepository
import me.centauri07.chatreport.plugin.configuration.InventoryConfiguration
import me.centauri07.chatreport.plugin.configuration.PluginConfiguration
import me.centauri07.chatreport.util.extensions.component
import me.centauri07.chatreport.util.extensions.toPlainText
import me.centauri07.chatreport.util.serializer.YamlFileSerializer
import org.bukkit.Bukkit
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
        PluginConfiguration("", 10, InventoryConfiguration("", listOf(), listOf())),
        File(dataFolder, "config.yml")
    ).value

    private val chatReportBot: ChatReportBot = ChatReportBot(this)
    private val chatReportRepository: ChatHistoryRepository = ChatHistoryRepository(File(dataFolder, "chat-histories"))

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun on(e: AsyncChatEvent) {

        Bukkit.getScheduler().runTaskAsynchronously(
            this, Runnable {
                chatReportRepository.find(e.player.uniqueId)
                    ?.addChat(
                        pluginConfiguration.chatHistoryLength,
                        Chat(e.message().toPlainText(), Instant.now(), false)
                    )

                chatReportRepository.save(e.player.uniqueId)
            })

        getCommand("chatreport")?.setExecutor(this)

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

        val player = Bukkit.getOfflinePlayer(args[0]).player ?: Bukkit.getOfflinePlayer(UUID.fromString(args[0])).player ?: run {
            sender.sendMessage("<red>Unable to find player. (${args[0]})")

            return true
        }

        chatReportRepository.find(player.uniqueId)
            ?.let { pluginConfiguration.inventoryConfiguration.inventory(sender, it) } ?: sender.sendMessage("<red>Player doesn't have a chat history.")


        return true
    }

}
package me.centauri07.chatreport.discord

import me.centauri07.chatreport.discord.configuration.DiscordConfiguration
import me.centauri07.chatreport.plugin.chat.ChatHistory
import me.centauri07.chatreport.util.extensions.applyPlaceholders
import me.centauri07.chatreport.util.extensions.isValidDuration
import me.centauri07.chatreport.util.serializer.YamlFileSerializer
import me.centauri07.jarbapi.BotApplication
import me.centauri07.jarbapi.component.callback
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.GatewayIntent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class ChatReportBot(private val plugin: JavaPlugin) : BotApplication() {

    private lateinit var discordConfiguration: DiscordConfiguration

    override fun onLoad() {
        dataFolder = plugin.dataFolder

        discordConfiguration = YamlFileSerializer(
            DiscordConfiguration::class.java,
            DiscordConfiguration(),
            File(dataFolder, "configuration.yml")
        ).value

        setToken(discordConfiguration.token)
        addIntent(*GatewayIntent.entries.toTypedArray())

        Bukkit.getLogger().info("Loading ChatReportBot...")
    }

    override fun onEnable() {
        Bukkit.getLogger().info("ChatReportBot has been enabled.")

        registerListener(this)
    }

    fun report(reporter: Player, chatHistory: ChatHistory, date: Instant): Boolean {

        val channel = jda.getTextChannelById(discordConfiguration.chatReportChannel) ?: throw NullPointerException("Report channel not found.")

        val chat = chatHistory.chats.firstOrNull { it.date == date } ?: return false

        channel.sendMessageEmbeds(
            discordConfiguration.chatReportMessageEmbed.toDiscordEmbed(
                mapOf(
                    "%reporter_name%" to reporter.name,
                    "%reporter_uuid%" to reporter.uniqueId.toString(),
                    "%content%" to chat.content,
                    "%date%" to SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date.from(chat.date)),
                )
            ).build()
        )

        return true

    }

    @SubscribeEvent
    fun event(e: ButtonInteractionEvent) {

        if (e.button.id?.startsWith("cbr-") == false) return

        val uuid = UUID.fromString(e.button.id!!.drop(4))

        val player = Bukkit.getOfflinePlayer(uuid).player ?: run {
            e.reply("Player does not exist.").setEphemeral(true).queue()
            return
        }

        e.replyModal(
            Modal.create(
                UUID.randomUUID().toString(),
                "Mute ${player.name}"
            ).addActionRow(
                TextInput.create(
                    "reason",
                    "Mute reason. (e.g. \"Cursing\", \"Spamming\", \"Advertising\")",
                    TextInputStyle.SHORT
                ).build()
            )
                .addActionRow(
                    TextInput.create(
                        "duration",
                        "Duration. (e.g. \"60s\", \"30m\", \"12h\", \"30d\"",
                        TextInputStyle.SHORT
                    ).build()
                )
                .build().callback {

                    val reason = it.getValue("reason")
                    val duration = it.getValue("duration")

                    if (reason == null || duration == null || !duration.asString.isValidDuration())
                        it.reply("Something went wrong. Please try again.").setEphemeral(true).queue()

                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(), discordConfiguration.muteCommand.applyPlaceholders(
                            mapOf(
                                "%player%" to uuid.toString(),
                                "%duration%" to duration!!.asString,
                                "%reason%" to reason!!.asString
                            )
                        )
                    )

                }
        ).queue()


    }

}
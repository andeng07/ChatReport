package me.centauri07.chatreport.discord

import me.centauri07.chatreport.discord.configuration.Author
import me.centauri07.chatreport.discord.configuration.DiscordConfiguration
import me.centauri07.chatreport.discord.configuration.EmbedMessage
import me.centauri07.chatreport.discord.configuration.Field
import me.centauri07.chatreport.plugin.ChatReportPlugin
import me.centauri07.chatreport.plugin.chat.Chat
import me.centauri07.chatreport.plugin.chat.ChatHistoryRepository
import me.centauri07.chatreport.util.callback.ModalCallback
import me.centauri07.chatreport.util.callback.callback
import me.centauri07.chatreport.util.extensions.applyPlaceholders
import me.centauri07.chatreport.util.extensions.isValidDuration
import me.centauri07.chatreport.util.serializer.YamlFileSerializer
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.GatewayIntent
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object ChatReportBot {
    lateinit var dataFolder: File

    private lateinit var discordConfiguration: DiscordConfiguration

    private lateinit var jda: JDA

    fun enable() {

        discordConfiguration = YamlFileSerializer(
            DiscordConfiguration::class.java,
            DiscordConfiguration(
                "token", 0, EmbedMessage(
                    author = Author("%target_name% has been reported by %executor_name%", null, null),
                    color = "#dbc85a",
                    thumbnail = "https://mc-heads.net/head/%target_uuid%/left",
                    fields = listOf(
                        Field("Content", "%content%"),
                        Field("Date", "%date%"),
                        Field("Reporter", "%executor_name%"),
                        Field("Reportee", "%target_name%", true)
                    )
                ), "mute %player% %duration% %reason%"
            ),
            File(dataFolder, "bot-config.yml")
        ).value

        jda = JDABuilder.createDefault(discordConfiguration.token, GatewayIntent.MESSAGE_CONTENT).build()

        Bukkit.getLogger().info("Loading ChatReportBot...")

        jda.setEventManager(AnnotatedEventManager())

        jda.awaitReady()

        Bukkit.getLogger().info("ChatReportBot has been enabled.")

        jda.addEventListener(this, ModalCallback)

    }

    fun report(executor: Player, target: OfflinePlayer, chat: Chat): Boolean {

        val channel = jda.getTextChannelById(discordConfiguration.chatReportChannel)
            ?: throw NullPointerException("Report channel not found.")

        channel.sendMessageEmbeds(
            discordConfiguration.chatReportMessageEmbed.toDiscordEmbed(
                mapOf(
                    "%executor_name%" to executor.name,
                    "%executor_uuid%" to executor.uniqueId.toString(),
                    "%target_name%" to target.name!!,
                    "%target_uuid%" to target.uniqueId.toString(),
                    "%content%" to chat.content,
                    "%date%" to SimpleDateFormat("MMM dd, yyyy (EEEE) hh:mm a").format(Date.from(Instant.ofEpochMilli(chat.date))),
                )
            ).build()
        ).addActionRow(Button.danger("cbr-${target.uniqueId}", "Mute")).queue()

        chat.isReported = true

        ChatHistoryRepository.save(target.uniqueId)

        return true

    }

    @SubscribeEvent
    fun on(e: ButtonInteractionEvent) {

        if (e.button.id?.startsWith("cbr-") == false) return

        val uuid = UUID.fromString(e.button.id!!.drop(4))

        val player = Bukkit.getOfflinePlayer(uuid)

        e.replyModal(
            Modal.create(
                e.button.id!!.toString(),
                "Mute ${player.name}"
            ).addActionRow(
                TextInput.create(
                    "reason",
                    "Mute reason. (e.g. \"Advertising\")",
                    TextInputStyle.SHORT
                ).build()
            )
                .addActionRow(
                    TextInput.create(
                        "duration",
                        "Duration. (e.g. \"30m\", \"12h\", \"30d\")",
                        TextInputStyle.SHORT
                    ).build()
                )
                .build().callback {

                    val reason = it.getValue("reason")
                    val duration = it.getValue("duration")

                    if (reason == null || duration == null || !duration.asString.isValidDuration())
                        it.reply("Something went wrong. Please try again.").setEphemeral(true).queue()

                    Bukkit.getScheduler().runTask(
                        ChatReportPlugin.instance, Runnable {
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
                    )

                    e.editButton(e.button.asDisabled()).queue()

                    it.reply("Command has been successfully executed.").setEphemeral(true).queue()

                }
        ).queue()
    }

}
package me.centauri07.chatreport.discord.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfiguration(
    val token: String,
    @SerialName("chat-report-channel")
    val chatReportChannel: Long,
    @SerialName("chat-report-message-embed")
    val chatReportMessageEmbed: EmbedMessage,
    @SerialName("mute-command")
    val muteCommand: String
)
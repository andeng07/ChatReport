package me.centauri07.chatreport.discord.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.centauri07.chatreport.util.extensions.applyPlaceholders
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

@Serializable
data class EmbedMessage(
    val author: Author? = null,
    val color: String? = null,
    val title: String? = null,
    val url: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val fields: List<Field> = emptyList(),
    val image: String? = null,
    val footer: Footer? = null
) {

    fun toDiscordEmbed(placeholderValues: Map<String, String>): EmbedBuilder {
        val embedBuilder = EmbedBuilder()

        embedBuilder.apply {
            author?.apply {
                setAuthor(
                    name?.applyPlaceholders(placeholderValues),
                    url?.applyPlaceholders(placeholderValues),
                    iconURL?.applyPlaceholders(placeholderValues)
                )
            }

            setColor(Color.decode(color))

            setTitle(title?.applyPlaceholders(placeholderValues))

            setUrl(url?.applyPlaceholders(placeholderValues))

            setDescription(description?.applyPlaceholders(placeholderValues))

            setThumbnail(thumbnail?.applyPlaceholders(placeholderValues))

            this@EmbedMessage.fields.forEach {
                addField(
                    it.name.applyPlaceholders(placeholderValues),
                    it.value.applyPlaceholders(placeholderValues),
                    it.inline
                )
            }

            setImage(image?.applyPlaceholders(placeholderValues))

            footer?.apply {
                setFooter(
                    text?.applyPlaceholders(placeholderValues),
                    iconURL?.applyPlaceholders(placeholderValues))
            }
        }

        return embedBuilder
    }

}

@Serializable
data class Author(
    val name: String?,
    val url: String?,
    @SerialName("icon-url")
    val iconURL: String?
)

@Serializable
data class Field(
    val name: String,
    val value: String,
    val inline: Boolean = false
)

@Serializable
data class Footer(
    val text: String?,
    @SerialName("icon-url")
    val iconURL: String?
)
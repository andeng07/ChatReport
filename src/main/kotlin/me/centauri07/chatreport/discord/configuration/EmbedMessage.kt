package me.centauri07.chatreport.discord.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
                    name?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } },
                    url?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } },
                    iconURL?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } }
                )
            }

            setColor(Color.decode(color))

            setTitle(title?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } })

            setUrl(url?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } })

            setDescription(description?.also {
                placeholderValues.forEach { pv ->
                    it.replace(
                        "%${pv.key}%",
                        pv.value
                    )
                }
            })

            setThumbnail(thumbnail?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } })

            this@EmbedMessage.fields.forEach {
                addField(
                    it.name.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } },
                    it.value.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } },
                    it.inline
                )
            }

            setImage(image?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } })

            footer?.apply {
                setFooter(
                    text?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } },
                    iconURL?.also { placeholderValues.forEach { pv -> it.replace("%${pv.key}%", pv.value) } })
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
package me.centauri07.chatreport.util.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

private val miniMessage = MiniMessage.miniMessage()

fun String.component(): Component = miniMessage.deserialize(this).decoration(TextDecoration.ITALIC, false)

fun String.applyPlaceholders(placeholders: Map<String, String>): String {
    var withPlaceholder = this

    placeholders.forEach {
        withPlaceholder = withPlaceholder.replace(it.key, it.value)
    }

    return withPlaceholder
}

fun String.isValidDuration(): Boolean = this.matches(Regex("^\\d+[dhms]$"))

fun Component.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
package me.centauri07.chatreport.plugin.chat

import java.time.Instant
import java.util.UUID

data class ChatHistory(
    val uuid: UUID,
    val chats: MutableList<Chat>
) {

    fun addChat(limit: Int, chat: Chat) {

        while (chats.size >= limit) {
            chats.dropLast(1)
        }

        chats.addFirst(chat)

    }

}

data class Chat(
    val content: String,
    val date: Instant,
    var isReported: Boolean
)
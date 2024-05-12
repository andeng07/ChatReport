package me.centauri07.chatreport.plugin.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatHistory(
    val chats: MutableList<Chat>
) {

    fun addChat(limit: Int, chat: Chat) {

        while (chats.size >= limit) {
            chats.dropLast(1)
        }

        chats.add(0, chat)

    }

}

@Serializable
data class Chat(
    val content: String,
    val date: Long,
    var isReported: Boolean
)
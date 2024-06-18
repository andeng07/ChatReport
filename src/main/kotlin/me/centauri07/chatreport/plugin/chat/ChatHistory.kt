package me.centauri07.chatreport.plugin.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatHistory(
    val chats: MutableList<Chat>
) {

    fun addChat(limit: Int, chat: Chat) {

        while (chats.size >= limit) {
            chats.removeAt(0)
        }

        chats.add(chat)

    }

}

@Serializable
data class Chat(
    val content: String,
    val date: Long,
    var isReported: Boolean
)
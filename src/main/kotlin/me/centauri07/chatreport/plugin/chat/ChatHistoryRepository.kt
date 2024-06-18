package me.centauri07.chatreport.plugin.chat

import me.centauri07.chatreport.util.serializer.JsonFileSerializer
import java.io.File
import java.util.UUID

object ChatHistoryRepository {

    lateinit var repositoryFolder: File

    private val cache: MutableMap<UUID, JsonFileSerializer<ChatHistory>> = mutableMapOf()

    fun find(uuid: UUID): ChatHistory? {
        val chatHistory: ChatHistory? = cache[uuid]?.value

        if (chatHistory != null) return chatHistory

        val playerChatHistoryFile = File(repositoryFolder, "$uuid.json")

        if (!playerChatHistoryFile.exists()) return null


        val chatHistoryJson =
            JsonFileSerializer(
                ChatHistory::class.java,
                ChatHistory(mutableListOf()),
                playerChatHistoryFile
            )

        cache[uuid] = chatHistoryJson

        return chatHistoryJson.value
    }

    fun insert(uuid: UUID, chatHistory: ChatHistory): Boolean {
        if (find(uuid) != null) return false

        val playerChatHistoryFile = File(repositoryFolder, "$uuid.json")

        val chatHistoryYaml =
            JsonFileSerializer(
                ChatHistory::class.java,
                chatHistory,
                playerChatHistoryFile
            )

        cache[uuid] = chatHistoryYaml

        return true
    }

    fun delete(uuid: UUID): Boolean {
        cache[uuid]?.file?.delete() ?: return false
        cache.remove(uuid)
        return true
    }

    fun invalidateCache(uuid: UUID) {
        cache.remove(uuid)
    }

    fun save(uuid: UUID): Boolean {
        cache[uuid]?.save() ?: return false

        return true
    }

}
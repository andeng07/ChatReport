package me.centauri07.chatreport.plugin.chat

import me.centauri07.chatreport.util.serializer.YamlFileSerializer
import java.io.File
import java.util.UUID

object ChatHistoryRepository {

    lateinit var repositoryFolder: File

    private val cache: MutableMap<UUID, YamlFileSerializer<ChatHistory>> = mutableMapOf()

    fun find(uuid: UUID): ChatHistory? {

        var chatHistory: ChatHistory? = cache[uuid]?.value

        if (chatHistory == null) {

            val playerChatHistoryFile = File(repositoryFolder, "$uuid.yml")

            if (playerChatHistoryFile.exists()) {
                val chatHistoryYaml =
                    YamlFileSerializer(
                        ChatHistory::class.java,
                        ChatHistory(uuid, mutableListOf()),
                        playerChatHistoryFile
                    )

                chatHistory = chatHistoryYaml.value

                cache[uuid] = chatHistoryYaml
            }

        }

        return chatHistory

    }

    fun insert(uuid: UUID, chatHistory: ChatHistory): Boolean {
        if (find(uuid) != null) return false

        val playerChatHistoryFile = File(repositoryFolder, "$uuid.yml")

        val chatHistoryYaml =
            YamlFileSerializer(
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

    fun save(uuid: UUID): Boolean {
        cache[uuid]?.save() ?: return false

        return true
    }

}
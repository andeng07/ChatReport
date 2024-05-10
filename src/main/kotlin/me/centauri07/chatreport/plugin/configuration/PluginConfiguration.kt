package me.centauri07.chatreport.plugin.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PluginConfiguration(
    @SerialName("database-connection-string")
    val databaseConnectionString: String,
    val chatHistoryLength: Int,
    val inventoryConfiguration: InventoryConfiguration
)
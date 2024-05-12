package me.centauri07.chatreport.util.serializer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class JsonFileSerializer<T>(override val classType: Class<T>, override var value: T, override val file: File) : FileSerializer<T> {

    init {
        if (!file.parentFile.exists()) file.parentFile.mkdirs()

        if (!file.exists()) {
            file.createNewFile()

            save()
        } else {
            value = load()
        }
    }

    companion object {
        private val GSON: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create()
    }

    override fun load(): T {
        val reader = FileReader(file)
        value = GSON.fromJson(reader, classType)
        reader.close()

        return value
    }

    override fun save() {
        val writer = FileWriter(file)
        GSON.toJson(value, writer)
        writer.close()
    }


}
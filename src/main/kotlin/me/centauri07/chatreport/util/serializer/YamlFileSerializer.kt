package me.centauri07.chatreport.util.serializer

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.serializer
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class YamlFileSerializer<T>(override val classType: Class<T>, override var value: T, override val file: File) :
    FileSerializer<T> {

    init {
        if (!file.parentFile.exists()) file.parentFile.mkdirs()

        if (!file.exists()) {
            file.createNewFile()

            save()
        } else {
            value = load()
        }
    }

    override fun load(): T {
        val reader = FileReader(file)
        value = Yaml.default.decodeFromString(serializer(classType), reader.readText()) as T
        reader.close()

        return value
    }

    override fun save() {
        val writer = FileWriter(file)
        writer.write(Yaml.default.encodeToString(serializer(classType), value!!))
        writer.close()
    }


}
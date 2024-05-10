package me.centauri07.chatreport.util.serializer

import java.io.File

interface FileSerializer<T> {
    val classType: Class<T>
    var value: T
    val file: File

    fun load(): T
    fun save()

}
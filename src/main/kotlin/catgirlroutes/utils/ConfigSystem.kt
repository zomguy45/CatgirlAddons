package catgirlroutes.utils

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object ConfigSystem {

    /**
        How to use:
        private val exampleFile = File("config/catgirlroutes/example.json")

        private fun loadWaypoints(): MutableList<[DataClass]> {
        return ConfigSystem.loadConfig(exampleFile, object : TypeToken<MutableList<[DataClass]>>() {}.type) ?: mutableListOf()
        }

        private fun saveWaypoints() {
        ConfigSystem.saveConfig(exampleFile, [Data])
        }
    **/
    private val gson = Gson()

    fun saveConfig(file: File, configData: Any) {
        try {
            val writer = FileWriter(file)
            gson.toJson(configData, writer)
            writer.close()
        } catch (e: IOException) {
            //
        }
    }

    fun <T> loadConfig(file: File, type: Type): T? {
        if (!file.exists()) {
            file.createNewFile()
            return null
        }

        try {
            val reader = FileReader(file)
            val configData = gson.fromJson<T>(reader, type)
            reader.close()
            return configData
        } catch (e: Exception) {
            return null
        }
    }

    fun saveToFile(file: File, key: String, value: Any) {
        val configData: MutableMap<String, Any> = loadConfig(file, object : TypeToken<MutableMap<String, Any>>() {}.type) ?: mutableMapOf()
        configData[key] = value
        saveConfig(file, configData)
    }

    inline fun <reified T> loadFromFile(file: File, key: String): T? {
        val configData: Map<String, Any> = loadConfig(file, object : TypeToken<Map<String, Any>>() {}.type) ?: return null
        return configData[key] as? T
    }
}

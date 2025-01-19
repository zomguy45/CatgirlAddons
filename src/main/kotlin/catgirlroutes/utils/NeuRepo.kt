package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.scope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking

object NeuRepo {
    var items = mutableListOf<Item>()
    var mobs = mutableListOf<JsonObject>()
    var constants = mutableListOf<JsonObject>()

    init {
        updateRepo()
    }

    private fun updateRepo() {
        runBlocking(scope.coroutineContext) {
            val result = downloadAndProcessRepo()
            result?.let {
                val gson = Gson()
                items = gson.fromJson(gson.toJson(it.first), object : TypeToken<List<Item>>() {}.type)
                mobs = it.second.toMutableList()
                constants = it.third.toMutableList()
            }
        }
    }

    fun getItemFromID(skyblockID: String): Item? = items.find { it.skyblockID == skyblockID }

    fun getItemFromName(name: String, contains: Boolean = true): Item? {
        return if (contains) {
            items.find { it.name.contains(name) }
        } else {
            items.find { it.name == name }
        }
    }
}

data class Item(
    @SerializedName("itemid") val id: String,
    @SerializedName("displayname") val name: String,
    @SerializedName("nbttag") val nbt: String,
    @SerializedName("lore") val lore: List<String>,
    @SerializedName("internalname") val skyblockID: String
)
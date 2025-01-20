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
            val result = downloadRepo()
            result?.let {
                val gson = Gson()

                val itemList: List<Item> = gson.fromJson(gson.toJson(it.first), object : TypeToken<List<Item>>() {}.type)

                items = itemList.map { item ->
                    // convert shitty neu skyblockID to normal format (WISDOM;2 -> ENCHANTMENT_WISDOM_2)
                    if (item.skyblockID.contains(";") && item.id == "minecraft:enchanted_book") {
                        val parts = item.skyblockID.split(";")
                        if (parts.size == 2) {
                            var enchantment = parts[0]
                            val tier = parts[1]
                            item.skyblockID = "ENCHANTMENT_${enchantment}_${tier}"
                            // also change name of enchantments
                            val colour = if (enchantment.startsWith("ULTIMATE")) "§9§d§l" else "§9"
                            enchantment = if (enchantment.contains("WISE")) enchantment else enchantment.replace("ULTIMATE_", "")
                            item.name = "$colour${enchantment.replace("_", " ").capitalizeWords()} $tier"
                        }
                    }
                    item
                }.toMutableList()

                mobs = it.second.toMutableList()
                constants = it.third.toMutableList()
            }
            updateItemsWithAuctionData()
            updateItemsWithBazaarData()
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }

    private fun updateItemsWithAuctionData() = runBlocking {
        val auctionDataJson = getDataFromServer("https://moulberry.codes/lowestbin.json")
        if (auctionDataJson.isNotEmpty()) {
            val auctionMap = Gson().fromJson(auctionDataJson, JsonObject::class.java)
                .entrySet().associate { it.key to it.value.asDouble }

            items.forEach { item ->
                val price = auctionMap[item.skyblockID] ?: 0.0
                item.auction = price > 0.0
                item.price = price
            }
        }
    }

    private fun updateItemsWithBazaarData() = runBlocking {
        val bazaarDataJson = getDataFromServer("https://api.hypixel.net/v2/skyblock/bazaar")
        if (bazaarDataJson.isNotEmpty()) {
            val products = Gson().fromJson(bazaarDataJson, JsonObject::class.java)
                .getAsJsonObject("products") ?: return@runBlocking

            items.forEach { item ->
                val price = products[item.skyblockID]?.asJsonObject
                    ?.getAsJsonObject("quick_status")?.get("buyPrice")?.asDouble ?: 0.0
                if (price > 0.0) {
                    item.bazaar = true
                    item.price = price
                }
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
    @SerializedName("displayname") var name: String,
    @SerializedName("nbttag") val nbt: String,
    @SerializedName("lore") val lore: List<String>,
    @SerializedName("internalname") var skyblockID: String,
    @SerializedName("damage") val damage: Int,
    var auction: Boolean = false,
    var bazaar: Boolean = false,
    var price: Double = 0.0
)
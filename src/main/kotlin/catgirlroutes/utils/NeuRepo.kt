package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.scope
import catgirlroutes.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.ResourceLocation

object NeuRepo {
    var repoItems = mutableListOf<RepoItem>()
    var mobs = mutableListOf<JsonObject>()
    var constants = mutableListOf<JsonObject>()

    private val gson = Gson()

    init {
        updateRepo()
    }

    private fun updateRepo() {
        scope.launch {
            val startTime = System.currentTimeMillis()

            val result = downloadRepo()
            result?.let { (items, mobsList, constantsList) ->
                processRepoItems(items)
                mobs = mobsList.toMutableList()
                constants = constantsList.toMutableList()

                val auctionJob = launch { updateItemsWithAuctionData() }
                val bazaarJob = launch { updateItemsWithBazaarData() }

                auctionJob.join()
                bazaarJob.join()

                val totalTime = System.currentTimeMillis() - startTime
                println("Loaded NeuRepo in ${totalTime}ms (${repoItems.size} items)")
            }
        }
    }

    private suspend fun processRepoItems(items: List<JsonObject>) {
        val processedItems = withContext(Dispatchers.IO) {
            items.mapNotNull { itemJson ->
                try {
                    val item: RepoItem = gson.fromJson(itemJson, RepoItem::class.java)

                    if (item.skyblockID.contains(";") && item.id == "minecraft:enchanted_book") {
                        val parts = item.skyblockID.split(";")
                        if (parts.size == 2) {
                            var enchantment = parts[0]
                            val tier = parts[1]
                            item.skyblockID = "ENCHANTMENT_${enchantment}_$tier"

                            val colour = if (enchantment.startsWith("ULTIMATE")) "§9§d§l" else "§9"
                            enchantment = if (enchantment.contains("WISE")) enchantment else enchantment.replace("ULTIMATE_", "")
                            item.name = "$colour${enchantment.replace("_", " ").capitalizeWords()} ${intToRoman(tier.toInt())}"
                        }
                    }
                    item
                } catch (e: Exception) {
                    null
                }
            }
        }
        repoItems = processedItems.toMutableList()
    }

    private suspend fun updateItemsWithAuctionData() {
        val auctionDataJson = getDataFromServer("https://moulberry.codes/lowestbin.json")
        if (auctionDataJson.isNotEmpty()) {
            val auctionMap = withContext(Dispatchers.IO) {
                gson.fromJson(auctionDataJson, JsonObject::class.java)
                    .entrySet().associate { it.key to it.value.asDouble }
            }

            val itemMap = repoItems.associateBy { it.skyblockID }

            auctionMap.forEach { (id, price) ->
                itemMap[id]?.let { item ->
                    item.auction = price > 0.0
                    item.price = price
                }
            }
        }
    }

    private suspend fun updateItemsWithBazaarData() {
        val bazaarDataJson = getDataFromServer("https://api.hypixel.net/v2/skyblock/bazaar")
        if (bazaarDataJson.isEmpty()) return

        val jsonObject = withContext(Dispatchers.IO) {
            gson.fromJson(bazaarDataJson, JsonObject::class.java)
        }

        val products = jsonObject.getAsJsonObject("products") ?: return

        val itemMap = repoItems.associateBy { it.skyblockID }

        products.entrySet().forEach { (id, product) ->
            product.asJsonObject?.getAsJsonObject("quick_status")?.get("buyPrice")?.asDouble?.let { price ->
                itemMap[id]?.let { item ->
                    item.bazaar = true
                    item.price = price
                }
            }
        }
    }

    fun getItemFromID(skyblockID: String): RepoItem? = repoItems.find { it.skyblockID == skyblockID }

    fun getItemFromName(name: String, contains: Boolean = true): RepoItem? {
        return if (contains) {
            repoItems.find { it.name.contains(name) || it.name.noControlCodes.contains(name) }
        } else {
            repoItems.find { it.name == name || it.name.noControlCodes == name || it.name.noControlCodes.removePrefix("[Lvl {LVL}] ") == name }
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalizeOnlyFirst() }

    // modified schizo shit from neu
    private val itemStackCache: MutableMap<String, ItemStack> = HashMap()
    fun RepoItem.toStack(
        useCache: Boolean = true,
        copyStack: Boolean = false
    ): ItemStack {
        var cacheEnabled = useCache

        if (this.skyblockID == "_") cacheEnabled = false

        if (cacheEnabled) {
            itemStackCache[this.skyblockID]?.let { stack ->
                return if (copyStack) stack.copy() else stack
            }
        }

        val stack = ItemStack(net.minecraft.item.Item.itemRegistry.getObject(ResourceLocation(this.id)), 1, this.damage)

        if (stack.item == null) {
            return ItemStack(net.minecraft.item.Item.getItemFromBlock(Blocks.stone), 0, 255) // Purple broken texture item
        } else {
            try {
                val tag: NBTTagCompound = JsonToNBT.getTagFromJson(this.nbt)
                stack.tagCompound = tag
            } catch (ignored: NBTException) { }

            val display = NBTTagCompound().apply {
                if (stack.tagCompound != null && stack.tagCompound!!.hasKey("display")) {
                    this.merge(stack.tagCompound!!.getCompoundTag("display"))
                }
                this.setTag("Lore", this@toStack.lore.processLore())
            }
            val tag = stack.tagCompound ?: NBTTagCompound()
            tag.setTag("display", display)
            stack.tagCompound = tag
        }

        if (cacheEnabled) itemStackCache[this.id] = stack
        return if (copyStack) stack.copy() else stack
    }

    private fun List<String>.processLore(): NBTTagList {
        val nbtLore = NBTTagList()
        for (line in this) {
            if (!line.contains("Click to view recipes!") &&
                !line.contains("Click to view recipe!")) {
                nbtLore.appendTag(NBTTagString(line))
            }
        }
        return nbtLore
    }
}

data class RepoItem(
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
package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.scope
import com.google.gson.*
import kotlinx.coroutines.runBlocking
import java.io.StringReader
import java.lang.reflect.Type

object CgaUsers {

    var users: HashMap<String, CgaUser> = HashMap()
    data class CgaUser(val xScale: Float = 1f, val yScale: Float = 1f, val zScale: Float = 1f)
    data class UserData(val username: String, val uuid: String, val permission: Int, val dimensions: Triple<Float, Float, Float>)

    class UserDeserializer : JsonDeserializer<UserData> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): UserData {
            val jsonObject = json?.asJsonObject
            val name = jsonObject?.get("name")?.asString
            val uuid = jsonObject?.get("uuid")?.asString
            val permission = jsonObject?.get("permission")?.asInt
            val dimensionsJsonObject = JsonParser().parse(jsonObject?.get("dimensions")?.asString?.let { StringReader(it) })?.asJsonObject
            val sizeTriple = Triple(
                dimensionsJsonObject?.get("x")?.asFloat ?: 0f,
                dimensionsJsonObject?.get("y")?.asFloat ?: 0f,
                dimensionsJsonObject?.get("z")?.asFloat ?: 0f
            )

            return UserData(name ?: "", uuid ?: "", permission ?: 1, sizeTriple)
        }
    }

    private fun convertDecimalToNumber(s: String): String {
        val pattern = Regex("""Decimal\('(-?\d+(?:\.\d+)?)'\)""")

        return s.replace(pattern) { match -> match.groupValues[1] }
    }

    fun updateUsers(): HashMap<String, CgaUser> {
        runBlocking(scope.coroutineContext) {
            val data = convertDecimalToNumber(getDataFromServer()).ifEmpty { return@runBlocking }
            val gson = GsonBuilder().registerTypeAdapter(UserData::class.java, UserDeserializer())?.create() ?: return@runBlocking
            gson.fromJson(data, Array<UserData>::class.java).forEach {
                users[it.username] = CgaUser(it.dimensions.first, it.dimensions.second, it.dimensions.third)
            }
        }
        return users
    }

    init {
        updateUsers()
    }

}
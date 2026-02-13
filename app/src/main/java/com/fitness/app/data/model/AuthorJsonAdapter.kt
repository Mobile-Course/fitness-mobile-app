package com.fitness.app.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class AuthorJsonAdapter : JsonDeserializer<Author> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Author {
        if (json == null || json.isJsonNull) {
            return Author(id = "", name = null, username = "Unknown", picture = null)
        }

        if (json.isJsonPrimitive) {
            val value = json.asString
            return Author(
                id = value,
                name = null,
                username = if (value.isBlank()) "Unknown" else value,
                picture = null
            )
        }

        if (!json.isJsonObject) {
            return Author(id = "", name = null, username = "Unknown", picture = null)
        }

        val obj = json.asJsonObject
        val id =
            when {
                obj.has("_id") && !obj.get("_id").isJsonNull -> obj.get("_id").asString
                obj.has("id") && !obj.get("id").isJsonNull -> obj.get("id").asString
                else -> ""
            }
        val username =
            when {
                obj.has("username") && !obj.get("username").isJsonNull -> obj.get("username").asString
                obj.has("name") && !obj.get("name").isJsonNull -> obj.get("name").asString
                obj.has("email") && !obj.get("email").isJsonNull -> obj.get("email").asString
                id.isNotBlank() -> id
                else -> "Unknown"
            }
        val name =
            if (obj.has("name") && !obj.get("name").isJsonNull) {
                obj.get("name").asString
            } else {
                null
            }
        val picture =
            if (obj.has("picture") && !obj.get("picture").isJsonNull) {
                obj.get("picture").asString
            } else {
                null
            }

        return Author(id = id, name = name, username = username, picture = picture)
    }
}

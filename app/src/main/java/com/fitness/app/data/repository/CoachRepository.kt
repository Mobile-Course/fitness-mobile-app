package com.fitness.app.data.repository

import com.fitness.app.data.api.RetrofitClient
import com.fitness.app.data.model.AskCoachRequest
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CoachRepository {
    private val apiService = RetrofitClient.coachApiService
    private val gson = Gson()

    fun askStream(question: String): Flow<String> = flow {
        val response = apiService.askStream(AskCoachRequest(question = question))
        if (!response.isSuccessful) {
            val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: response.message()
                ?: "Unknown error"
            throw IllegalStateException("AI stream request failed: $message")
        }

        val body = response.body() ?: throw IllegalStateException("Empty AI stream response")
        body.byteStream().bufferedReader().useLines { lines ->
            lines.forEach { rawLine ->
                val line = rawLine.trim()
                if (!line.startsWith("data:")) return@forEach
                val payload = line.removePrefix("data:").trim()
                if (payload.isBlank() || payload == "[DONE]") return@forEach

                val text = extractText(payload)
                if (text.isNotBlank()) emit(text)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun extractText(payload: String): String {
        val parsed = runCatching { gson.fromJson(payload, JsonElement::class.java) }.getOrNull()
            ?: return if (looksLikeJson(payload)) "" else payload

        if (parsed.isJsonPrimitive) return parsed.asString
        if (!parsed.isJsonObject) return ""

        val obj = parsed.asJsonObject
        val direct = extractFromObject(obj)
        if (!direct.isNullOrBlank()) return direct

        val dataElement = obj.get("data")
        val nested =
            when {
                dataElement == null || dataElement.isJsonNull -> null
                dataElement.isJsonPrimitive -> dataElement.asString
                dataElement.isJsonObject -> extractFromObject(dataElement.asJsonObject)
                else -> null
            }
        if (!nested.isNullOrBlank()) return nested

        // OpenAI-like stream shape: { choices: [{ delta: { content: "..." } }] }
        val choicesElement = obj.get("choices")
        if (choicesElement != null && choicesElement.isJsonArray && choicesElement.asJsonArray.size() > 0) {
            val choices = choicesElement.asJsonArray
            val firstChoice = choices.get(0)
            if (firstChoice != null && firstChoice.isJsonObject) {
                val deltaObj = firstChoice.asJsonObject.getAsJsonObject("delta")
                val deltaContent =
                    deltaObj?.get("content")?.takeIf { it.isJsonPrimitive }?.asString
                if (!deltaContent.isNullOrBlank()) return deltaContent

                val messageObj = firstChoice.asJsonObject.getAsJsonObject("message")
                val messageContent =
                    messageObj?.get("content")?.takeIf { it.isJsonPrimitive }?.asString
                if (!messageContent.isNullOrBlank()) return messageContent
            }
        }

        // Ignore metadata / unrelated JSON messages.
        return ""
    }

    private fun extractFromObject(obj: com.google.gson.JsonObject): String? {
        return listOf("content", "text", "message", "token", "answer")
            .firstNotNullOfOrNull { key ->
                obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString
            }
    }

    private fun looksLikeJson(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("{") || trimmed.startsWith("[")
    }
}

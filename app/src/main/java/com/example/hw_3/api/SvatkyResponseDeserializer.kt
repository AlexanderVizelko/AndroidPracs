package com.example.hw_3.api

import com.example.hw_3.data.SvatkyResponse
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Кастомный десериализатор для SvatkyResponse, который может обрабатывать
 * как объект, так и массив объектов
 */
class SvatkyResponseDeserializer : JsonDeserializer<SvatkyResponse> {
    // Создаем стандартный Gson без кастомных десериализаторов для избежания рекурсии
    private val standardGson = Gson()
    
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SvatkyResponse {
        if (json == null) {
            return SvatkyResponse(null, null, null, null, null)
        }
        
        return try {
            // Если это объект - десериализуем напрямую
            if (json.isJsonObject) {
                standardGson.fromJson(json, SvatkyResponse::class.java)
            } 
            // Если это массив, берем первый элемент
            else if (json.isJsonArray) {
                val array = json.asJsonArray
                if (array.size() > 0) {
                    standardGson.fromJson(array[0], SvatkyResponse::class.java)
                } else {
                    SvatkyResponse(null, null, null, null, null)
                }
            } else {
                SvatkyResponse(null, null, null, null, null)
            }
        } catch (e: Exception) {
            android.util.Log.e("SvatkyDeserializer", "Error deserializing: ${e.message}", e)
            SvatkyResponse(null, null, null, null, null)
        }
    }
}


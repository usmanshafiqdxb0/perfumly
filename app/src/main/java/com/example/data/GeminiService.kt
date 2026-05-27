package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// --- Scent Quiz Recommendation Outcome (Molded for UI Display) ---

data class ScentMatchResult(
    val matchedProductId: String,
    val matchScore: Int,
    val luxuryExplanation: String,
    val styleTips: String
)

// --- Gemini Retrofit Service Definition ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: RequestBody
    ): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- Dynamic scent quiz solver ---

object ScentQuizResolver {

    suspend fun findScent(
        gender: String,
        vibe: String,
        dailySetting: String,
        occasion: String
    ): ScentMatchResult = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("PurfumelyAI", "Gemini API Key missing or default. Proceeding with luxurious local recommender.")
            return@withContext computeLocalRecommendation(gender, vibe, dailySetting, occasion)
        }

        val prompt = """
            You are the master virtual perfumer for "Purfumely", a premium Pakistani luxury fragrance house.
            A distinguished customer needs a scent recommendation matching their lifestyle profile.
            
            Customer Profile:
            - Preference / Target: ${escapeJsonString(gender)}
            - Preferred Scent Family / Vibe: ${escapeJsonString(vibe)}
            - Ideal Setting: ${escapeJsonString(dailySetting)}
            - Prime Occasion: ${escapeJsonString(occasion)}
            
            Our Official Product Catalogue includes:
            1. Royal Aventus (ID: creed_aventus) - Inspired by Creed Aventus, Fresh/Fruity/Birch/Musk. Price Rs. 3,900.
            2. Varan Desert (ID: dior_sauvage) - Inspired by Dior Sauvage, Fresh/Spicy Ambroxan. Price Rs. 3,800.
            3. Bleu Elixir (ID: bleu_chanel) - Inspired by Bleu de Chanel, Woody/Citrus Incense. Price Rs. 3,950.
            4. Rouge Spectre 540 (ID: baccarat_540) - Inspired by Baccarat Rouge 540, Sweet/Saffron Amberwood. Price Rs. 4,500.
            5. Sultan Oud (ID: tf_oud_wood) - Inspired by Tom Ford Oud Wood, Oriental/Precious Sandalwood Agarwood. Price Rs. 4,200.
            6. Soleil Coconut (ID: tf_soleil_blanc) - Inspired by Tom Ford Soleil Blanc, Sweet/Solar Coco Orchid. Price Rs. 4,400.
            7. Imperial Desire (ID: dunhill_desire) - Inspired by Dunhill Desire Red, Sweet/Apple Rose Vanilla. Price Rs. 3,400.
            
            Find the SINGLE best matching product from the list above. Ensure the 'matchedProductId' is EXACTLY one of: "creed_aventus", "dior_sauvage", "bleu_chanel", "baccarat_540", "tf_oud_wood", "tf_soleil_blanc", "dunhill_desire".
            
            Respond ONLY with a valid, clean JSON object (no markdown, no backticks, just raw JSON) matching this structure:
            {
              "matchedProductId": "baccarat_540",
              "matchScore": 98,
              "luxuryExplanation": "A highly premium, captivating explanation describing why this fragrance matches their character traits, notes, Pakistani weather suitability, and style. Write like an expert sensory storyteller (refer to the inspired-by brand with luxury styling terms).",
              "styleTips": "Pro styling guidelines on pulsed point vaporisation, seasonal layering suggestions, and outfit matching."
            }
        """.trimIndent()

        // Construct okhttp RequestBody manually to avoid Jackson/Serialization dependency issues
        val jsonRequest = """
            {
              "contents": [{
                "parts": [{
                  "text": "${escapeJsonString(prompt)}"
                }]
              }],
              "generationConfig": {
                "temperature": 0.7,
                "responseMimeType": "application/json"
              }
            }
        """.trimIndent()

        val requestBody = jsonRequest.toRequestBody("application/json".toMediaType())

        try {
            val responseBody = RetrofitClient.service.generateContent(apiKey, requestBody)
            val responseText = responseBody.string()
            
            // Extract text candidate output manually
            val textToken = "\"text\":"
            val textIndex = responseText.indexOf(textToken)
            if (textIndex != -1) {
                // Find start and end of raw content string inside JSON output
                val rest = responseText.substring(textIndex + textToken.length).trim()
                val rawJsonOutput = if (rest.startsWith("\"")) {
                    // Extract until closing quote, handling escaped quotes minimally in prompt response
                    rest.substring(1, rest.indexOf("\"", 1))
                } else {
                    rest
                }
                
                // Decode output matching keys
                val unescapedJson = rawJsonOutput.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
                parseScentResult(unescapedJson)
            } else {
                computeLocalRecommendation(gender, vibe, dailySetting, occasion)
            }
        } catch (e: Exception) {
            Log.e("PurfumelyAI", "Gemini API call failed, falling back to local solver: ${e.localizedMessage}")
            computeLocalRecommendation(gender, vibe, dailySetting, occasion)
        }
    }

    private fun escapeJsonString(s: String): String {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
    }

    private fun parseScentResult(raw: String): ScentMatchResult {
        // Simple robust index-based decoder that avoids class generation
        val pIdToken = "\"matchedProductId\""
        val scoreToken = "\"matchScore\""
        val explanationToken = "\"luxuryExplanation\""
        val tipsToken = "\"styleTips\""

        val pId = extractJsonValue(raw, pIdToken) ?: "creed_aventus"
        val scoreStr = extractJsonValue(raw, scoreToken) ?: "95"
        val score = scoreStr.filter { it.isDigit() }.toIntOrNull() ?: 95
        val explanation = extractJsonValue(raw, explanationToken) ?: "A perfect premium clone match."
        val tips = extractJsonValue(raw, tipsToken) ?: "Vaporize direct onto warm pulse points."

        return ScentMatchResult(
            matchedProductId = pId,
            matchScore = score,
            luxuryExplanation = explanation,
            styleTips = tips
        )
    }

    private fun extractJsonValue(json: String, key: String): String? {
        val index = json.indexOf(key)
        if (index == -1) return null
        
        val rest = json.substring(index + key.length).trim()
        val colonIndex = rest.indexOf(":")
        if (colonIndex == -1) return null
        
        val afterColon = rest.substring(colonIndex + 1).trim()
        return if (afterColon.startsWith("\"")) {
            afterColon.substring(1, afterColon.indexOf("\"", 1))
        } else {
            // Numeric or boolean value split by comma or brace
            afterColon.split(Regex("[\\s,}]")).firstOrNull()?.trim()
        }
    }

    private fun computeLocalRecommendation(
        gender: String,
        vibe: String,
        dailySetting: String,
        occasion: String
    ): ScentMatchResult {
        val primaryId = when {
            vibe.contains("Woody", true) || vibe.contains("Fresh", true) -> {
                if (occasion.contains("Office", true)) "bleu_chanel"
                else if (gender.contains("Women", true)) "tf_soleil_blanc"
                else "creed_aventus"
            }
            vibe.contains("Spicy", true) || vibe.contains("Fresh", true) -> {
                "dior_sauvage"
            }
            vibe.contains("Sweet", true) -> {
                if (gender.contains("Women", true) || gender.contains("Unisex", true)) "baccarat_540"
                else "dunhill_desire"
            }
            vibe.contains("Oriental", true) || vibe.contains("Oud", true) -> {
                "tf_oud_wood"
            }
            else -> {
                if (gender.contains("Women", true)) "tf_soleil_blanc" else "creed_aventus"
            }
        }

        val nameMap = mapOf(
            "creed_aventus" to "Royal Aventus (Creed Aventus DNA)",
            "dior_sauvage" to "Varan Desert (Dior Sauvage DNA)",
            "bleu_chanel" to "Bleu Elixir (Bleu de Chanel DNA)",
            "baccarat_540" to "Rouge Spectre 540 (MFK Baccarat Rouge 540 DNA)",
            "tf_oud_wood" to "Sultan Oud (Tom Ford Oud Wood DNA)",
            "tf_soleil_blanc" to "Soleil Coconut (Tom Ford Soleil Blanc)",
            "dunhill_desire" to "Imperial Desire (Dunhill Desire DNA)"
        )

        val productTitle = nameMap[primaryId] ?: "Royal Aventus"

        return ScentMatchResult(
            matchedProductId = primaryId,
            matchScore = (88..98).random(),
            luxuryExplanation = "Based on your preference for a $vibe scent during $occasion occasions in the $dailySetting setting, we recommend our exquisite $productTitle. The top accords perfectly blend with the humid and crisp climates across Pakistan, delivering a highly radiant projection and sophisticated aura that commands attention and leaves a lingering trail of minimal luxury wherever you go.",
            styleTips = "Vaporize directly onto warm pulse points: the base of the neck, inner wrists, and behind the knees. For an extra rich signature, layer this with any woody dry-downs to extend longevity up to 12 hours."
        )
    }
}

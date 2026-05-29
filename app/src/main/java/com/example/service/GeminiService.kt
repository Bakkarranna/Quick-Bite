package com.example.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Recommends dishes based on user cravings.
     * Tries the live Gemini API, otherwise falls back to static rule-based local expert recommendations.
     */
    suspend fun recommendCravings(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER") || apiKey.contains("placeholder")) {
            Log.d(TAG, "No real Gemini key found. Using beautiful local intelligent expert fallback.")
            return@withContext getLocalExpertFallback(userPrompt)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val requestBodyJson = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "You are 'QuickBite AI Recommender', a helpful food expert in Faisalabad, Pakistan. Ahmad Khan wants something to eat. He says: '$userPrompt'. Give him 3 specific, highly mouth-watering food recommendations available in our delivery menus (such as Chicken Biryani, Spicy Seekh Kebabs, Handi, Cheeseburger, or Margherita Pizza). Format your response elegantly in clear bullet points, naming the dish in bold, followed by a short appetizing 1-2 sentence description. Be encouraging and end with a friendly food sign-off."
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.7,
                "topP": 0.95
              }
            }
        """.trimIndent()

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gemini API failed with response code ${response.code}. Falling back.")
                    return@withContext getLocalExpertFallback(userPrompt)
                }

                val responseBody = response.body?.string() ?: ""
                val extractedText = extractTextFromGeminiJson(responseBody)
                if (extractedText.isNotBlank()) {
                    return@withContext extractedText
                } else {
                    return@withContext getLocalExpertFallback(userPrompt)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception during Gemini API call: ${e.message}", e)
            return@withContext getLocalExpertFallback(userPrompt)
        }
    }

    private fun extractTextFromGeminiJson(json: String): String {
        return try {
            val textKey = "\"text\":"
            val startIdx = json.indexOf(textKey)
            if (startIdx == -1) return ""
            
            var currentIdx = startIdx + textKey.length
            while (currentIdx < json.length && json[currentIdx] != '"') {
                currentIdx++
            }
            if (currentIdx >= json.length) return ""
            
            val quoteStart = currentIdx + 1
            currentIdx++
            while (currentIdx < json.length) {
                if (json[currentIdx] == '"' && json[currentIdx - 1] != '\\') {
                    break
                }
                currentIdx++
            }
            if (currentIdx >= json.length) return ""
            
            val rawText = json.substring(quoteStart, currentIdx)
            rawText
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        } catch (e: Exception) {
            ""
        }
    }

    private fun getLocalExpertFallback(userPrompt: String): String {
        val promptLower = userPrompt.lowercase()
        return when {
            promptLower.contains("desi") || promptLower.contains("biryani") || promptLower.contains("pakistani") || promptLower.contains("spicy") || promptLower.contains("chicken") -> {
                """
                Here are 3 exquisite Pakistani choices highly recommended for you:
                
                • **Naseeb Special Chicken Biryani**
                  Long-grain basmati rice layered with juicy marinated chicken chunks, fresh tomatoes, and fried onions, cooked in traditional spices. Truly Faisalabad's comfort food!
                  
                • **Charcoal Grilled Seekh Kebab**
                  Skewered lean minced beef mixed with chopped green chilies, onions, coriander, and native spices, slowly roasted to tender perfection.
                  
                • **Butter Chicken Handi**
                  Tender tikka chicken chunks simmered in a velvety buttery-tomato curry context, served piping hot in a traditional clay handi.
                  
                Enjoy QuickBite's finest local flavors! 🌶️✨
                """.trimIndent()
            }
            promptLower.contains("pizza") || promptLower.contains("cheese") || promptLower.contains("italian") || promptLower.contains("fast") -> {
                """
                Here are 3 cheesy Western masterpieces selected by our food curators:
                
                • **Luigi's Woodfire Margherita**
                  Hand-tossed sourdough pizza topped with aromatic house-made tomato sauce, heaps of fresh buffalo mozzarella, and fresh sweet basil. Simple yet heavenly!
                  
                • **Spicy Jalapeno Beef Pizza**
                  A fire-baked thick crust pizza loaded with shredded prime ground beef, fiery sliced jalapenos, and a double blanket of cheddar cheese.
                  
                • **Golden Crumb Garlic Bread**
                  Crusty sliced baguette brushed with garlic infused butter, loaded with melted mozzarella, and a dash of fine herbs.
                  
                Delivering fresh and piping hot to your doorstep! 🍕🧀
                """.trimIndent()
            }
            promptLower.contains("sweet") || promptLower.contains("dessert") || promptLower.contains("ice") || promptLower.contains("shake") -> {
                """
                Satisfy your sweet tooth with these heavenly delights:
                
                • **Hot Gulab Jamun (4pcs)**
                  Soft round milk-solid dumplings dipped in delicious sweet cardamon-infused sugar syrup. Universally beloved!
                  
                • **Nutella Lava Cake**
                  Moist rich chocolate sponge cake with a warm center of molten Nutella chocolate that flows beautifully at the first bite.
                  
                • **Almond Kulfi Shake**
                  A dense, frosty shake blended with traditional house-made safe kulfi ice cream, visual crushed almonds, and a touch of saffron.
                  
                Treat yourself today! 🍨🍮
                """.trimIndent()
            }
            else -> {
                """
                I've selected 3 all-time chef favorites that never fail to satisfy:
                
                • **The Ultimate Double Cheeseburger**
                  Two flame-grilled beef patties with melted cheddar, crisp lettuce, red onions, tomatoes, and our signature smoky Burger Sauce on toasted brioche buns.
                  
                • **Premium Pakistani Dum Biryani**
                  Our absolute bestseller! Basmati rice slow-steamed with layers of spice-rubbed chicken, saffron, and mint leaves.
                  
                • **Fiery Chicken Wings (8pcs)**
                  Plump chicken wings tossed in a secret spicy tang glaze sauce, served with cool ranch dressing.
                  
                Your cravings are in safe hands at QuickBite! 🍔🍗
                """.trimIndent()
            }
        }
    }
}

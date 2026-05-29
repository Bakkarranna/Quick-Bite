package com.example.service

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

data class FirebaseConfig(
    val apiKey: String,
    val appId: String,
    val projectId: String,
    val databaseUrl: String = "",
    val authDomain: String = "",
    val storageBucket: String = ""
) {
    fun isValid(): Boolean {
        return apiKey.isNotBlank() && appId.isNotBlank() && projectId.isNotBlank()
    }
}

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val PREFS_NAME = "firebase_config_prefs"
    
    // Config keys in Shared Prefs
    private const val KEY_API_KEY = "firebase_api_key"
    private const val KEY_APP_ID = "firebase_app_id"
    private const val KEY_PROJECT_ID = "firebase_project_id"
    private const val KEY_DB_URL = "firebase_database_url"
    
    private var isFirebaseAppInitialized = false

    /**
     * Attempts to read a config value from BuildConfig using compile-safe reflection.
     */
    private fun getBuildConfigString(fieldName: String): String? {
        return try {
            val clazz = Class.forName("com.example.BuildConfig")
            val field = clazz.getField(fieldName)
            val value = field.get(null) as? String
            if (value.isNullOrBlank() || value == "placeholder" || value.startsWith("MY_")) null else value
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Reads the current Firebase configuration from SharedPreferences or BuildConfig.
     */
    fun getConfig(context: Context): FirebaseConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // 1. Check SharedPreferences
        val apiKey = prefs.getString(KEY_API_KEY, null)
        val appId = prefs.getString(KEY_APP_ID, null)
        val projectId = prefs.getString(KEY_PROJECT_ID, null)
        val dbUrl = prefs.getString(KEY_DB_URL, "") ?: ""
        
        if (!apiKey.isNullOrBlank() && !appId.isNullOrBlank() && !projectId.isNullOrBlank()) {
            return FirebaseConfig(apiKey, appId, projectId, dbUrl)
        }
        
        // 2. Fallback to BuildConfig / .env properties
        val envApiKey = getBuildConfigString("FIREBASE_API_KEY") ?: ""
        val envAppId = getBuildConfigString("FIREBASE_APP_ID") ?: ""
        val envProjectId = getBuildConfigString("FIREBASE_PROJECT_ID") ?: ""
        val envDbUrl = getBuildConfigString("FIREBASE_DATABASE_URL") ?: ""
        
        return FirebaseConfig(envApiKey, envAppId, envProjectId, envDbUrl)
    }

    /**
     * Saves the Firebase configuration to SharedPreferences.
     */
    fun saveConfig(context: Context, config: FirebaseConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_API_KEY, config.apiKey.trim())
            .putString(KEY_APP_ID, config.appId.trim())
            .putString(KEY_PROJECT_ID, config.projectId.trim())
            .putString(KEY_DB_URL, config.databaseUrl.trim())
            .apply()
        
        // Reset initialization so it can be re-initialized with new credentials
        isFirebaseAppInitialized = false
    }

    /**
     * Clears local Firebase dynamic configuration.
     */
    fun clearConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        isFirebaseAppInitialized = false
    }

    /**
     * Verifies if Firebase is currently programmatically initialized.
     */
    fun isInitialized(): Boolean {
        return isFirebaseAppInitialized
    }

    /**
     * Initializes Firebase App programmatically using stored or environment configurations.
     */
    fun initialize(context: Context): Pair<Boolean, String> {
        if (isFirebaseAppInitialized) {
            return Pair(true, "Firebase already initialized.")
        }

        val config = getConfig(context)
        if (!config.isValid()) {
            return Pair(false, "Firebase config keys are missing or invalid. Set them in Settings or .env.")
        }

        return try {
            // Check if app is already initialized
            val existingApps = FirebaseApp.getApps(context)
            if (existingApps.isNotEmpty()) {
                isFirebaseAppInitialized = true
                return Pair(true, "Firebase already active.")
            }

            val builder = FirebaseOptions.Builder()
                .setApiKey(config.apiKey)
                .setApplicationId(config.appId)
                .setProjectId(config.projectId)
            
            if (config.databaseUrl.isNotBlank()) {
                builder.setDatabaseUrl(config.databaseUrl)
            }

            FirebaseApp.initializeApp(context, builder.build())
            isFirebaseAppInitialized = true
            Log.d(TAG, "Successfully initialized Firebase programmatically!")
            Pair(true, "Firebase connected successfully!")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed initialized Firebase programmatically", e)
            Pair(false, "Connection error: ${e.message}")
        }
    }

    /**
     * Returns an instance of FirebaseAuth, or null if Firebase is not initialized.
     */
    fun getAuth(context: Context): FirebaseAuth? {
        if (!isFirebaseAppInitialized) {
            initialize(context)
        }
        return if (isFirebaseAppInitialized) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.e(TAG, "FirebaseAuth.getInstance() failed", e)
                null
            }
        } else null
    }

    /**
     * Returns an instance of FirebaseFirestore, or null if Firebase is not initialized.
     */
    fun getFirestore(context: Context): FirebaseFirestore? {
        if (!isFirebaseAppInitialized) {
            initialize(context)
        }
        return if (isFirebaseAppInitialized) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Throwable) {
                Log.e(TAG, "FirebaseFirestore.getInstance() failed", e)
                null
            }
        } else null
    }

    /**
     * Returns an instance of FirebaseDatabase, or null if Firebase is not initialized.
     */
    fun getDatabase(context: Context): FirebaseDatabase? {
        if (!isFirebaseAppInitialized) {
            initialize(context)
        }
        return if (isFirebaseAppInitialized) {
            try {
                FirebaseDatabase.getInstance()
            } catch (e: Throwable) {
                Log.e(TAG, "FirebaseDatabase.getInstance() failed", e)
                null
            }
        } else null
    }
}

package com.example.campusbuddy

import android.app.Application
import android.content.res.Configuration
import com.example.campusbuddy.utils.LANG_ENGLISH
import java.util.Locale

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set default language from preferences
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val lang = sharedPref.getString("APP_LANGUAGE", LANG_ENGLISH) ?: LANG_ENGLISH
        setAppLanguage(lang)

        // Suppress hidden API warnings
        try {
            val methods = Class.forName("dalvik.system.CloseGuard")
                .getDeclaredMethod("setEnabled", Boolean::class.javaPrimitiveType)
            methods.invoke(null, true)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}
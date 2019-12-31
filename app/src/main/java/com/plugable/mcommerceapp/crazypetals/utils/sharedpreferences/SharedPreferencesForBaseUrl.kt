package com.plugable.mcommerceapp.crazypetals.utils.sharedpreferences

import android.content.Context
import com.google.gson.Gson
import com.plugable.mcommerceapp.crazypetals.mcommerce.models.Host
import com.plugable.mcommerceapp.crazypetals.utils.constants.SharedPreferences.HOST_FILE_NAME
import com.plugable.mcommerceapp.crazypetals.utils.constants.SharedPreferences.HOST_NAME_FETCH_MODEL

/**
 * shared preference class for base url data persistence
 */
class SharedPreferencesForBaseUrl constructor(context: Context) {

    private var appSharedPreferences: android.content.SharedPreferences? = null
    private lateinit var gson: Gson


    var tokenDataPreference: Host.ApiConfig?
        get() {
            val tokenDataString = appSharedPreferences!!.getString(HOST_NAME_FETCH_MODEL, null)
            return gson.fromJson(tokenDataString, Host.ApiConfig::class.java)
        }
        set(tokenData) {
            val tokenDataString = gson.toJson(tokenData)
            appSharedPreferences!!.edit().putString(HOST_NAME_FETCH_MODEL, tokenDataString).apply()
        }

    init {
        if (appSharedPreferences == null) {
            appSharedPreferences = context.getSharedPreferences(HOST_FILE_NAME, Context.MODE_PRIVATE)
            gson = Gson()
        }
    }

    companion object {

        fun getInstance(context: Context): SharedPreferencesForBaseUrl {
            return SharedPreferencesForBaseUrl(context)
        }
    }
}
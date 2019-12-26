package com.plugable.mcommerceapp.cpmvp1.utils.sharedpreferences

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.ApplicationTheme
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetCartResponse
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.GetMyProfile
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.APPLICATION_THEME_OBJECT
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.FILE_NAME
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.HOST_URL
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.IS_USER_LOGGED_IN
import com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.IS_USER_SKIPPED_LOGIN


/**
 * shared preference class for data persistence
 */
class SharedPreferences private constructor(context: Context) {

    private var appSharedPreferences: android.content.SharedPreferences? = null
    private lateinit var gson: Gson

    var themeDataPreference: ApplicationTheme.Data?
        get() {
            val tokenDataString = appSharedPreferences!!.getString(APPLICATION_THEME_OBJECT, null)
            return gson.fromJson(tokenDataString, ApplicationTheme.Data::class.java)
        }
        set(tokenData) {
            val tokenDataString = gson.toJson(tokenData)
            appSharedPreferences!!.edit().putString(APPLICATION_THEME_OBJECT, tokenDataString)
                .apply()
        }

    fun setStringValue(KEY_VALUE: String, value: String) {
        appSharedPreferences!!.edit().putString(KEY_VALUE, value).apply()
    }

    fun getStringValue(KEY_VALUE: String): String? {
        return appSharedPreferences!!.getString(KEY_VALUE, "")
    }


    fun setCartCountString( value: String) {
        appSharedPreferences!!.edit().putString(com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT, value).apply()
    }

    fun getCartCountString(): String? {
        return appSharedPreferences!!.getString(com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_COUNT, "0")
    }



    fun setProfile(profile: GetMyProfile.Data) {
        appSharedPreferences!!.edit().putString(
            com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.PROFILE,
            Gson().toJson(profile)
        ).apply()
    }

    fun getProfile(): GetMyProfile.Data? {
        val existingValue = appSharedPreferences!!.getString(
            com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.PROFILE,
            ""
        )
        if (TextUtils.isEmpty(existingValue)) {
            return null
        } else {
            return Gson().fromJson(
                existingValue, object : TypeToken<GetMyProfile.Data>() {}.type
            )
        }
    }

    fun setAddToCartData(body: GetCartResponse) {
        appSharedPreferences!!.edit().putString(
            com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_DATA,
            Gson().toJson(body)
        ).apply()
    }

    fun getAddtoCartData(): GetCartResponse? {
        val cartData = appSharedPreferences!!.getString(
            com.plugable.mcommerceapp.cpmvp1.utils.constants.SharedPreferences.SHARED_PREFERENCES_CART_DATA,
            ""
        )

        return if (TextUtils.isEmpty(cartData)) {
            null
        } else {
            return try {
                Gson().fromJson(
                    cartData,
                    object : TypeToken<GetCartResponse>() {}.rawType
                ) as GetCartResponse?

            } catch (e: Exception) {
                null
            }
        }
    }

    var hostUrl: String?
        get() {
            return appSharedPreferences!!.getString(HOST_URL, "")
        }
        set(url) {
            appSharedPreferences!!.edit().putString(HOST_URL, url).apply()
        }


    var isUserSkippedLogin: Boolean
        get() {
            return appSharedPreferences!!.getBoolean(IS_USER_SKIPPED_LOGIN, false)
        }
        set(isSkip) {
            appSharedPreferences!!.edit().putBoolean(IS_USER_SKIPPED_LOGIN, isSkip).apply()
        }

    var isUserLoggedIn: Boolean
        get() {
            return appSharedPreferences!!.getBoolean(IS_USER_LOGGED_IN, false)
        }
        set(isSkip) {
            appSharedPreferences!!.edit().putBoolean(IS_USER_LOGGED_IN, isSkip).apply()
        }


    init {
        if (appSharedPreferences == null) {
            appSharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            gson = Gson()
        }
    }

    companion object {

        fun getInstance(context: Context): SharedPreferences {
            return SharedPreferences(context)
        }
    }


}


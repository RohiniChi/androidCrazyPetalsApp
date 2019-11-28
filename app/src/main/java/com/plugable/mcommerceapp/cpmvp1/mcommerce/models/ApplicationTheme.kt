package com.plugable.mcommerceapp.cpmvp1.mcommerce.models

/**
 * [ApplicationTheme] is a model class for dynamic theme setting
 *
 * @property data
 * @property message
 * @property statusCode
 */

data class ApplicationTheme(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val appLogoURL: String,
        val appName: String,
        val currencySymbol: String,
        val primaryColor: String,
        val secondryColor: String,
        val statusBarColor: String,
        val tertiaryColor: String,
        val textColor: String
    )
}
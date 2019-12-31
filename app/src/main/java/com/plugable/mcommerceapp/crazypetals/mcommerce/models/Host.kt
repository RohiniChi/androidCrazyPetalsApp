package com.plugable.mcommerceapp.crazypetals.mcommerce.models

/**
 * [Host] is a model class for fetching host url
 *
 * @property apiConfig
 */


data class Host(
    val apiConfig: List<ApiConfig>
) {
    data class ApiConfig(
        val devBaseUrl: String,
        val testBaseUrl: String,
        val prodBaseUrl: String,
        val projectCode: String,
        val description: String
    )
}
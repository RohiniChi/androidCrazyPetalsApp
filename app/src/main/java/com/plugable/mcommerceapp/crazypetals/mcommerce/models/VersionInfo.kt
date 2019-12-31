package com.plugable.mcommerceapp.crazypetals.mcommerce.models

/*
data class VersionInfo(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val appId: String,
        val currentLiveVersion: Boolean,
        val date: String,
        val id: Int,
        val updateType: String,
        val versionNumber: Int
    )
}*/

data class VersionInfo(
    val `data`: Data,
    val message: String,
    val statusCode: String
) {
    data class Data(
        val currentLiveVersion: Boolean,
        val date: String,
        val id: Int,
        val updateType: String,
        val versionNumber: String
    )
}
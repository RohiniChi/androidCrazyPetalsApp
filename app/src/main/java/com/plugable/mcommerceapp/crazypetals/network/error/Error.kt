package com.plugable.mcommerceapp.crazypetals.network.error

import android.text.TextUtils
import retrofit2.HttpException
import java.io.IOException
import java.util.*

class Error(private val error: Throwable?) : Throwable(error) {

    fun getErrorMessage(): String {
        if (this.error is IOException) return NETWORK_ERROR_MESSAGE
        if (this.error !is HttpException) return DEFAULT_ERROR_MESSAGE

        val response = this.error.response()
        val status = getJsonStringFromResponse(response!!)
        if (!TextUtils.isEmpty(status)) return status

        val headers = response.headers().toMultimap()
        if (headers.containsKey(ERROR_MESSAGE_HEADER))
            return Objects.requireNonNull<List<String>>(headers[ERROR_MESSAGE_HEADER])[0]

        return DEFAULT_ERROR_MESSAGE
    }

    override fun getLocalizedMessage(): String? {
        return error!!.message
    }

    private fun getJsonStringFromResponse(response: retrofit2.Response<*>): String {
        return response.toString()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Error?

        return if (error != null) error == that!!.error else that!!.error == null

    }

    override fun hashCode(): Int {
        return error?.hashCode() ?: 0
    }

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Something went wrong! Please try again."
        private const val NETWORK_ERROR_MESSAGE ="Oops! No Internet Connection"
        private const val ERROR_MESSAGE_HEADER = "Error-Message"
    }
}

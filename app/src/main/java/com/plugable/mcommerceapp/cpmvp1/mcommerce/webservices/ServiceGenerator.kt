import com.google.gson.GsonBuilder
import com.plugable.mcommerceapp.cpmvp1.BuildConfig
import com.plugable.mcommerceapp.cpmvp1.utils.application.App
import com.plugable.mcommerceapp.cpmvp1.utils.constants.WebApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * [ServiceGenerator] is an object(Singleton) class for creating single instance for Retrofit  object
 */
object ServiceGenerator {


    fun <API_SERVICE> createService(
        serviceClass: Class<API_SERVICE>
    ): API_SERVICE {


        val baseUrl = App.HostUrl

        // for accepting malformed JSON Format
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))

        var retrofit = builder.build()

        val logging = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)


        val httpClient = OkHttpClient.Builder()
            .connectTimeout(WebApi.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(WebApi.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WebApi.WRITE_TIMEOUT, TimeUnit.SECONDS)

        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging)
            builder.client(httpClient.build())
            retrofit = builder.build()
        }

        return retrofit.create(serviceClass)
    }

    fun <API_SERVICE> createHostnameService(
        serviceClass: Class<API_SERVICE>
    ): API_SERVICE {

        val hostUrl = BuildConfig.HOST_URL

        // for accepting malformed JSON Format
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val builder = Retrofit.Builder()
            .baseUrl(hostUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))

        var retrofit = builder.build()

        val logging = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)


        val httpClient = OkHttpClient.Builder()
            .connectTimeout(WebApi.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(WebApi.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WebApi.WRITE_TIMEOUT, TimeUnit.SECONDS)


        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging)
            builder.client(httpClient.build())
            retrofit = builder.build()
        }

        return retrofit.create(serviceClass)
    }
}
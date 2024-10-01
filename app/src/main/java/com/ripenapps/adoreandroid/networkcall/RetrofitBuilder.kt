package com.ripenapps.adoreandroid.networkcall

import com.ripenapps.adoreandroid.preferences.UserPreference
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object RetrofitBuilder {
//    private const val BASE_URL = "http://13.235.137.221:6870/"    //dev
    private const val BASE_URL = "https://adore-dating.com:6870/"   //live
    // Create a trust manager that does not validate certificate chains
    val trustAllCertificates = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            // Trust all client certificates
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            // Trust all server certificates
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    // Install the all-trusting trust manager
    val sslContext = SSLContext.getInstance("SSL")
    // Create an ssl socket factory with our all-trusting manager

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build() //Doesn't require the adapter
    }

    private val httpClient: OkHttpClient =
        OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(Interceptor(fun(chain: Interceptor.Chain): Response {
                val ongoing: Request.Builder = chain.request().newBuilder()
                ongoing.addHeader("Accept", "application/json")
                ongoing.addHeader("Content-Type", "application/json")
                ongoing.addHeader("accept-language", UserPreference.savedLanguageCode)
                if (UserPreference.drugKey.isNotEmpty())
                    ongoing.addHeader("Authorization", UserPreference.drugKey)
                else if (UserPreference.token.isNotEmpty())
                    ongoing.addHeader("Authorization", "Bearer " + UserPreference.token)
                return chain.proceed(ongoing.build())
            })).addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .sslSocketFactory(getSSLContext(), trustAllCertificates)
            .hostnameVerifier { _, _ -> true }
            .build()

    fun getSSLContext():SSLSocketFactory{
        sslContext.init(null, arrayOf<TrustManager>(trustAllCertificates), SecureRandom())
        return sslContext.socketFactory
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
    /*fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCertificates = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    // Trust all client certificates
                }

                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    // Trust all server certificates
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL").init(null, arrayOf<TrustManager>(trustAllCertificates), java.security.SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            // Build the OkHttp client with the custom SSL settings
            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCertificates)
                .hostnameVerifier { _, _ -> true } // Disable hostname verification
                .build()

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }*/
}
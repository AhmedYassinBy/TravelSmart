package com.example.voyageproject.network

import android.content.Context
import android.util.Log
import com.example.voyageproject.model.Circuit
import com.example.voyageproject.model.Flight
import com.example.voyageproject.model.Reservation
import com.example.voyageproject.model.ReservationDetails
import com.example.voyageproject.model.ReservationDetailsDeserializer
import com.example.voyageproject.utils.CircuitDeserializer
import com.example.voyageproject.utils.FlightDeserializer
import com.example.voyageproject.utils.ReservationDeserializer
import com.example.voyageproject.utils.SessionManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Use 10.0.2.2 for Android emulator (maps to host's localhost)
    // Use your machine's IP address for physical device testing
    private const val BASE_URL = "http://10.0.2.2:8085/"
    private var context: Context? = null

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    // Gson avec CircuitDeserializer, FlightDeserializer, ReservationDeserializer et ReservationDetailsDeserializer
    private val gson = GsonBuilder()
        .registerTypeAdapter(Circuit::class.java, CircuitDeserializer())
        .registerTypeAdapter(Flight::class.java, FlightDeserializer())
        .registerTypeAdapter(Reservation::class.java, ReservationDeserializer())
        .registerTypeAdapter(ReservationDetails::class.java, ReservationDetailsDeserializer())
        .setLenient()
        .create()

    // Intercepteur simple qui ajoute l'email SEULEMENT si pas déjà présent
    private class SimpleAuthInterceptor(private val ctx: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val session = SessionManager(ctx)
            val email = session.getEmail()
            
            // Si pas d'email, utiliser un email par défaut pour les endpoints publics
            val emailToUse = email ?: "guest@app.com"
            
            // Vérifier si l'email est déjà dans l'URL
            val hasEmailParam = original.url.queryParameter("email") != null
            
            val url = if (!hasEmailParam) {
                // Ajouter l'email seulement s'il n'existe pas déjà
                original.url.newBuilder()
                    .addQueryParameter("email", emailToUse)
                    .build()
            } else {
                // Garder l'URL originale si l'email existe déjà
                original.url
            }
            
            val request = original.newBuilder()
                .url(url)
                .build()
            
            Log.d("HTTP", "📞 ${original.method} ${url}")
            Log.d("HTTP", "📧 Email: ${if (hasEmailParam) "déjà présent" else emailToUse}")
            
            val response = chain.proceed(request)
            Log.d("HTTP", "✅ Code: ${response.code}")
            
            return response
        }
    }

    // OkHttp avec intercepteur
    private val okHttpClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .apply {
                context?.let { ctx ->
                    addInterceptor(SimpleAuthInterceptor(ctx))
                }
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

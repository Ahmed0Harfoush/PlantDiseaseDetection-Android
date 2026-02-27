package com.example.plantdiseasedetection

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict")
    fun uploadImage(@Part image: MultipartBody.Part): Call<PredictionResponse>

    @FormUrlEncoded
    @POST("predict-url") // This was returning 404; ensure Flask has this route
    fun predictByUrl(@Field("url") imageUrl: String): Call<PredictionResponse>
}
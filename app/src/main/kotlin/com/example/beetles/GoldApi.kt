package com.example.beetles

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApi {
    @GET("scripts/xml_metall.asp")
    fun getGoldPrice(
        @Query("date_req1") dateReq1: String,
        @Query("date_req2") dateReq2: String
    ): Call<String>

    companion object {
        fun create(): GoldApi {
            return Retrofit.Builder()
                .baseUrl("https://www.cbr.ru/")
                .addConverterFactory(ToStringConverterFactory())
                .build()
                .create(GoldApi::class.java)
        }
    }
}

class ToStringConverterFactory : retrofit2.Converter.Factory() {
    override fun responseBodyConverter(
        type: java.lang.reflect.Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): retrofit2.Converter<okhttp3.ResponseBody, *>? {
        if (type == String::class.java) {
            return retrofit2.Converter { value -> value.string() }
        }
        return null
    }
}

package com.example.beetles

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

interface GoldApi {
    @GET("scripts/xml_metall.asp")
    fun getGoldPrice(): Call<String>

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

// Простой конвертер, чтобы получать ответ как String, а не парсить сложный XML
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
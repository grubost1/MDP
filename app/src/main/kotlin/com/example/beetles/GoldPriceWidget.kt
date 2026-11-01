package com.example.beetles

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoldPriceWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_gold_price)
        views.setTextViewText(R.id.tvGoldPrice, "Загрузка...")
        appWidgetManager.updateAppWidget(appWidgetId, views)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = awaitGoldPrice()
                val price = extractGoldPrice(response)
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.tvGoldPrice, "Au:\n$price ₽")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.tvGoldPrice, "Ошибка")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private suspend fun awaitGoldPrice(): String = suspendCancellableCoroutine { continuation ->
        GoldApi.create().getGoldPrice().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null) {
                    continuation.resume(response.body()!!, null)
                } else {
                    continuation.resumeWithException(Exception("Network error"))
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

    private fun extractGoldPrice(xml: String): String {
        // Ищем цену золота в XML ЦБ РФ
        val regex = Regex("<Name>Золото</Name>.*?<Price>([0-9.]+)</Price>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1) ?: "0.00"
    }
}
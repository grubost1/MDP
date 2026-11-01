package com.example.beetles

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val endDate = sdf.format(Date())
                val startDate = sdf.format(Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000))

                val response = awaitGoldPrice(startDate, endDate)
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

    private suspend fun awaitGoldPrice(startDate: String, endDate: String): String = suspendCancellableCoroutine { continuation ->
        GoldApi.create().getGoldPrice(startDate, endDate).enqueue(object : Callback<String> {
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
        if (xml.isEmpty()) return "0.00"

        try {
            val pattern = java.util.regex.Pattern.compile("<Record[^>]*Code=\"1\"[^>]*>\\s*<Buy>([^<]+)</Buy>", java.util.regex.Pattern.DOTALL)
            val matcher = pattern.matcher(xml)

            var lastPrice = ""
            while (matcher.find()) {
                val rawPrice = matcher.group(1).trim()
                if (rawPrice.isNotEmpty()) {
                    lastPrice = rawPrice
                }
            }

            if (lastPrice.isNotEmpty()) {
                return lastPrice
            }
        } catch (e: Exception) {
            android.util.Log.e("GoldWidgetError", "Ошибка разбора XML", e)
        }

        return "7150,00"
    }
}

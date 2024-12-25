package com.example.lab17

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var btnQuery: Button
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnQuery = findViewById(R.id.btnQuery)
        btnQuery.setOnClickListener {
            btnQuery.isEnabled = false
            fetchAirQualityData()
        }
    }

    // 使用 Coroutine 發送請求
    private fun fetchAirQualityData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://api.italkutalk.com/api/air"
                val request = Request.Builder().url(url).build()

                // 發送請求並獲取回應
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val json = response.body?.string()
                val myObject = Gson().fromJson(json, MyObject::class.java)

                // 顯示結果
                withContext(Dispatchers.Main) {
                    showResultDialog(myObject)
                }
            } catch (e: Exception) {
                // 處理異常
                withContext(Dispatchers.Main) {
                    btnQuery.isEnabled = true
                    Toast.makeText(this@MainActivity, "查詢失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 顯示查詢結果
    private fun showResultDialog(myObject: MyObject) {
        val items = myObject.result.records.map { record ->
            "地區：${record.SiteName}, 狀態：${record.Status}"
        }
        AlertDialog.Builder(this)
            .setTitle("臺北市空氣品質")
            .setItems(items.toTypedArray(), null)
            .setOnDismissListener {
                btnQuery.isEnabled = true
            }
            .show()
    }
}

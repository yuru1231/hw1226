package com.example.lab16_1

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // 初始化變數
    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText
    private lateinit var btnInsert: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnQuery: Button
    private lateinit var listView: ListView

    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 設定邊距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化資料庫
        dbrw = MyDBHelper(this).writableDatabase

        // 綁定視圖
        initViews()

        // 初始化 Adapter 與 ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        // 設定按鈕監聽器
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close() // 關閉資料庫
    }

    // 綁定視圖
    private fun initViews() {
        edBook = findViewById(R.id.edBook)
        edPrice = findViewById(R.id.edPrice)
        btnInsert = findViewById(R.id.btnInsert)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnQuery = findViewById(R.id.btnQuery)
        listView = findViewById(R.id.listView)
    }

    // 設定按鈕監聽器
    private fun setListeners() {
        btnInsert.setOnClickListener {
            if (isInputValid()) {
                executeSQL(
                    "INSERT INTO myTable(book, price) VALUES(?, ?)",
                    arrayOf(edBook.text.toString(), edPrice.text.toString())
                )
                showToast("新增成功: 書名 ${edBook.text}, 價格 ${edPrice.text}")
                clearInputs()
            }
        }

        btnUpdate.setOnClickListener {
            if (isInputValid()) {
                executeSQL(
                    "UPDATE myTable SET price = ? WHERE book LIKE ?",
                    arrayOf(edPrice.text.toString(), edBook.text.toString())
                )
                showToast("更新成功: 書名 ${edBook.text}, 價格 ${edPrice.text}")
                clearInputs()
            }
        }

        btnDelete.setOnClickListener {
            if (edBook.text.isBlank()) {
                showToast("書名請勿留空")
            } else {
                executeSQL(
                    "DELETE FROM myTable WHERE book LIKE ?",
                    arrayOf(edBook.text.toString())
                )
                showToast("刪除成功: 書名 ${edBook.text}")
                clearInputs()
            }
        }

        btnQuery.setOnClickListener {
            val query = if (edBook.text.isBlank()) {
                "SELECT * FROM myTable"
            } else {
                "SELECT * FROM myTable WHERE book LIKE ?"
            }
            val args = if (edBook.text.isBlank()) null else arrayOf(edBook.text.toString())
            querySQL(query, args)
        }
    }

    // 執行 SQL 語句（增刪改）
    private fun executeSQL(query: String, args: Array<String>? = null) {
        try {
            dbrw.execSQL(query, args)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("操作失敗: ${e.message}")
        }
    }

    // 查詢 SQL 語句
    private fun querySQL(query: String, args: Array<String>? = null) {
        val cursor = dbrw.rawQuery(query, args)
        cursor?.let {
            items.clear()
            showToast("共有 ${cursor.count} 筆資料")
            while (cursor.moveToNext()) {
                val book = cursor.getString(0)
                val price = cursor.getInt(1)
                items.add("書名: $book\t\t價格: $price")
            }
            adapter.notifyDataSetChanged()
            cursor.close()
        }
    }

    // 驗證輸入是否有效
    private fun isInputValid(): Boolean {
        return if (edBook.text.isBlank() || edPrice.text.isBlank()) {
            showToast("欄位請勿留空")
            false
        } else {
            true
        }
    }

    // 顯示提示訊息
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 清空輸入
    private fun clearInputs() {
        edBook.text.clear()
        edPrice.text.clear()
    }
}

package com.example.lab11

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText
    private lateinit var btnQuery: Button
    private lateinit var btnInsert: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var listView: ListView

    private lateinit var adapter: ArrayAdapter<String>
    private val items = ArrayList<String>()

    private var dbrw: SQLiteDatabase? = null

    override fun onDestroy() {
        super.onDestroy()
        dbrw?.close() // 關閉資料庫
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化元件
        edBook = findViewById(R.id.ed_book)
        edPrice = findViewById(R.id.ed_price)
        btnQuery = findViewById(R.id.btn_query)
        btnInsert = findViewById(R.id.btn_insert)
        btnUpdate = findViewById(R.id.btn_update)
        btnDelete = findViewById(R.id.btn_delete)
        listView = findViewById(R.id.listView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        // 初始化資料庫
        dbrw = MyDBHelper(this).writableDatabase

        // 新增資料
        btnInsert.setOnClickListener {
            if (edBook.text.isBlank() || edPrice.text.isBlank()) {
                showToast("欄位請勿留空")
            } else {
                try {
                    dbrw?.execSQL(
                        "INSERT INTO myTable(book, price) VALUES(?, ?)",
                        arrayOf(edBook.text.toString(), edPrice.text.toString())
                    )
                    showToast("新增成功: 書名 ${edBook.text}, 價格 ${edPrice.text}")
                    clearFields()
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("新增失敗: ${e.message}")
                }
            }
        }

        // 更新資料
        btnUpdate.setOnClickListener {
            if (edBook.text.isBlank() || edPrice.text.isBlank()) {
                showToast("欄位請勿留空")
            } else {
                try {
                    dbrw?.execSQL(
                        "UPDATE myTable SET price = ? WHERE book LIKE ?",
                        arrayOf(edPrice.text.toString(), edBook.text.toString())
                    )
                    showToast("更新成功: 書名 ${edBook.text}, 價格 ${edPrice.text}")
                    clearFields()
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("更新失敗: ${e.message}")
                }
            }
        }

        // 刪除資料
        btnDelete.setOnClickListener {
            if (edBook.text.isBlank()) {
                showToast("書名請勿留空")
            } else {
                try {
                    dbrw?.execSQL(
                        "DELETE FROM myTable WHERE book LIKE ?",
                        arrayOf(edBook.text.toString())
                    )
                    showToast("刪除成功: 書名 ${edBook.text}")
                    clearFields()
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("刪除失敗: ${e.message}")
                }
            }
        }

        // 查詢資料
        btnQuery.setOnClickListener {
            val query = if (edBook.text.isBlank()) {
                "SELECT * FROM myTable"
            } else {
                "SELECT * FROM myTable WHERE book LIKE ?"
            }
            val args = if (edBook.text.isBlank()) null else arrayOf(edBook.text.toString())
            performQuery(query, args)
        }
    }

    // 執行查詢
    private fun performQuery(query: String, args: Array<String>?) {
        val cursor = dbrw?.rawQuery(query, args)
        cursor?.let {
            items.clear()
            showToast("共有 ${it.count} 筆資料")
            while (it.moveToNext()) {
                items.add("書籍: ${it.getString(0)}\t\t價格: ${it.getString(1)}")
            }
            adapter.notifyDataSetChanged()
            it.close()
        }
    }

    // 顯示提示訊息
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 清空輸入欄位
    private fun clearFields() {
        edBook.text.clear()
        edPrice.text.clear()
    }
}

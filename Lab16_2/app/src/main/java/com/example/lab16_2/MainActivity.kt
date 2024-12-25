package com.example.lab16_2

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val items: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>
    private val uri: Uri = Uri.parse("content://com.example.lab16")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        setListeners()
    }

    private fun setListeners() {
        val edBook = findViewById<EditText>(R.id.edBook)
        val edPrice = findViewById<EditText>(R.id.edPrice)

        // 新增資料
        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            handleInsert(edBook, edPrice)
        }

        // 更新資料
        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            handleUpdate(edBook, edPrice)
        }

        // 刪除資料
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            handleDelete(edBook)
        }

        // 查詢資料
        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            handleQuery(edBook)
        }
    }

    private fun handleInsert(edBook: EditText, edPrice: EditText) {
        val name = edBook.text.toString().trim()
        val price = edPrice.text.toString().trim()
        if (!validateInput(name, price)) return

        val values = ContentValues().apply {
            put("book", name)
            put("price", price)
        }
        val contentUri = contentResolver.insert(uri, values)

        if (contentUri != null) {
            showToast("新增成功: 書名 $name, 價格 $price")
            clearInputs(edBook, edPrice)
        } else {
            showToast("新增失敗")
        }
    }

    private fun handleUpdate(edBook: EditText, edPrice: EditText) {
        val name = edBook.text.toString().trim()
        val price = edPrice.text.toString().trim()
        if (!validateInput(name, price)) return

        val values = ContentValues().apply {
            put("price", price)
        }
        val count = contentResolver.update(uri, values, "book = ?", arrayOf(name))

        if (count > 0) {
            showToast("更新成功: 書名 $name, 價格 $price")
            clearInputs(edBook, edPrice)
        } else {
            showToast("更新失敗")
        }
    }

    private fun handleDelete(edBook: EditText) {
        val name = edBook.text.toString().trim()
        if (name.isEmpty()) {
            showToast("書名請勿留空")
            return
        }

        val count = contentResolver.delete(uri, "book = ?", arrayOf(name))
        if (count > 0) {
            showToast("刪除成功: 書名 $name")
            clearInputs(edBook)
        } else {
            showToast("刪除失敗")
        }
    }

    private fun handleQuery(edBook: EditText) {
        val name = edBook.text.toString().trim()
        val selection = if (name.isEmpty()) null else "book = ?"
        val selectionArgs = if (name.isEmpty()) null else arrayOf(name)

        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)
        cursor?.use {
            items.clear()
            if (it.count > 0) {
                showToast("共查詢到 ${it.count} 筆資料")
                while (it.moveToNext()) {
                    val book = it.getString(it.getColumnIndexOrThrow("book"))
                    val price = it.getInt(it.getColumnIndexOrThrow("price"))
                    items.add("書名: $book\t\t價格: $price")
                }
            } else {
                showToast("查無資料")
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun validateInput(name: String, price: String): Boolean {
        if (name.isEmpty() || price.isEmpty()) {
            showToast("欄位請勿留空")
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs(vararg fields: EditText) {
        fields.forEach { it.text.clear() }
    }
}

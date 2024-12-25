import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.example.lab16_1.MyDBHelper

class MyContentProvider : ContentProvider() {

    private lateinit var dbrw: SQLiteDatabase

    companion object {
        const val AUTHORITY = "com.example.lab16_1"
        const val TABLE_NAME = "myTable"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TABLE_NAME")

        private const val URI_CODE_TABLE = 1
        private const val URI_CODE_ROW = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE_NAME, URI_CODE_TABLE)
            addURI(AUTHORITY, "$TABLE_NAME/#", URI_CODE_ROW)
        }
    }

    override fun onCreate(): Boolean {
        val context = context ?: return false
        dbrw = MyDBHelper(context).writableDatabase
        return true
    }

    // 插入資料
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != URI_CODE_TABLE) return null
        if (values == null) return null
        return try {
            val rowId = dbrw.insert(TABLE_NAME, null, values)
            if (rowId != -1L) {
                ContentUris.withAppendedId(CONTENT_URI, rowId)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 更新資料
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        if (uriMatcher.match(uri) != URI_CODE_TABLE) return 0
        if (values == null || selection == null) return 0
        return try {
            dbrw.update(TABLE_NAME, values, selection, selectionArgs)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // 刪除資料
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (uriMatcher.match(uri) != URI_CODE_TABLE) return 0
        if (selection == null) return 0
        return try {
            dbrw.delete(TABLE_NAME, selection, selectionArgs)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // 查詢資料
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        if (uriMatcher.match(uri) != URI_CODE_TABLE) return null
        return try {
            dbrw.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 返回 MIME 類型
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            URI_CODE_TABLE -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE_NAME"
            URI_CODE_ROW -> "vnd.android.cursor.item/vnd.$AUTHORITY.$TABLE_NAME"
            else -> null
        }
    }
}

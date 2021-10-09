package com.example.qrcode.common.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FileManager @Inject constructor(@ApplicationContext private var context: Context?) {
    fun getRealPath(fileUri: Uri?): String? {
        var realPath: String? = null
        // SDK < API11
        realPath = if (Build.VERSION.SDK_INT < 11) {
            fileUri?.let { getRealPathFromURIBelowAPI11(it) }
        } else if (Build.VERSION.SDK_INT < 19) {
            fileUri?.let { getRealPathFromURIAPI11to18(it) }
        } else {
            fileUri?.let { getRealPathFromURIAPI19(it) }
        }
        if (realPath == null) {
            realPath = fileUri?.path
        }
        return realPath
    }


    @SuppressLint("NewApi")
    private fun getRealPathFromURIAPI11to18(contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var result: String? = null
        try {
            val cursorLoader = context?.let { CursorLoader(it, contentUri, proj, null, null, null) }
            val cursor: Cursor? = cursorLoader?.loadInBackground()
            if (cursor != null) {
                val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                result = cursor.getString(column_index)
                cursor.close()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return result
    }

    private fun getRealPathFromURIBelowAPI11(contentUri: Uri): String? {
        var result = ""
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? =
                context?.contentResolver?.query(contentUri, proj, null, null, null)
            var column_index = 0
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                result = cursor.getString(column_index)
                cursor.close()
                return result
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return result
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * URIからファイルパスを取得します。これにより、ストレージアクセスのパスが取得されます
     * フレームワークドキュメント、およびMediaStoreとの_dataフィールド
     * その他のファイルベースのContentProvider。
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    private fun getRealPathFromURIAPI19(uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.getLastPathSegment() else getDataColumn(
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
            return uri.getPath()
        }
        return uri.getPath()
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * このURIのデータ列の値を取得します。これは
     * MediaStore Uris、およびその他のファイルベースのContentProvider。
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = uri?.let {
                context?.contentResolver?.query(
                    it, projection, selection, selectionArgs,
                    null
                )
            }
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.getAuthority()
    }

    fun isExistsFile(pathOfImage: String?) = if(pathOfImage != null) File(pathOfImage).exists() else false


    private val K: Long = 1000 // 1KB

    val M = K * K // 1MB

    private val G = M * K // 1GB

    private val T = G * K // 1TB
}
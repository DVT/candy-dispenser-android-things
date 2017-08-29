package za.co.riggaroo.iwantcandy.utils

import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File

/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
internal object FileUtils {
    private val MEDIA_SCHEME = "com.android.providers.media.documents"

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        if (isKitKat && isMediaDocumentAuthority(uri)) {
            val documentId = DocumentsContract.getDocumentId(uri) // e.g. "image:1234"
            val parts = documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = parts[0]

            val contentUri: Uri
            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                // reject video or audio documents
                return null
            }

            // query content resolver for MediaStore id column
            val selection = "_id=?"
            val args = arrayOf(parts[1])
            return resolveFilePath(context, contentUri, selection, args)
        } else if (isContentScheme(uri)) {
            return resolveFilePath(context, uri, null, null)
        } else if (isFileScheme(uri)) {
            return uri.path
        }
        return null
    }

    fun isMediaDocumentAuthority(uri: Uri): Boolean {
        return MEDIA_SCHEME.equals(uri.authority, ignoreCase = true)
    }

    fun isContentScheme(uri: Uri): Boolean {
        return ContentResolver.SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)
    }

    fun isFileScheme(uri: Uri): Boolean {
        return ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)
    }

    fun resolveFilePath(context: Context, uri: Uri, selection: String?, args: Array<String>?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        try {
            cursor = context.contentResolver.query(uri, projection, selection, args, null)
            if (cursor != null && cursor.moveToFirst()) {
                val i = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(i)
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        return null
    }

    /**
     * @return The MIME type for the given file.
     */
    fun getMimeType(file: File): String {
        val ext = getExtension(file.name)
        if (!TextUtils.isEmpty(ext)) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        }
        // default from https://dev.twitter.com/rest/public/uploading-media
        return "application/octet-stream"
    }

    /**
     * @return the extension of the given file name, excluding the dot. For example, "png", "jpg".
     */
    fun getExtension(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val i = filename.lastIndexOf(".")
        return if (i < 0) "" else filename.substring(i + 1)
    }
}
package za.co.riggaroo.iwantcandy.utils

import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File

/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
internal object FileUtils {
    private val MEDIA_SCHEME = "com.android.providers.media.documents"
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
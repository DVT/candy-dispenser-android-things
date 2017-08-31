package za.co.riggaroo.iwantcandy.repo

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.models.Media
import com.twitter.sdk.android.core.models.Tweet
import okhttp3.MediaType
import okhttp3.RequestBody
import za.co.riggaroo.iwantcandy.BuildConfig
import za.co.riggaroo.iwantcandy.utils.FileUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
class TwitterRepository(private val dependencyProvider: DependencyProvider, private val context: Context,
                        private val tweetTextOptions: Array<String>, private val tweetEmojiOptions: Array<String>) {

    private val TAG: String? = "TwitterRepo"
    private val randomGenerator = Random()

    fun sendTweet(photo: Bitmap, callback: TweetCallback) {
        val authToken = TwitterAuthToken(BuildConfig.TWITTER_API_TOKEN, BuildConfig.TWITTER_API_SECRET)
        val twitterSession = TwitterSession(authToken, BuildConfig.TWITTER_USER_ID, BuildConfig.TWITTER_USERNAME)


        val file = File(context.cacheDir, "smile_" + System.currentTimeMillis() + ".jpg")
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapdata = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        uploadTweet(twitterSession, getRandomTweetText(), file, callback)

    }

    private fun getRandomTweetText(): String {
        val randomNumber = randomGenerator.nextInt(tweetTextOptions.size)
        val anotherRandomNumber = randomGenerator.nextInt(tweetEmojiOptions.size)
        return tweetTextOptions[randomNumber] + tweetEmojiOptions[anotherRandomNumber]
    }

    private fun uploadTweet(session: TwitterSession, text: String, imageUri: File, callback: TweetCallback) {

        uploadMedia(session, imageUri, object : Callback<Media>() {
            override fun success(result: Result<Media>) {
                uploadTweetWithMedia(session, text, result.data.mediaIdString, callback)
            }

            override fun failure(exception: TwitterException) {
                callback.onError(exception)
            }

        })
    }

    internal fun uploadTweetWithMedia(session: TwitterSession, text: String, mediaId: String?, callback: TweetCallback) {
        val client = dependencyProvider.getTwitterApiClient(session)

        client.statusesService.update(text, null, null, null, null, null, null, true, mediaId)
                .enqueue(
                        object : Callback<Tweet>() {
                            override fun success(result: Result<Tweet>) {
                                callback.onSuccess()
                                Log.d(TAG, "Post tweet successful")
                            }

                            override fun failure(exception: TwitterException) {
                                callback.onError(exception)
                                Log.e(TAG, "Post Tweet failed:" + exception.message, exception)
                            }
                        })
    }


    private fun uploadMedia(session: TwitterSession, file: File, callback: Callback<Media>) {
        val client = dependencyProvider.getTwitterApiClient(session)

        val mimeType = FileUtils.getMimeType(file)
        val media = RequestBody.create(MediaType.parse(mimeType), file)

        client.mediaService.upload(media, null, null).enqueue(callback)
    }


    class DependencyProvider {

        fun getTwitterApiClient(session: TwitterSession): TwitterApiClient {
            return TwitterCore.getInstance().getApiClient(session)
        }
    }

    interface TweetCallback {
        fun onSuccess()
        fun onError(exception: Exception)
    }
}

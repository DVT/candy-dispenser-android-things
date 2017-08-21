package za.co.riggaroo.iwantcandy.twitter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.models.Media
import com.twitter.sdk.android.core.models.Tweet
import okhttp3.MediaType
import okhttp3.RequestBody
import za.co.riggaroo.iwantcandy.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
class TwitterRepository(private val dependencyProvider: DependencyProvider, private val context: Context) {

    private val TWEET_TEXT = arrayOf(
            "I just got some candy from #TheCandyBot",
            "I smiled for 4 pieces of candy #OhWow #TheCandyBot",
            "Did someone say candy? #TheCandyBot",
            "Smiling for Candy #TheCandyBot",
            "Sugar Rush - free candy #TheCandyBot")


    fun sendTweet(photo: Bitmap) {
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
        uploadTweet(twitterSession, getRandomTweetText(), file)

    }

    private fun getRandomTweetText(): String {
        val randomNumber = Random().nextInt(TWEET_TEXT.size)
        return TWEET_TEXT[randomNumber]
    }

    internal fun uploadTweet(session: TwitterSession, text: String, imageUri: File?) {
        if (imageUri != null) {
            uploadMedia(session, imageUri, object : Callback<Media>() {
                override fun success(result: Result<Media>) {
                    uploadTweetWithMedia(session, text, result.data.mediaIdString)
                }

                override fun failure(exception: TwitterException) {
                    fail(exception)
                }

            })
        } else {
            uploadTweetWithMedia(session, text, null)
        }
    }

    internal fun uploadTweetWithMedia(session: TwitterSession, text: String, mediaId: String?) {
        val client = dependencyProvider.getTwitterApiClient(session)

        client.statusesService.update(text, null, null, null, null, null, null, true, mediaId)
                .enqueue(
                        object : Callback<Tweet>() {
                            override fun success(result: Result<Tweet>) {
                                sendSuccessBroadcast(result.data.getId())

                            }

                            override fun failure(exception: TwitterException) {
                                fail(exception)
                            }
                        })
    }

    private fun sendSuccessBroadcast(id: Long) {

    }

    private fun uploadMedia(session: TwitterSession, file: File, callback: Callback<Media>) {
        val client = dependencyProvider.getTwitterApiClient(session)

        val mimeType = FileUtils.getMimeType(file)
        val media = RequestBody.create(MediaType.parse(mimeType), file)

        client.mediaService.upload(media, null, null).enqueue(callback)
    }

    private val TAG: String? = "TwitterRepo"

    private fun fail(e: TwitterException) {
        //   sendFailureBroadcast(intent)
        Log.e(TAG, "Post Tweet failed", e)

    }

    private fun sendFailureBroadcast(intent: Any) {

    }

    class DependencyProvider {

        fun getTwitterApiClient(session: TwitterSession): TwitterApiClient {
            return TwitterCore.getInstance().getApiClient(session)
        }
    }
}

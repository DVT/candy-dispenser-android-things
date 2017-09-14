package za.co.riggaroo.iwantcandy.repo

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterSession
import timber.log.Timber
import za.co.riggaroo.iwantcandy.BuildConfig
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * @author rebeccafranks
 * @since 2017/08/31.
 */

class TweetJob(private val twitterRepository: TwitterRepository) : Job() {


    override fun onRunJob(params: Params?): Result {
        val authToken = TwitterAuthToken(BuildConfig.TWITTER_API_TOKEN, BuildConfig.TWITTER_API_SECRET)
        val twitterSession = TwitterSession(authToken, BuildConfig.TWITTER_USER_ID, BuildConfig.TWITTER_USERNAME)

        val fileName = params?.extras?.getString(PHOTO_LOCATION, null)
        if (fileName == null) {
            Timber.d("Filename is null")
            return Result.SUCCESS
        }

        val file = File(context.cacheDir, fileName)
        val tweetText = params.extras.getString(TWEET_TEXT, "")
        val countDownLatch = CountDownLatch(1)
        var shouldRetry = Retry(false)

        twitterRepository.uploadTweet(twitterSession, tweetText, file, object : TwitterRepository.TweetCallback {
            override fun onSuccess() {
                shouldRetry = Retry(false)
                Timber.d("TweetJob - Upload tweet successful")
                countDownLatch.countDown()

            }

            override fun onError(exception: Exception) {
                shouldRetry = Retry(true)
                Timber.e("TweetJob - Upload tweet failed:" + exception.message, exception)
                countDownLatch.countDown()

            }

        })
        try {
            countDownLatch.await(10000, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Timber.d("Interrupted:", e)
        }
        return if (shouldRetry.shouldRetry) {
            Result.RESCHEDULE
        } else {
            Result.SUCCESS
        }


    }

    companion object {
        val JOB_TAG = "tweet_job"
        private val PHOTO_LOCATION = "photo_location"

        private val TWEET_TEXT = "tweet_text"

        fun scheduleTweetSendingJob(tweetText: String, photoLocation: String) {
            val extras = PersistableBundleCompat()
            extras.putString(TWEET_TEXT, tweetText)
            extras.putString(PHOTO_LOCATION, photoLocation)
            JobRequest.Builder(JOB_TAG)
                    .setExecutionWindow(30_000L, 40_000L)

                    .setBackoffCriteria(30_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setPersisted(true)
                    .setExtras(extras)
                    .build()
                    .schedule()
        }
    }

}

data class Retry(val shouldRetry: Boolean)
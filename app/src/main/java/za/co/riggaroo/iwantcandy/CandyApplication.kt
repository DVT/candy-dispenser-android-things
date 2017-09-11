package za.co.riggaroo.iwantcandy

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.evernote.android.job.JobManager
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import za.co.riggaroo.iwantcandy.repo.TweetJobCreator
import za.co.riggaroo.iwantcandy.repo.TwitterRepository


/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
class CandyApplication : Application() {

    private val DEFAULT_FONT_PATH = "fonts/LifeSavers-Regular.ttf"

    override fun onCreate() {
        super.onCreate()
        val config = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(BuildConfig.TWITTER_CONSUMER_KEY, BuildConfig.TWITTER_CONSUMER_SECRET))
                .debug(BuildConfig.DEBUG)
                .build()
        Twitter.initialize(config)
        Fabric.with(this, Crashlytics())

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath(DEFAULT_FONT_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        JobManager.create(this).addJobCreator(TweetJobCreator(TwitterRepository(TwitterRepository.DependencyProvider(),
                this,
                resources.getStringArray(R.array.tweet_text),
                resources.getStringArray(R.array.tweet_random_emoji))))
    }
}
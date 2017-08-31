package za.co.riggaroo.iwantcandy

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig


/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
class CandyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(BuildConfig.TWITTER_CONSUMER_KEY, BuildConfig.TWITTER_CONSUMER_SECRET))
                .debug(BuildConfig.DEBUG)
                .build()
        Twitter.initialize(config)
        Fabric.with(this,  Crashlytics())

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/LifeSavers-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}
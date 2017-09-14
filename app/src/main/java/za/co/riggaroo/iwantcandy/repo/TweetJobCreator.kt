package za.co.riggaroo.iwantcandy.repo

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


/**
 * @author rebeccafranks
 * @since 2017/09/11.
 */
class TweetJobCreator(private val twitterRepository: TwitterRepository) : JobCreator {

    override fun create(tag: String): Job? {
        return when (tag) {
            TweetJob.JOB_TAG -> TweetJob(twitterRepository)
            else -> null
        }
    }
}
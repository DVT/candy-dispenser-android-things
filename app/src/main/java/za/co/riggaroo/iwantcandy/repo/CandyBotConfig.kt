package za.co.riggaroo.iwantcandy.repo

/**
 * @author rebeccafranks
 * @since 2017/10/06.
 */
data class CandyBotConfig(val photoOverlay: String? = null, val tweetHashtag: String = "#DVTBot",
                          val tweetTextOptions: List<String> = listOf("CandyBot!", "I smiled for Candy!"))
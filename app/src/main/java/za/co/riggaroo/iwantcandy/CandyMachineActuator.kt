package za.co.riggaroo.iwantcandy

import android.os.CountDownTimer
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

/**
 * @author rebeccafranks
 * @since 2017/07/08.
 */
class CandyMachineActuator(gpio: String) : AutoCloseable {

    private val TAG: String? = "CandyMachineActuator"
    private val CANDY_DISPENSING_DURATION_MILLIS = 2000L
    private var candyGpioPin: Gpio? = null
    private var mCandiesTimer: CountDownTimer? = null

    init {
        val service = PeripheralManagerService()
        candyGpioPin = service.openGpio(gpio)
        candyGpioPin?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        candyGpioPin?.registerGpioCallback(object : GpioCallback() {
            override fun onGpioError(gpio: Gpio?, error: Int) {
                super.onGpioError(gpio, error)
                Log.e(TAG, "onGpioError : $error : $gpio")
            }

            override fun onGpioEdge(gpio: Gpio?): Boolean {
                Log.d(TAG, "onGpioEdge : $gpio")
                return super.onGpioEdge(gpio)

            }
        })
        candyGpioPin?.setActiveType(Gpio.ACTIVE_HIGH)
    }

    fun giveCandies() {
        Log.d(TAG, "giveCandies")
        candyGpioPin?.value = true

        mCandiesTimer?.cancel()
        mCandiesTimer = object : CountDownTimer(CANDY_DISPENSING_DURATION_MILLIS, CANDY_DISPENSING_DURATION_MILLIS) {
            override fun onTick(remainingMillis: Long) {
            }

            override fun onFinish() {
                candyGpioPin?.value = false
            }
        }
        mCandiesTimer?.start()
    }

    override fun close() {
        candyGpioPin?.value = false
        mCandiesTimer?.cancel()
        candyGpioPin?.close()
    }

}
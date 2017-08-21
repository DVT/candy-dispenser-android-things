package za.co.riggaroo.iwantcandy

import android.os.Build


/**
 * @author rebeccafranks
 * @since 2017/08/02.
 */
object BoardDefaults {
    private val DEVICE_RPI3 = "rpi3"
    private val DEVICE_IMX7D_PICO = "imx7d_pico"
    private var sBoardVariant = ""

    val candyDispensingPin: String
        get() {
            return when (boardVariant) {
                DEVICE_RPI3 -> "BCM20"
                DEVICE_IMX7D_PICO -> "GPIO_175"
                else -> throw UnsupportedOperationException("Unknown device: " + Build.DEVICE)
            }
        }

    private val boardVariant: String
        get() {
            if (!sBoardVariant.isEmpty()) {
                return sBoardVariant
            }
            sBoardVariant = Build.DEVICE
            return sBoardVariant
        }
}
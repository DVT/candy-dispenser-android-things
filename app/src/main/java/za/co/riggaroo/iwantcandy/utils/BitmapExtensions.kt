package za.co.riggaroo.iwantcandy.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix


/**
 * @author rebeccafranks
 * @since 2017/08/18.
 */
fun Bitmap.overlay(bmp2: Bitmap): Bitmap {
    val bmOverlay = Bitmap.createBitmap(this.width, this.height, this.config)
    val resizedOverlay = Bitmap.createScaledBitmap(bmp2, this.width, this.height, false)
    val canvas = Canvas(bmOverlay)
    canvas.drawBitmap(this, Matrix(), null)
    canvas.drawBitmap(resizedOverlay, Matrix(), null)
    return bmOverlay
}
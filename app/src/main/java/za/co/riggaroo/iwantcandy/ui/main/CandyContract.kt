package za.co.riggaroo.iwantcandy.ui.main

import android.graphics.Bitmap

/**
 * @author rebeccafranks
 * @since 2017/08/22.
 */

interface CandyContract {

    interface CandyView {
        fun showLoading()
        fun hideLoading()
        fun showNoSmileDetected()
        fun showDispensingCandy()
        fun showNoFacesDetected()
        fun clearMessages()
        fun showFaceDetectorNotOperational()
        fun showImage(overlayedBitmap: Bitmap)
        fun dispenseCandy()
        fun triggerCamera()
        fun showTweetPosted()
        fun showTweetError(exception: Exception)
    }

    interface CandyPresenter {
        fun onPictureTaken(imageBytes: ByteArray)
        fun onTakePhotoPressed()
    }
}



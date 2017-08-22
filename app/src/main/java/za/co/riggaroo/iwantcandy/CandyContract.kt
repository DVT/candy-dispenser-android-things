package za.co.riggaroo.iwantcandy

import android.graphics.Bitmap

/**
 * @author rebeccafranks
 * @since 2017/08/22.
 */

interface CandyContract {

    interface CandyView {
        fun displayNoSmileDetected()
        fun displayDispensingCandy()
        fun displayNoFacesDetected()
        fun showLoading()
        fun hideLoading()
        fun clearMessages()
        fun displayFaceDetectorNotOperational()
        fun showImage(overlayedBitmap: Bitmap)
        fun dispenseCandy()
        fun triggerCamera()
    }

    interface CandyPresenter {
        fun onPictureTaken(imageBytes: ByteArray)
        fun onTakePhotoPressed()
    }
}



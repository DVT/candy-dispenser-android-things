package za.co.riggaroo.iwantcandy.ui.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import za.co.riggaroo.iwantcandy.repo.TwitterRepository
import za.co.riggaroo.iwantcandy.utils.overlay

/**
 * @author rebeccafranks
 * @since 2017/08/22.
 */

class CandyPresenter(private var twitterService: TwitterRepository,
                     private var faceDetector: FaceDetector?,
                     private var view: CandyContract.CandyView,
                     private var photoOverlay: Bitmap) : CandyContract.CandyPresenter {

    private val TAG: String = "CandyPresenter"

    override fun onTakePhotoPressed() {
        view.triggerCamera()
        view.showLoading()
        view.clearMessages()
    }

    override fun onPictureTaken(imageBytes: ByteArray) {
        Log.d(TAG, "Picture Taken with " + imageBytes.size)

        val originalBitmap = getBitmapFromByteArray(imageBytes)
        val overlayedBitmap = originalBitmap.overlay(photoOverlay)

        view.showImage(overlayedBitmap)
        faceDetector?.let { faceDetector ->
            if (!faceDetector.isOperational) {
                view.showFaceDetectorNotOperational()
                return
            }
            val sparseArray = getFaceSparseArray(originalBitmap, faceDetector)
            if (sparseArray.size() == 0) {
                view.showNoFacesDetected()
                return
            }

            if (isSomeoneSmiling(sparseArray)) {
                view.dispenseCandy()
                view.showDispensingCandy()
                twitterService.sendTweet(overlayedBitmap, object : TwitterRepository.TweetCallback {
                    override fun onError(exception: Exception) {
                        view.showTweetError(exception)
                    }

                    override fun onSuccess() {
                        view.showTweetPosted()
                    }

                })
            } else {
                view.showNoSmileDetected()
            }

        }

    }

    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(180f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun isSomeoneSmiling(sparseArray: SparseArray<Face>) = (0 until sparseArray.size())
            .map { sparseArray.valueAt(it) }
            .any { it != null && it.isSmilingProbability > 0.4 }


    private fun getFaceSparseArray(bitmap: Bitmap, faceDetector: FaceDetector): SparseArray<Face> {
        val frame = Frame.Builder().setBitmap(bitmap).build()
        return faceDetector.detect(frame)
    }
}

package za.co.riggaroo.iwantcandy

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import za.co.riggaroo.iwantcandy.twitter.TwitterRepository


class CandyActivity : Activity() {

    private val TAG: String = "CandyActivity"
    private var candyMachine: CandyMachine? = null
    private var faceDetector: FaceDetector? = null

    private lateinit var camera: CandyCamera
    private lateinit var photoImageView: ImageView
    private lateinit var photoOverlay: Bitmap
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var errorTextView: TextView

    private lateinit var twitterService: TwitterRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupFaceDetector()
        setupCamera()
        setupScreenElements()
        setupCandyDispenser()
        twitterService = TwitterRepository(TwitterRepository.DependencyProvider(), this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun setupCandyDispenser() {
        candyMachine = CandyMachine(BoardDefaults.candyDispensingPin)
    }

    private fun setupCamera() {
        camera = CandyCamera.getInstance()
        camera.initializeCamera(this, Handler(), mOnImageAvailableListener)
    }

    private lateinit var buttonCandy: Button

    private fun setupScreenElements() {
        errorTextView = findViewById(R.id.text_view_error)
        progressBarLoading = findViewById(R.id.progress_bar_loading)
        buttonCandy = findViewById(R.id.button_activate_candy)
        buttonCandy.setOnClickListener { _ ->

            pressSmile()
        }
        photoImageView = findViewById(R.id.image_view_photo)

        val overlayBitmapOptions = BitmapFactory.Options()
        overlayBitmapOptions.inMutable = true
        photoOverlay = BitmapFactory.decodeResource(resources, R.drawable.picture_frame, overlayBitmapOptions)
    }

    private fun pressSmile() {
        clearMessages()
        Log.d(TAG, "Pressed: on")
        showLoading()
        camera.takePicture()
    }


    private fun setupFaceDetector() {
        faceDetector = FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        val imageBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuffer.remaining())
        imageBuffer.get(imageBytes)
        image.close()

        onPictureTaken(imageBytes)
    }


    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(180f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun onPictureTaken(imageBytes: ByteArray) {
        Log.d(TAG, "Picture Taken with " + imageBytes.size)

        val originalBitmap = getBitmapFromByteArray(imageBytes)
        val overlayedBitmap = originalBitmap.overlay(photoOverlay)
        photoImageView.setImageBitmap(overlayedBitmap)
        faceDetector?.let { faceDetector ->
            if (!faceDetector.isOperational) {
                showErrorMessage(R.string.error_face_detector_not_operational)
                return
            }
            val sparseArray = getFaceSparseArray(originalBitmap, faceDetector)
            if (sparseArray.size() == 0) {
                showErrorMessage(R.string.error_no_faces_detected)
                return
            }

            if (isSomeoneSmiling(sparseArray)) {
                dispenseCandy(overlayedBitmap)
            } else {
                showErrorMessage(R.string.error_not_smiling)
            }

        }

    }

    private fun getFaceSparseArray(bitmap: Bitmap, faceDetector: FaceDetector): SparseArray<Face> {
        val frame = Frame.Builder().setBitmap(bitmap).build()
        return faceDetector.detect(frame)
    }

    private fun isSomeoneSmiling(sparseArray: SparseArray<Face>): Boolean {
        return (0 until sparseArray.size())
                .map { sparseArray.valueAt(it) }
                .any { it != null && it.isSmilingProbability > 0.5 }
    }

    private fun showErrorMessage(@StringRes message: Int) {
        Log.d(TAG, getString(message))
        errorTextView.text = getString(message)
        hideLoading()
    }

    private fun clearMessages() {
        errorTextView.text = ""
    }

    private fun showLoading() {
        buttonCandy.visibility = View.INVISIBLE
        progressBarLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        buttonCandy.visibility = View.VISIBLE
        progressBarLoading.visibility = View.INVISIBLE
    }

    private fun dispenseCandy(bitmap: Bitmap) {
        twitterService.sendTweet(bitmap)
        candyMachine?.giveCandies()
        errorTextView.text = getString(R.string.success_dispensing_candy)
        Log.d(TAG, "Yay you smiled")
        hideLoading()
    }


    override fun onDestroy() {
        super.onDestroy()
        candyMachine?.close()
        camera.close()
    }
}
package za.co.riggaroo.iwantcandy.ui.components

/**
 * @author rebeccafranks
 * @since 2017/07/08.
 */
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log


class CandyCamera : AutoCloseable {

    private var mImageReader: ImageReader? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null

    companion object InstanceHolder {
        val IMAGE_WIDTH = 640
        val IMAGE_HEIGHT = 480
        val MAX_IMAGES = 1
        private val mCamera = CandyCamera()

        fun getInstance(): CandyCamera {
            return mCamera
        }
    }

    fun initializeCamera(context: Context, backgroundHandler: Handler, imageListener: ImageReader.OnImageAvailableListener) {
        val manager = context.getSystemService(CAMERA_SERVICE) as CameraManager
        var camIds = emptyArray<String>()
        try {
            camIds = manager.cameraIdList
        } catch (e: CameraAccessException) {
            Log.d(TAG, "Cam access exception getting ids", e)
        }
        if (camIds.isEmpty()) {
            Log.d(TAG, "No cameras found")
            return
        }

        val id = camIds[0]
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                ImageFormat.JPEG, MAX_IMAGES)
        mImageReader?.setOnImageAvailableListener(imageListener, backgroundHandler)
        try {
            manager.openCamera(id, mStateCallback, backgroundHandler)
        } catch (cae: Exception) {
            Log.d(TAG, "Camera access exception", cae)
        }
    }

    fun takePicture() {
        mCameraDevice?.createCaptureSession(
                arrayListOf(mImageReader?.surface),
                mSessionCallback,
                null)
    }

    private fun triggerImageCapture() {
        val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder?.addTarget(mImageReader!!.surface)
        captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        Log.d(TAG, "Session initialized.")
        mCaptureSession?.capture(captureBuilder?.build(), mCaptureCallback, null)
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            Log.d(TAG, "Partial result")
        }

        override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
            Log.d(TAG, "Capture session failed")
        }

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            session?.close()
            mCaptureSession = null
            Log.d(TAG, "Capture session closed")
        }
    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession?) {
            Log.w(TAG, "Failed to configure camera")
        }

        override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
            if (mCameraDevice == null) {
                return
            }
            mCaptureSession = cameraCaptureSession
            triggerImageCapture()
        }

    }

    private val mStateCallback = object : CameraDevice.StateCallback() {
        override fun onError(cameraDevice: CameraDevice, code: Int) {
            Log.d(TAG, "Camera device error, closing")
            cameraDevice.close()
        }

        override fun onOpened(cameraDevice: CameraDevice) {
            Log.d(TAG, "Opened camera.")
            mCameraDevice = cameraDevice
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d(TAG, "Camera disconnected, closing")
            cameraDevice.close()
        }

        override fun onClosed(camera: CameraDevice) {
            Log.d(TAG, "Closed camera, releasing")
            mCameraDevice = null
        }
    }

    override fun close() {
        mCameraDevice?.close()
    }
}
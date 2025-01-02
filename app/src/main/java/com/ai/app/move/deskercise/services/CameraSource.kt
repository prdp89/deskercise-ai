/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package com.ai.app.move.deskercise.services

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.data.ValidFramePosition
import com.ai.app.move.deskercise.ml.PoseDetector
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraSource manages the reading of the camera feed, displaying the camera feed to the screen in the correct orientation
 * and drawing of the keyPoints over the camera feed
 */

class CameraSource(
    private val surfaceView: SurfaceView,
    private val listener: CameraSourceListener? = null,
) {

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var isTrackerEnabled = false
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    /** Frame count that have been processed so far in an one second interval to calculate FPS. */
    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0
    private var displayable_bodyJoints: List<Pair<BodyPart, BodyPart>> =
        listOf(
            Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
            Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
            Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
            Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
            Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
            Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
            Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
            Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
            Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
            Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE),
            Pair(BodyPart.NECK, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.NECK, BodyPart.RIGHT_SHOULDER),
        )
    private var displayable_bodyPoints: List<BodyPart> = listOf(
        BodyPart.NOSE,
        BodyPart.LEFT_EYE,
        BodyPart.LEFT_EAR,
        BodyPart.LEFT_SHOULDER,
        BodyPart.LEFT_ELBOW,
        BodyPart.LEFT_HIP,
        BodyPart.LEFT_WRIST,
        BodyPart.LEFT_KNEE,
        BodyPart.RIGHT_EYE,
        BodyPart.RIGHT_EAR,
        BodyPart.RIGHT_SHOULDER,
        BodyPart.RIGHT_ELBOW,
        BodyPart.RIGHT_HIP,
        BodyPart.RIGHT_WRIST,
        BodyPart.RIGHT_KNEE,
        BodyPart.NECK,
    )

    // refreshFrequencyInMs flag to reduce framerate of keypoints.
    // Useful for exercises harder for the model to track to reduce the jitteriness of the points
    private var refreshFrequencyInMs: Long? = null
    private var confidenceLevel: Float = GlobalSettings.getDefaultConfidence()
    private var isDrawValidFrame: Boolean = false
    private var validFramePosition: ValidFramePosition? = null

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** Readers used as buffers for camera still shots */
    private var imageReader: ImageReader? = null

    /** The [CameraDevice] that will be opened in this fragment */
    private var camera: CameraDevice? = null

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private var session: CameraCaptureSession? = null

    /** [HandlerThread] where all buffer reading operations run */
    private var imageReaderThread: HandlerThread? = null

    /** [Handler] corresponding to [imageReaderThread] */
    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""
    suspend fun initCamera() {
        camera = openCamera(cameraManager, cameraId)
        imageReader =
            ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                if (!::imageBitmap.isInitialized) {
                    imageBitmap =
                        Bitmap.createBitmap(
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            Bitmap.Config.ARGB_8888,
                        )
                }
                try {
                    yuvConverter.yuvToRgb(image, imageBitmap)
                    // Create rotated version for portrait display
                    val rotateMatrix = Matrix()
                    if (MIRROR_PREVIEW) {
                        rotateMatrix.preScale(1f, -1f) // mirror
                    }
                    rotateMatrix.postRotate(PHYSICAL_CAMERA_ROTATION_FOR_PREVIEW)

                    val rotatedBitmap = Bitmap.createBitmap(
                        imageBitmap,
                        0,
                        0,
                        PREVIEW_WIDTH,
                        PREVIEW_HEIGHT,
                        rotateMatrix,
                        false,
                    )
                    processImage(rotatedBitmap)
                } catch (e: InterruptedException) {
                    Log.d(TAG, e.message.toString())
                } finally {
                    image.close()
                }
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW,
            )?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->

            camera?.createCaptureSession(
                targets,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(captureSession: CameraCaptureSession) {
                        cont.resume(captureSession)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        cont.resumeWithException(Exception("Session error"))
                    }
                },
                null,
            )
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(
                cameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                    }
                },
                imageReaderHandler,
            )
        }

    fun prepareCamera() {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // We want to use a front facing camera in our project, hence we loop thru and get the first front facing camera we find
            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (
                cameraDirection != CameraCharacteristics.LENS_FACING_FRONT
            ) {
                continue
            }
            // get the first front facing camera
            this.cameraId = cameraId
            break
        }
    }

    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
            }
            this.detector = detector
        }
    }

    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000,
        )
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        stopImageReaderThread()
        detector?.close()
        detector = null
        fpsTimer?.cancel()
        fpsTimer = null
        frameProcessedInOneSecondInterval = 0
        framesPerSecond = 0
    }

    // process image
    private fun processImage(bitmap: Bitmap) {
        val persons = mutableListOf<Person>()
        val classificationResult: List<Pair<String, Float>>? = null

        synchronized(lock) {
            detector?.estimatePoses(bitmap)?.let {
                persons.addAll(it)
            }
        }
        frameProcessedInOneSecondInterval++
        if (frameProcessedInOneSecondInterval == 1) {
            // send fps to view
            listener?.onFPSListener(framesPerSecond)
        }

        // if the model returns only one item, show that item's score.
        if (persons.isNotEmpty()) {
            // CUSTOM NECK CALCULATION
            // calculate and add Custom Neck Points and Score
            for (p in persons) {
                val leftShoulderKeyPoint =
                    p.keyPoints.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER }
                val rightShoulderKeyPoint =
                    p.keyPoints.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER }
                if (leftShoulderKeyPoint != null &&
                    rightShoulderKeyPoint != null
                ) {
                    val neck_x_coordinate =
                        (leftShoulderKeyPoint.coordinate.x + rightShoulderKeyPoint.coordinate.x) / 2
                    val neck_y_coordinate =
                        (leftShoulderKeyPoint.coordinate.y + rightShoulderKeyPoint.coordinate.y) / 2
                    val neck_x_coordinate_translated =
                        (leftShoulderKeyPoint.translated_coordinate.x + rightShoulderKeyPoint.translated_coordinate.x) / 2
                    val neck_y_coordinate_translated =
                        (leftShoulderKeyPoint.translated_coordinate.y + rightShoulderKeyPoint.translated_coordinate.y) / 2
                    val shoulder_min_score =
                        if (leftShoulderKeyPoint.score < rightShoulderKeyPoint.score) leftShoulderKeyPoint.score else rightShoulderKeyPoint.score

                    val neckKeyPoint = KeyPoint(
                        BodyPart.NECK,
                        PointF(neck_x_coordinate, neck_y_coordinate),
                        shoulder_min_score,
                        PointF(neck_x_coordinate_translated, neck_y_coordinate_translated),
                    )
                    p.keyPoints.add(neckKeyPoint)
                }
            }
            listener?.onDetectedInfo(persons[0], classificationResult)
        }

        visualize(persons, bitmap)
    }

    fun updateDrawnKeyPoints(
        myDisplayable_bodyPoints: List<BodyPart>,
        myDisplayable_bodyJoints: List<Pair<BodyPart, BodyPart>>,
        myRefreshFrequencyInMs: Long? = null,
        myConfidenceLevel: Float = GlobalSettings.getDefaultConfidence(),
        myIsDrawValidFrame: Boolean = false,
        myValidFramePosition: ValidFramePosition? = null,
    ) {
        displayable_bodyJoints = myDisplayable_bodyJoints
        displayable_bodyPoints = myDisplayable_bodyPoints
        refreshFrequencyInMs = myRefreshFrequencyInMs
        confidenceLevel = myConfidenceLevel
        isDrawValidFrame = myIsDrawValidFrame
        validFramePosition = myValidFramePosition
    }

    private fun visualize(persons: List<Person>, bitmap: Bitmap) {
        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE },
            isTrackerEnabled,
            displayable_bodyJoints,
            displayable_bodyPoints,
            refreshFrequencyInMs,
            confidenceLevel,
            isDrawValidFrame,
            validFramePosition
        )

        val holder = surfaceView.holder
        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            if (canvas.height < canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else {
                val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                screenHeight = canvas.height
                top = 0
                screenWidth = (canvas.height * ratio).toInt()
                left = (canvas.width - screenWidth) / 2
            }
            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            canvas.drawBitmap(
                outputBitmap,
                Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom),
                null,
            )

            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message.toString())
        }
    }

    interface CameraSourceListener {
        fun onFPSListener(fps: Int)
        fun onDetectedInfo(person: Person, poseLabels: List<Pair<String, Float>>?)
    }

    companion object {
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480

        // The rotation is so that camera's physical orientation matches the screen preview's orientation
        private const val PHYSICAL_CAMERA_ROTATION_FOR_PREVIEW = -90.0f

        // Mirror preview flag to mirror camera preview
        private const val MIRROR_PREVIEW = true

        /** Threshold for confidence score. */
        private val MIN_CONFIDENCE = GlobalSettings.getDefaultConfidence() // .2f
        private const val TAG = "Camera Source"
    }
}

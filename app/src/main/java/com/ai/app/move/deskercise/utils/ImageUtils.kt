package com.thefashion.common.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.ai.app.move.deskercise.utils.compressSize
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created on 12/14/20.
 */
object ImageUtils {
    fun getBitmapFromRes(context: Context, res: Int): Bitmap? {
        return AppCompatResources.getDrawable(context, res)?.toBitmap()
    }

    @Throws(IOException::class)
    fun drawableFromUrl(url: String?): Drawable {
        val x: Bitmap
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.inputStream
        x = BitmapFactory.decodeStream(input)
        return BitmapDrawable(Resources.getSystem(), x)
    }

    @Suppress("DEPRECATION")
    fun saveImage(finalBitmap: Bitmap): Uri {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/fashion")
        myDir.mkdirs()

        val fName = "fashion_${System.currentTimeMillis()}.jpg"

        val file = File(myDir, fName)
        val out = FileOutputStream(file)
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        return Uri.fromFile(file)
    }

    fun decodeFile(f: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        var bitmap: Bitmap? = null

        try {
            // decode image size
            var o: BitmapFactory.Options? = BitmapFactory.Options()
            o!!.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, o)

            // decode with inSampleSize
            var o2: BitmapFactory.Options? = BitmapFactory.Options()
            o2!!.inSampleSize = calculateInSampleSize(
                o,
                null,
                reqWidth,
                reqHeight,
            )
            bitmap = BitmapFactory.decodeStream(
                FileInputStream(f),
                null,
                o2,
            )

            o = null
            o2 = null
            val path = f.path
            if (bitmap != null) {
                val exif = ExifInterface(path)
                val tag = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                var degree = 0
                if (tag == ExifInterface.ORIENTATION_ROTATE_180) {
                    degree = 180
                } else if (tag == ExifInterface.ORIENTATION_ROTATE_90) {
                    degree = 90
                } else if (tag == ExifInterface.ORIENTATION_ROTATE_270) {
                    degree = 270
                }

                var temp: Bitmap? = null
                if (degree != 0) {
                    val matrix = Matrix()
                    matrix.setRotate(degree.toFloat())

                    temp = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true,
                    )
                }

                if (temp != null) {
                    bitmap.recycle()

                    bitmap = temp
                }
                temp = null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            bitmap = null
        } catch (oome: OutOfMemoryError) {
            oome.printStackTrace()
            bitmap = null
        }

        return bitmap
    }

    fun calculateInSampleSize(
        option: BitmapFactory.Options?,
        bitmap: Bitmap?,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        var height = 0
        var width = 0

        if (option != null) {
            height = option.outWidth
            width = option.outHeight
        } else {
            height = bitmap!!.height
            width = bitmap.width
        }

        if (height == 0 || width == 0) {
            return 0
        }

        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round(height.toFloat() / reqHeight.toFloat())
            } else {
                inSampleSize = Math.round(width.toFloat() / reqWidth.toFloat())
            }
        }

        return inSampleSize
    }

    fun textToBitmap(text: String, textSize: Float, textColor: Int): Bitmap {
        val paint = Paint(ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative

        val width = (paint.measureText(text) + 0.5f).toInt() // round

        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text, 0F, baseline, paint)
        return image
    }

    fun decodeFile(context: Context, bitmapUri: Uri, reqWidth: Int, reqHeight: Int): Uri? {
        val path = bitmapUri.path ?: return null
        val originFile = File(path)
        val fileName = "resized_${bitmapUri.lastPathSegment}"
        return decodeFile(originFile, reqWidth, reqHeight)?.let { bitmap ->
            val dir = File(context.cacheDir, "resized")
            if (!dir.exists()) dir.mkdir()
            val savedFile = File(dir, fileName)
            val fOut = FileOutputStream(savedFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, fOut)
            fOut.flush()
            fOut.close()
            Uri.fromFile(savedFile)
        } ?: return null
    }

    fun decodeFromFilePath(
        context: Context,
        filePath: String?,
        reqWidth: Int = 720,
        reqHeight: Int = 720,
    ): File? {
        filePath ?: return null
        return File(filePath).compressSize(context, reqWidth, reqHeight)
    }
}

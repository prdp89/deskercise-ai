package com.ai.app.move.deskercise.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseBindingActivity
import com.ai.app.move.deskercise.base.BaseFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.network.NetworkException
import com.ai.app.move.deskercise.network.RefreshTokenException
import com.ai.app.move.deskercise.ui.login.LoginActivity
import com.thefashion.common.utils.ImageUtils
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.*

const val FOLDER_NAME = "deskercise-media"

fun View.getLayoutInflater(): LayoutInflater = LayoutInflater.from(context)

fun CharSequence?.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.isVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setSafeClick(listener: () -> Unit) {
    setOnClickListener {
        isEnabled = false
        listener.invoke()
        postDelayed({
            isEnabled = true
        }, 1000)
    }
}

fun TextView.setTextColorCompat(color: Int) {
    setTextColor(ContextCompat.getColor(context, color))
}

fun ImageView.setTintColorCompat(color: Int) {
    setColorFilter(ContextCompat.getColor(context, color))
}

fun ImageView.loadImage(url: String?) {
    Glide.with(context).load(url).centerCrop().placeholder(R.drawable.profiledefault).into(this)
}

fun Float.toPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics,
    ).toInt()
}

fun Float.toFPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics,
    )
}

fun RecyclerView.addVerticalSpacing(space: Float) {
    addItemDecoration(object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            outRect.bottom = space.toPx(view.context)
        }
    })
}

fun RecyclerView.addHorizontalSpacing(space: Float) {
    addItemDecoration(object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            outRect.right = space.toPx(view.context)
        }
    })
}

fun Context.getFileUri(fileName: String): Uri {
    val directory = File(filesDir, "images")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val file = File(directory, fileName)
    val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
    return uri
}

fun Context.getRealPathFromURI(contentURI: Uri): String {
    val result: String
    val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
    if (cursor == null) {
        result = contentURI.path.orEmpty()
    } else {
        cursor.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}

/**
 * execute simply observer
 *
 */
fun <DATA> BaseFragment.simplyObserver(
    liveData: LiveData<State<DATA>>,
    loading: (() -> Any?)? = null,
    error: ((Throwable) -> Any?)? = null,
    success: ((State.Success<DATA>) -> Unit),
) {
    liveData.observe(viewLifecycleOwner) { state ->
        when (state) {
            is State.Starting<*> -> {
                loading?.invoke() ?: showLoading(true)
            }

            is State.Error<DATA> -> {
                error?.invoke(state.error) ?: run {
                    hideLoading()
                    state.error.printStackTrace()

                    if (state.error is ConnectException) {
                        showToast(getString(R.string.unable_to_make_a_connection))
                        return@run
                    }

                    showToast(state.error.message.orEmpty())
                    if (state.error is RefreshTokenException || state.error is NetworkException) {
                        LoginActivity.startNewTask(requireContext())
                    }
                }
            }

            is State.Success<DATA> -> {
                loading ?: hideLoading()
                success.invoke(state)
            }
        }
    }
}

fun <V : ViewBinding, DATA> BaseBindingActivity<V>.simplyObserver(
    liveData: LiveData<State<DATA>>,
    loading: (() -> Any?)? = null,
    error: ((Throwable) -> Any?)? = null,
    success: ((DATA) -> Unit),
) {
    liveData.observe(this) { state ->
        when (state) {
            is State.Starting<*> -> {
                loading?.invoke() ?: showLoading(true)
            }

            is State.Error<DATA> -> {
                error?.invoke(state.error) ?: run {
                    hideLoading()
                    state.error.printStackTrace()

                    if (state.error is ConnectException) {
                        showToast(getString(R.string.unable_to_make_a_connection))
                        return@run
                    }

                    showToast(state.error.message.orEmpty())
                }
            }

            is State.Success<DATA> -> {
                loading ?: hideLoading()
                success.invoke(state.data)
            }
        }
    }
}

/**
 * return pattern "[applicationId]/data/@params[folderName]/"
 */
fun Context.createInternalFolderIfNeeded(folderName: String): String {
    val path = "${this.cacheDir}/$folderName/"
    val folder = File(path)
    if (!folder.exists()) {
        folder.mkdir() // If there is no folder it will be created.
    }
    return folder.absolutePath
}

fun String?.compressSize(context: Context, reqWidth: Int = 720, reqHeight: Int = 720): File? {
    val filePath = this ?: return null
    val file = File(filePath)
    if (!file.exists()) return null
    return file.compressSize(context, reqWidth, reqHeight)
}

const val RANDOM_LENGTH = 20
fun File.randomFileName(): String {
    return randomToString(RANDOM_LENGTH) + "." + this.extension
}

fun randomToString(stringLength: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..stringLength)
        .map { allowedChars.random() }
        .joinToString("")
}

fun File.compressSize(context: Context, reqWidth: Int = 720, reqHeight: Int = 720): File? {
    return ImageUtils.decodeFile(this, reqWidth, reqHeight)?.let { bitmap ->
        val outputFileName = this.randomFileName()
        val outputFolderPath = context.createInternalFolderIfNeeded(FOLDER_NAME)
        val savedFile = File(outputFolderPath, outputFileName)
        val outputFile = FileOutputStream(savedFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputFile)
        outputFile.flush()
        outputFile.close()
        savedFile
    } ?: return null
}

fun String.parseTime(pattern: String): Date? {
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
}

fun Date.parseString(pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

fun String.isUrl(): Boolean {
    return startsWith("https://") or startsWith("http://")
}

fun WebView.onLoadContent(
    content: String,
    isDarkMode: Boolean,
    onPageFinished: ((View?) -> Unit)? = null
) {
    val textColor = if (isDarkMode) "#FFFFFF" else "#1A1A1A"
    val metaString = "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no\">"
    val cssStyleString = "<style type=\"text/css\">" +
            "body { padding: 16px; margin: 0px; font-size: 14px; text-align: justify; color: $textColor; }" +
            "img { max-width: 100%; margin-top: 6px; }" +
            "</style>"
    val htmlString = "<html><head>$metaString</head>$cssStyleString<body>${content.replace("\n", "</br>")}</body></html>"
    this.apply {
        loadDataWithBaseURL("file:///android_asset/", htmlString, "text/html; charset=UTF-8", "UTF-8", null)
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent))
        settings.defaultTextEncodingName = "UTF-8"
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        isEnabled = false
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke(view)
            }
        }
    }
}
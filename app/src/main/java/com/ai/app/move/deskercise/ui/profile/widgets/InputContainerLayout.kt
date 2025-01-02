package com.ai.app.move.deskercise.ui.profile.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.contains
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.utils.setTextColorCompat

class InputContainerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    init {
        orientation = VERTICAL
    }

    private var title: String = ""

    init {
        attrs?.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.InputContainerLayout)
            title = styledAttributes.getString(R.styleable.InputContainerLayout_iclTitle).orEmpty()
            styledAttributes.recycle()
        }
    }

    private val titleView: TextView by lazy {
        TextView(context, null, R.style.Text_Small_Gray).apply {
            textSize = 11f
            setTextColorCompat(R.color.gray)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!contains(titleView)) {
            titleView.text = title
            addView(titleView, 0)
        }
    }
}

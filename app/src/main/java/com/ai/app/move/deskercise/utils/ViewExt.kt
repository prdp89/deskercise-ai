package com.ai.app.move.deskercise.utils

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

fun Spinner.simpleItemSelected(callBack: (Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            adapter: AdapterView<*>?,
            view: View?,
            position: Int,
            l: Long,
        ) {
            callBack.invoke(position)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }
    }
}

package com.ai.app.move.deskercise.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ai.app.move.deskercise.utils.LoadingDialog
import timber.log.Timber

open class BaseFragment : Fragment() {
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    private val toaster by lazy { Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener {
            // just do nothing
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d(">> onResume ${this.javaClass.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        Timber.d(">> onPause ${this.javaClass.simpleName}")
    }

    fun showToast(message: String) {
        toaster.cancel()
        toaster.setText(message)
        toaster.show()
    }

    fun showToast(message: Int) {
        toaster.cancel()
        toaster.setText(message)
        toaster.show()
    }
    fun showLoading(cancellable: Boolean = false) {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        loadingDialog.setCancelable(cancellable)
        loadingDialog.show()
    }

    fun hideLoading() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}

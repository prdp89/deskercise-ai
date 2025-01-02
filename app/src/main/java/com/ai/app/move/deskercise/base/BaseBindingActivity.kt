package com.ai.app.move.deskercise.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.ai.app.move.deskercise.utils.LoadingDialog

abstract class BaseBindingActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    private val loadingDialog by lazy { LoadingDialog(this) }

    abstract fun getViewBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
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

    private val toaster by lazy { Toast.makeText(this, "", Toast.LENGTH_SHORT) }

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
}

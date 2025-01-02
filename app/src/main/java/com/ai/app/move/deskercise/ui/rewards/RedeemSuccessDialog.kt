package com.ai.app.move.deskercise.ui.rewards

import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseBindingDialogFragment
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.databinding.DialogRedeemSuccessfulBinding
import com.ai.app.move.deskercise.utils.toPx
import org.koin.android.ext.android.inject

class RedeemSuccessDialog : BaseBindingDialogFragment<DialogRedeemSuccessfulBinding>() {

    private val userManager: UserManager by inject()
    private val user: User by lazy {
        userManager.getUserInfo() ?: throw Exception("User is null")
    }

    override fun getViewBinding(): DialogRedeemSuccessfulBinding {
        return DialogRedeemSuccessfulBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { d ->
            d.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPoint.text = buildSpannedString {
            append(
                "You have left AAA ${
                    resources.getQuantityString(
                        R.plurals._points,
                        user.point,
                        user.point,
                    )
                }",
            )
            val fireDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_fire_active) ?: return
            val size = 24f.toPx(requireContext())
            fireDrawable.setBounds(0, 0, size, size)
            val imageSpan = ImageSpan(fireDrawable, ImageSpan.ALIGN_BASELINE)
            setSpan(imageSpan, 14, 17, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            val grayColor = ContextCompat.getColor(requireContext(), R.color.gray)
            val graySpan = ForegroundColorSpan(grayColor)
            val textSizeSpan = RelativeSizeSpan(.9f)
            setSpan(graySpan, 18, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            setSpan(textSizeSpan, 18, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        binding.tvOk.setOnClickListener {
            dismiss()
        }
    }
}

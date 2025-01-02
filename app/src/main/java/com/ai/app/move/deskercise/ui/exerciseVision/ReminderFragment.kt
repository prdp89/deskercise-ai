package com.ai.app.move.deskercise.ui.exerciseVision

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ai.app.move.deskercise.databinding.FragmentReminderBinding
import com.ai.app.move.deskercise.ui.pdfView.WebViewActivity

class ReminderFragment : DialogFragment() {
    private lateinit var binding: FragmentReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = FragmentReminderBinding.inflate(inflater, container, false)

        binding.tvOpenPdfGuide.setOnClickListener {
            startActivity(Intent(requireContext(), WebViewActivity::class.java).putExtra(
                WebViewActivity.URL_LINK,
                "https://sites.google.com/deskercise.com/deskercise"
            ))
        }
        binding.btOkay.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

}
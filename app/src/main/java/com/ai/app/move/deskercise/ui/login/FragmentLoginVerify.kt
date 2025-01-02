package com.ai.app.move.deskercise.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseMvvMFragment
import com.ai.app.move.deskercise.databinding.FragmentLoginVerifyBinding
import com.ai.app.move.deskercise.utils.simplyObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLoginVerify : BaseMvvMFragment<FragmentLoginVerifyBinding, VerifyViewModel>() {
    private val verifyViewModel by viewModel<VerifyViewModel>()
    override fun getViewBinding(): FragmentLoginVerifyBinding {
        return FragmentLoginVerifyBinding.inflate(layoutInflater)
    }

    override fun bindViewModel(): VerifyViewModel {
        return verifyViewModel
    }

    override fun registerEvents(viewModel: VerifyViewModel) {
        super.registerEvents(viewModel)
        simplyObserver(viewModel.verifyLiveData) {
            showToast(R.string.msg_successful)
            val navController = findNavController()
            navController.popBackStack(R.id.fragmentLoginPage, false)
        }
        viewModel.resendTimerLiveData.observe(viewLifecycleOwner) { value ->
            val resendText = if (value == 0L) {
                getString(R.string.resend)
            } else {
                getString(
                    R.string.resend_in_d_sec_s,
                    value,
                )
            }
            binding.tvResend.apply {
                text = resendText
                isEnabled = value < 30
            }
        }
        simplyObserver(viewModel.resendLiveData) {
            // just nothing for now
            showToast(R.string.msg_resent_code_successful)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.getString("email") ?: ""
        verifyViewModel.updateEmail(email)

        binding.instruction.text = context?.getString(R.string.enter_verification_title, email) ?: ""

        binding.verificationEt.doAfterTextChanged {
            verifyViewModel.updateConfirmationCode(it?.toString().orEmpty())
        }

        binding.backButton.setOnClickListener {
            val nav = findNavController()
            nav.popBackStack()
        }

        binding.verifyBtn.setOnClickListener {
            verifyViewModel.verify()
        }
        binding.tvResend.setOnClickListener {
            verifyViewModel.resend()
        }

        // to trigger VerifyViewModel to manually send confirmation code
        val sendCode = arguments?.getBoolean("send_code") ?: false
        if (sendCode){
            verifyViewModel.resend()
        }

        // start the Resend code timer countdown
        verifyViewModel.startCountDown()
    }
}

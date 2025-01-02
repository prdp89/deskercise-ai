package com.ai.app.move.deskercise.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseMvvMFragment
import com.ai.app.move.deskercise.databinding.FragmentLoginForgotBinding
import com.ai.app.move.deskercise.utils.simplyObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLoginForgot : BaseMvvMFragment<FragmentLoginForgotBinding, ForgetViewModel>() {
    private val forgetViewModel by viewModel<ForgetViewModel>()

    override fun getViewBinding(): FragmentLoginForgotBinding {
        return FragmentLoginForgotBinding.inflate(layoutInflater)
    }

    override fun bindViewModel(): ForgetViewModel {
        return forgetViewModel
    }

    private var email = ""

    /**
     * Saved Instance State to prevent Navigation from auto-navigating
     * from Forgot page to Reset page because of State.Success
     */
    private var resetInProgress = false

    companion object {
        // Key that identifies the `resetInProgress` state in the Bundle
        private const val RESET_KEY = "resetKey"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check whether navigation is allowed
        resetInProgress = savedInstanceState?.getBoolean(RESET_KEY) ?: false

        binding.emailEt.doAfterTextChanged {
            email = it?.toString().orEmpty()
            forgetViewModel.updateEmail(it?.toString().orEmpty())
        }

        binding.backButton.setOnClickListener {
            val nav = findNavController()
            nav.popBackStack()
        }
        binding.resetPassword.setOnClickListener {
            forgetViewModel.forget()
            resetInProgress = true // In case navigation leads to FragmentLoginReset
        }
    }

    override fun registerEvents(viewModel: ForgetViewModel) {
        super.registerEvents(forgetViewModel)

        simplyObserver(forgetViewModel.forgetLiveData){
            if(resetInProgress){
                resetInProgress = false
                showToast(R.string.msg_successful)
                val nav = findNavController()
                nav.navigate(R.id.action_fragmentLoginReset_to_loginReset)
            }
        }
    }
}

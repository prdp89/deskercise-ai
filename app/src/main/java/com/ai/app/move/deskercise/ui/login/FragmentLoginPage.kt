package com.ai.app.move.deskercise.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.DeviceManager
import com.ai.app.move.deskercise.base.BaseMvvMFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.databinding.FragmentLoginPageBinding
import com.ai.app.move.deskercise.ui.home.HomeActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLoginPage : BaseMvvMFragment<FragmentLoginPageBinding, LoginViewModel>() {
    private val loginViewModel by viewModel<LoginViewModel>()

    /**
     * Saved Instance State to prevent Navigation from auto-navigating
     * from Forgot page to Verify page because of State.Success
     */
    private var verifyInProgress = false

    override fun getViewBinding(): FragmentLoginPageBinding {
        return FragmentLoginPageBinding.inflate(layoutInflater)
    }

    override fun bindViewModel(): LoginViewModel {
        return loginViewModel
    }

    private val deviceManager: DeviceManager by inject()

    companion object {
        // Key that identifies the `verifyInProgress` state in the Bundle
        private const val VERIFY_KEY = "verifyKey"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check whether navigation is allowed
        verifyInProgress = savedInstanceState?.getBoolean(VERIFY_KEY) ?: false

        binding.etEmail.doAfterTextChanged {
            loginViewModel.updateEmail((it?.toString().orEmpty().trim()))
        }

        binding.etPassword.doAfterTextChanged {
            loginViewModel.updatePassword(it?.toString().orEmpty())
        }
        binding.login.setOnClickListener {
            if (binding.cbRemember.isChecked) {
                deviceManager.storePreviousEmail((binding.etEmail.text.toString()).trim())
            } else {
                deviceManager.storePreviousEmail("")
            }

            loginViewModel.login()
            verifyInProgress = true // In case navigation leads to FragmentLoginVerify
        }

        binding.tvRegister.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_fragmentLoginPage_to_fragmentLoginRegister)
        }

        binding.tvForgot.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_fragmentLoginPage_to_fragmentLoginResetPassword)
        }
        binding.etEmail.append(deviceManager.getPreviousEmail())
    }

    override fun registerEvents(viewModel: LoginViewModel) {
        super.registerEvents(viewModel)

        loginViewModel.loginLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                    if (state.error.message.toString()
                            .startsWith("User is not confirmed") &&
                        verifyInProgress
                    ) {
                        verifyInProgress = false
                        val email = binding.etEmail.text.toString()
                        val bundle = bundleOf(
                            "email" to email,
                            "send_code" to true,
                        )
                        val navController = findNavController()
                        navController.navigate(R.id.action_fragmentLoginPage_to_loginVerify, bundle)
                    }
                    showToast(state.error.message.orEmpty())
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    HomeActivity.startNewTask(requireContext())
                    requireActivity().finish()
                }
            }
        }
    }
}
//

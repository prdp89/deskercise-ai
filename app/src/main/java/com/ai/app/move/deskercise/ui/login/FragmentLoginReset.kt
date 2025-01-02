package com.ai.app.move.deskercise.ui.login

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseMvvMFragment
import com.ai.app.move.deskercise.databinding.FragmentLoginResetBinding
import com.ai.app.move.deskercise.utils.simplyObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLoginReset : BaseMvvMFragment<FragmentLoginResetBinding, ResetViewModel>() {
    private val resetViewModel by viewModel<ResetViewModel>()

    override fun bindViewModel(): ResetViewModel {
        return resetViewModel
    }

    override fun getViewBinding(): FragmentLoginResetBinding {
        return FragmentLoginResetBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emailEt.doAfterTextChanged {
            resetViewModel.updateEmail(it?.toString().orEmpty())
        }

        binding.passwordEt.doAfterTextChanged {
            resetViewModel.updatePassword(it?.toString().orEmpty())
        }

        resetViewModel.passwordValidatorLiveData.observe(viewLifecycleOwner) { pV ->
            setCheckOrCross(binding.minEightChar, pV.hasLength)
            setCheckOrCross(binding.oneUppercaseLetter, pV.hasUppercase)
            setCheckOrCross(binding.oneLowercaseLetter, pV.hasLowercase)
            setCheckOrCross(binding.oneNumber, pV.hasDigit)
            setCheckOrCross(binding.oneSpecialChar, pV.hasSpecialChar)
        }

        binding.cmfPasswordEt.doAfterTextChanged {
            resetViewModel.updateConfirmPassword(it?.toString().orEmpty())
        }

        resetViewModel.passwordIsMatchLiveData.observe(viewLifecycleOwner) { isMatch ->
            if(isMatch) {
                binding.passwordMatch.text = getString(R.string.passwords_match)
                binding.passwordMatch.setTextColor(resources.getColor(R.color.greenTheme, null))
            } else {
                binding.passwordMatch.text = getString(R.string.passwords_do_not_match)
                binding.passwordMatch.setTextColor(resources.getColor(R.color.colorRed, null))
            }
        }

        binding.verificationEt.doAfterTextChanged {
            resetViewModel.updateCode(it?.toString().orEmpty())
        }

        binding.backButton.setOnClickListener {
            val nav = findNavController()
            nav.popBackStack()
        }

        binding.resetPassword.setOnClickListener {
            resetViewModel.reset()
        }
    }

    /**
     * Depending on the condition of the verification check,
     * set a tick/check or a cross on the verification message
     *
     * @param[view] The TextView which the check or cross will be affected
     * @param[check] Whether to set a tick or a cross
     */
    private fun setCheckOrCross(view: TextView, check: Boolean) {
        if(check) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_check_14_greentheme,0,0,0)
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.close_redtheme_14,0,0,0)
        }
    }

    override fun registerEvents(viewModel: ResetViewModel) {
        super.registerEvents(viewModel)

        simplyObserver(resetViewModel.resetLiveData){
            showToast(R.string.msg_successful)
            val nav = findNavController()
            nav.popBackStack(R.id.fragmentLoginPage, false)
        }
    }
}

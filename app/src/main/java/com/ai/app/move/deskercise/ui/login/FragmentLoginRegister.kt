package com.ai.app.move.deskercise.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseMvvMFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.databinding.FragmentLoginRegisterBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentLoginRegister : BaseMvvMFragment<FragmentLoginRegisterBinding, RegisterViewModel>() {
    private val registerViewModel by viewModel<RegisterViewModel>()

    override fun getViewBinding(): FragmentLoginRegisterBinding {
        return FragmentLoginRegisterBinding.inflate(layoutInflater)
    }

    override fun bindViewModel(): RegisterViewModel {
        return registerViewModel
    }

    private lateinit var snackBar: Snackbar

    override fun registerEvents(viewModel: RegisterViewModel) {
        super.registerEvents(viewModel)
        viewModel.snackBarLiveData.observe(viewLifecycleOwner) { res ->
            if (snackBar.isShown) snackBar.dismiss()
            snackBar.setText(res)
            snackBar.show()
        }

        registerViewModel.signUpLiveData.observe(viewLifecycleOwner) { state: State<*> ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                    showToast(state.error.message.orEmpty())
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    val email = binding.emailEt.text.toString()
                    val navController = findNavController()
                    val bundle = bundleOf("email" to email)
                    navController.navigate(R.id.action_fragmentLoginRegister_to_loginVerify, bundle)

                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackBar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "",
            Snackbar.LENGTH_SHORT,
        ).setTextMaxLines(4)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

//        binding.domains.apply {
//            val domainsAdapter = StringSpinnerAdapter(context)
//            val values = resources.getStringArray(R.array.domains)
//            adapter = domainsAdapter
//            domainsAdapter.setData(values.toList())
//            simpleItemSelected { pos ->
//                val domain = values[pos]
//                registerViewModel.updateEmail((binding.emailEt.text.toString() + domain).trim())
//            }
//        }

        binding.emailEt.doAfterTextChanged {
            registerViewModel.updateEmail(
                (it?.toString().orEmpty()).trim(),
            )
        }

        binding.passwordEt.doAfterTextChanged {
            registerViewModel.updatePassword(it?.toString().orEmpty())
        }

        registerViewModel.passwordValidatorLiveData.observe(viewLifecycleOwner) { pV ->
            setCheckOrCross(binding.minEightChar, pV.hasLength)
            setCheckOrCross(binding.oneUppercaseLetter, pV.hasUppercase)
            setCheckOrCross(binding.oneLowercaseLetter, pV.hasLowercase)
            setCheckOrCross(binding.oneNumber, pV.hasDigit)
            setCheckOrCross(binding.oneSpecialChar, pV.hasSpecialChar)
        }

        binding.cmfPasswordEt.doAfterTextChanged {
            registerViewModel.updateConfirmPassword(it?.toString().orEmpty())
        }

        registerViewModel.passwordIsMatchLiveData.observe(viewLifecycleOwner) { isMatch ->
            if(isMatch) {
                binding.passwordMatch.text = getString(R.string.passwords_match)
                binding.passwordMatch.setTextColor(resources.getColor(R.color.greenTheme, null))
            } else {
                binding.passwordMatch.text = getString(R.string.passwords_do_not_match)
                binding.passwordMatch.setTextColor(resources.getColor(R.color.colorRed, null))
            }
        }

        binding.nameEt.doAfterTextChanged {
            registerViewModel.updateName(it?.toString().orEmpty())
        }

        binding.codeEt.doAfterTextChanged {
            registerViewModel.updateSignUpCode(it?.toString().orEmpty())
        }

        binding.ibCodeInfo.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setMessage(getString(R.string.code_info)).setPositiveButton(
                R.string.ok,
                { dialog, _ -> dialog.dismiss() },
            ).create()
            builder.show()
        }

        val spanText =
            SpannableString("I agree to the Terms & Conditions & Privacy Policy set out by this site.")

        val clickabletnc = object : ClickableSpan() {
            override fun onClick(view: View) {
                val myWebLink = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.deskercise.com/terms-conditions/"),
                )
                startActivity(myWebLink)
            }
        }
        val clickableppc = object : ClickableSpan() {
            override fun onClick(view: View) {
                val myWebLink =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.deskercise.com/privacy-policy/"))
                startActivity(myWebLink)
            }
        }
        spanText.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0, // start
            14, // end
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spanText.setSpan(
            clickabletnc,
            15,
            33,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spanText.setSpan(
            clickableppc,
            35,
            50,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spanText.setSpan(
            ForegroundColorSpan(Color.BLACK),
            51, // start
            spanText.length, // end
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        binding.tnc.movementMethod = LinkMovementMethod.getInstance()
        binding.tnc.setText(spanText, TextView.BufferType.SPANNABLE)

        binding.termsCheckbox.setOnCheckedChangeListener { _, checked ->
            viewModel.updateAcceptTerm(
                checked,
            )
        }
        binding.register.setOnClickListener {
            registerViewModel.signup()
        }

        binding.login.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
}

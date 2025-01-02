package com.ai.app.move.deskercise.ui.exerciseGroupMenu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.BuildConfig
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.databinding.FragmentExerciseGroupMenuBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroupProgramModel
import com.ai.app.move.deskercise.ui.exerciseMenu.RecyclerViewAdapterAvailableExercise
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivity
import com.ai.app.move.deskercise.ui.home.HomeBridge
import com.ai.app.move.deskercise.ui.login.LoginActivity
import com.ai.app.move.deskercise.ui.pdfView.WebViewActivity
import com.ai.app.move.deskercise.ui.profile.FragmentEditProfile
import com.ai.app.move.deskercise.ui.profile.MyProfileActivity
import com.ai.app.move.deskercise.ui.profile.adapters.ProgramAdapter
import com.ai.app.move.deskercise.utils.addVerticalSpacing
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.simplyObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * ExerciseStartingFragment contains the logic to handle the introduction screen before the exercise begins
 */

class ExerciseGroupMenuFragment : BaseFragment() {
    private lateinit var fragmentInstance: Fragment
    var firstInstance = true
    private val exercise_group_menu_fragment_tag = "EXERCISE_GROUP_FRAGMENT"
    private val exercise_group_preview_fragment_tag = "EXERCISE_GROUP_PREVIEW_FRAGMENT"

    private var _binding: FragmentExerciseGroupMenuBinding? = null
    private val startProgramViewModel by activityViewModel<StartProgramViewModel>()

    private val userManager: UserManager by inject()
    private val programAdapter by lazy {
        ProgramAdapter(arrayListOf(4, 7, 10), ::startExerciseGroupProgram, ::startDirect)
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentInstance = this
    }

    private lateinit var programModel: ExerciseGroupProgramModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerLogout()

        simplyObserver(startProgramViewModel.startExerciseLiveData) {
            startDirect(startProgramViewModel.programModel)
        }

        binding.rvProgramList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = programAdapter
            addVerticalSpacing(8f)
        }

        binding.civAvatar.setOnClickListener {
            (requireActivity() as? HomeBridge)?.openProfile() ?: startActivity(
                Intent(
                    requireContext(),
                    MyProfileActivity::class.java
                )
            )
        }

        binding.tvHowToDoDeskercises.setOnClickListener {
            startActivity(
                Intent(requireContext(), WebViewActivity::class.java).putExtra(
                    WebViewActivity.URL_LINK,
                    "https://sites.google.com/deskercise.com/deskercise"
                )
            )
        }

        binding.logout.setOnClickListener { startProgramViewModel.logout() }

        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val versionInfo = "Version $versionName ($versionCode)"
        binding.tvVersionInfo.text = versionInfo
    }

    private fun handleUserMissingInfo() {
        userManager.getUserInfo()?.let { user ->
            val companyFieldsValid =
                user.signUpCode.isNullOrEmpty() || (user.company != null) && (user.team != null) &&
                        (user.designation != null)
            val shouldOpenEditProfilePage =
                user.name.isEmpty() or user.dob.isNullOrEmpty() or user.country.isNullOrEmpty() or user.phoneNumber
                    .isNullOrEmpty() or !companyFieldsValid
            if (shouldOpenEditProfilePage) {
                FragmentEditProfile.start(requireContext())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfoUi()
        handleUserMissingInfo()
    }

    private fun updateUserInfoUi() {
        userManager.getUserInfo()?.let { user ->
            binding.civAvatar.loadImage(user.avatar)

            binding.currentPoints.text = buildString {
                append(getString(R.string.points, user.totalScore))
            }

            binding.streak.text =
                resources.getQuantityString(R.plurals.day_streak, user.streak, user.streak)
        }
    }

    private fun startDirect(id: Int, isRandom: Boolean, programModel: ExerciseGroupProgramModel) {
        startProgramViewModel.updateProgramID(id)
        startProgramViewModel.startProgram(isRandom)
        updateProgramModel(programModel)
    }

    private fun startDirect(programModel: ExerciseGroupProgramModel) {
        if (programModel.availableMoves.isEmpty()) {
            Toast.makeText(context, "No Exercises Added Yet", Toast.LENGTH_SHORT).show()
        } else {
            RecyclerViewAdapterAvailableExercise.listOfSelectedMovesShared =
                programModel.availableMoves
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startExerciseGroupProgram(programModel: ExerciseGroupProgramModel) {
        val exerciseGroupPreviewFragment = ExerciseGroupPreviewFragment.newInstance(programModel)

        val ft: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(
            R.id.cl_fragment_exercise_group_menu,
            exerciseGroupPreviewFragment,
            exercise_group_preview_fragment_tag,
        )
        ft.addToBackStack(exercise_group_menu_fragment_tag)
        ft.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentExerciseGroupMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun updateProgramModel(programModel: ExerciseGroupProgramModel) {
        this.programModel = programModel
        startProgramViewModel.updateProgramModel(programModel)
    }

    private fun goToLoginPage() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun registerLogout() {
        startProgramViewModel.logoutLiveData.observe(viewLifecycleOwner) { state: State<*> ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                    Toast.makeText(context, "You have been logged out.", Toast.LENGTH_SHORT).show()
                    goToLoginPage()
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    Toast.makeText(context, "You have been logged out.", Toast.LENGTH_SHORT).show()
                    goToLoginPage()
                }
            }
        }
    }
}

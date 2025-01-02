package com.ai.app.move.deskercise.ui.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.features.registerImagePicker
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.databinding.FragmentMyProfileBinding
import com.ai.app.move.deskercise.ui.myPoints.MyPointsActivity
import com.ai.app.move.deskercise.ui.profile.adapters.DynamicPointAdapter
import com.ai.app.move.deskercise.utils.addVerticalSpacing
import com.ai.app.move.deskercise.utils.getRealPathFromURI
import com.ai.app.move.deskercise.utils.gone
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.setTextColorCompat
import com.ai.app.move.deskercise.utils.simplyObserver
import com.ai.app.move.deskercise.utils.visible
import com.permissionx.guolindev.PermissionX
import com.thefashion.common.utils.ImageUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyProfileFragment : BaseBindingFragment<FragmentMyProfileBinding>() {
    enum class Mode {
        YOUR_POINT, TEAM_POINT
    }

    private val viewModel by viewModel<MyProfileViewModel>()
    override fun getViewBinding(): FragmentMyProfileBinding {
        return FragmentMyProfileBinding.inflate(layoutInflater)
    }

    private val dynamicPointAdapter by lazy { DynamicPointAdapter() }

    private val imagePickerLauncher = registerImagePicker { result ->
        val item = result.firstOrNull() ?: return@registerImagePicker
        uploadNewAvatar(item.uri)
    }

    private fun uploadNewAvatar(uri: Uri) {
        val path = requireContext().getRealPathFromURI(uri)
        val decodedFile =
            ImageUtils.decodeFile(requireContext(), Uri.parse(path), 400, 400) ?: return
        viewModel.changeAvatar(decodedFile)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.userInfoLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    setUpUserInfo(state.data)
                }
            }
        }

        viewModel.historiesLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    dynamicPointAdapter.updateData(state.data)
                }
            }
        }

        viewModel.teamUsersLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    dynamicPointAdapter.updateData(state.data)
                }
            }
        }

//        viewModel.scoreConditionLiveData.observe(viewLifecycleOwner) { state ->
//            if (state is State.Success) {
//                setUpCondition(state.data)
//            }
//        }
        binding.tvRewards.setOnClickListener {
            startActivity(Intent(this.context, MyPointsActivity::class.java))
        }

        simplyObserver(viewModel.updateAvatarLiveData) { state ->
            binding.ivAvatar.loadImage(state.data.avatar)
        }

        binding.actionTeam.setOnClickListener {
            changeMode(Mode.TEAM_POINT)
        }
        binding.tvYourPoints.setOnClickListener {
            changeMode(Mode.YOUR_POINT)
        }

        binding.rvDynamicPoints.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dynamicPointAdapter
            addVerticalSpacing(8f)
        }

        binding.tvEditProfile.setOnClickListener {
            FragmentEditProfile.start(requireContext())
        }
        binding.ivEditAvatar.setOnClickListener {
            openImagePickerDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateUser()
        viewModel.getUserHistories()
        viewModel.getTeamUsers()
        viewModel.getScoreCondition()
    }

    private fun changeMode(mode: Mode) {
        when (mode) {
            Mode.YOUR_POINT -> {
                binding.tvYourPoints.apply {
                    setBackgroundResource(R.drawable.bg_layout_corner)
                    setTextColorCompat(R.color.white)
                }
                binding.actionTeam.apply {
                    setBackgroundResource(R.drawable.bg_layout_corner_inactive)
                    setTextColorCompat(R.color.black)
                }
                dynamicPointAdapter.togglePointsType()
            }

            Mode.TEAM_POINT -> {
                binding.actionTeam.apply {
                    setBackgroundResource(R.drawable.bg_layout_corner)
                    setTextColorCompat(R.color.white)
                }
                binding.tvYourPoints.apply {
                    setBackgroundResource(R.drawable.bg_layout_corner_inactive)
                    setTextColorCompat(R.color.black)
                }
                dynamicPointAdapter.togglePointsType()
            }
        }
    }

    private fun setUpUserInfo(user: User) {
        binding.apply {
            tvUserName.text = user.name
            ivAvatar.loadImage(user.avatar)
            tvCompanyAndTeam.text = buildString {
                append(user.company?.name)
                append(" • ")
                append(user.team?.name)
            }
            if (user.signUpCode == null) {
                tvCompanyAndTeam.gone()
                tvYourPoints.setOnClickListener(null)
                changeMode(Mode.YOUR_POINT)
                actionTeam.gone()
            } else {
                tvCompanyAndTeam.visible()
                actionTeam.visible()
            }
            scoreSection.enableTeamPoint(user.signUpCode != null)
            scoreSection.setYourScore(user.totalScore)
            val totalTeamPoint = user.team?.totalScore ?: 0.0f
            val teamMemberCount: Int = user.team?.countUser ?: 1
            val teamPointAverage = totalTeamPoint / teamMemberCount
            scoreSection.setTeamPoint(teamPointAverage.toInt())
            scoreSection.setSessionScore(user.countSessionCompleted)
            streakSection.setStreak(user.streak)
        }
    }

//    private fun setUpCondition(res: ScoreConditionResponse) {
//        binding.streakSection.setDayStreaks(res.streaks)
//    }

    private fun openImagePickerDialog() {
        val actions =
            arrayOf(getString(R.string.choose_from_gallery), getString(R.string.capture_photo))
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_avatar))
            .setItems(actions) { dialog, which ->
                dialog.dismiss()
                if (which == 0) {
                    openGallery()
                } else {
                    openCamera()
                }
            }.create().show()
    }

    private fun openGallery() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.permission_explained),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.permission_enable_manually),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    imagePickerLauncher.launch(
                        ImagePickerConfig().apply {
                            mode = ImagePickerMode.SINGLE
                            isShowCamera = false
                        },
                    )
                }
            }
    }

    private fun openCamera() {
        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.permission_explained),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.permission_enable_manually),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    imagePickerLauncher.launch(CameraOnlyConfig())
                }
            }
    }
}

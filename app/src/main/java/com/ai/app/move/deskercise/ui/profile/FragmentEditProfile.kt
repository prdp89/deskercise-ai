package com.ai.app.move.deskercise.ui.profile

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.features.registerImagePicker
import com.hbb20.countrypicker.dialog.launchCountryPickerDialog
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseMvvmActivity
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.databinding.FragmentProfileEditBinding
import com.ai.app.move.deskercise.ui.login.LoginActivity
import com.ai.app.move.deskercise.ui.profile.adapters.CompanySpinnerAdapter
import com.ai.app.move.deskercise.ui.profile.adapters.StringSpinnerAdapter
import com.ai.app.move.deskercise.ui.profile.adapters.TeamSpinnerAdapter
import com.ai.app.move.deskercise.utils.getRealPathFromURI
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.parseTime
import com.ai.app.move.deskercise.utils.simpleItemSelected
import com.ai.app.move.deskercise.utils.simplyObserver
import com.permissionx.guolindev.PermissionX
import com.thefashion.common.utils.ImageUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class FragmentEditProfile : BaseMvvmActivity<FragmentProfileEditBinding, EditProfileViewModel>() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, FragmentEditProfile::class.java))
        }
    }

    override fun getViewBinding(): FragmentProfileEditBinding {
        return FragmentProfileEditBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModel<EditProfileViewModel>()
    override fun bindViewModel(): EditProfileViewModel {
        return viewModel
    }

    private val teamAdapter by lazy { TeamSpinnerAdapter(this) }
    private val companyAdapter: CompanySpinnerAdapter by lazy { CompanySpinnerAdapter(this) }
    private lateinit var user: User
    override fun registerEvents(viewModel: EditProfileViewModel) {
        super.registerEvents(viewModel)
        simplyObserver(viewModel.companyLiveData) { companies ->
            Timber.d(">> companies=${companies.size}")
            companyAdapter.setData(companies)
            companies.firstOrNull { it.id == user.company?.id }?.let { currentCompany ->
                val index = companies.indexOf(currentCompany)
                binding.companyName.setSelection(index)
                viewModel.getTeam(currentCompany.id)
            }
        }

        simplyObserver(viewModel.teamLiveData) { teams ->
            teamAdapter.setData(teams)
            teams.firstOrNull { it.id == user.team?.id }?.let { currentTeam ->
                val index = teams.indexOf(currentTeam)
                binding.teamName.setSelection(index)
            } ?: kotlin.run {
                binding.teamName.setSelection(0)
                user.team = teams[0]
            }
        }

        simplyObserver(viewModel.logoutLiveData,
            success = {
                Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
                goToLoginPage()
            })
        simplyObserver(viewModel.userLiveData,
            success = { user ->
                this.user = user
                initUserInfo(user)

                if (user.signUpCode.isNullOrEmpty()) {
                    hideCompanyFields()
                }
            }, error = {
                LoginActivity.startNewTask(this)
            })
        simplyObserver(viewModel.deleteAccountLiveData, success = {
            LoginActivity.startNewTask(this)
        }, error = {
            LoginActivity.startNewTask(this)
        })
        simplyObserver(viewModel.updateUserLiveData) {
            finish()
        }
    }

    private fun hideCompanyFields() {
        binding.iclDesignation.visibility = View.GONE
        binding.iclCompany.visibility = View.GONE
        binding.iclTeam.visibility = View.GONE
    }

    private fun initUserInfo(user: User) {
        binding.apply {
            etName.setText(user.name)
            dob.text = user.dob
            user.designation?.let { c ->
                val values = resources.getStringArray(R.array.Designation)
                val index = values.indexOf(c)
                if (index != -1) {
                    binding.etDesignation.setSelection(index)
                }
            }
            etPhoneNumber.setText(user.phoneNumber)
            gender.setSelection(user.gender)
            profile.loadImage(user.avatar)
            user.country?.let { c ->
                binding.country.text = c
            }
            viewModel.getCompany()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.ivEditAvatar.setOnClickListener {
            openImagePickerDialog()
        }

        initCountries()
        initGenders()
        initCompanyAdapter()
        initTeamAdapter()
        initDesignation()

        binding.backButtondiff.setOnClickListener {
            finish()
        }
        binding.dob.setOnClickListener {
            showDobDatePicker()
        }
        binding.logout.setOnClickListener {
            viewModel.logout()
        }
        binding.save.setOnClickListener {
            Timber.d(">> current user $user")
            user.timezone = TimeZone.getDefault().id
            viewModel.updateUserProfile(user)
            viewModel.sendProfile()
        }
//        binding.etDesignation.doAfterTextChanged { text ->
//            user.designation = text.toString()
//        }
        binding.etName.doAfterTextChanged { text ->
            user.name = text.toString()
        }
        binding.etPhoneNumber.doAfterTextChanged { text ->
            user.phoneNumber = text.toString()
        }
        binding.deleteAccount.setOnClickListener {
            askForDeleteAccount()
        }
        viewModel.getCurrentUserInfo()
    }

    private fun askForDeleteAccount() {
        AlertDialog.Builder(this)
            .setMessage(R.string.msg_confirm_delete_account)
            .setPositiveButton(R.string.lb_delete_account_positive) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.lb_delete_account_negative) { dialog, _ ->
                dialog.dismiss()
                viewModel.requestDeleteAccount()
            }
            .show()
    }

    private fun showDobDatePicker() {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        if (user.dob != null) {
            val date = user.dob?.parseTime("yyyy-MM-dd")
            val c = Calendar.getInstance()
            if (date != null) c.time = date
            val y: Int = c.get(Calendar.YEAR)
            val m: Int = c.get(Calendar.MONTH)
            val d: Int = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    user.dob = sdf.format(cal.time).toString()
                    binding.dob.text = user.dob
                },
                y,
                m,
                d,
            )
                .show()
        } else {
            val c = Calendar.getInstance()
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, monthOfYear)
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    binding.dob.text = sdf.format(c.time)
                    user.dob = sdf.format(c.time).toString()
                }

            DatePickerDialog(
                this,
                dateSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH),
            ).show()
        }
    }

    private fun initDesignation() {
        binding.etDesignation.apply {
            val designationAdapter = StringSpinnerAdapter(context)
            val values = resources.getStringArray(R.array.Designation)
            adapter = designationAdapter
            designationAdapter.setData(values.toList())
            simpleItemSelected { pos ->
                val selected = values[pos]
                user.designation = selected
            }
        }
    }

    private fun initGenders() {
        binding.gender.apply {
            val genderAdapter = StringSpinnerAdapter(context)
            val values = resources.getStringArray(R.array.Gender)
            adapter = genderAdapter
            genderAdapter.setData(values.toList())
            simpleItemSelected { pos ->
                user.gender = pos
            }
        }
    }

    private fun initCountries() {
        binding.country.apply {
            setOnClickListener {
                // Open dropdown menu to pick a country
                context.launchCountryPickerDialog(
                    preferredCountryCodes = "SG,MY,VN,IN,GB,US,CA"
                ) {
                    binding.country.text = it?.name
                    user.country = it?.name
                }
            }
        }
    }

    private fun initCompanyAdapter() {
        binding.companyName.adapter = companyAdapter
        binding.companyName.apply {
            adapter = companyAdapter
            simpleItemSelected { position ->
                val item = companyAdapter.getItem(position)
                user.company = item
                viewModel.getTeam(item.id)
            }
        }
    }

    private fun initTeamAdapter() {
        binding.teamName.apply {
            adapter = teamAdapter
            simpleItemSelected { position ->
                val item = teamAdapter.getItem(position)
                user.team = item
            }
        }
    }

    private val imagePickerLauncher = registerImagePicker { result ->
        val item = result.firstOrNull() ?: return@registerImagePicker
        uploadNewAvatar(item.uri)
    }

    private fun uploadNewAvatar(uri: Uri) {
        val path = this.getRealPathFromURI(uri)
        val decodedFile =
            ImageUtils.decodeFile(this, Uri.parse(path), 400, 400) ?: return
        user.avatar = decodedFile.path.orEmpty()
        binding.profile.loadImage(user.avatar)
    }

    private fun openImagePickerDialog() {
        val actions =
            arrayOf(
                getString(R.string.choose_from_gallery),
                getString(R.string.capture_photo),
                getString(R.string.delete_photo),
            )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.change_avatar))
            .setItems(actions) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                    else -> removeAvatar()
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

    private fun removeAvatar() {
        user.avatar = ""
        binding.profile.loadImage(null)
    }

    private fun goToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }
}

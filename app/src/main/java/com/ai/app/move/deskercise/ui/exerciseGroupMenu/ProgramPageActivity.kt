package com.ai.app.move.deskercise.ui.exerciseGroupMenu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.databinding.ActivityProgramPageBinding
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivity
import com.ai.app.move.deskercise.ui.profile.FragmentEditProfile

class ProgramPageActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProgramPageBinding
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                MainActivity.ErrorDialog.newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, MainActivity.FRAGMENT_DIALOG)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityProgramPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_program_page)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set the context so it starts the first audio without delay
        AudioManagerService(applicationContext)

        if (!isCameraPermissionGranted()) {
            requestPermission()
        }
//        binding.backButtondiff.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
//        }
        binding.profile.setOnClickListener {
            startActivity(Intent(this, FragmentEditProfile::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_program_page)
        return navController.navigateUp(appBarConfiguration) ||
            super.onSupportNavigateUp()
    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid(),
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA,
            ),
            -> {
                // DO NOTHING
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA,
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @Override
    override fun onBackPressed() {
        startActivity(Intent(this@ProgramPageActivity, ProgramPageActivity::class.java))
        finish()
    }
}

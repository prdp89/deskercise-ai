package com.ai.app.move.deskercise.ui.leaderboard

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.databinding.FragmentLeaderboardBinding
import com.ai.app.move.deskercise.network.responses.PersonalBoardResponse
import com.ai.app.move.deskercise.network.responses.TeamBoardResponse
import com.ai.app.move.deskercise.ui.home.HomeActivity
import com.ai.app.move.deskercise.ui.home.HomeBridge
import com.ai.app.move.deskercise.ui.leaderboard.adapters.TeamBoardAdapter
import com.ai.app.move.deskercise.ui.leaderboard.adapters.UserBoardAdapter
import com.ai.app.move.deskercise.utils.gone
import com.ai.app.move.deskercise.utils.isVisible
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.visible
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LeaderboardFragment : BaseBindingFragment<FragmentLeaderboardBinding>() {

    private val viewModel: LeaderBoardViewModel by viewModel()

    private val userManager: UserManager by inject()
    override fun getViewBinding(): FragmentLeaderboardBinding {
        return FragmentLeaderboardBinding.inflate(layoutInflater)
    }

    private val userBoardAdapter by lazy { UserBoardAdapter(userManager) }
    private val teamBoardAdapter by lazy { TeamBoardAdapter(userManager) }
    private lateinit var leaderBoardBridge: LeaderBoardBridge
    override fun onAttach(context: Context) {
        super.onAttach(context)
        leaderBoardBridge = context as? LeaderBoardBridge ?: LeaderBoardBridgeDefault()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerEvents()

        binding.rvExerciseLeaderboard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userBoardAdapter
        }

        binding.rvTeam.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = teamBoardAdapter
        }

        val spinner: Spinner = binding.leaderboardSpinner
        val hideTeamInfo = userManager.getUserInfo()?.signUpCode.isNullOrBlank()
        if (hideTeamInfo) spinner.visibility = View.GONE

        val spinnerAdapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.LeaderboardOptions,
            android.R.layout.simple_list_item_1,
        )

        spinner.adapter = spinnerAdapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                pos: Int,
                id: Long,
            ) {
                val selectedItem = spinner.selectedItem.toString()
                viewModel.updateBoardOption(selectedItem)
                viewModel.requestUpdateData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //  display indiv leaderboard information
            }
        }

        binding.today.setOnClickListener {
            viewModel.updateFilterValue("today")
            viewModel.requestUpdateData()
//            binding.tvResetTime.visible()
//            binding.tvResetTime.text = requireContext().getString(R.string.msg_reset_time)
            binding.week.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_unselected, null)
            binding.today.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_teal, null)
            binding.overall.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_unselected, null)
        }

        binding.week.setOnClickListener {
//            binding.tvResetTime.visible()
//            binding.tvResetTime.text = requireContext().getString(R.string.msg_reset_time_weekly)
            binding.week.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_teal, null)
            binding.today.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_unselected, null)
            binding.overall.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_unselected, null)
            viewModel.updateFilterValue("week")
            viewModel.requestUpdateData()
        }

        binding.overall.setOnClickListener {
//            binding.tvResetTime.gone()
            binding.overall.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_teal, null)
            binding.today.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_unselected, null)
            binding.week.background =
                ResourcesCompat.getDrawable(resources, R.drawable.circle_unselected, null)
            viewModel.updateFilterValue("overall")
            viewModel.requestUpdateData()
        }

        binding.exitButton.setOnClickListener {
            (requireActivity() as? HomeBridge)?.goToHome() ?: HomeActivity.startNewTask(
                requireContext(),)
        }

        viewModel.updateFilterValue("today")
        viewModel.updateBoardOption("Individual")
        viewModel.requestUpdateData()
        updateUserInfo()
    }

    override fun onResume() {
        super.onResume()
        binding.exitButton.isVisible(leaderBoardBridge.shouldShowBackButton())
        updateUserInfo()
        val selectedItem = binding.leaderboardSpinner.selectedItem.toString()
        viewModel.updateBoardOption(selectedItem)
        viewModel.requestUpdateData()
    }

    private fun updateUserInfo() {
        val user = userManager.getUserInfo() ?: return
        binding.currentUser.text = user.name
        if (user.signUpCode == null) {
            binding.leaderboardSpinner.gone()
        } else {
            binding.leaderboardSpinner.visible()
        }
    }

    private fun registerEvents() {
        viewModel.personalBoardLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                }

                is State.Starting -> {
                }

                is State.Success -> {
                    updateTop3UI(state.data)
                }
            }
        }

        viewModel.teamBoardLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                }

                is State.Starting -> {
                }

                is State.Success -> {
                    updateTop3TeamUI(state.data)
                }
            }
        }

        viewModel.profileBoardLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                }

                is State.Starting -> {
                }

                is State.Success -> {
                    val user = state.data
                    binding.currentRank.text = user.currentRank.toString()
                    val statusResId = when {
                        user.currentRank == user.beforeRank -> {
                            0
                        }

                        user.currentRank < user.beforeRank -> {
                            R.drawable.up
                        }

                        else -> {
                            R.drawable.ic_down
                        }
                    }
                    binding.ivRankStatus.setImageResource(statusResId)
                }
            }
        }
    }

    private fun updateTop3UI(users: List<PersonalBoardResponse>) {
        val size = users.size
        if (size > 1) {
            val top1 = users[0]
            binding.firstPlaceProfile.loadImage(top1.avatar)
            binding.firstPlaceName.text = top1.name
        }
        if (size > 2) {
            val top2 = users[1]
            binding.secondPlaceProfile.loadImage(top2.avatar)
            binding.secondPlaceName.text = top2.name
        }
        if (size > 3) {
            val top3 = users[2]
            binding.thirdPlaceProfile.loadImage(top3.avatar)
            binding.thirdPlaceName.text = top3.name
        }

        userBoardAdapter.setData(users)
        binding.switcher.displayedChild = 0
        val user = userManager.getUserInfo() ?: return
        val ownerItem = users.firstOrNull { it.id == user.id } ?: return kotlin.run {
            binding.currentUser.text = user.name
        }
        binding.currentUser.text = ownerItem.name
    }

    private fun updateTop3TeamUI(users: List<TeamBoardResponse>) {
        val size = users.size
        if (size > 1) {
            val top1 = users[0]
            binding.firstPlaceProfile.loadImage(null)
            binding.firstPlaceName.text = top1.name
        }
        if (size > 2) {
            val top2 = users[1]
            binding.secondPlaceProfile.loadImage(null)
            binding.secondPlaceName.text = top2.name
        }
        if (size > 3) {
            val top3 = users[2]
            binding.thirdPlaceProfile.loadImage(null)
            binding.thirdPlaceName.text = top3.name
        }

        teamBoardAdapter.setData(users)
        binding.switcher.displayedChild = 1

        val user = userManager.getUserInfo() ?: return
        val ownerItem = users.firstOrNull { it.id == user.team?.id } ?: return kotlin.run {
            binding.currentUser.text = user.team?.name
        }
        binding.currentUser.text = ownerItem.name
    }
}

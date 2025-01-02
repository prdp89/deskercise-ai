package com.ai.app.move.deskercise.ui.rewards.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.data.RewardDetail
import com.ai.app.move.deskercise.databinding.FragmentRewardDetailBinding
import com.ai.app.move.deskercise.ui.rewards.RedeemSuccessDialog
import com.ai.app.move.deskercise.ui.rewards.redeem.RedeemRewardBottomSheet
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.onLoadContent
import com.ai.app.move.deskercise.utils.simplyObserver
import org.koin.android.ext.android.inject

class RewardDetailFragment : BaseBindingFragment<FragmentRewardDetailBinding>() {

    companion object {
        var rewardId: Int = 0
    }

    private val viewModel by inject<RewardDetailViewModel>()

    private var linkMoreDetail: String = ""
    private var linkWebsite: String = ""
    lateinit var rewardDetail: RewardDetail
    override fun getViewBinding(): FragmentRewardDetailBinding {
        return FragmentRewardDetailBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getRewardDetail(rewardId)
        observeViewModel()
        initEventListener()
    }

    private fun initEventListener() {
        binding.ivBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvLinkMoreDetails.setOnClickListener {
            openWebsite(linkMoreDetail)
        }
        binding.llWebSite.setOnClickListener {
            openWebsite(linkWebsite)
        }
        binding.llPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            val phoneNumber = rewardDetail.contactDetails.phone
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }
        binding.tvRedeem.setOnClickListener {
            RedeemRewardBottomSheet(
                rewardDetail,
                onUseNowListener = {
                    viewModel.redeemReward(rewardId)
                },
                onUseLaterListener = {
                    //nothing
                }
            ).show(childFragmentManager, RedeemRewardBottomSheet::class.simpleName)
        }
    }

    private fun observeViewModel() {
        simplyObserver(viewModel.rewardDetailLiveData,
            success = { state ->
                linkMoreDetail = state.data.merchant.website
                linkWebsite = state.data.website
                rewardDetail = state.data
                initUIRewardDetail(state.data)
            }, error = {
                //nothing
            })

        simplyObserver(viewModel.redeemRewardLiveData) {
            showRedeemSuccessPopup()
        }
    }

    private fun initUIRewardDetail(rewardDetail: RewardDetail) {
        binding.apply {
            ivRewardImage.loadImage(rewardDetail.displayImgUrl)
            tvRewardName.text = rewardDetail.name
            tvRewardRequirePoint.text = "${rewardDetail.points} Point required"
            rewardDetail.merchant.description?.let {
                wvMerchantDescription.onLoadContent(
                    it,
                    false
                )
            }
            wvTermsAndConditions.onLoadContent(rewardDetail.termsAndConditions, false)
            val mSpannableString = SpannableString(tvLinkMoreDetails.text)
            mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
            tvLinkMoreDetails.text = mSpannableString
        }
    }

    private fun openWebsite(url: String) {
        val uri = Uri.parse(url)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun showRedeemSuccessPopup() {
        val dialog = RedeemSuccessDialog()
        dialog.show(parentFragmentManager, RedeemSuccessDialog::class.java.simpleName)
    }
}
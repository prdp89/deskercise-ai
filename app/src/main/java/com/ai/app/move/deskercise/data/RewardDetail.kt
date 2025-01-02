package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName

data class RewardDetail(
    @SerializedName("pk") val pk: Int,
    @SerializedName("name") var name: String,
    @SerializedName("points") val points: Int,
    @SerializedName("decimal_points") val decimalPoints: String,
    @SerializedName("merchant") val merchant: Merchant,
    @SerializedName("description") val description: String? = null,
    @SerializedName("contact_details") val contactDetails: ContactDetails,
    @SerializedName("is_points_dynamic") val isPointsDynamic: Boolean,
    @SerializedName("dynamic_points_conversion_ratio") val dynamicPointsConversionRatio: String? = null,
    @SerializedName("category") val category: Category,
    @SerializedName("display_category") val displayCategory: String,
    @SerializedName("reward_subcategory") val rewardSubcategory: String,
    @SerializedName("reward_description") val rewardDescription: String,
    @SerializedName("tier") val tier: Int,
    @SerializedName("website") val website: String,
    @SerializedName("terms_and_conditions") val termsAndConditions: String,
    @SerializedName("locations") val locations: List<String>,
    @SerializedName("brochure") val brochure: String,
    @SerializedName("allow_bulk") val allowBulk: Boolean,
    @SerializedName("valid_until") val validUntil: String,
    @SerializedName("thumbnail_img_url") val thumbnailImgUrl: String = "",
    @SerializedName("display_img_url") val displayImgUrl: String = "",
    @SerializedName("redemption_type") val redemptionType: String,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("delivery_fees") val deliveryFees: Int,
    @SerializedName("is_featured") val isFeatured: Boolean,
    @SerializedName("number_of_items") val numberOfItems: Int? = null,
    @SerializedName("qr_enabled") val qrEnabled: Boolean,
    @SerializedName("one_time_only") val oneTimeOnly: Boolean,
    @SerializedName("maximum_redemption_allowed") val maximumRedemptionAllowed: Int,
    @SerializedName("delivery_supported") val deliverySupported: Boolean,
    @SerializedName("redeemable") val redeemable: Boolean,
    @SerializedName("reason") val reason: String,
    @SerializedName("real_money") val realMoney: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("rating_count") val ratingCount: Int? = null,
    @SerializedName("stock") val stock: Int,
    @SerializedName("how_to_redeem") val howToRedeem: String,
    @SerializedName("options") val options: Options,
    @SerializedName("template") val template: Int,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("low_quantity") val lowQuantity: Int,
    @SerializedName("reward_department") val rewardDepartment: List<String>,
    @SerializedName("is_amazon_product") val isAmazonProduct: Boolean,
    @SerializedName("hide_reward_department") val hideRewardDepartment: Boolean,
) {
    data class Merchant(
        @SerializedName("pk") val pk: Int,
        @SerializedName("name") val name: String,
        @SerializedName("description") val description: String? = null,
        @SerializedName("website") val website: String,
    )

    data class ContactDetails(
        @SerializedName("pk") val pk: Int,
        @SerializedName("name") val name: String,
        @SerializedName("email") val email: String,
        @SerializedName("phone") val phone: String,
        @SerializedName("sms_number") val smsNumber: String,
        @SerializedName("user_img") val userImg: String,
    )

    data class Category(
        @SerializedName("pk") val pk: Int,
        @SerializedName("slug") val slug: String,
        @SerializedName("name") val name: String,
        @SerializedName("display_category") val displayCategory: String,
        @SerializedName("description") val description: String? = null,
        @SerializedName("thumbnail_img_url") val thumbnailImgUrl: String = "",
        @SerializedName("display_img_url") val displayImgUrl: String = "",
        @SerializedName("sub_categories") val subCategories: List<SubCategory>,
        @SerializedName("icon") val icon: String? = null,
        @SerializedName("allow_tier_sort") val allowTierSort: Boolean,
    ) {
        data class SubCategory(
            @SerializedName("pk") val pk: Int,
            @SerializedName("slug") val slug: String,
            @SerializedName("name") val name: String,
            @SerializedName("display_category") val description: String? = null,
            @SerializedName("thumbnail_img_url") val thumbnailImgUrl: String = "",
            @SerializedName("display_img_url") val displayImgUrl: String = "",
            @SerializedName("icon") val icon: String? = null,
        )
    }

    data class Options(
        @SerializedName("one_time_only") val oneTimeOnly: String,
        @SerializedName("min_inventory_count") val minInventoryCount: String,
        @SerializedName("dedicated_charity") val dedicatedCharity: String,
        @SerializedName("account_type") val accountType: String,
        @SerializedName("product_id") val productId: String,
        @SerializedName("redemption_count") val redemptionCount: String,
        @SerializedName("deliver_by") val deliverBy: String,
        @SerializedName("barcode") val barcode: String,
        @SerializedName("custom_template_title") val customTemplateTitle: String,
        @SerializedName("theme") val theme: String,
        @SerializedName("generated") val generated: String,
        @SerializedName("external_api") val externalApi: String,
        @SerializedName("qrcode") val qrcode: String,
        @SerializedName("allow_bulk_redemption") val allowBulkRedemption: String,
        @SerializedName("ug_order_id") val ugOrderId: String,
        @SerializedName("charity") val charity: String,
    )
}
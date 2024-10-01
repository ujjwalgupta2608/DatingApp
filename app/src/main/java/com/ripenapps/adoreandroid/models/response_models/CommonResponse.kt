package com.ripenapps.adoreandroid.models.response_models

import com.google.gson.annotations.SerializedName

data class CommonResponse(
    @SerializedName("message")val message: String,
    @SerializedName("status")val status: Int,
    @SerializedName("expiryTimeMillis") val expiryTimeMillis: Long,
    @SerializedName("startTimeMillis") val startTimeMillis: Long,
    @SerializedName("autoRenewing") val autoRenewing: Boolean,
    @SerializedName("priceCurrencyCode") val priceCurrencyCode: String,
    @SerializedName("priceAmountMicros") val priceAmountMicros: Long,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("developerPayload") val developerPayload: String?,
    @SerializedName("paymentState") val paymentState: Int,
    @SerializedName("cancelReason") val cancelReason: Int?,
    @SerializedName("userCancellationTimeMillis") val userCancellationTimeMillis: Long?,
    @SerializedName("cancelSurveyResult") val cancelSurveyResult: CancelSurveyResult?,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("purchaseType") val purchaseType: Int?,
    @SerializedName("acknowledgementState") val acknowledgementState: Int,
    @SerializedName("kind") val kind: String
)

data class CancelSurveyResult(
    @SerializedName("cancelSurveyReason") val cancelSurveyReason: Int,
    @SerializedName("userInputCancelReason") val userInputCancelReason: String?
)
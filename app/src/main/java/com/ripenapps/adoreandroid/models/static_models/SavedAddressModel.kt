package com.ripenapps.adoreandroid.models.static_models

import com.google.gson.annotations.SerializedName

data class SavedAddressModel(
    @SerializedName("city") var city: String? = "",
    @SerializedName("country") var country: String? = "",
    @SerializedName("latitude") var latitude: String? = "",
    @SerializedName("longitude") var longitude: String? = "",
    @SerializedName("locality") var locality: String? = "",
    @SerializedName("fullAddress") var fullAddress: String? = ""
)

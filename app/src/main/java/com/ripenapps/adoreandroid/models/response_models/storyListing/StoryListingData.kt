package com.ripenapps.adoreandroid.models.response_models.storyListing

data class StoryListingData(
    val listing: List<StoryListing>,
    val total: Int,
    val userData: UserData
) {
    data class UserData(
        val isStoryExists: Boolean,
        val profile: String? = ""
    )
}
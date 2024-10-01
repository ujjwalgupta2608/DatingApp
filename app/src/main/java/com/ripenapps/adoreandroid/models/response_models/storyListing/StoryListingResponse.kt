package com.ripenapps.adoreandroid.models.response_models.storyListing

data class StoryListingResponse(
    val data: StoryListingData,
    val message: String,
    val status: Int
)
package com.ripenapps.adoreandroid.models.static_models

data class AppPermission(
    val permission: Array<String>,
    val requestCode: Int,
    val desc: String
)


data class AppDialog(
    val title: String,
    val desc: String,
    val positiveActionText: String? = null,
    val negativeActionText: String? = null,
    val positiveActionColor: Int? = null,
    val icon: Int? = 0,
    val showButtons:Int?=1,
    )
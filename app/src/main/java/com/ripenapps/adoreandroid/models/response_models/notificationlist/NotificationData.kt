package com.ripenapps.adoreandroid.models.response_models.notificationlist

import com.ripenapps.adoreandroid.models.response_models.likeUnlikeResponse.Sender
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NotificationData(
    val __v: Int?=0,
    val _id: String?="",
    val createdAt: String?="",
    val isCancelled: Boolean?=false,
    val isSchedule: Boolean?=false,
//    val jobId: Any?,
    val message: String?="",
    val title: String?="",
    val notificationType:String?="",
    val callStatus:Boolean?=false,
    val roomId:String?="",
    val sender:Sender?=Sender(),
    val user:String?="",
    val totalSend: Int?=0,
    val updatedAt: String?="",
    val duration:String?="",
    val chatId:String?=""
): Serializable
package com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse

data class ReceivedMessageResponse(
    var isStory:Boolean?=false,
    var isOnline:String?="",
    var lastseen:String?="",
    var roomId:String?="",
    var _id:String?="",
    var receiverId:String?="",
    var messageType:String?="",
    var message:String?="",
    var isRead:Boolean?=false,
    var createdAt:String?="",
    var updatedAt:String?="",
    var dateToShow:String="",
    var callStatus:String="",
    var duration:String="",
    var media:MutableList<ReceivedMediaClass> = mutableListOf()
)

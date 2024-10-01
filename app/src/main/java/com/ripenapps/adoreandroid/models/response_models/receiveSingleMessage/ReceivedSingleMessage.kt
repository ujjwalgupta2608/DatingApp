package com.ripenapps.adoreandroid.models.response_models.receiveSingleMessage

import com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse.ReceivedMessageResponse

data class ReceivedSingleMessage (
    var status:Int?=0,
    var message:String?="",
    var result:ReceivedMessageResponse = ReceivedMessageResponse()
)
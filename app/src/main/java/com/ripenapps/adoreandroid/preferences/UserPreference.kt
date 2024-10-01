package com.ripenapps.adoreandroid.preferences

import com.ripenapps.adoreandroid.models.response_models.FilterCardListRequestKeys
import com.ripenapps.adoreandroid.models.static_models.NotificationMessageData

object UserPreference {
    var token = ""
    var drugKey = ""
    var deviceToken = ""
    var fullName = "fullName"
    var mobileNumber = ""
    var countryCode = ""
    var emailAddress = "emailAddress"
    var userName = "userName"
    var password = "password"
    var isUserDisliked = ""
    var id = ""
    var uploadStory = ""
    var path = ""
    var type = -1
    var isNewUserRegistered = false
    var backFromChatMedia = false
    var isManualLocSelected = false
    var filterCardListRequestKeys = FilterCardListRequestKeys()
    var filterMapListRequestKeys = FilterCardListRequestKeys()
    var isRewind = false  //whenever card is rewind the card after the rewind card automatically swipeLeft or swipeRight based on rewind card was swipeLeft or swipeRight, to avoid this, this check is taken
    var isFilterApplied = false
    var numberOfRewind = 0
    var boostStatus = false
    var endTime: String? = ""
    var boostCount: Int? = 5
    var AGORA_TOKEN = "007eJxTYDDp3SWzb5uimNhB/V+mL5lvLmFi/xjmUq06+8V/76bHa+YoMJimJVkamZlaWiamJpkkJRpbGiYbWRqZmCcnGyeZpZkbKnYuTG4IZGRgnyjPxMgAgSA+D0NBYkFiZWJyfkpqUTEDAwAi+yGx"
    var CHANNEL_NAME = "papayacoders"
    var CALL_USER_IMAGE = "calluserimage"
    var CALL_USER_NAME = "callusernname"
    var MSG_ID = "msgid"
    var ROOM_ID = "roomid"
    var RECEIVER_ID = "receiver_id"
    var NOTIFICATION = "notification"
    var SENDER_ID = "sender_id"
    var NOTIFICATION_ID = "notification_id"
    var MESSAGE_USER_NAME = "message_user_name"
    var MESSAGE_USER_PROFILE = "message_user_profile"
    var NotificationData = NotificationMessageData()
    var messagedUserId=""
    var socialId=""
    var deepLinkProfileId=""
    var anotherUserSubscribedFromThisPlayStore=""
    var hasUserSubscribedBefore=""
    var userSubscribedFromAnotherPlayStore="0"
    var isChatFragmentOpen="0"
    var savedLanguageCode=""

    fun clear() {
        token = ""
        drugKey = ""
        deviceToken = ""
        fullName = "fullName"
        mobileNumber = ""
        countryCode = ""
        emailAddress = "emailAddress"
        userName = "userName"
        password = "password"
        isUserDisliked = ""
        id = ""
        uploadStory = ""
        path = ""
        type = -1
        isNewUserRegistered = false
        backFromChatMedia = false
        isManualLocSelected = false
        filterCardListRequestKeys = FilterCardListRequestKeys()
        filterMapListRequestKeys = FilterCardListRequestKeys()
        isRewind = false
        isFilterApplied = false
        numberOfRewind = 0
        boostStatus = false
        endTime = ""
        boostCount = 5
        AGORA_TOKEN =
            "007eJxTYDDp3SWzb5uimNhB/V+mL5lvLmFi/xjmUq06+8V/76bHa+YoMJimJVkamZlaWiamJpkkJRpbGiYbWRqZmCcnGyeZpZkbKnYuTG4IZGRgnyjPxMgAgSA+D0NBYkFiZWJyfkpqUTEDAwAi+yGx"
        CHANNEL_NAME = "papayacoders"
        CALL_USER_IMAGE = "calluserimage"
        CALL_USER_NAME = "callusernname"
        MSG_ID = "msgid"
        ROOM_ID = "roomid"
        MESSAGE_USER_NAME = "message_user_name"
        MESSAGE_USER_PROFILE = "message_user_profile"
        NotificationData = NotificationMessageData()
        messagedUserId=""
        anotherUserSubscribedFromThisPlayStore=""
        hasUserSubscribedBefore="0"
        userSubscribedFromAnotherPlayStore="0"
        isChatFragmentOpen="0"
        savedLanguageCode=""

    }
}
package com.ripenapps.adoreandroid.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ripenapps.adoreandroid.models.static_models.AppPermission

private const val SHARED_PREF_NAME = "permissions_preferences"
val permissionTitle = "Permissions"
val LOCATION_REQUEST_CODE by lazy { 100 }
val CAMERA_REQUEST_CODE by lazy { 102 }
val permissionsList by lazy {
    HashMap<Int, AppPermission>().apply {
        put(
            LOCATION_REQUEST_CODE,
            locationPermission
        )
        put(CAMERA_REQUEST_CODE, cameraPermission)
    }
}
val locationPermission by lazy {
    AppPermission(
        arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ),
        LOCATION_REQUEST_CODE,
        "Allow Shindindi to access your location to show nearby provider"
    )
}
val cameraPermission by lazy {
    AppPermission(
        arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            /*android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO*/
        ), CAMERA_REQUEST_CODE,
        "You need to allow these permissions to access camera or gallery media."
    )
}

/*fun permissionAsked(requestCode: Int): Boolean {
    return Hawk.get(requestCode.toString(), false)
}

fun setPermissionAsked(requestCode: Int) {
    if (!permissionAsked(requestCode)) {
        Hawk.put(requestCode.toString(), true)
    }
}*/
fun permissionAsked(context: Context, requestCode: Int): Boolean {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(requestCode.toString(), false)
}

fun setPermissionAsked(context: Context, requestCode: Int) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    if (!permissionAsked(context, requestCode)) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(requestCode.toString(), true)
        editor.apply()
    }
}

fun Fragment.askForPermission(
    permission: AppPermission,
    listener: SetPermissionListener? = null,
    showMessage: Boolean = false,
    isMust: Boolean = false
) {
    context?.let {
        if (isGranted(it, permission.permission)) {
            listener?.onPermissionGranted(permission.requestCode)
        } else {
            if (!isUserGuidRequired(permission.permission) && permissionAsked(it, permission.requestCode)) {
                createInfoDialog(
                    context!!,
                    permissionTitle,
                    "${permission.desc}\n${
                        "To allow this permission, go to your App Permission Settings."   
                    }",
                    cancellable = !isMust
                )
                return@let
            }
            if (showMessage) {
                if (isMust)
                    createInfoDialogWithOption(
                        requireContext(), permissionTitle, permission.desc, false,
                        object : AppDialogListener {
                            override fun onPositiveButtonClickListener(dialog: Dialog) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestRequiredPermission(
                                        it,
                                        permission.permission,
                                        permission.requestCode
                                    )
                                }
                                dialog.dismiss()
                            }

                            override fun onNegativeButtonClickListener(dialog: Dialog) {
                                dialog.dismiss()
                            }
                        },
                    ) else
                    createYesNoDialog(
                        object : AppDialogListener {
                            override fun onPositiveButtonClickListener(dialog: Dialog) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestRequiredPermission(
                                        it,
                                        permission.permission,
                                        permission.requestCode
                                    )
                                }
                                dialog.dismiss()
                            }

                            override fun onNegativeButtonClickListener(dialog: Dialog) {
                                dialog.dismiss()
                            }
                        },
                        it,
                        permissionTitle,
                        permission.desc,
                        "Ask",
                        "Cancel"
                    )

            } else {
                requestRequiredPermission(
                    it,
                    permission.permission,
                    permission.requestCode
                )
            }
        }
    }

}


fun Fragment.requestRequiredPermission(context: Context, p: Array<String>, requestCode: Int) {
    setPermissionAsked(context, requestCode = requestCode)
    requestPermissions(p, requestCode)

}

fun Activity.requestRequiredPermission(context: Context, p: Array<String>, requestCode: Int) {
    setPermissionAsked(context , requestCode = requestCode)
    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        requestPermissions(p, requestCode)
    }
}

fun isGranted(context: Context, permission: Array<String>): Boolean {
    for (p in permission) {
        if (ContextCompat.checkSelfPermission(
                context,
                p
            ) != PackageManager.PERMISSION_GRANTED
        )
            return false
    }
    return true
}

fun Fragment.isUserGuidRequired(permission: Array<String>): Boolean {
    var isShow = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        for (p in permission) {
            if (shouldShowRequestPermissionRationale(p))
                isShow = true
        }
    return isShow
}

fun Activity.isUserGuidRequired(permission: Array<String>): Boolean {
    var isShow = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        for (p in permission) {
            if (shouldShowRequestPermissionRationale(p))
                isShow = true
        }
    return isShow
}


interface SetPermissionListener {
    fun onPermissionGranted(requestCode: Int)
    fun onPermissionDenied(requestCode: Int) {}

    fun Fragment.onRequestPermissions(
        listener: SetPermissionListener?,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        permissionsList[requestCode]?.let {
            askForPermission(it, listener, true)
        }
    }

    fun Fragment.askForPermission(
        context: Context,
        permission: AppPermission,
        listener: SetPermissionListener? = null,
        showMessage: Boolean = false
    ) {

        if (isGranted(context, permission.permission)) {
            listener?.onPermissionGranted(permission.requestCode)
        } else {
            if (isUserGuidRequired(permission.permission)) {
                createInfoDialog(
                    context,
                    permissionTitle,
                    "You need to enable the required permissions to access the camera or gallery media.",
                    true
                )
                listener?.onPermissionDenied(permission.requestCode)
                return
            }else if (!isUserGuidRequired(permission.permission)) {
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestRequiredPermission(
                                    context,
                                    permission.permission,
                                    permission.requestCode
                                )
                            }
                            dialog.dismiss()
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            listener?.onPermissionDenied(permission.requestCode)
                            dialog.dismiss()
                        }

                    },
                    context,
                    permissionTitle,
                    permission.desc,
                    "Ask",
                    "Cancel"
                )

            } else {
                requestRequiredPermission(
                    context,
                    permission.permission,
                    permission.requestCode
                )
            }
        }
    }



    fun Fragment.onRequestPermissions(
        context: Context,
        listener: SetPermissionListener?,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        permissionsList[requestCode]?.let {
            askForPermission(context,it, listener, true)
        }
    }

}
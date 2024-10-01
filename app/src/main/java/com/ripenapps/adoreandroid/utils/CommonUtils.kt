package com.ripenapps.adoreandroid.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.ripenapps.adoreandroid.R
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URISyntaxException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.*
import java.util.regex.Pattern


/*class KeyboardVisibilityListener(private val activity: Activity, private val onKeyboardVisibilityChanged: (Boolean) -> Unit) {

    private var rootView: View = activity.window.decorView.rootView
    private var isKeyboardVisible = false

    init {
        rootView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onPreDraw(): Boolean {
                val currentVisibility = isKeyboardVisible
                val insets = rootView.rootWindowInsets
                isKeyboardVisible = insets?.isVisible(InputMethodManager.SHOW_IMPLICIT) == true
                if (currentVisibility != isKeyboardVisible) {
                    onKeyboardVisibilityChanged.invoke(isKeyboardVisible)
                }
                return true
            }
        })
    }
}*/

object CommonUtils {
    fun hideKeyBoard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethodManager.isActive) {
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            } else
                return
        } catch (e: Exception) {

        }

    }

    fun showKeyBoard(activity: Activity, view: View) {
        try {
            view.requestFocus()
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            // Handle exceptions as needed
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver, inImage,
            "Title" + System.currentTimeMillis(), null
        )
        return Uri.parse(path)
    }

    fun getRealPathFromDocumentUri(context: Context, contentURI: Uri): String? {
        var filePath = ""
        val p = Pattern.compile("(\\d+)$")
        val m = p.matcher(contentURI.toString())
        if (!m.find()) {
            return filePath
        }
        val imgId = m.group()
        val column = arrayOf(MediaStore.Images.Media.DATA)
        val sel = MediaStore.Images.Media._ID + "=?"
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(imgId), null
        )?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(column[0])
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
        }
        return filePath
    }
    /*fun getUriFromUrl(context: Context, url: String, onSuccess: (Uri) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val connection = URL(url).openConnection()
                connection.connect()
                inputStream = connection.getInputStream()

                val outputFile = File(context.cacheDir, "shared_image")
                outputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()

                val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", outputFile)
                onSuccess(uri)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    } */
    fun createFileSmall(filePath: String?, context: Context?): File? {
        val compressedBitmap = ImageUtils.getInstant().getCompressedBitmap(filePath)

        if (compressedBitmap != null) {
            val imageUri = getImageUri(context!!, compressedBitmap)
            val realPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getImageFilePath(context, imageUri)
            } else {
                getRealPathFromURI(context, imageUri)
            }

            if (realPath != null) {
                return File(realPath)
            } else {
                Log.e("QuestionsActivity", "Real path is null in createFileSmall")
            }
        } else {
            Toast.makeText(context, "Unsupported file format!", Toast.LENGTH_SHORT).show()
        }

        return null
    }

    private fun getImageFilePath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    fun getRealPathFromURI(context: Context, uri: Uri): String {
        var path = ""

        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    path = cursor.getString(idx)
                }

                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return path
    }
    private fun showRationaleDialog(activity: Activity, token: PermissionToken) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setMessage("To capture photos and videos, allow Adore access to your camera and your device’s photos, media, and files.")
        dialog.setTitle(activity.resources.getString(R.string.app_name))
        dialog.setPositiveButton("Allow") { _, _ ->
            token.continuePermissionRequest()
        }
        dialog.setNegativeButton("Deny") { dialog, _ ->
            dialog.dismiss()
            showSettingsDialog(activity, 100)
            // Handle denial, if needed
        }
        dialog.show()
    }
    private fun showSettingsDialog(activity: Activity, PERMISSION_REQUEST_CODE: Int) {
        val dialog = AlertDialog.Builder(activity)
        dialog.setMessage(activity.getString(R.string.shindindi_needs_this_permission))
        dialog.setTitle(activity.resources.getString(R.string.app_name))
        dialog.setPositiveButton("Ok") { _, _ ->
            openSettings(activity, PERMISSION_REQUEST_CODE)
        }
        dialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            // Handle denial, if needed
        }
        dialog.show()
    }
    private fun openSettings(activity: Activity, PERMISSION_REQUEST_CODE: Int) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }
    fun requestPermissionsNew(
        activity: Activity, PERMISSION_REQUEST_CODE: Int, permissions: Array<String?>
    ): Boolean {
        val isAllPermissionGranted = booleanArrayOf(false) // Initialize with false

        Dexter.withContext(activity).withPermissions(*permissions)
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        isAllPermissionGranted[0] = true
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog(activity, PERMISSION_REQUEST_CODE)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>, token: PermissionToken
                ) {
                    // Handle the rationale message and re-request the permission
                    showRationaleDialog(activity, token)
                }
            }).withErrorListener {
                showSettingsDialog(activity, PERMISSION_REQUEST_CODE)
                Log.e("TAG", "requestPermissions: " + it)
            }.onSameThread().check()

        return isAllPermissionGranted[0]
    }
    fun requestPermissions(context: Context?, permissions: List<String>): Boolean {
        Log.i("TAG", "requestPermission: " + permissions.toString())
        val isAllPermissionGuaranted = BooleanArray(1)
        Dexter.withContext(context as Activity?)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    // check if all permissions are granted
                    if (!p0.areAllPermissionsGranted()) {
                        //                            Toast.makeText(context, "Please allow All Permissions", Toast.LENGTH_SHORT).show();
                        //                            requestPermissions(context, permissions);
                        showSetting(context!!)
                    } else
                        isAllPermissionGuaranted[0] = true


                    // check for permanent denial of any permission


                    // check for permanent denial of any permission
                    if (p0 != null) {
                        if (p0.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    if (p1 != null) {
                        p1.continuePermissionRequest()
                    }
                    showSetting(context!!)

                }
            }).check()
        return isAllPermissionGuaranted[0]
    }

    private fun showSetting(context: Context) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.shindindi_needs_this_permission))
            .setTitle(context.getString(R.string.app_name))
            .setCancelable(false)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog: DialogInterface?, whichButton: Int ->
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri =
                    Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            .show()
    }


    fun isImageUri(contentResolver: ContentResolver, uri: Uri): Boolean {
        val projection = arrayOf(MediaStore.MediaColumns.MIME_TYPE)

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val mimeType =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                return mimeType.startsWith("image/")
            }
        }

        return false
    }

    fun isVideoUri(contentResolver: ContentResolver, uri: Uri): Boolean {
        val projection = arrayOf(MediaStore.MediaColumns.MIME_TYPE)

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val mimeType =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                return mimeType.startsWith("video/")
            }
        }

        return false
    }

    public object DotPasswordTransformationMethod : PasswordTransformationMethod() {
        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(super.getTransformation(source, view))
        }

        private class PasswordCharSequence(
            val transformation: CharSequence
        ) : CharSequence by transformation {
            override fun get(index: Int): Char = if (transformation[index] == DOT) {
                STAR
            } else {
                transformation[index]
            }
        }

        private const val DOT = '\u2022'
        private const val STAR = '*'
    }

    @Throws(URISyntaxException::class)
    fun getFilePath(context: Context, uri: Uri): String? {
        var selection: String? = null
        var selectionArgs: Array<String>? = null

        var modifiedUri = uri // Declare a new variable to handle modified uri

        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(
                context.applicationContext,
                uri
            )
        ) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    modifiedUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                }

                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    when {
                        "image" == type -> modifiedUri =
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                        "video" == type -> modifiedUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" == type -> modifiedUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
        }

        if ("content".equals(modifiedUri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(modifiedUri)) {
                return modifiedUri.lastPathSegment
            }

            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null

            try {
                cursor = context.contentResolver.query(
                    modifiedUri,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )

                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        } else if ("file".equals(modifiedUri.scheme, ignoreCase = true)) {
            return modifiedUri.path
        }

        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
    /*
        fun decodeFile(path: String, DESIREDWIDTH: Int, DESIREDHEIGHT: Int): String? {
            var strMyImagePath: String? = null
            var scaledBitmap: Bitmap? = null
            try {
                // Part 1: Decode image
                val unscaledBitmap: Bitmap = ScalingUtilities.decodeFile(
                    path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT
                )
                scaledBitmap =
                    if (!(unscaledBitmap.width <= DESIREDWIDTH && unscaledBitmap.height <= DESIREDHEIGHT)) {
                        // Part 2: Scale image
                        ScalingUtilities.createScaledBitmap(
                            unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT
                        )
                    } else {
                        unscaledBitmap.recycle()
                        return path
                    }

                // Store to tmp file
                val extr = Environment.getExternalStorageDirectory().toString()
                val mFolder = File("$extr/TMMFOLDER")
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                val s = "tmp.png"
                val f = File(mFolder.absolutePath, s)
                strMyImagePath = f.absolutePath
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(f)
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos)
                    fos.flush()
                    fos.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                scaledBitmap.recycle()
            } catch (e: Throwable) {
            }
            return path
        }
    */
    fun isValidPassword(input: String): Boolean {
        val regex =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[d\$@$!%*?&#])[A-Za-z\\dd\$@$!%*?&#]{8,}")
        return regex.matches(input)
    }

    fun passwordValidation(passwordValue: String): String? {
        if (passwordValue.isNullOrEmpty()) {
            return "Please enter a password."
        }
        if (!isValidPassword(passwordValue)) {
            return "Password length can not be less than 8 digit."
        }
        if (passwordValue.length < 8) {
            return "Invalid password, it must be a combination of capital letters, small letters, numbers, and special characters."
        }
        return null
    }
    fun isVideoFile(filePath: String): Boolean {
        val videoExtensions = arrayOf("mp4", "avi", "mkv", "mov", "flv", "wmv") // Add more extensions if needed
        val fileExtension = filePath.substringAfterLast(".", "")

        return videoExtensions.contains(fileExtension.toLowerCase())
    }

    fun isImageFile(filePath: String): Boolean {
        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp") // Add more extensions if needed
        val fileExtension = filePath.substringAfterLast(".", "")

        return imageExtensions.contains(fileExtension.toLowerCase())
    }
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    fun getJson(obj: Any): String {
        return Gson().toJson(obj)
    }

    fun <T : Any> getObj(json: String?, t: Class<T>): T? {
        if (json.isNullOrEmpty())
            return null
        return try {
            Gson().fromJson(json, t)
        } catch (e: Exception) {
            null
        }
    }
    fun getMediaDuration(context: Context, uri: Uri): Int {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val durationString =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toLongOrNull() ?: 0

            retriever.release()

            // Convert duration to seconds
            return (duration / 1000).toInt()
        } catch (e: Exception) {
            // Handle exceptions (e.g., IOException, IllegalArgumentException)
            e.printStackTrace()
        }

        return 0
    }
    fun convertTimeToDateFormat(timeString: String, outputFormat: String): String {
        // Input format assumed to be "HH:mm:ss" for time
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat(outputFormat, Locale.getDefault())

        try {
            val time = inputFormat.parse(timeString)
            return outputFormat.format(time)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return an empty string or any default value if conversion fails
        return ""
    }
    /*fun convertTimestampToAndroidTime(inputTimestamp: String, format:String): String {
        try {
            // Parse the input timestamp
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = inputFormat.parse(inputTimestamp)

            // Format it to the desired time format
            val outputFormat = SimpleDateFormat(format, Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("GMT+11")
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception or return an error message as needed
            return "Error converting timestamp"
        }
    }*/
    fun convertTimestampToAndroidTime(inputTimestamp: String, format: String, targetTimeZone: String = TimeZone.getDefault().id): String {
        return try {
            // Parse the input timestamp
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Input timestamp is in UTC by default

            val date = inputFormat.parse(inputTimestamp)

            // Format it to the desired time format and target timezone
            val outputFormat = SimpleDateFormat(format, Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone(targetTimeZone) // Use target timezone

            outputFormat.format(date) ?: "Invalid date"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error converting timestamp"
        }
    }
    /*fun getFormattedDateToday(requiredFormat: String): String {
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.HOUR_OF_DAY, 5)
        calendar.add(Calendar.MINUTE, 30)
        // Format the updated date
        val dateFormat = SimpleDateFormat(requiredFormat, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(calendar.time)
    }*/
    fun getFormattedDateToday(requiredFormat: String): String {
        // Get the current date
        val currentDate = Date()

        // Create a SimpleDateFormat instance with the required format
        val dateFormat = SimpleDateFormat(requiredFormat, Locale.getDefault())

        // Set the timezone for formatting
        dateFormat.timeZone = TimeZone.getDefault()

        // Format the current date according to the timezone
        return dateFormat.format(currentDate)
    }

    fun getFormattedDateYesterday(requiredFormat: String): String {
        // Get the current date
        val currentDate = Date()

        // Calculate yesterday's date
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Subtract 1 day for yesterday

        // Create a SimpleDateFormat instance with the required format
        val yesterdayFormat = SimpleDateFormat(requiredFormat, Locale.getDefault())

        // Set the timezone for formatting
        yesterdayFormat.timeZone = TimeZone.getDefault()

        // Format yesterday's date according to the timezone
        return yesterdayFormat.format(calendar.time)
    }
    fun getCurrentTimeInUTC(requiredFormat: String): String {
        val currentTime = Date()

        // Format the current time in UTC timezone
        val timeFormat = SimpleDateFormat(requiredFormat, Locale.getDefault())
        timeFormat.timeZone = TimeZone.getTimeZone("UTC")
        return timeFormat.format(currentTime)
    }
    fun compareDates(dateString1: String, dateString2: String, dateFormat: String): Boolean {
        if (dateString1.isEmpty() || dateString2.isEmpty()) {
            // Handle empty strings, return false or throw an exception as needed
            return false
        }

        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        try {
            val date1 = sdf.parse(dateString1)
            val date2 = sdf.parse(dateString2)
            return date1 > date2
        } catch (e: ParseException) {
            e.printStackTrace()
            // Handle parsing exception here
        }

        // Default return value in case of parsing errors
        return false
    }
    fun subractTimes(dateString1: String, dateString2: String, dateFormat: String): Long {
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val date1 = sdf.parse(dateString1)
        val date2 = sdf.parse(dateString2)

        val diffInMillis = date1.time - date2.time

        // Convert milliseconds difference to desired format
        val diff = diffInMillis / (1000 * 60)

        return diff
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNumberOfDaysInCurrentMonth(): Int {
        val currentDate = LocalDate.now()
        val yearMonth = YearMonth.of(currentDate.year, currentDate.month)
        return yearMonth.lengthOfMonth()
    }
     fun checkPermissions(context:Context): Boolean {

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
     fun isLocationEnabled(activity:Activity): Boolean {
        val locationManager: LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    fun isBluetoothConnectd(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return (bluetoothAdapter != null && BluetoothAdapter.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET))
    }
    fun isAppOnForeground(context: Context): Boolean {
        val appPackageName = context.packageName.toString()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == appPackageName) {
                return true
            }
        }
        return false
    }
    fun isForegroundServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    fun stopBackgroundMusicService(context: Context) {
        val svc = Intent(context, SoundService::class.java)
        context.stopService(svc)
    }
    fun startBackgroundMusicService(context: Context) {
        try {
            val svc = Intent(context, SoundService::class.java)
            context.startService(svc)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }
    fun <A : Activity> Context.launchActivityWithBundle(activity: Class<A>, bundle: Bundle) {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).also {
            it.putExtra("bundle", bundle)
            it.setClassName("com.ripenapps.adoreandroid", activity.name)
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.flags = Intent.FLAG_FROM_BACKGROUND
            it.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isMiUi(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
    }

    fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }
    fun formatSecondsToMinutesAndSeconds(secondsStr: String): String {
        val seconds = secondsStr.toIntOrNull() ?: 0
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    fun getVideoPathFromUri(context: Context, videoUri: Uri): String? {
        var path: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                videoUri
            )
        ) {
            // Document URI (e.g., content://com.android.providers.media.documents/document/video:12345)
            val documentId = DocumentsContract.getDocumentId(videoUri)
            val id = documentId.split(":")[1]

            val selection = MediaStore.Video.Media._ID + "=?"
            val selectionArgs = arrayOf(id)

            val column = "_data"
            val projection = arrayOf(column)

            val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val cursor: Cursor? =
                context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(column)
                    path = it.getString(columnIndex)
                }
            }
        } else {
            // MediaStore URI
            val projection = arrayOf(MediaStore.Video.Media.DATA)
            val cursor: Cursor? = context.contentResolver.query(videoUri, projection, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    path = it.getString(columnIndex)
                }
            }
        }

        return path
    }
    fun checkVideoFileSize(videoFilePath: String): Long {
        val videoFile = File(videoFilePath)

        // Check if the file exists
        if (!videoFile.exists()) {
            // Handle the case when the file doesn't exist
            return -1
        }

        // Get the file size in bytes
        val fileSizeInBytes = videoFile.length()

        // Convert bytes to megabytes (1 MB = 1024 KB)
        val fileSizeInMB = fileSizeInBytes / (1024 * 1024)

        // Log or print the file size
        println("Video File Size: $fileSizeInMB MB")
        Log.e("TAG", "checkVideoFileSize: $fileSizeInMB")

        return fileSizeInMB
    }
     fun isValidMedia(uri: Uri, context: Context): Boolean {
        // Implement your validation logic here (e.g., check if it's an image or a video less than 30 seconds)
        val mimeType = context.contentResolver.getType(uri)
        Log.d("TAG", "MIME type: $mimeType")
        val isImage = isImage(uri, context)
        val isVideo = isVideo(uri, context)
        val duration = if (isVideo) CommonUtils.getMediaDuration(context, uri) else 0

        return (isImage || (isVideo && duration < 30))
    }
     fun isImage(uri: Uri, context: Context): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true
    }

     fun isVideo(uri: Uri, context: Context): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("video/") == true
    }
    fun getFileFromUri(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "tempFile")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                }
            }
        }
        return file
    }
     fun setLocale(languageCode: String, context:Context) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        // Example log to see locale change
        Log.d("Locale", "Locale changed to $languageCode")
    }
    fun getLocalisedString(id:Int, locale: Locale, configuration: Configuration, application:Application):String{
        configuration.setLocale(locale)
        val resources = application.createConfigurationContext(configuration).resources
        return resources.getString(id)
    }
}
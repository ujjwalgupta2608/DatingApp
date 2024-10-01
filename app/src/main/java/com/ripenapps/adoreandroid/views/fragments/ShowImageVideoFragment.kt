package com.ripenapps.adoreandroid.views.fragments

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentShowImageVideoBinding
import com.ripenapps.adoreandroid.models.response_models.carddetails.MediaList
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.views.adapters.AdapterShowMediaList
import com.ripenapps.adoreandroid.views.adapters.AdapterShowMediaViewPager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ShowImageVideoFragment : BaseFragment<FragmentShowImageVideoBinding>() {
    var mediaList = MediaList()
    lateinit var adapterViewPager: AdapterShowMediaViewPager
    lateinit var adapterShowMediaList: AdapterShowMediaList
    var previousSelection = ""
    var previousScreen=""
    private var singleMediaPosition = ""
    lateinit var mediaController : MediaController


    //    lateinit var mediaController : MediaController
    override fun setLayout(): Int {
        return R.layout.fragment_show_image_video
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
        viewPagerListener()
        mediaList = ShowImageVideoFragmentArgs.fromBundle(requireArguments()).mediaList!!
        previousSelection =
            ShowImageVideoFragmentArgs.fromBundle(requireArguments()).previousSelection!!
        singleMediaPosition =
            ShowImageVideoFragmentArgs.fromBundle(requireArguments()).singleMediaPosition!!
        previousScreen = ShowImageVideoFragmentArgs.fromBundle(requireArguments()).previousScreen!!
        if (previousScreen=="showChatMedia"||previousScreen=="specificChat"){
            binding.shareMediaButton.isVisible=true
        }
        if (previousSelection=="profileImage"){
            Glide.with(requireContext()).load(singleMediaPosition)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                .skipMemoryCache(true)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image).into(binding.imageView)
        }else if (previousScreen=="specificChat"/*||previousScreen=="showChatMedia"*/){  //previousSelection is "media" when media is clicked from side button popup and "image" or "video" when single message is clicked
            if (previousSelection=="image"){
                Glide.with(requireContext()).load(mediaList.mediaList?.get(0)?.url)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image).into(binding.imageView)
            }else{
                binding.SingleVideoView.isVisible = true
                binding.imageView.isVisible = false
                mediaController = MediaController(requireContext())
                mediaController.setAnchorView(binding.SingleVideoView)
                binding.SingleVideoView.setMediaController(mediaController)
                binding.SingleVideoView.setVideoURI(Uri.parse(mediaList.mediaList?.get(0)?.url))
                binding.SingleVideoView.requestFocus()
                binding.SingleVideoView.setOnPreparedListener { binding.SingleVideoView.start() }
            }
        }
        else{
            adapterViewPager = AdapterShowMediaViewPager(mediaList?.mediaList, previousScreen)
            binding.showMediaViewPager.adapter = adapterViewPager
            if (previousSelection == "viewAll") {
                adapterShowMediaList = AdapterShowMediaList(::getPosition, mediaList?.mediaList, 0, previousScreen)
                binding.mediaListRecycler.adapter =adapterShowMediaList
            } else if (previousSelection == "singleMedia") {
                adapterShowMediaList = AdapterShowMediaList(::getPosition, mediaList?.mediaList, singleMediaPosition.toInt(), previousScreen)
                binding.mediaListRecycler.adapter =adapterShowMediaList
                binding.showMediaViewPager.currentItem = singleMediaPosition.toInt()
            }
        }
    }

    private fun viewPagerListener() {
        binding.showMediaViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapterViewPager.notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                adapterShowMediaList.updatePosition(position)

            }
        })
    }
    private inner class ShareTask : AsyncTask<String, Void, Bitmap?>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            val imageUrl = urls[0]
//            ProcessDialog.showDialog(requireActivity(), true)
            return try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
//            ProcessDialog.dismissDialog(true)
            if (result != null) {
                shareMedia(result)
            }
        }
    }
    private fun shareMedia(bitmap: Bitmap) {
        val uri = getImageUri(bitmap)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*" // For images. For videos, use "video/*"
        if (uri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } else {
            // Handle the case where URI is null
        }
    }
    private fun getImageUri(inImage: Bitmap): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageName = "IMG_$timeStamp.jpg"
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageFile = File(imagesDir, imageName)
            val fos = FileOutputStream(imageFile)
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            Uri.parse(
                MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                imageFile.absolutePath,
                imageFile.name,
                null
            ))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.unable_to_share), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            null
        }
    }
    private fun downloadVideoAndShare(videoUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(videoUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                Toast.makeText(requireContext(), getString(R.string.failed_to_share_this_media_format), Toast.LENGTH_SHORT).show()
                ProcessDialog.dismissDialog(true)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_share_this_media_format), Toast.LENGTH_SHORT).show()
                    // Handle unsuccessful response
                    return
                }
                ProcessDialog.dismissDialog(true)
                val fileName = "video_${System.currentTimeMillis()}.mp4"
                val cacheDir = requireContext().cacheDir
                val cachedFile = File(cacheDir, fileName)

                val inputStream = response.body?.byteStream()
                val outputStream = FileOutputStream(cachedFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Share the downloaded file
                if (isAdded){
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "video/mp4"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", cachedFile))
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
                }
            }
        })
    }
    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.shareMediaButton.setOnClickListener {
            var url=""
            if (previousScreen=="specificChat"){
                url = mediaList.mediaList?.get(0)?.url!!
            }else if (previousScreen=="showChatMedia"){
                if (mediaList.mediaList?.get(binding.showMediaViewPager.currentItem)?.type=="image"){
                    url = mediaList.mediaList?.get(binding.showMediaViewPager.currentItem)?.image!!
                }else{
                    url=mediaList.mediaList?.get(binding.showMediaViewPager.currentItem)?.video!!
                }
            }
            if (CommonUtils.isVideoFile(url)){
                ProcessDialog.showDialog(requireActivity(), true)
                downloadVideoAndShare(url)
            }else{
                ShareTask().execute(url)
            }
        }
    }

    private fun getPosition(position: Int) {
        binding.mediaListRecycler.scrollToPosition(position)
        binding.showMediaViewPager.currentItem = position
    }

}
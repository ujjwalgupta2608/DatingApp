package com.ripenapps.adoreandroid.views.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentInnerStoryBinding
import com.ripenapps.adoreandroid.models.response_models.myStoryList.StoryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AdapterStory(
    private val context: Context,
    private val callbacks: VideoCallback,
    storyData: StoryData,
    private val lifecycleScope: LifecycleCoroutineScope,
) : RecyclerView.Adapter<AdapterStory.ViewHolder>() {

    var storyData = storyData
    var isVideoPlaying = false
    var storyJob: Job? = null
    var seekTo: Int? = null
    var pause: Boolean = false
    lateinit var binding: FragmentInnerStoryBinding
    private var startY = 0f


    inner class ViewHolder(val binding: FragmentInnerStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnTouchListener { _, event -> gestureDetector?.onTouchEvent(event)
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (isLongPressActive) {
                            handleRelease()
                        }
                    }
                    MotionEvent.ACTION_DOWN -> {

                        startY = event.y
                    }
                    MotionEvent.ACTION_MOVE->{
                        val deltaY = event.y - startY
                        if (deltaY > 0) {
                            callbacks.actionDownEvent()
                        } else if (deltaY < 0) {
                            callbacks.openViewersList()
                        }
                        startY = event.y
                    }

                    else -> {
                        false
                    }
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<FragmentInnerStoryBinding>(
            LayoutInflater.from(parent.context),
            R.layout.fragment_inner_story,
            parent,
            false
        )
        this.binding = binding
        return ViewHolder(binding)
    }

    private var isLongPressActive = false

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                handleLongPress()
            }

            override fun onSingleTapUp(p0: MotionEvent): Boolean {
                handleRelease()
                return true
            }
        })

    private fun handleLongPress() {
        isLongPressActive = true
        callbacks.hideViews()
        pauseStory()
    }

    private fun handleRelease() {
        isLongPressActive = false
        callbacks.showViews()
        resumeStory()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.tag = position
        holder.binding.image.visibility = View.GONE
        holder.binding.SingleVideoView.visibility = View.GONE
        holder.binding.pbar.visibility = View.GONE
        initUi(position)
        resumeStory()
    }

    override fun getItemCount(): Int {
        return storyData.story.size
    }

    fun clearPreviousJob() {
        if (storyJob != null) {
            storyJob?.cancel()
            storyJob = null
        }
        binding.SingleVideoView.stopPlayback()
    }

    fun initUi(position: Int) {
        clearPreviousJob()
        if (!storyData.story[position].type.isNullOrEmpty() && storyData.story[position].type == "video") {
            callbacks.initVideo()
            binding.image.visibility = View.GONE
            binding.SingleVideoView.visibility = View.VISIBLE
            binding.pbar.visibility = View.VISIBLE
            val video = storyData.story[position].media

            binding.SingleVideoView.setMediaController(null)
            binding.SingleVideoView.setVideoURI(Uri.parse(video))
            binding.SingleVideoView.requestFocus()

            binding.SingleVideoView.setOnPreparedListener {
                binding.SingleVideoView.start()
                binding.pbar.visibility = View.GONE
                isVideoPlaying = true
                callbacks.onStoryLoad(it.duration)

                storyJob = lifecycleScope.launch(Dispatchers.Main) {
                    while (isVideoPlaying) {
                        it?.let {
                            if (!pause) {
                                callbacks.loading(position, it.currentPosition)
                            }
                        }
                        delay(10)
                    }
                }
            }

            binding.SingleVideoView.setOnCompletionListener {
                callbacks.onStoryCompleted(it.duration)
                storyJob?.cancel()
                isVideoPlaying = false
                clearPreviousJob()
            }

            binding.SingleVideoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(
                    context, "An Error Occured " +
                            "While Playing Video !!!", Toast.LENGTH_LONG
                ).show()
                clearPreviousJob()
                false
            }
        } else {
            binding.SingleVideoView.visibility = View.GONE
            binding.image.visibility = View.VISIBLE
            binding.pbar.visibility = View.GONE

            Glide.with(context)
                .load(storyData.story[position].media)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: Target<Drawable>?,
                        p3: Boolean,
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        p2: Target<Drawable>?,
                        p3: DataSource?,
                        p4: Boolean,
                    ): Boolean {
                        var count = 0
                        val total = 400
                        callbacks.onStoryLoad(total)

                        storyJob = lifecycleScope.launch(Dispatchers.Main) {
                            while (count <= total) {
                                if (!pause) {
                                    if (count == total) {
                                        callbacks.onStoryCompleted(total)
                                        clearPreviousJob()
                                    } else {
                                        callbacks.loading(position, count)
                                    }
                                    count++
                                }
                                delay(10)
                            }
                        }
                        return false
                    }
                })
                .into(binding.image)
        }
    }

    fun resumeStory() {
        pause = false
        if (seekTo != null) {
            binding.SingleVideoView.start()
            binding.SingleVideoView.seekTo(seekTo!!)
        }
    }

    fun pauseStory() {
        pause = true
        if (binding.SingleVideoView.isPlaying) {
            binding.SingleVideoView.pause()
            seekTo = binding.SingleVideoView.currentPosition
        } else if (seekTo != null) {
            seekTo = null
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        clearPreviousJob()
    }
    fun updateStories(){
        this.storyData = StoryData(mutableListOf())
        notifyDataSetChanged()
    }

}


interface VideoCallback {
    fun initVideo()
    fun onStoryLoad(duration: Int)
    fun loading(fragPost: Int, progress: Int)
    fun onStoryCompleted(duration: Int)
    fun actionDownEvent()
    fun hideViews()
    fun showViews()
    fun openViewersList()
}
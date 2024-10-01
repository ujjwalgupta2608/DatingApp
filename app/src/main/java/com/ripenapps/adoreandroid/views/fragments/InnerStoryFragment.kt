package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.view.View
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentInnerStoryBinding
import com.ripenapps.adoreandroid.models.response_models.myStoryList.Story
import com.ripenapps.adoreandroid.preferences.DATA
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils.getObj
import com.ripenapps.adoreandroid.views.adapters.VideoCallback
import kotlinx.coroutines.Job


class InnerStoryFragment(
    val fragPos: Int,
    /*val story: Story,*/
    val listener: VideoCallback) : BaseFragment<FragmentInnerStoryBinding>() {
    private var isVideoPlaying = false
    private var storyJob: Job? = null
    private var seekTo: Int? = null
    private var pause: Boolean = false
    private lateinit var story:Story
    private var url =""
    override fun setLayout(): Int {
        return R.layout.fragment_inner_story
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        data = ViewStoriesData()

    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
    }

    override fun onResume() {
        super.onResume()
        story = getObj(arguments?.getString(DATA), Story::class.java)!!

        if (::story.isInitialized) {
//        initUi()
        resumeStory()//added for prev tap
//            if (!data.isRead && user.id != other.id) {
//                hitReadStoryAPI(data.id!!)
//            }
        }
    }
    private fun clearPreviousJob() {
        if (storyJob != null) {
            storyJob?.cancel()
            storyJob = null
        }
    }


    /*private fun initUi() {
        clearPreviousJob()
        if (!story.type.isNullOrEmpty()&&story.type=="video") {
            listener.initVideo()
            binding.SingleVideoView.visibility = View.VISIBLE
            binding.image.visibility = View.GONE
            binding.pbar.visibility = View.VISIBLE
            val video = story.media
            // set the media controller for video view
            binding.SingleVideoView.setMediaController(null)
            // set the absolute path of the video file which is going to be played
            binding.SingleVideoView.setVideoURI(Uri.parse(video))
            binding.SingleVideoView.requestFocus()
            binding.SingleVideoView.setOnPreparedListener {
                binding.SingleVideoView.start()
                binding.pbar.visibility = View.GONE
                isVideoPlaying = true
                listener.onStoryLoad(it.duration)
                storyJob = lifecycleScope.launch {
                    while (isVideoPlaying) {
                        if (!pause) {
                            listener.loading(fragPos, it.currentPosition)
                        }
                        delay(10)
                    }
                }
            }
            binding.SingleVideoView.setOnCompletionListener {
                listener.onStoryCompleted(it.duration)
                storyJob?.cancel()
                isVideoPlaying = false
                clearPreviousJob()
            }

            // display a toast message if any
            // error occurs while playing the video
            binding.SingleVideoView.setOnErrorListener { mp, what, extra ->
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

            Glide.with(requireActivity())
                .load(story.media)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.image)

//            Picasso.get().load(story.media).into(binding.image);

            var count = 0
            val total = 1000
            listener.onStoryLoad(total)

            storyJob = lifecycleScope.launch(Dispatchers.Main) {
                while (count <= total) {
                    if (!pause) {
                        if (count == total) {
                            listener.onStoryCompleted(total)
                            clearPreviousJob()
                        } else {
                            listener.loading(fragPos, count)
                        }
                        count++
                    }
                    delay(10)
                }
            }

        }

    }*/

    private fun onClick() {

    }

    override fun onDestroy() {
        super.onDestroy()
        clearPreviousJob()
        binding.SingleVideoView.stopPlayback()

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
        if (/*::data.isInitialized && !data.video.isNullOrBlank() && */binding.SingleVideoView.isPlaying) {
            binding.SingleVideoView.pause()
            seekTo = binding.SingleVideoView.currentPosition
        } else if (seekTo != null)
            seekTo = null
    }

    fun deleteStory() {}


}

package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentShowChatMediaBinding
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media
import com.ripenapps.adoreandroid.models.response_models.chatMediaList.ChatMediaResponse
import com.ripenapps.adoreandroid.models.response_models.chatMediaList.MediaList
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.views.adapters.AdapterChatMediaItems
import com.ripenapps.adoreandroid.views.adapters.AdapterMyProfileMedia
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ShowChatMediaFragment : BaseFragment<FragmentShowChatMediaBinding>(), AdapterMyProfileMedia.ProfileMediaSelector {
    private lateinit var adapterChatMediaItems:AdapterChatMediaItems
    var roomId=""
    var receiverId=""
    private lateinit var socket: Socket
    var mediaList=MediaList()
    var chatMediaResponse=ChatMediaResponse()


    override fun setLayout(): Int {
        return R.layout.fragment_show_chat_media
    }

    override fun initView(savedInstanceState: Bundle?) {
        roomId = ShowChatMediaFragmentArgs.fromBundle(requireArguments()).roomId
        receiverId = ShowChatMediaFragmentArgs.fromBundle(requireArguments()).receiverId
        onClick()
        val app = requireActivity().application as MyApplication
        socket = app.getSocket()
        emitMediaList()
        onClearMedia()
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        binding.deleteButton.isVisible=false
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.off("mediaMessages")
        socket.off("clearMedia")

    }
    private fun emitMediaList() {
        val data = JSONObject()
        data.put("receiver",receiverId)
        data.put("roomId",roomId)
        data.put("loginId",Preferences.getStringPreference(requireContext(), USER_ID))
        socket.emit("mediaList", data)
        onMediaMessages()
    }
    private fun onMediaMessages(){
        socket.on("mediaMessages", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    Log.i("TAG mediaMessages data", data1.toString())

                    try {
                        Log.i("TAG mediaMessages data", data1.toString())

                        chatMediaResponse = Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            ChatMediaResponse::class.java)
                        if (chatMediaResponse.status==200){
                            adapterChatMediaItems= AdapterChatMediaItems(chatMediaResponse.result, this, requireContext())
                            binding.mediaitemsRecycler.adapter = adapterChatMediaItems
                            binding.noMediaLayout.isVisible = chatMediaResponse.result?.size==0
                            binding.deleteButton.isVisible=false
                        }
                    } catch (ex: JSONException) {

                    }
                }
            }
            socket.off("mediaMessages")
        })
    }
    private fun onClearMedia() {
        socket.on("clearMedia", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    Log.i("TAG clearMedia data", data1.toString())

                    try {
                        Log.i("TAG clearMedia data", data1.toString())
                        var commonResponse = Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            CommonResponse::class.java)
                        if (commonResponse.status==200){
                            emitMediaList()
                        }

                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onClick() {
        binding.backButton.setOnClickListener {
            binding.deleteButton.isVisible=false
            if (adapterChatMediaItems.isLongPressActive) {
                adapterChatMediaItems.isLongPressActive = false
                adapterChatMediaItems.inactiveLongPress()
            } else {
                findNavController().popBackStack()
            }
        }
        binding.mainLayout.setOnClickListener {
            binding.deleteButton.isVisible=false
            if (adapterChatMediaItems.isLongPressActive) {
                adapterChatMediaItems.isLongPressActive = false
                adapterChatMediaItems.inactiveLongPress()
            }
        }
        binding.deleteButton.setOnClickListener {
            if (adapterChatMediaItems.getMediaListToDelete()?.size!! >0){
                Log.i("TAG", "listSize: "+adapterChatMediaItems.getMediaListToDelete().size)
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            emitMediaDelete()
                            dialog.dismiss()
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    getString(R.string.delete_media),
                    getString(R.string.are_you_sure_you_want_to_delete_selected_media),
                    getString(R.string.yes),
                    getString(R.string.no),
                    2
                )
            } else{
                Snackbar.make(binding.topLayout, getString(R.string.please_select_a_media_to_delete), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun emitMediaDelete() {
        val data = JSONObject()
        data.put("receiver", receiverId)
        data.put("loginId",Preferences.getStringPreference(requireContext(), USER_ID))
        data.put("roomId",roomId)
        data.put("mediaIds", JSONArray(adapterChatMediaItems.getMediaListToDelete())) //pass the whole list when socket will change
        Log.i("TAG", "emitMediaDelete: $data")
        socket.emit("mediaDelete", data)
    }

    override fun showMedia(position: Int) {
        var tempList:MutableList<Media> = mutableListOf()
        chatMediaResponse.result?.forEachIndexed { index, item ->
            tempList.add(chatMediaResponse.result?.get(index)?.media?.get(0)!!)
        }
        findNavController().navigate(ShowChatMediaFragmentDirections.showChatMediaToShowImageVideo(com.ripenapps.adoreandroid.models.response_models.carddetails.MediaList(tempList), "singleMedia", position.toString(), "showChatMedia"))
       /* if (chatMediaResponse.result?.get(position)?.media?.get(0)?.type=="image"){
            findNavController().navigate(ShowChatMediaFragmentDirections.showChatMediaToShowImageVideo(
                com.example.shindindidatingapp.models.response_models.carddetails.MediaList(
                    mutableListOf(Media(url = chatMediaResponse.result?.get(position)?.media?.get(0)?.image))
                ), "image", "", "showChatMedia"))
        }else{
            findNavController().navigate(ShowChatMediaFragmentDirections.showChatMediaToShowImageVideo(
                com.example.shindindidatingapp.models.response_models.carddetails.MediaList(
                    mutableListOf(Media(url = chatMediaResponse.result?.get(position)?.media?.get(0)?.video))
                ), "video", "", "showChatMedia"))
        }*/

    }

    override fun getSelectedMediaPosition(position: Int) {
        if (adapterChatMediaItems.getMediaListToDelete()?.size!! >0){
           binding.deleteButton.isVisible=true
        } else{
            binding.deleteButton.isVisible=false
        }
    }

    override fun setLongPress(status: Boolean) {}
}
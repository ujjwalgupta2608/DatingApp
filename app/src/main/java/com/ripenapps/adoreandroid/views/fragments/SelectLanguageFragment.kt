package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSelectLanguageBinding
import com.ripenapps.adoreandroid.models.static_models.SelectLanguageModel
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.views.activities.HomeActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterSelectLanguage

class SelectLanguageFragment : BaseFragment<FragmentSelectLanguageBinding>() {
    private var previousLanguage=""
    private lateinit var list: MutableList<SelectLanguageModel>
    private lateinit var adapter:AdapterSelectLanguage
    private var selectedLanguageCode=""
    private var previousScreen = ""
    override fun setLayout(): Int {
        return R.layout.fragment_select_language
    }

    override fun initView(savedInstanceState: Bundle?) {
        previousLanguage=Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE).toString()
        previousScreen = SelectLanguageFragmentArgs.fromBundle(requireArguments()).previousScreen
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLanguageList()
        intiViews()
        onClick()
    }

    private fun intiViews() {
        binding.selectButton.text = getString(R.string.select_language)
        if (previousScreen=="splash"){
            binding.selectButton.text = getString(R.string.select)
        }
        binding.backButton.isVisible = previousScreen=="settings"||previousScreen=="login"||previousScreen=="signup"
    }

    private fun initLanguageList() {
        if (Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE).isNullOrEmpty()){
            selectedLanguageCode="en"
            Preferences.setStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE, "en")
        }else{
            selectedLanguageCode= Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE).toString()
        }
        addItems()
        var selectedPos=-1
        list.forEachIndexed { position, item ->
            if(item.code==Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)){
                selectedPos=position
            }
        }
        Log.i("TAG", "initLanguageList: "+Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE))
        list[selectedPos].isSelected=true
        adapter = AdapterSelectLanguage(list, ::getSelectedLanguage, selectedPos)
        binding.adaptorSelectLanguage.adapter = adapter
    }

    private fun addItems() {
        list = mutableListOf(
            SelectLanguageModel("zu", "${getString(R.string.language_zulu)} (IsiZulu)"),
            SelectLanguageModel("en", "${getString(R.string.language_english)} (English)"),
            SelectLanguageModel("af", "${getString(R.string.language_afrikaans)} (Afrikaans)"),
            SelectLanguageModel("sw", "${getString(R.string.language_swahili)} (Kiswahili)"),
            SelectLanguageModel("pt", "${getString(R.string.language_portuguese)} (Português)"),
            SelectLanguageModel("ar", "${getString(R.string.language_arabic)} (العربية)"),
            SelectLanguageModel("fr", "${getString(R.string.language_french)} (Français)"),
            SelectLanguageModel("zh", "${getString(R.string.language_mandarin)} (中文)"),
            SelectLanguageModel("hi", "${getString(R.string.language_hindi)} (हिन्दी)"),
            SelectLanguageModel("ru", "${getString(R.string.language_russian)} (Русский)"),
            SelectLanguageModel("es", "${getString(R.string.language_spanish)} (Español)"),
            SelectLanguageModel("bn", "${getString(R.string.language_bengali)} (বাংলা)"),
            SelectLanguageModel("ja", "${getString(R.string.language_japanese)} (日本語)"),
            SelectLanguageModel("id", "${getString(R.string.language_indonesian)} (Bahasa Indonesia)"),
            SelectLanguageModel("de", "${getString(R.string.language_german)} (Deutsch)"),
            SelectLanguageModel("vi", "${getString(R.string.language_vietnamese)} (Tiếng Việt)"),
            SelectLanguageModel("te", "${getString(R.string.language_telugu)} (తెలుగు)"),
            SelectLanguageModel("tr", "${getString(R.string.language_turkish)} (Türkçe)"),
            SelectLanguageModel("mr", "${getString(R.string.language_marathi)} (मराठी)"),
            SelectLanguageModel("nso", "${getString(R.string.language_sesotho)} (Sesotho)"),
            SelectLanguageModel("pcm", "${getString(R.string.language_nigerian_pidgin)} (Naijá)"),
            SelectLanguageModel("ta", "${getString(R.string.language_tamil)} (தமிழ்)"),
            SelectLanguageModel("ro", "${getString(R.string.language_romanian)} (Română)"),
            SelectLanguageModel("pl", "${getString(R.string.language_polish)} (Polski)"),
            SelectLanguageModel("uk", "${getString(R.string.language_ukrainian)} (Українська)"),
            SelectLanguageModel("it", "${getString(R.string.language_italian)} (Italiano)"),
            SelectLanguageModel("ko", "${getString(R.string.language_korean)} (한국어)"),
            SelectLanguageModel("th", "${getString(R.string.language_thai)} (ไทย)"),
            SelectLanguageModel("fil", "${getString(R.string.language_filipino)} (Filipino)"),
            SelectLanguageModel("nl", "${getString(R.string.language_dutch)} (Nederlands)")
        )
    }

    private fun getSelectedLanguage(position:Int){
        Log.i("TAG", "getSelectedLanguage: $position")
        selectedLanguageCode=list[position].code.toString()
        CommonUtils.setLocale(selectedLanguageCode, requireContext())
        list.clear()
        addItems()
        var selectedPos=-1
        list.forEachIndexed { position, item ->
            if(item.code==selectedLanguageCode){
                selectedPos=position
            }
        }
        Log.i("TAG", "initLanguageList: "+Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE))
        list[selectedPos].isSelected=true
        adapter.updateList(list)
        binding.selectButton.text = getString(R.string.select_language)
        binding.title.text = getString(R.string.select_language)

    }
    private fun onClick() {
        binding.selectButton.setOnClickListener {
            Preferences.setStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE, selectedLanguageCode)
            UserPreference.savedLanguageCode= selectedLanguageCode
            CommonUtils.setLocale(selectedLanguageCode, requireContext())
            if (previousScreen=="login"||previousScreen=="signup"){
                val result = Bundle().apply {
                    if (previousLanguage==Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE)){
                        putString("resultKey", "0")
                    }else{
                        putString("resultKey", "1")
                    }
                } // Set the result
                parentFragmentManager.setFragmentResult("requestKey", result)
            }
            when (previousScreen) {
                "splash" -> {
                    findNavController().navigate(SelectLanguageFragmentDirections.selectLanguageToWelcome())
                }
                "login" -> {
                    findNavController().popBackStack()
                }
                "signup"->{
                    findNavController().popBackStack()
                }
                else -> {
                    if (Places.isInitialized()){
                        Places.deinitialize()
                    }
                    val intent = Intent(requireActivity(), HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    requireActivity().finish()

                }
            }
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}
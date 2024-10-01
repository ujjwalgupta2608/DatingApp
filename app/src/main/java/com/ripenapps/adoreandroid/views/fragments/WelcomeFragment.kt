package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.viewModels
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentWelcomeBinding
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.view_models.WelcomeViewModel
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {
    private val viewModel by viewModels<WelcomeViewModel>()
    override fun setLayout(): Int {
        return R.layout.fragment_welcome
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        setTitleColour()
    }
    private fun setTitleColour() {
        var title = "<font color=#6D53F4>${getString(R.string.discover_love)}</font> <font color=#242424>${getString(R.string.where_your_story_begins)}</font>"
        binding.title.text = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY)
    }


}
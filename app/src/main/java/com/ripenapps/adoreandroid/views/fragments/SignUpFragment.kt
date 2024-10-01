package com.ripenapps.adoreandroid.views.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSignUpBinding
import com.ripenapps.adoreandroid.models.request_models.SocialLoginRequest
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_LOGIN
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.SignUpViewModel
import com.ripenapps.adoreandroid.views.activities.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.ripenapps.adoreandroid.preferences.EMAIL
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.preferences.SOCIAL_TYPE
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>() {
    private val viewModel by viewModels<SignUpViewModel>()
    private var isPassHidden = true
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth

    override fun setLayout(): Int {
        return R.layout.fragment_sign_up

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.nameInLogin.imeHintLocales = LocaleList(Locale(Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)))
        UserPreference.password = ""
        onBackListener()
        setObserver()
    }
    private fun onBackListener() {
        parentFragmentManager.setFragmentResultListener("requestKey", this) { requestKey, bundle ->
            val result = bundle.getString("resultKey")
            if (result.equals("1")){
                viewModel.locale = Locale(Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE))
                setUpUI()
            }
        }
    }

    private fun setUpUI() {
        binding.apply {
            loginText.text = getString(R.string.create_account)
            textView4.text = getString(R.string.fill_your_information_below_or_register_with_you_social_account)
            textView5.text = getString(R.string.email)
            emailInLogin.hint = getString(R.string.enter_email)
            textView6.text = getString(R.string.password)
            passwordLogin.hint = getString(R.string.enter_password)
            nameText.text = getString(R.string.name)
            nameInLogin.hint = getString(R.string.enter_name)
            signupInSignup.text = getString(R.string.sign_up)
            orSignUpWith.text = getString(R.string.or_sign_up_with)
            alreadyHaveAnAccount.text = getString(R.string.already_have_an_account)
            loginFromSignup.text = getString(R.string.login)
            setTermsConditions()
        }
    }

    private fun setObserver() {
        viewModel.getSendOtpLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        "200" -> {
                            Preferences.setStringPreference(requireContext(), SOCIAL_TYPE, "")  //when no social login
//                            UserPreference.socialType=""
                            UserPreference.password = viewModel.password.get()!!
                            it.data?.message?.let { it1 ->
                                Toast.makeText(requireActivity(), it1, Toast.LENGTH_SHORT).show()
                            }
                            findNavController().navigate(
                                SignUpFragmentDirections.signUpToVerifyCodeSignup(
                                    "email"
                                )
                            )
                        }

                        else -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(requireActivity(), it1, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
        viewModel.getSocialLoginLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            ProcessDialog.dismissDialog(true)
                            Log.i(TAG, "dataSocailLogin: "+it.data)
                            Log.i(TAG, "dataSocailLogin: "+Gson().toJson(it.data))
                            if (!it.data.data?.user?.userName.isNullOrEmpty()){   // login for existing user
                                Preferences.setStringPreference(requireActivity(), IS_LOGIN, "true")
                                Preferences.setStringPreference(requireActivity(), TOKEN, it.data.data?.token)
                                Preferences.setStringPreference(requireActivity(), NAME, it.data.data?.user?.name)
                                Preferences.setStringPreference(requireActivity(), USER_NAME, it.data.data?.user?.userName)
                                Preferences.setStringPreference(requireActivity(), USER_ID, it.data.data?.user?._id)
                                Preferences.setStringPreference(requireActivity(), EMAIL, it.data.data?.user?.email)
                                UserPreference.isNewUserRegistered=true
                                val intent = Intent(activity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                activity?.finish()
                            }else{  //to register for a new user
                                findNavController().navigate(SignUpFragmentDirections.signUpToSignupVerifyMobile())
                            }
                        }

                        else -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(requireActivity(), it1, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog(true)
                }
            }
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        setPasswordToggle()
        setTermsConditions()
        onClick()
        initGoogleSignInOptions()
    }

    private fun initGoogleSignInOptions() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
//        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun onClick() {
        binding.google.setOnClickListener {
            mGoogleSignInClient.signOut()
            signInGoogle()
        }
        binding.changeLanguage.setOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.signUpToSelectLanguage("signup"))
        }
    }
    private fun setPasswordToggle() {
        binding.passwordLogin.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        binding.passwordEye.setOnClickListener {
            isPassHidden = if (isPassHidden) {
                binding.passwordEye.setImageResource(R.drawable.open_eye)
                binding.passwordLogin.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                binding.passwordEye.setImageResource(R.drawable.close_eye)
                binding.passwordLogin.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            binding.passwordLogin.setSelection(binding.passwordLogin.text.toString().length)
        }
    }
    fun containsSubstring(string: String, substring: String): Boolean {
        return string.contains(substring)
    }
    private fun setTermsConditions() {
        val ss = SpannableString(getString(R.string.agree_with_terms_condition))
        val value = getString(R.string.agree_with_terms_condition)
        val firstString = getString(R.string.terms_condition)
        var firstIndex=0
        var firstLastCharIndex=ss.length
        if (containsSubstring(value, firstString)){
            firstIndex = value.indexOf(firstString)
            firstLastCharIndex = firstIndex + firstString.length
        }
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(SignUpFragmentDirections.signUpToWebview("termsConditions"))
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.theme)
                ds.isUnderlineText = true
                ds.underlineColor = resources.getColor(R.color.theme)
//                ds.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
            }
        }
        ss.setSpan(clickableSpan1, firstIndex, firstLastCharIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.termsAndConditions.text = ss
        binding.termsAndConditions.movementMethod = LinkMovementMethod.getInstance()
    }
    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
            Log.d("TAG", "handleResult: ${Gson().toJson(completedTask)}")
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                Log.i("TAG", "accountNotNull: ${Gson().toJson(account)}")
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Log.i("TAG", "handleResult: "+e.statusCode)
            Log.i("TAG", "handleResult: "+Gson().toJson(e))

        }
    }
    private fun UpdateUI(account: GoogleSignInAccount) {
        if (account.email!=null){
//            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
                    var request=SocialLoginRequest()
                    request.email = account.email
                    request.name = account.displayName
                    Log.i(TAG, "UpdateUI: "+account.givenName)
                    Log.i(TAG, "UpdateUI: "+account.familyName)
                    Log.i(TAG, "UpdateUI: "+account.displayName)

                    request.socialType = "google"
                    request.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN)
                    request.deviceType = "android"
                    request.socialId = account.id
                    UserPreference.fullName = account.displayName.toString()
                    UserPreference.socialId = account.id.toString()
//                    UserPreference.socialType = "google"
                    Preferences.setStringPreference(requireContext(), SOCIAL_TYPE, "google")
                    UserPreference.emailAddress = account.email.toString()
                    viewModel.hitSocialLoginLiveData(request)
//                }
//            }
        }
    }
}
package com.ripenapps.adoreandroid.views.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentLoginBinding
import com.ripenapps.adoreandroid.models.request_models.LoginRequest
import com.ripenapps.adoreandroid.models.request_models.SocialLoginRequest
import com.ripenapps.adoreandroid.preferences.EMAIL
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_LOGIN
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.preferences.SOCIAL_TYPE
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.LoginViewModel
import com.ripenapps.adoreandroid.view_models.SignUpViewModel
import com.ripenapps.adoreandroid.views.activities.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private var isPassHidden = true
    private val viewModel by viewModels<LoginViewModel>()
    private val signupViewModel by viewModels<SignUpViewModel>()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    override fun setLayout(): Int {
        return R.layout.fragment_login
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        onBackListener()
        Preferences.setStringPreference(requireActivity(), IS_WELCOME_DONE, "true")
        setObserver()
    }

    private fun setObserver() {
        viewModel.getLoginLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Preferences.setStringPreference(requireContext(), SOCIAL_TYPE, "")
//                                UserPreference.socialType=""    //when no social login
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

                            }

                            else -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
        signupViewModel.getSocialLoginLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            ProcessDialog.dismissDialog(true)
                            Log.i(ContentValues.TAG, "dataSocailLogin: "+it.data)
                            Log.i(ContentValues.TAG, "dataSocailLogin: "+Gson().toJson(it.data))
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
                                findNavController().navigate(LoginFragmentDirections.loginToSignupVerifyMobile())
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
            loginText.text = getString(R.string.login)
            textView4.text = getString(R.string.hi_welcome_back_you_ve_been_missed)
            textView5.text = getString(R.string.email_or_userid)
            emailInLogin.hint = getString(R.string.enter_email)
            textView6.text = getString(R.string.password)
            passwordLogin.hint = getString(R.string.enter_password)
            forgotPasswordInLogin.text = getString(R.string.forgot_password)
            loginFromLogin.text = getString(R.string.login)
            loginWithMobileinLogin.text = getString(R.string.login_with_mobile)
            orLoginWith.text = getString(R.string.or_login_with)
            didntHaveAccount.text = getString(R.string.don_t_have_an_account)
            signupFromWelcome.text = getString(R.string.sign_up)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
        setPasswordToggle()
        initFacebookSdk()
        initGoogleSignInOptions()

    }
    private fun initFacebookSdk() {
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext())
        // Create CallbackManager
        callbackManager = CallbackManager.Factory.create()
    }
    private fun initGoogleSignInOptions() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            /*.requestIdToken(getString(R.string.web_client_id))*/
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        firebaseAuth = FirebaseAuth.getInstance()
    }
    private fun loginWithFacebook() {
        // Request necessary permissions
        LoginManager.getInstance()
            .logInWithReadPermissions(this, mutableListOf("email", "public_profile"))

        // Handle login result
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // Handle successful login
                    val accessToken = loginResult.accessToken
                    // Proceed with your app logic
                    val request = GraphRequest.newMeRequest(accessToken) { data, response ->
                            try {
                                // Retrieve user information
                                val userId = data?.getString("id")
                                val userName = data?.getString("name")
                                val userEmail = data?.getString("email")
                                // You can retrieve more information as needed

                                // Proceed with your app logic using the retrieved user information
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                }

                override fun onCancel() {
                    // Handle canceled login
                }

                override fun onError(error: FacebookException) {
                    // Handle error in login
                }
            })
    }
    private fun onClick() {
        binding.google.setOnClickListener {
            mGoogleSignInClient.signOut()
            signInGoogle()
        }
        binding.facebook.setOnClickListener {
            loginWithFacebook()
        }
        binding.loginFromLogin.setOnClickListener {
            val validationError = viewModel.validateInput()

            if (validationError != null) {
                Snackbar.make(binding.loginFromLogin, validationError, Snackbar.LENGTH_SHORT).show()
            } else {
                var loginRequest = LoginRequest()
                if (viewModel.isValidEmail(viewModel.email.get()!!.trim())){
                    UserPreference.emailAddress = viewModel.email.get()!!.trim()
                    loginRequest.email = viewModel.email.get()!!.trim()
                } else{
                    loginRequest.userName = "@"+viewModel.email.get()!!.trim()
                }
                loginRequest.password = viewModel.password.get()!!.trim()
                loginRequest.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN).toString()
                loginRequest.deviceType = "android"
                viewModel.hitUserLoginApi(loginRequest)
            }
        }
        binding.changeLanguage.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.loginToSelectLanguage("login"))
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
    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        } else{
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        Log.d("TAG", "handleResult: ${Gson().toJson(completedTask)}")
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                Log.i("TAG", "accountNotNull: ${Gson().toJson(account)}")
                UpdateUI(account)
            } else{
                Toast.makeText(
                    requireActivity(),
                    "Auth Error!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: ApiException) {
            Log.i("TAG", "handleResult: "+ Gson().toJson(e))

        }
    }
    private fun UpdateUI(account: GoogleSignInAccount) {
        Log.i("TAG", "UpdateUI123: "+account.email)
        if (account.email!=null){
//            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
                    var request= SocialLoginRequest()
                    request.email = account.email
                    request.name = account.displayName
                    request.socialType = "google"
                    request.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN)
                    request.deviceType = "android"
                    request.socialId = account.id
                    UserPreference.fullName = account.displayName.toString()
                    UserPreference.socialId = account.id.toString()
                    Preferences.setStringPreference(requireContext(), SOCIAL_TYPE, "google")
                    UserPreference.emailAddress = account.email.toString()
                    signupViewModel.hitSocialLoginLiveData(request)
//                }
//            }
        }
    }

}
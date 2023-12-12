package com.steven.androidchatroom.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.steven.androidchatroom.MainApplication
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.databinding.ActivityLoginBinding
import com.steven.androidchatroom.dialog.LoadingDialog
import com.steven.androidchatroom.util.dataStore
import com.steven.androidchatroom.viewModel.LoginViewModel
import com.steven.androidchatroom.web.ApiInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding
    private val mViewModel: LoginViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = mViewModel
        setContentView(mBinding.root)

        loadingDialog = LoadingDialog(this)

        mViewModel.checkIsRememberAccount(dataStore)
        initListener()
        initObserver()
        askNotificationPermission()
    }

    private fun initObserver(){
        mViewModel.loginResponse?.observe(this){
            it?.let { data ->
                updateFcmToken()
                mViewModel.setRememberAccount(dataStore)
                MainApplication.mMemberId = data.memberId.toString()
                val intent = Intent()
                intent.putExtra("userName", data.userName)
                intent.putExtra("memberId", data.memberId)
                intent.setClass(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
        mViewModel.errorResponse?.observe(this){
            it?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        mViewModel.dialogLoading?.observe(this){
            if(it){
                loadingDialog.show()
            }else{
                loadingDialog.dismiss()
            }
        }
    }

    private fun initListener(){

        mBinding.btRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        mBinding.btLogin.setOnClickListener{
            if(mViewModel.account.value?.isBlank() == true || mViewModel.password.value?.isBlank() == true){
                Toast.makeText(this@LoginActivity, "請輸入帳號或密碼", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mViewModel.login()
        }

    }

    private fun updateFcmToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            mViewModel.updateFcmToken(token)
        })
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
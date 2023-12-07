package com.steven.androidchatroom.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
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
    }

    private fun initObserver(){
        mViewModel.loginResponse?.observe(this){
            it?.let { data ->
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
}
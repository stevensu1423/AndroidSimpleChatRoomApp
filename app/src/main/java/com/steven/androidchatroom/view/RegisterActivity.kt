package com.steven.androidchatroom.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.databinding.ActivityRegisterBinding
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.viewModel.RegisterViewModel
import com.steven.androidchatroom.web.ApiInterface
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Response

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityRegisterBinding
    private val mViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = mViewModel
        setContentView(mBinding.root)

        initListener()
        initObserver()
    }

    private fun initObserver(){
        mViewModel.registerResponse.observe(this){
            val intent = Intent()
            intent.putExtra("userName", it?.userName)
            intent.putExtra("memberId", it?.memberId)
            intent.setClass(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initListener(){

        mBinding.btRegister.setOnClickListener{
            val name = mBinding.etName.text.toString()
            val email = mBinding.etEmail.text.toString()
            val password = mBinding.etPassword.text.toString()
            if(email.isBlank()){
                Toast.makeText(
                    this@RegisterActivity,
                    "請輸入帳號",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if(password.isBlank()){
                Toast.makeText(
                    this@RegisterActivity,
                    "請輸入密碼",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if(name.isBlank()){
                Toast.makeText(
                    this@RegisterActivity,
                    "請輸入稱呼",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            mViewModel.register(email, password, name)
        }
    }
}
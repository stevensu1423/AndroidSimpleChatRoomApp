package com.steven.androidchatroom.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.databinding.ActivityLoginBinding
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.viewModel.LoginViewModel
import com.steven.androidchatroom.web.ApiInterface
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding
    private val mViewModel: LoginViewModel by viewModels()
    private val mApiClient = ApiClient()
    private lateinit var api: ApiInterface


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        mBinding.lifecycleOwner = this
        mBinding.vm = mViewModel
        setContentView(mBinding.root)
        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

        initListener()
        initObserver()
    }

    private fun initObserver(){
        mViewModel.loginiRespose?.observe(this){
            it?.let { data ->
                val intent = Intent()
                intent.putExtra("userName", data.userName)
                intent.putExtra("memberId", data.memberId)
                intent.setClass(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initListener(){

        mBinding.btRegister.setOnClickListener{
            intent.setClass(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        mBinding.btLogin.setOnClickListener{
            if(mViewModel.account.value?.isBlank() == true || mViewModel.password.value?.isBlank() == true){
                Toast.makeText(this@LoginActivity, "請輸入帳號或密碼", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mViewModel.login()
//            api.login(email, password).enqueue(object : Callback<ApiResponse.LoginResponse> {
//                override fun onResponse(
//                    call: Call<ApiResponse.LoginResponse>,
//                    response: Response<ApiResponse.LoginResponse>
//                ) {
//                    if (response.body()?.status == 200) {
//                        val data = response.body()
//                        val intent = Intent()
//                        intent.putExtra("userName", data?.userName)
//                        intent.putExtra("memberId", data?.memberId)
//                        intent.setClass(this@LoginActivity, MainActivity::class.java)
//                        startActivity(intent)
//                    } else {
//                        Toast.makeText(
//                            this@LoginActivity,
//                            response.body()?.message.toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onFailure(
//                    call: Call<ApiResponse.LoginResponse>,
//                    t: Throwable
//                ) {
//                    t.printStackTrace()
//                }
//            })
        }

    }
}
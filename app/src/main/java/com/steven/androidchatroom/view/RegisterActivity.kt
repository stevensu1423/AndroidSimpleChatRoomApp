package com.steven.androidchatroom.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.databinding.ActivityRegisterBinding
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.web.ApiInterface
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Response

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityRegisterBinding
    private val mApiClient = ApiClient()
    private lateinit var api: ApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

        initListener()
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
            api.register(email, password, name).enqueue(object : retrofit2.Callback<ApiResponse.RegisterResponse>{
                override fun onResponse(
                    call: Call<ApiResponse.RegisterResponse>,
                    response: Response<ApiResponse.RegisterResponse>
                ) {
                    if(response.isSuccessful){
                        if(response.body()?.status == 200){
                            val data = response.body()
                            val intent = Intent()
                            intent.putExtra("userName", data?.userName)
                            intent.putExtra("memberId", data?.memberId)
                            intent.setClass(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(
                                this@RegisterActivity,
                                response.body()?.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse.RegisterResponse>, t: Throwable) {
                }
            })


        }
    }
}
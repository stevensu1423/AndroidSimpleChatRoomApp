package com.steven.androidchatroom.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.R
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.databinding.ActivityMainBinding
import com.steven.androidchatroom.model.adapter.FriendAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.util.toast
import com.steven.androidchatroom.viewModel.MainViewModel
import com.steven.androidchatroom.web.ApiInterface
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val mViewModel: MainViewModel by viewModels()
    private lateinit var mNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        intent?.getStringExtra("memberId")?.let {
            mViewModel.memberId.value = it
        }
        intent?.getStringExtra("userName")?.let {
            mViewModel.userName.value = it
        }
        initObserver()
    }

    private fun initObserver(){
        mViewModel.friendChatUnreadCount.observe(this){
            val badge = mBinding.bnvHome.getOrCreateBadge(R.id.navigation_home)
            badge.number = it
        }
        mViewModel.friendRequestCount.observe(this){
            val badge = mBinding.bnvHome.getOrCreateBadge(R.id.navigation_friend)
            badge.number = it
        }
        mViewModel.errorResponse?.observe(this){
            toast(it.message)
        }
    }
    override fun onStart() {
        super.onStart()
        mNavController = mBinding.navHome.findNavController()
        mBinding.bnvHome.setupWithNavController(mNavController)
        mBinding.bnvHome.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home -> {
                    mBinding.tvTitle.text = "聊天"
                }
                R.id.navigation_friend -> {
                    mBinding.tvTitle.text = "好友邀請"
                }
            }
            mNavController.navigate(it.itemId)
            true
        }
    }


}
package com.steven.androidchatroom.util

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast

fun Activity.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Dialog.setLayoutSize(mActivity: Activity, heightFloat: Float?, widthFloat: Float?){
    val displayMetrics = DisplayMetrics()
    mActivity.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    val displayWidth: Int = displayMetrics.widthPixels
    val displayHeight: Int = displayMetrics.heightPixels
    val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
    layoutParams.copyFrom(this.window!!.attributes)
    heightFloat?.let {
        layoutParams.height = (displayHeight * heightFloat).toInt()
    }
    widthFloat?.let {
        layoutParams.width = (displayWidth * widthFloat).toInt()
    }
    this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    this.window!!.attributes = layoutParams
}
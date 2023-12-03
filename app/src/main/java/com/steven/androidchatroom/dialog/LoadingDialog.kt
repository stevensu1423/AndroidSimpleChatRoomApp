package com.steven.androidchatroom.dialog

import android.app.Activity
import android.app.Dialog
import com.steven.androidchatroom.R
import com.steven.androidchatroom.databinding.DialogLoadingBinding
import com.steven.androidchatroom.util.setLayoutSize

class LoadingDialog(var mActivity: Activity) {
    val view: DialogLoadingBinding = DialogLoadingBinding.inflate(mActivity.layoutInflater)
    val dialog: Dialog = Dialog(mActivity)

    init {
        dialog.setContentView(view.root)
        dialog.setCancelable(false)
        dialog.setLayoutSize(mActivity, 0.225f, 0.475f)
    }

    fun show(){
        if(!mActivity.isFinishing && !dialog.isShowing)
            dialog.show()
    }

    fun dismiss(){
        if(!mActivity.isFinishing && dialog.isShowing){
           dialog.dismiss()
        }
    }
}
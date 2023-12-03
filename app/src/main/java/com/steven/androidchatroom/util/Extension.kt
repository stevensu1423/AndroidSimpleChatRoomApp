package com.steven.androidchatroom.util

import android.app.Activity
import android.widget.Toast

fun Activity.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
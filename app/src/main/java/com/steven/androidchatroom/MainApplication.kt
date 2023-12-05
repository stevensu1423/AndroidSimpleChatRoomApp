package com.steven.androidchatroom

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    companion object{
        var mMemberId: String = ""
    }
}
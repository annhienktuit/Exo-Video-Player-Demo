package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast

object Extensions {
    fun Activity.toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    fun Activity.isLandscapeOrientation() : Boolean {
        val orientation = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}
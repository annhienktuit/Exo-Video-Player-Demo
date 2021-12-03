package com.annhienktuit.exoplayervideoplayerzalo.animations

import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator

class RotateAnimation {
    private var view:View
    constructor(view: View){
        this.view = view
    }

    fun startAnimation(){
        val rotate = android.view.animation.RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 20000
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = Animation.INFINITE
        rotate.isFillEnabled = true
        rotate.fillAfter = true
        view.startAnimation(rotate)
    }

    fun stopAnimation(){
        view.clearAnimation()
    }

}
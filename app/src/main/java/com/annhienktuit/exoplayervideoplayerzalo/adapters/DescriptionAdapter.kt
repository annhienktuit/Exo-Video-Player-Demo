package com.annhienktuit.exoplayervideoplayerzalo.adapters

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter

class DescriptionAdapter(private val controller: MediaControllerCompat) : MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): String {
        val window = player.currentWindowIndex
        return window.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? =
        controller.sessionActivity

    override fun getCurrentContentText(player: Player): String? {
        return null
    }

    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
        return null
    }

}
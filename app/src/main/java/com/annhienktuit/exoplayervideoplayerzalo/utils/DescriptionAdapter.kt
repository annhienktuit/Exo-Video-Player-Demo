package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.app.PendingIntent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter

class DescriptionAdapter(private val controller: MediaControllerCompat) : MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): String {
        return "Demo exo player"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? =
        controller.sessionActivity

    override fun getCurrentContentText(player: Player): CharSequence? {
        return "Context Text"
    }

    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
        return null
    }

}
package com.annhienktuit.exoplayervideoplayerzalo.adapters

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter

import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.annhienktuit.exoplayervideoplayerzalo.R
import android.content.Intent
import com.annhienktuit.exoplayervideoplayerzalo.activities.MediaStreamingActivity
import com.annhienktuit.exoplayervideoplayerzalo.services.MusicService


class DescriptionAdapter(val context:Context, private val controller: MediaControllerCompat) : MediaDescriptionAdapter {
    private var mContext:Context = context
    override fun getCurrentContentTitle(player: Player): String {
        val window = player.currentWindowIndex
        return window.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return controller.sessionActivity
    }

    override fun getCurrentContentText(player: Player): String? {
        return null
    }

    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
        return getBitmapFromVectorDrawable(mContext, R.drawable.logo)
    }

    private fun getBitmapFromVectorDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, drawableId)?.let {
            val drawable = DrawableCompat.wrap(it).mutate()
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }


}
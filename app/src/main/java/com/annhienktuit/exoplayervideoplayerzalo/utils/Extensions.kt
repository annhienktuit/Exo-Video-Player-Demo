package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.app.Activity
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast


object Extensions {
    fun Activity.toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    fun Activity.isLandscapeOrientation() : Boolean {
        val orientation = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    fun Activity.getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = managedQuery(contentUri, proj, null, null, null)
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }
}
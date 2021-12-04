package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.checkPermissions
import java.math.BigInteger
import java.security.MessageDigest


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

    fun Activity.checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.FOREGROUND_SERVICE), 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WAKE_LOCK), 1);
        }
    }

    fun Activity.requestFilePermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1);
        }
    }

    fun Activity.fileType(url:String):String{
        var result:String = "."
        var stack = ArrayDeque<Char>()
        var lastCharacter = url[url.lastIndex]
        var lastIndex = url.lastIndex
        while (lastCharacter != '.'){
            stack.addFirst(lastCharacter)
            lastIndex --
            lastCharacter = url[lastIndex]
        }
        for(i in stack){
            result += i
        }
        println(result)
        if(result == ".mp3") return "Music"
        else if(result == ".mp4" || result == ".mkv" || result == ".m3u8") return "Video"
        else return "Unknown type"
    }

    fun Activity.splitSongName(url:String):String{
        var start = false
        var result:String = ""
        var stack = ArrayDeque<Char>()
        var lastCharacter = url[url.lastIndex]
        var lastIndex = url.lastIndex
        while (lastCharacter != '/'){
            if(lastCharacter == '_') start = true
            if(start == true){
                stack.addFirst(lastCharacter)
            }
            lastIndex --
            lastCharacter = url[lastIndex]
        }
        for(i in stack){
            result += i
        }
        result = result.dropLast(1)
        println(result)
        return result
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun getBitmapFromResource(context: Context, @DrawableRes bitmapResource: Int): Bitmap? {
        return (context.resources.getDrawable(bitmapResource) as BitmapDrawable).bitmap
    }


}
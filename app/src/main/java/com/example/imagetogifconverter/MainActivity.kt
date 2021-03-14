package com.example.imagetogifconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.imagetogifconverter.util.GifUtil
import com.example.imagetogifconverter.util.Permission

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Permission.verifyStoragePermissions(this)
        GifUtil.saveGif(this)
    }
}
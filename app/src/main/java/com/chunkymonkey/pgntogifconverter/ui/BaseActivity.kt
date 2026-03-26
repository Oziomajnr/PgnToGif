package com.chunkymonkey.pgntogifconverter.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


/**
 * Base Activity.
 */

abstract class BaseActivity<B : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: B

    @get:LayoutRes
    protected abstract val layout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apps targeting API 35+ default to edge-to-edge; without this, content can draw under the
        // action bar and status bar so the toolbar appears to cover the layout.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}

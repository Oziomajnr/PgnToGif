package com.chunkymonkey.pgntogifconverter.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}

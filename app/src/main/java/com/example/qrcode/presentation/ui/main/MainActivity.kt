package com.example.qrcode.presentation.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.example.qrcode.R
import com.example.qrcode.common.Status
import com.example.qrcode.databinding.ActivityMainBinding
import com.example.qrcode.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun initViewModel() {
        viewModel = getViewModel(MainViewModel::class.java)
    }

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun initViews(savedInstanceState: Bundle?) {

    }

    override fun initObservers() {

    }

    override fun initListeners() {

    }

    override fun onClick(v: View?) {

    }

}
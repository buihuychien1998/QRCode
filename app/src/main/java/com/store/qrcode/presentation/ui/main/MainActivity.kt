package com.store.qrcode.presentation.ui.main

import android.os.Bundle
import android.view.View
import com.store.qrcode.R
import com.store.qrcode.databinding.ActivityMainBinding
import com.store.qrcode.presentation.base.BaseActivity
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
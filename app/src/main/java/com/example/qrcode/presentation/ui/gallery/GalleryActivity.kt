package com.example.qrcode.presentation.ui.gallery

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.example.qrcode.R
import com.example.qrcode.common.DEFAULT_INDEX
import com.example.qrcode.common.GALLERY_RESULT
import com.example.qrcode.common.Status
import com.example.qrcode.common.utils.PermissionHelper
import com.example.qrcode.common.utils.openSetting
import com.example.qrcode.common.widget.SpacesItemDecoration
import com.example.qrcode.databinding.ActivityGalleryBinding
import com.example.qrcode.presentation.base.BaseActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class GalleryActivity : BaseActivity<ActivityGalleryBinding, GalleryViewModel>() {
    private val NUMBER_OF_COLUMN = 2
    private var galleryAdapter: GalleryAdapter? = null

    @Inject
    lateinit var permissionHelper: PermissionHelper
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.e("DEBUG", "${it.key} = ${it.value}")
            }

            if (permissions.containsValue(false)) {
                showToast(R.string.permission_not_enough_message)
                finish()
            } else {
                viewModel.getImages(contentResolver)
            }
        }

    override fun initViewModel() {
        viewModel = getViewModel(GalleryViewModel::class.java)
    }

    override val layoutId: Int
        get() = R.layout.activity_gallery

    override fun initViews(savedInstanceState: Bundle?) {
        initGallery()
        initPermissions()
//        if (!permissionHelper.hasPermissions(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//        ) {
//            requestMultiplePermissions.launch(
//                arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                )
//            )
//            return
//        }
//        viewModel.getImages(contentResolver)
    }

    private fun initPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEach(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->  // will emit 2 Permission objects
                when {
                    permission.granted -> {
                        // `permission.name` is granted !
                        viewModel.getImages(contentResolver)
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        // Denied permission without ask never again
                        showToast(R.string.permission_not_enough_message)
                        finish()
                    }
                    else -> {
                        // Denied permission with ask never again
                        // Need to go to the settings
                        openSetting()
                    }
                }
            }
    }

    private fun initGallery() {
        galleryAdapter =
            GalleryAdapter { galleryItem ->
                var drawable = R.drawable.ic_back
                if (galleryAdapter?.selectedIndex != DEFAULT_INDEX) {
                    drawable = R.drawable.ic_check
                }
                viewBinding.btnBackGallery.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        drawable
                    )
                )
            }
        val gridLayoutManager = GridLayoutManager(this, NUMBER_OF_COLUMN)
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (galleryAdapter?.isLastItem(position) == true) gridLayoutManager.spanCount else 1
            }
        }
        viewBinding.rvListGallery.layoutManager = gridLayoutManager
        viewBinding.rvListGallery.addItemDecoration(SpacesItemDecoration(10, 30))
        viewBinding.rvListGallery.adapter = galleryAdapter
    }

    override fun initObservers() {
        viewModel.imagesLiveData.observe(this, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    result.data?.let { data ->
                        galleryAdapter?.setData(data)
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                }
                Status.LOADING -> {
                    showProgressDialog(R.string.loading_message)
                }
            }

        })
    }

    override fun initListeners() {
        viewBinding.btnBackGallery.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackGallery -> {
                if (galleryAdapter?.selectedIndex != DEFAULT_INDEX) {
                    val returnIntent = Intent()
                    val path = galleryAdapter?.getSelectedItem()?.path
                    returnIntent.putExtra(GALLERY_RESULT, path)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                    return
                }
//                val returnIntent = Intent()
//                setResult(RESULT_CANCELED, returnIntent)
                finish()
            }
        }
    }
}
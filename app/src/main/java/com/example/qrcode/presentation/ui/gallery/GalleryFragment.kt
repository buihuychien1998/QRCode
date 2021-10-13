package com.example.qrcode.presentation.ui.gallery

import android.Manifest
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.qrcode.R
import com.example.qrcode.common.DEFAULT_INDEX
import com.example.qrcode.common.Status
import com.example.qrcode.common.utils.PermissionHelper
import com.example.qrcode.common.utils.openSetting
import com.example.qrcode.common.widget.SpacesItemDecoration
import com.example.qrcode.databinding.FragmentGalleryBinding
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    companion object {
        @JvmStatic
        fun newInstance() = GalleryFragment()
    }

    private val NUMBER_OF_COLUMN = 2
    private var galleryAdapter: GalleryAdapter? = null

    @Inject
    lateinit var permissionHelper: PermissionHelper

    override val layoutId: Int
        get() = R.layout.fragment_gallery

    override fun initViewModel() {
        viewModel = getViewModel(GalleryViewModel::class.java)
    }

    override fun initViews() {
        initGallery()
        handlePermissions()
        handleOnBackPress()
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
                        requireContext(),
                        drawable
                    )
                )
            }
        val gridLayoutManager = GridLayoutManager(requireContext(), NUMBER_OF_COLUMN)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (galleryAdapter?.isLastItem(position) == true) gridLayoutManager.spanCount else 1
            }
        }
        viewBinding.rvListGallery.layoutManager = gridLayoutManager
        viewBinding.rvListGallery.addItemDecoration(SpacesItemDecoration(10, 30))
        viewBinding.rvListGallery.adapter = galleryAdapter
    }

    private fun handlePermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->  // will emit 2 Permission objects if using requestEach
                when {
                    permission.granted -> {
                        // `permission.name` is granted !
                        viewModel.getImages(requireActivity().contentResolver)
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        // Denied permission without ask never again
                        showToast(R.string.permission_not_enough_message)
                        handleBackEvent()
                    }
                    else -> {
                        // Denied permission with ask never again
                        // Need to go to the settings
                        context?.openSetting()
                    }
                }
            }
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
                    val path = galleryAdapter?.getSelectedItem()?.path
                    sharedViewModel.setImagePath(path)
                    handleBackEvent()
                    return
                }
                handleBackEvent()
            }
        }
    }

    private fun handleOnBackPress() {
        val callback = object : OnBackPressedCallback(
            true
            /** true means that the callback is enabled */
        ) {
            override fun handleOnBackPressed() {
                // Show your dialog and handle navigation
                handleBackEvent()
            }
        }

        // note that you could enable/disable the callback here as well by setting callback.isEnabled = true/false

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun handleBackEvent() {
        sharedViewModel.enableQRDetect(true)
        (requireActivity() as MainActivity).popFragment(GalleryFragment::class.java.simpleName)
    }
}
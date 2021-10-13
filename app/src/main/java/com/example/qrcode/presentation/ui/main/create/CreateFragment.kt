package com.example.qrcode.presentation.ui.main.create

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.qrcode.R
import com.example.qrcode.R.*
import com.example.qrcode.common.HTTP
import com.example.qrcode.common.HTTPS
import com.example.qrcode.common.Status
import com.example.qrcode.common.utils.*
import com.example.qrcode.databinding.FragmentCreateBinding
import com.example.qrcode.model.entity.Barcode
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import net.glxn.qrgen.android.QRCode
import timber.log.Timber
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [CreateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CreateFragment : BaseFragment<FragmentCreateBinding, CreateViewModel>() {
    private val barcode by lazy {
        arguments?.getParcelable<Barcode>(BARCODE_ARG)
    }
    private var logo: Bitmap? = null
    private var galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {

            override fun onActivityResult(result: ActivityResult?) {
                if (result == null || result.data == null) {
                    return
                }
                val selectedPhotoUri = result.data?.data ?: return
                //.. your picked image result here
                try {
                    logo = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(
                            activity?.contentResolver,
                            selectedPhotoUri
                        )
                    } else {
                        val source =
                            ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                selectedPhotoUri
                            )
                        ImageDecoder.decodeBitmap(source)
                    }
                    viewBinding.ivLogo.setImageBitmap(logo)
                } catch (e: Exception) {
                    showToast(string.error_message)
                }
            }
        })

    override val layoutId: Int
        get() = layout.fragment_create

    override fun initViewModel() {
        viewModel = getViewModel(CreateViewModel::class.java)
    }

    override fun initViews() {
        initData()
        handleOnBackPress()
    }

    private fun initData() {
        viewBinding.edtText.post {
            viewBinding.edtText.setText(barcode?.qrCode)
            viewBinding.edtText.requestFocus()
            barcode?.qrCode?.length?.let { viewBinding.edtText.setSelection(it) }
            showKeyboard()
        }
    }

    override fun initObservers() {
        viewModel.qrCodeInsertResult.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    result.data?.let { data ->
                        if (data >= 0) {
                            hideKeyboard()
                            showToast(string.created)
                            handleBackEvent()
                            return@observe
                        }
                        showToast(string.create_failed)
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    showToast(string.error_message)
                }
                Status.LOADING -> {
                    showProgressDialog(string.loading_message)
                }
            }
        }

        viewModel.qrCodeUpdateResult.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    result.data?.let { data ->
                        if (data >= 0) {
                            hideKeyboard()
                            showToast(string.updated)
                            handleBackEvent()
                            return@observe
                        }
                        showToast(string.update_failed)
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    showToast(string.error_message)
                }
                Status.LOADING -> {
                    showProgressDialog(string.loading_message)
                }
            }
        }
    }

    override fun initListeners() {
        viewBinding.btnBackCreate.setOnClickListener(this)
        viewBinding.btnSaveQRCode.setOnClickListener(this)
        viewBinding.ivLogo.setOnClickListener(this)
        viewBinding.edtText.addTextChangedListener {
            viewBinding.tvCount.text = getString(string.counter_text, it?.length)
            if (viewBinding.edtText.error != null) {
                viewBinding.edtText.error = null
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackCreate -> {
                hideKeyboard()
                handleBackEvent()
            }
            R.id.btnSaveQRCode -> {
                val text = viewBinding.edtText.text.toString()
                if (TextUtils.isEmpty(text) || text.isBlank()) {
                    viewBinding.edtText.error = getString(string.text_require)
                    return
                }
                val bitmap = QRCode.from(text).withSize(300, 300).bitmap()
                var endBitmap: Bitmap? = bitmap
                if (logo != null) {
                    logo = logo?.copy(Bitmap.Config.ARGB_8888, true)
                    try {
                        val displayMetrics = DisplayMetrics()
                        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

                        val size =
                            displayMetrics.widthPixels.coerceAtMost(displayMetrics.heightPixels)
                        val resizeLogo = logo?.let {
                            Bitmap.createScaledBitmap(
                                it,
                                72.dpToPx(),
                                72.dpToPx(),
                                true
                            )
                        }
                            ?: return
                        endBitmap = text.encodeAsQrCodeBitmap(size, resizeLogo)
//                        endBitmap = mergeBitmaps(logo, bitmap)
                    } catch (e: Exception) {
                        showToast(R.string.error_message)
                        return
                    }
                }


                val byte = endBitmap?.let { convertBitmapToByteArray(it) }
                var genre = QRGenre.TEXT
                if (text.startsWith(HTTP) || text.startsWith(HTTPS)) {
                    genre = QRGenre.WEBSITE
                }
                if (barcode == null) {
                    viewModel.insert(Barcode(text, byte, genre, DateConverter.fromDate(Date())))
                    return
                }
                barcode?.qrCodeId?.let { viewModel.update(text, byte, genre, it) }
            }

            R.id.ivLogo -> {
                handlePermissions()
            }
        }
    }

    private fun handlePermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->  // will emit 2 Permission objects
                when {
                    permission.granted -> {
                        // `permission.name` is granted !
//                        galleryLauncher.launch("image/*")
                        val photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "*/*"
                        photoPickerIntent.putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            arrayOf("image/*")
                        )
                        galleryLauncher.launch(photoPickerIntent)
                        Timber.d("handlePermissions")
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
        (requireActivity() as MainActivity).popFragment(CreateFragment::class.java.simpleName)
    }

    companion object {
        val BARCODE_ARG = "BARCODE_ARG"

        @JvmStatic
        fun newInstance(barcode: Barcode?) = CreateFragment().apply {
            arguments = Bundle().apply {
                putParcelable(BARCODE_ARG, barcode)
            }
        }
    }
}
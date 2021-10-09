package com.example.qrcode.presentation.ui.main.create

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.qrcode.R
import com.example.qrcode.common.HTTP
import com.example.qrcode.common.HTTPS
import com.example.qrcode.common.Status
import com.example.qrcode.common.utils.DateConverter
import com.example.qrcode.common.utils.QRGenre
import com.example.qrcode.common.utils.convertBitmapToByteArray
import com.example.qrcode.databinding.FragmentCreateBinding
import com.example.qrcode.model.entity.Barcode
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import net.glxn.qrgen.android.QRCode
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

    companion object {
        val BARCODE_ARG = "BARCODE_ARG"

        @JvmStatic
        fun newInstance(barcode: Barcode?) = CreateFragment().apply {
            arguments = Bundle().apply {
                putParcelable(BARCODE_ARG, barcode)
            }
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_create

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
                            showToast(R.string.created)
                            handleBackEvent()
                            return@observe
                        }
                        showToast(R.string.create_failed)
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    showToast(R.string.error_message)
                }
                Status.LOADING -> {
                    showProgressDialog(R.string.loading_message)
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
                            showToast(R.string.updated)
                            handleBackEvent()
                            return@observe
                        }
                        showToast(R.string.update_failed)
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    showToast(R.string.error_message)
                }
                Status.LOADING -> {
                    showProgressDialog(R.string.loading_message)
                }
            }
        }
    }

    override fun initListeners() {
        viewBinding.btnBackCreate.setOnClickListener(this)
        viewBinding.btnSaveQRCode.setOnClickListener(this)
        viewBinding.edtText.addTextChangedListener {
            viewBinding.tvCount.text = getString(R.string.counter_text, it?.length)
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
                    viewBinding.edtText.error = getString(R.string.text_require)
                    return
                }
                val bitmap = QRCode.from(text).withSize(512, 512).bitmap()
                val byte = convertBitmapToByteArray(bitmap)
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

}
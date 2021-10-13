package com.example.qrcode.presentation.ui.main.qrcode

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.qrcode.R
import com.example.qrcode.common.SHARE_FILE
import com.example.qrcode.common.Status
import com.example.qrcode.common.widget.SwipeSimpleCallback
import com.example.qrcode.databinding.FragmentQRCodeBinding
import com.example.qrcode.model.entity.Barcode
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import com.example.qrcode.presentation.ui.main.create.CreateFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


@AndroidEntryPoint
class QRCodeFragment : BaseFragment<FragmentQRCodeBinding, QRCodeViewModel>() {

    companion object {
        @JvmStatic
        fun newInstance() = QRCodeFragment()
    }

    private lateinit var qrCodeAdapter: QRCodeAdapter
    var imageUri: Uri? = null
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            lifecycleScope.launch {
                viewModel.deleteBitmap(activity?.contentResolver, imageUri)
            }
//            if (result.resultCode == Activity.RESULT_OK) {
//                // There are no request codes
//                val data: Intent? = result.data
//
//            }
        }

    override val layoutId: Int
        get() = R.layout.fragment_q_r_code

    override fun initViewModel() {
        viewModel = getViewModel(QRCodeViewModel::class.java)
    }

    override fun initViews() {
        initQRCode()
        handleOnBackPress()
    }

    private fun initQRCode() {
        qrCodeAdapter = QRCodeAdapter()
        qrCodeAdapter.itemClickListener = object : QRCodeAdapter.ItemClickListener {
            override fun onItemClickListener(barcode: Barcode?) {
                openCreateBarcodeScreen(barcode)
            }

        }
        viewBinding.rvQrCode.adapter = qrCodeAdapter
        val swipeSimpleCallback = object : SwipeSimpleCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                if (swipeDir == ItemTouchHelper.LEFT) {
                    viewModel.delete(qrCodeAdapter.getQRCode(position))
                } else {
                    val title = SHARE_FILE
                    val image = qrCodeAdapter.getQRCode(position).image
                    if (image == null) {
                        showToast(R.string.error_message)
                        qrCodeAdapter.notifyItemChanged(position)
                        return
                    }
                    lifecycleScope.launch {
                        val path = viewModel.insertBitmap(activity?.contentResolver, image, title)
                        Timber.d("Image path: $path")
                        imageUri = Uri.parse(path)
                        val share = Intent(Intent.ACTION_SEND)
                        share.type = "image/jpeg"
                        share.putExtra(Intent.EXTRA_STREAM, imageUri)
                        resultLauncher.launch(Intent.createChooser(share, "Share"))
                        qrCodeAdapter.notifyItemChanged(position)
                    }
                }
            }
        }
        swipeSimpleCallback.leftIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        swipeSimpleCallback.rightIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_share)
        val itemTouchHelper = ItemTouchHelper(swipeSimpleCallback)
        itemTouchHelper.attachToRecyclerView(viewBinding.rvQrCode)
    }

    override fun initObservers() {
        viewModel.barList.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    result.data?.let { data ->
                        if (data.isEmpty()) {
                            viewBinding.lnlNoQRCode.visibility = View.VISIBLE
                            viewBinding.rvQrCode.visibility = View.GONE
                            viewBinding.tvCreateTool.visibility = View.GONE
                            return@observe
                        }
                        viewBinding.lnlNoQRCode.visibility = View.GONE
                        viewBinding.rvQrCode.visibility = View.VISIBLE
                        viewBinding.tvCreateTool.visibility = View.VISIBLE
                        qrCodeAdapter.submitList(data)
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
        })
        viewModel.qrCodeDeleteResult.observe(viewLifecycleOwner) { result ->
            if (result == null) {
                return@observe
            }
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    viewModel.clearData()
                    result.data?.let { data ->
                        if (data >= 0) {
                            showToast(R.string.deleted)
                            return@observe
                        }
                        showToast(R.string.delete_failed)
                    }
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    showToast(R.string.error_message)
                    viewModel.clearData()
                }
                Status.LOADING -> {
                    showProgressDialog(R.string.loading_message)
                }
            }
        }
        viewModel.insertImageResult.observe(viewLifecycleOwner) { result ->
            if (result == null) {
                return@observe
            }
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    viewModel.clearData()
                }
                Status.ERROR -> {
                    dismissProgressDialog()
                    showToast(R.string.error_message)
                    viewModel.clearData()
                }
                Status.LOADING -> {
                    showProgressDialog(R.string.loading_message)
                }

            }
        }
    }

    override fun initListeners() {
        viewBinding.btnBackQRCode.setOnClickListener(this)
        viewBinding.tvCreate.setOnClickListener(this)
        viewBinding.tvCreateTool.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackQRCode -> {
                handleBackEvent()
            }
            R.id.tvCreate, R.id.tvCreateTool -> {
                openCreateBarcodeScreen(null)
            }
        }
    }

    private fun openCreateBarcodeScreen(barcode: Barcode?) {
        (requireActivity() as MainActivity).openFullScreenFragment(
            CreateFragment.newInstance(barcode),
            CreateFragment::class.java.simpleName
        )
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
        (requireActivity() as MainActivity).popFragment(QRCodeFragment::class.java.simpleName)
    }
}
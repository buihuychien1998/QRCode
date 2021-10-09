package com.example.qrcode.presentation.ui.main.qrcode

import android.graphics.*
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.qrcode.R
import com.example.qrcode.common.Status
import com.example.qrcode.common.utils.createShareIntent
import com.example.qrcode.common.widget.SwipeSimpleCallback
import com.example.qrcode.databinding.FragmentQRCodeBinding
import com.example.qrcode.model.entity.Barcode
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import com.example.qrcode.presentation.ui.main.create.CreateFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class QRCodeFragment : BaseFragment<FragmentQRCodeBinding, QRCodeViewModel>() {

    companion object {
        @JvmStatic
        fun newInstance() = QRCodeFragment()
    }

    private lateinit var qrCodeAdapter: QRCodeAdapter

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
        qrCodeAdapter.itemClickListener = object: QRCodeAdapter.ItemClickListener{
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
                    context.createShareIntent(qrCodeAdapter.getQRCode(position).qrCode)
                    qrCodeAdapter.notifyItemChanged(position)
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
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
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
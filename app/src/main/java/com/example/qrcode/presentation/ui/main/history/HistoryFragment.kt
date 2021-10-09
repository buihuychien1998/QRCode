package com.example.qrcode.presentation.ui.main.history

import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.example.qrcode.R
import com.example.qrcode.common.Status
import com.example.qrcode.databinding.FragmentHistoryBinding
import com.example.qrcode.model.entity.History
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding, HistoryViewModel>() {

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_history
    private lateinit var historyAdapter: HistoryAdapter

    override fun initViewModel() {
        viewModel = getViewModel(HistoryViewModel::class.java)
    }

    override fun initViews() {
        initHistory()
        handleOnBackPress()
    }

    private fun initHistory() {
        historyAdapter = HistoryAdapter()
        historyAdapter.itemEvent = object : HistoryAdapter.ItemEvent {
            override fun onDelete(position: Int, history: History?) {
                history?.let { viewModel.delete(it) }
            }
        }
        viewBinding.rvListHistory.adapter = historyAdapter
    }

    override fun initObservers() {
//        viewModel.allHistories.observe(viewLifecycleOwner) {
//            historyAdapter.submitList(it)
//        }
        viewModel.allHistories.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    dismissProgressDialog()
                    result.data?.let { data ->
                        if (data.isEmpty()) {
                            viewBinding.lnlNoHistory.visibility = View.VISIBLE
                            viewBinding.rvListHistory.visibility = View.GONE
                            viewBinding.btnClearHistory.visibility = View.GONE
                            return@observe
                        }
                        viewBinding.lnlNoHistory.visibility = View.GONE
                        viewBinding.rvListHistory.visibility = View.VISIBLE
                        viewBinding.btnClearHistory.visibility = View.VISIBLE
                        historyAdapter.submitList(data)

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
        viewModel.historyDeleteResult.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { data ->
                        //data is number delete record
                        if (data >= 0) {
                            showToast(R.string.deleted)
                            return@observe
                        }
                        showToast(R.string.delete_failed)
                    }
                    dismissProgressDialog()
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
        viewModel.historyDeleteAllResult.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { data ->
                        //data is number delete record
                        if (data >= 0) {
                            showToast(R.string.deleted)
                            return@observe
                        }
                        showToast(R.string.delete_failed)
                    }
                    dismissProgressDialog()
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
    }

    override fun initListeners() {
        viewBinding.btnBackHistory.setOnClickListener(this)
        viewBinding.btnClearHistory.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackHistory -> {
                handleBackEvent()
            }
            R.id.btnClearHistory -> {
                showClearHistoryDialog()
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
        (requireActivity() as MainActivity).popFragment(HistoryFragment::class.java.simpleName)
    }

    private fun showClearHistoryDialog() {
        val reqAlertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        reqAlertDialog.setTitle(R.string.clear_history_title)
        reqAlertDialog.setMessage(R.string.clear_history_message)
        reqAlertDialog.setPositiveButton(R.string.ok) { _, _ ->
            viewModel.deleteAll()
        }
        reqAlertDialog.setNegativeButton(
            R.string.cancel
        ) { _, _ ->
        }
        reqAlertDialog.create().show()
    }
}
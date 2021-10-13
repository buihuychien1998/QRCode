package com.store.qrcode.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.store.qrcode.R
import com.store.qrcode.common.utils.DialogUtils

abstract class BaseFragment<DB : ViewDataBinding, VM : BaseViewModel> :
    Fragment(), View.OnClickListener {
    protected lateinit var viewBinding: DB
    protected lateinit var viewModel: VM
    protected lateinit var sharedViewModel: SharedViewModel

    abstract val layoutId: Int
    abstract fun initViewModel()

    /**
     * Initialize views
     */
    abstract fun initViews()
    abstract fun initObservers()

    /**
     * Initialize listeners
     */
    abstract fun initListeners()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DataBindingUtil.inflate<DB>(inflater, layoutId, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initSharedViewModel()
        initViews()
        initBaseObservers()
        initObservers()
        initListeners()
    }

    private fun initBaseObservers() {
        viewModel.getIsLoading().observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                showProgressDialog(R.string.loading_message)
            }
            dismissProgressDialog()
        })
    }

    private fun initSharedViewModel() {
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    /**
     * This function to get ViewModel
     *
     * @param clazz
     * @return
     */
    protected fun getViewModel(clazz: Class<VM>): VM {
        return ViewModelProvider(this)[clazz]
    }

    /**
     * This function to get parent ViewModel
     * 親ViewModelを取得するこの関数
     *
     * @param clazz
     * @param <PVM>
     * @return
    </PVM> */
    protected fun <PVM : ViewModel?> getParentViewModel(clazz: Class<PVM>): PVM {
        return ViewModelProvider(requireActivity())[clazz]
    }

    /**
     * This function is used to show soft keyboard
     * この機能は、ソフトキーボードを表示するために使用されます
     */
    fun showKeyboard() {
        val view = requireActivity().currentFocus ?: return
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(
            view,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    fun forceShowKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    /**
     * This function is used to hide soft keyboard
     * この機能は、ソフトキーボードを非表示にするために使用されます
     */
    fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
            ?: //            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            return
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showProgressDialog(message: String?) {
        DialogUtils.showProgressDialog(context, message)
    }

    fun showProgressDialog(@StringRes messageId: Int) {
        showProgressDialog(getString(messageId))
    }

    fun dismissProgressDialog() {
        DialogUtils.dismissProgressDialog()
    }

    fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(@StringRes resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
}
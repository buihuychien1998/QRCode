package com.example.qrcode.presentation.base

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.qrcode.R
import com.example.qrcode.common.LANGUAGE
import com.example.qrcode.common.SHARED_PREFERENCE_LANGUAGE
import com.example.qrcode.common.utils.ContextUtils
import com.example.qrcode.common.utils.DialogUtils
import com.example.qrcode.common.utils.Languages
import com.example.qrcode.common.utils.changeLanguage
import com.example.qrcode.datastore.PrefsStore
import java.util.*


abstract class BaseActivity<DB : ViewDataBinding, VM : BaseViewModel> :
    AppCompatActivity(), View.OnClickListener {
    protected lateinit var viewBinding: DB
    protected lateinit var viewModel: VM
    protected lateinit var sharedViewModel: SharedViewModel
    abstract fun initViewModel()
    abstract val layoutId: Int

    lateinit var prefsStore: PrefsStore

    /**
     * Initialize views
     *
     * @param savedInstanceState
     */
    abstract fun initViews(savedInstanceState: Bundle?)
    abstract fun initObservers()

    /**
     * Initialize listeners
     */
    abstract fun initListeners()
    override fun attachBaseContext(newBase: Context) {
        // get chosen language from shread preference
//        prefsStore = DataStoreHelper(newBase)
//        val lang = runBlocking(Dispatchers.IO) {
//            prefsStore.getLanguage().first()
//        }
        val mSp = newBase.getSharedPreferences(SHARED_PREFERENCE_LANGUAGE, MODE_PRIVATE)
        val lang = mSp?.getString(LANGUAGE, Languages.DEFAULT) ?:  Languages.DEFAULT
        val localeToSwitchTo = Locale(lang)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun recreate() {
        super.recreate()
        val mSp = getSharedPreferences(SHARED_PREFERENCE_LANGUAGE, MODE_PRIVATE)
        val lang = mSp?.getString(LANGUAGE, Languages.DEFAULT) ?:  Languages.DEFAULT
        this.changeLanguage(lang)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView<DB>(this, layoutId)
        initViewModel()
        initSharedViewModel()
        initViews(savedInstanceState)
        initBaseObservers()
        initObservers()
        initListeners()
    }

    private fun initBaseObservers() {
        viewModel.getIsLoading().observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog(R.string.loading_message)
                return@Observer
            }
            dismissProgressDialog()
        })
    }

    private fun initSharedViewModel() {
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
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
     * This function is used to show soft keyboard
     * この機能は、ソフトキーボードを表示するために使用されます
     */
    fun showKeyboard() {
        val view = currentFocus ?: return
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(
            view,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    fun forceShowKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
            ?: //            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            return
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showProgressDialog(message: String?) {
        DialogUtils.showProgressDialog(this, message)
    }

    fun showProgressDialog(@StringRes messageId: Int) {
        showProgressDialog(getString(messageId))
    }

    fun dismissProgressDialog() {
        DialogUtils.dismissProgressDialog()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    open fun openFullScreenFragment(fragment: Fragment, tag: String?) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top)
            .replace(android.R.id.content, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    open fun popFragment(tag: String?) {
        supportFragmentManager.popBackStack(tag, POP_BACK_STACK_INCLUSIVE)
    }
}
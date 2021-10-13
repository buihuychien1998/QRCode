package com.example.qrcode.presentation.ui.main.setting

import android.content.Context.MODE_PRIVATE
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.example.qrcode.R
import com.example.qrcode.common.LANGUAGE
import com.example.qrcode.common.PreferencesKeys
import com.example.qrcode.common.SHARED_PREFERENCE_LANGUAGE
import com.example.qrcode.common.utils.Languages
import com.example.qrcode.databinding.FragmentSettingBinding
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>() {

    companion object {
        @JvmStatic
        fun newInstance() = SettingFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_setting

    override fun initViewModel() {
        viewModel = getViewModel(SettingViewModel::class.java)
    }

    override fun initViews() {
        handleOnBackPress()
        val mSp = activity?.getSharedPreferences(SHARED_PREFERENCE_LANGUAGE, MODE_PRIVATE)
        val lang = mSp?.getString(LANGUAGE, Languages.DEFAULT)
        when (lang) {
            Languages.VIETNAMESE -> {
                viewBinding.rbVietnamese.isChecked = true
            }
            else -> viewBinding.rbEnglish.isChecked = true
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

    override fun initObservers() {
        viewModel.setting.observe(viewLifecycleOwner) {
            viewBinding.swSound.isChecked = it.sound
            viewBinding.swVibrate.isChecked = it.vibrate
            viewBinding.swSaveHistory.isChecked = it.saveHistory
            viewBinding.swRemoveAds.isChecked = it.removeAds
        }
//        viewModel.language.observe(viewLifecycleOwner){
//            when (it) {
//                Languages.VIETNAMESE -> {
//                    viewBinding.rbVietnamese.isChecked = true
//                }
//                else -> viewBinding.rbEnglish.isChecked = true
//            }
//        }
    }

    override fun initListeners() {
        viewBinding.btnBackSetting.setOnClickListener(this)
        viewBinding.swSound.setOnClickListener(this)
        viewBinding.swVibrate.setOnClickListener(this)
        viewBinding.swSaveHistory.setOnClickListener(this)
        viewBinding.swRemoveAds.setOnClickListener(this)
        viewBinding.rbEnglish.setOnClickListener(this)
        viewBinding.rbVietnamese.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackSetting -> {
                handleBackEvent()
            }
            R.id.swSound -> {
                val sound = viewBinding.swSound.isChecked
                viewModel.enableSetting(PreferencesKeys.SOUND, sound)
            }
            R.id.swVibrate -> {
                val vibrate = viewBinding.swVibrate.isChecked
                viewModel.enableSetting(PreferencesKeys.VIBRATE, vibrate)
            }
            R.id.swSaveHistory -> {
                val saveHistory = viewBinding.swSaveHistory.isChecked
                viewModel.enableSetting(PreferencesKeys.SAVE_HISTORY, saveHistory)
            }
            R.id.swRemoveAds -> {
                val removeAds = viewBinding.swRemoveAds.isChecked
                viewModel.enableSetting(PreferencesKeys.REMOVE_ADS, removeAds)
            }
            R.id.rbEnglish -> {
                val mSp = activity?.getSharedPreferences(SHARED_PREFERENCE_LANGUAGE, MODE_PRIVATE)
                val storedLang = mSp?.getString(LANGUAGE, Languages.DEFAULT)
                if(Languages.DEFAULT.equals(storedLang)){
                    return
                }
                changeLanguage(Languages.DEFAULT)
                activity?.recreate()
//                activity?.refreshLayout()
//                context?.changeLanguage(Languages.DEFAULT)
            }
            R.id.rbVietnamese -> {
                val mSp = activity?.getSharedPreferences(SHARED_PREFERENCE_LANGUAGE, MODE_PRIVATE)
                val storedLang = mSp?.getString(LANGUAGE, Languages.DEFAULT)
                if(Languages.VIETNAMESE.equals(storedLang)){
                    return
                }
                changeLanguage(Languages.VIETNAMESE)
                activity?.recreate()
//                activity?.refreshLayout()
//                context?.changeLanguage(Languages.VIETNAMESE)
            }
        }
    }

    private fun handleBackEvent() {
        sharedViewModel.enableQRDetect(true)
        (requireActivity() as MainActivity).popFragment(SettingFragment::class.java.simpleName)
    }

    private fun changeLanguage(@Languages lang: String){
        val mSp = activity?.getSharedPreferences(SHARED_PREFERENCE_LANGUAGE, MODE_PRIVATE)
        val editor = mSp?.edit()
        editor?.putString(LANGUAGE, lang)
        editor?.apply()
    }
}
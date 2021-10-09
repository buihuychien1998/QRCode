package com.example.qrcode.presentation.ui.main.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.qrcode.R
import com.example.qrcode.common.*
import com.example.qrcode.common.utils.*
import com.example.qrcode.databinding.FragmentZxingBinding
import com.example.qrcode.presentation.base.BaseFragment
import com.example.qrcode.presentation.ui.gallery.GalleryFragment
import com.example.qrcode.presentation.ui.main.MainActivity
import com.example.qrcode.presentation.ui.main.history.HistoryFragment
import com.example.qrcode.presentation.ui.main.qrcode.QRCodeFragment
import com.example.qrcode.presentation.ui.main.setting.SettingFragment
import com.example.qrcode.presentation.ui.main.setting.SettingPreferences
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.vision.Frame
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ZxingFragment : BaseFragment<FragmentZxingBinding, ScanViewModel>() {
    @Inject
    lateinit var permissionHelper: PermissionHelper
    private var isFlashSupported: Boolean? = false
    private var isTorchOn = false
    private var setting: SettingPreferences? = null
    private var mInterstitialAd: InterstitialAd? = null

    @SuppressLint("MissingPermission")
    private val permissionReq =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                setUpBarcodeDetect()
            } else {
                //Now further we check if used denied permanently or not
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // case 4 User has denied permission but not permanently
                    showRequestPermissionDialog()
                } else {
                    // case 5. Permission denied permanently.
                    // You can open Permission setting's page from here now.
                    context?.openSetting()
                }
            }
        }

    private fun setUpBarcodeDetect() {
        viewBinding.barcodeView.decodeContinuous(callback)
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            showQRCodeConfirmDialog(result.text)
            //Do something with code result
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ZxingFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override val layoutId: Int
        get() = R.layout.fragment_zxing

    override fun initViewModel() {
        viewModel = getViewModel(ScanViewModel::class.java)
    }

    override fun initViews() {
        handlePermission()
        loadBanner()
        loadAd()
    }

    @SuppressLint("MissingPermission")
    private fun loadBanner() {
        if (setting?.removeAds == true) {
            viewBinding.adView.visibility = View.GONE
            return
        }
        MobileAds.initialize(requireContext()) { }
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(Arrays.asList("735489F18FFFC3A3B7C3E29C48E6589C")).build()
        MobileAds.setRequestConfiguration(configuration)
        val adRequest =
            AdRequest.Builder().build()
        viewBinding.adView.loadAd(adRequest)
        viewBinding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Timber.d("adView onAdLoaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                Timber.d("adView onAdFailedToLoad $adError")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Timber.d("adView onAdOpened")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Timber.d("adView onAdClicked")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Timber.d("adView onAdClicked")
            }
        }
    }

    /**
     * Case 1: User doesn't have permission
     * Case 2: User has permission
     *
     * Case 3: User has never seen the permission Dialog
     * Case 4: User has denied permission once but he din't clicked on "Never Show again" check box
     * Case 5: User denied the permission and also clicked on the "Never Show again" check box.
     * Case 6: User has allowed the permission
     *
     */
    private fun handlePermission() {
        if (!permissionHelper.hasPermission(Manifest.permission.CAMERA)) {
            // This is Case 1. Now we need to check further if permission was shown before or not
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // This is Case 4.
                showRequestPermissionDialog()
                return
            }
            // This is Case 3. Request for permission here
            permissionReq.launch(Manifest.permission.CAMERA)
            return
        }
        // This is Case 2. You have permission now you can do anything related to it
        setUpBarcodeDetect()
    }

    private fun handleSetting() {
        setting?.let {
            if (it.sound) {
                context?.ring()
            }
            if (it.vibrate) {
                context?.vibrate()
            }
        }
    }

    private fun showQRCodeConfirmDialog(qrCode: String) {
        val qrConfirmDialog = AlertDialog.Builder(requireContext())
            .setMessage(qrCode)
            .setPositiveButton(R.string.copy) { _, _ ->
                handleQRCode(qrCode)
            }.setNegativeButton(R.string.cancel) { _, _ ->

            }
            .show()
        qrConfirmDialog.setCancelable(false)
//        qrConfirmDialog.setOnDismissListener {
//            viewBinding.ivScan.setColorFilter(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.color_icon
//                ), PorterDuff.Mode.MULTIPLY
//            )
//        }
    }

    private fun handleQRCode(displayValue: String) {
        context?.copy(displayValue)
        if (displayValue.startsWith(HTTP) || displayValue.startsWith(HTTPS)) {
            try {
                val myIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(displayValue)
                )
                startActivity(myIntent)
            } catch (e: ActivityNotFoundException) {
                showToast(R.string.not_found_browser_message)
                e.printStackTrace()
            }
            return
        }

    }


    override fun initObservers() {
        viewModel.setting.observe(viewLifecycleOwner) {
            setting = it
            if (it.removeAds) {
                viewBinding.adView.visibility = View.GONE
                return@observe
            }
            viewBinding.adView.visibility = View.VISIBLE
        }
        viewModel.historyInsertResult.observe(viewLifecycleOwner, {
            handleInsertHistoryResult(it)
        })
        sharedViewModel.imagePath.observe(viewLifecycleOwner) { filePath ->
            if (filePath == null) {
                return@observe
            }
            handleGalleryResult(filePath)
        }
    }

    private fun handleGalleryResult(filePath: String?) {
        filePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            val frame = Frame.Builder().setBitmap(bitmap).build()
//            val barcodes = mBarcodeDetector?.detect(frame)
            val barcodes = ""
//            if (barcodes != null && barcodes.size() > 0) {
//                barcodes.valueAt(0)?.displayValue?.let { qrCode ->
//                    if (setting?.saveHistory == true) {
//                        viewModel.insert(History(qrCode, DateConverter.fromDate(Date())))
//                    }
//                    showQRCodeConfirmDialog(qrCode)
//                }
//                sharedViewModel.setImagePath(null)
//                return@let
//            }
            sharedViewModel.setImagePath(null)
            showToast(R.string.scan_failed_message)
        }
    }

    private fun handleInsertHistoryResult(result: ResultWrapper<Long>) {
        when (result.status) {
            Status.SUCCESS -> {
                dismissProgressDialog()
                result.data?.let { value ->
                    if (value == DEFAULT_INDEX.toLong()) {
                        showToast(R.string.error_message)
                    }
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

    override fun initListeners() {
        viewBinding.btnFlash.setOnClickListener(this)
        viewBinding.btnGallery.clickWithDebounce { view -> onClick(view) }
        viewBinding.btnFlipCamera.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnFlash -> {
                switchFlash()
            }
            R.id.btnGallery -> {
                openGallery()
            }
            R.id.btnFlipCamera -> {
                showOptionMenu(v)
            }
        }
    }

    private fun openQRCode() {
        sharedViewModel.enableQRDetect(false)
        if (isTorchOn) {
            switchFlash()
        }
        (requireActivity() as MainActivity).openFullScreenFragment(
            QRCodeFragment.newInstance(),
            QRCodeFragment::class.java.simpleName
        )
    }

    private fun openHistory() {
        sharedViewModel.enableQRDetect(false)
        if (isTorchOn) {
            switchFlash()
        }
        (requireActivity() as MainActivity).openFullScreenFragment(
            HistoryFragment.newInstance(),
            HistoryFragment::class.java.simpleName
        )
    }

    private fun openSetting() {
        sharedViewModel.enableQRDetect(false)
        if (isTorchOn) {
            switchFlash()
        }
        (requireActivity() as MainActivity).openFullScreenFragment(
            SettingFragment.newInstance(),
            SettingFragment::class.java.simpleName
        )
    }

    private fun openGallery() {
//        val galleryIntent = Intent(requireActivity(), GalleryActivity::class.java)
//        galleryResultLauncher.launch(galleryIntent)
        sharedViewModel.enableQRDetect(false)
        if (isTorchOn) {
            switchFlash()
        }
        (requireActivity() as MainActivity).openFullScreenFragment(
            GalleryFragment.newInstance(),
            GalleryFragment::class.java.simpleName
        )
    }

    private fun showRequestPermissionDialog() {
        val reqAlertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        reqAlertDialog.setTitle(R.string.camera_permission_needed)
        reqAlertDialog.setMessage(R.string.camera_permission_alert_msg)
        reqAlertDialog.setPositiveButton(R.string.ok) { dialog, _ ->
            permissionReq.launch(Manifest.permission.CAMERA)
            dialog.dismiss()
        }
        reqAlertDialog.setNegativeButton(
            R.string.cancel
        ) { dialog, _ ->
            run {
                dialog.dismiss()
                activity?.finish()
            }
        }
        reqAlertDialog.create().show()
    }

    private fun showAboutDialog() {
        val reqAlertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        reqAlertDialog.setTitle(R.string.about_title)
        reqAlertDialog.setMessage(getString(R.string.about_message, VERSION))
        reqAlertDialog.setPositiveButton(R.string.ok) { _, _ ->
        }
        reqAlertDialog.setCancelable(false)
        reqAlertDialog.create().show()
    }

    private fun switchFlash() {
        val camManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = camManager.cameraIdList[0]
        val camChars = camManager.getCameraCharacteristics(cameraId)

        isFlashSupported = camChars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
        if (isFlashSupported == false) {
            showToast(R.string.flash_not_available)
            return
        }
    }

    private fun showOptionMenu(view: View?) {
        // inflate the layout of the popup window
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.popup_option, null)
        val menuQRCode: TextView? = popupView?.findViewById(R.id.menuQRCode)
        val menuHistory: TextView? = popupView?.findViewById(R.id.menuHistory)
        val menuSetting: TextView? = popupView?.findViewById(R.id.menuSetting)
        val menuAbout: TextView? = popupView?.findViewById(R.id.menuAbout)

        // create the popup window
        val popupWindow = PopupWindow(context)
        popupWindow.contentView = popupView
        popupWindow.setBackgroundDrawable(null)
        popupWindow.isOutsideTouchable = true

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
//        popupWindow.showAtLocation(view , Gravity.CENTER, 0, 0)
        popupWindow.showAsDropDown(view)

        // dismiss the popup window when touched
        menuQRCode?.setOnClickListener {
            popupWindow.dismiss()
            openQRCode()
        }
        menuHistory?.setOnClickListener {
            popupWindow.dismiss()
            openHistory()
        }
        menuSetting?.setOnClickListener {
            popupWindow.dismiss()
            openSetting()
        }
        menuAbout?.setOnClickListener {
            popupWindow.dismiss()
            showAboutDialog()
        }
    }

    override fun onPause() {
        viewBinding.adView.pause()
        super.onPause()
        viewBinding.barcodeView.pause()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        viewBinding.adView.resume()
        viewBinding.barcodeView.resume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.adView.destroy()
    }

    // load ad
    private fun loadAd() {
        if (setting?.removeAds == true) {
            return
        }
        InterstitialAd.load(
            requireActivity(),
            getString(R.string.full_sceen_ad_unit_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                    toNextLevel()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    mInterstitialAd = null
                }
            })
    }

    private fun toNextLevel() {
        if (setting?.removeAds == true) {
            return
        }
        // Show the interstitial if it is ready. Otherwise, proceed to the next level
        // without ever showing it
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                @SuppressLint("MissingPermission")
                override fun onAdDismissedFullScreenContent() {
                    Timber.d("Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
//                    loadAd()
//                    mCameraSource?.start(viewBinding.cameraView.holder)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Timber.d("Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Timber.d("Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                }
            }
            mInterstitialAd?.show(requireActivity())
        } else {
            nextLevel()

            // in case you want to load a new ad
            requestNewInterstitial()
        }
    }

    private fun nextLevel() {

    }

    private fun requestNewInterstitial() {
        if (mInterstitialAd == null) {
            loadAd()
        }
    }

    private fun openFlashLight() {
        if (isTorchOn) {
            viewBinding.barcodeView.setTorchOn()
            isTorchOn = false
            viewBinding.btnFlash.setImageResource(R.drawable.icon_flash_off_new)
        } else {
            viewBinding.barcodeView.setTorchOff()
            isTorchOn = true
            viewBinding.btnFlash.setImageResource(R.drawable.ic_flash_qr_new)
        }
    }

    private fun swapCamera() {
        val settings: CameraSettings = viewBinding.barcodeView.getBarcodeView().getCameraSettings()
        if (viewBinding.barcodeView.getBarcodeView().isPreviewActive()) {
            viewBinding.barcodeView.pause()
        }
        //swap the id of the camera to be used
        if (settings.requestedCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            settings.requestedCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            settings.requestedCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        viewBinding.barcodeView.getBarcodeView().setCameraSettings(settings)
        viewBinding.barcodeView.resume()
    }

}
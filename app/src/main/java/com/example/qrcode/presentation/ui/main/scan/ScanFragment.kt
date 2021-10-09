package com.example.qrcode.presentation.ui.main.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.qrcode.R
import com.example.qrcode.common.*
import com.example.qrcode.common.utils.*
import com.example.qrcode.common.utils.DateConverter.fromDate
import com.example.qrcode.databinding.FragmentScanBinding
import com.example.qrcode.model.entity.History
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
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [ScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ScanFragment : BaseFragment<FragmentScanBinding, ScanViewModel>() {

    @Inject
    lateinit var permissionHelper: PermissionHelper
    private var mBarcodeDetector: BarcodeDetector? = null
    private var mCameraSource: CameraSource? = null
    private var shouldScan = false
    private var isFlashSupported: Boolean? = false
    private var isTorchOn = false
    private var camera: Camera? = null
    private var setting: SettingPreferences? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var isPause = false

    @SuppressLint("MissingPermission")
    private val permissionReq =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                initBarcodeDetector()
                mCameraSource?.start(viewBinding.cameraView.holder)
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

    private val galleryResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data = result.data
                if (data == null) {
                    showToast(R.string.scan_failed_message)
                    return@registerForActivityResult
                }
                val filePath = data.getStringExtra(GALLERY_RESULT)
                handleGalleryResult(filePath)
            }
        }

    private val callback = object : SurfaceHolder.Callback {
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            mCameraSource?.start(viewBinding.cameraView.holder)
        }

        @SuppressLint("MissingPermission")
        override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
            mCameraSource?.start(viewBinding.cameraView.holder)
            Timber.d("surfaceChanged")
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            mCameraSource?.stop()
            camera?.release()
        }
    }

    private val processorCallBack = object : Detector.Processor<Barcode> {
        override fun release() {}

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            if (!sharedViewModel.isEnableQRDetect()) {
                return
            }
            val barcodes = detections.detectedItems
            if (!shouldScan && barcodes.size() > 0) {
                activity?.runOnUiThread {
                    viewBinding.ivScan.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_text
                        ), PorterDuff.Mode.MULTIPLY
                    )
                    shouldScan = true
                    val qrCode = barcodes.valueAt(0).displayValue
                    handleSetting()
                    if (setting?.saveHistory == true) {
                        viewModel.insert(History(qrCode, fromDate(Date())))
                    }
                    // If you would like to resume scanning, call this method below:
                    showQRCodeConfirmDialog(qrCode)
                }

            }
        }
    }

    private fun handleGalleryResult(filePath: String?) {
        filePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val barcodes = mBarcodeDetector?.detect(frame)
            if (barcodes != null && barcodes.size() > 0) {
                barcodes.valueAt(0)?.displayValue?.let { qrCode ->
                    if (setting?.saveHistory == true) {
                        viewModel.insert(History(qrCode, fromDate(Date())))
                    }
                    showQRCodeConfirmDialog(qrCode)
                }
                sharedViewModel.setImagePath(null)
                return@let
            }
            sharedViewModel.setImagePath(null)
            showToast(R.string.scan_failed_message)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment ScanFragment.
         */
        @JvmStatic
        fun newInstance() = ScanFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_scan

    override fun initViewModel() {
        viewModel = getViewModel(ScanViewModel::class.java)
    }

    override fun initViews() {
        handlePermission()
        loadBanner()
        loadAd()
    }

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
        initBarcodeDetector()
    }

    //
    private fun initBarcodeDetector() {
        mBarcodeDetector =
            BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        mBarcodeDetector?.setProcessor(processorCallBack)
        mCameraSource = CameraSource.Builder(requireContext(), mBarcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setAutoFocusEnabled(true)
            .build()
        viewBinding.cameraView.holder.addCallback(callback)
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
                shouldScan = false
            }
            .show()
        qrConfirmDialog.setCancelable(false)
        qrConfirmDialog.setOnDismissListener {
            viewBinding.ivScan.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_icon
                ), PorterDuff.Mode.MULTIPLY
            )
        }
    }

    private fun handleQRCode(displayValue: String) {
        shouldScan = false
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

    private fun switchFlash() {
        val camManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = camManager.cameraIdList[0]
        val camChars = camManager.getCameraCharacteristics(cameraId)

        isFlashSupported = camChars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
        if (isFlashSupported == false) {
            showToast(R.string.flash_not_available)
            return
        }

        initCamera()
        camera?.let { cam ->
            try {
                val param = cam.parameters
                param.flashMode = if (!isTorchOn) Camera.Parameters.FLASH_MODE_TORCH
                else Camera.Parameters.FLASH_MODE_OFF
                cam.parameters = param
                isTorchOn = !isTorchOn
                if (isTorchOn) {
                    showToast(R.string.flash_switched_on)
                    return
                }
                showToast(R.string.flash_switched_off)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun initCamera() {
        if (camera == null) {
            camera = mCameraSource?.let { getCamera(it) }
        }
    }

    private fun getCamera(cameraSource: CameraSource): Camera? {
        val declaredFields = CameraSource::class.java.declaredFields

        for (field in declaredFields) {
            if (field.type === Camera::class.java) {
                field.isAccessible = true
                try {
                    return field.get(cameraSource) as Camera
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

                break
            }
        }
        return null
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

    private fun releaseCamera() {
        isTorchOn = false
        mCameraSource?.stop()
        camera?.release()
        camera = null
    }

    private fun showOptionMenu(view: View?) {
        // inflate the layout of the popup window
        val inflater = context?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
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
        releaseCamera()
        isPause = true
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        viewBinding.adView.resume()
        if (isPause) {
            isPause = false
            releaseCamera()
            mCameraSource?.start(viewBinding.cameraView.holder)
        }
    }

    override fun onStop() {
        super.onStop()
        isPause = false
    }

    override fun onDestroy() {
        viewBinding.adView.destroy()
        super.onDestroy()
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
                    mCameraSource?.start(viewBinding.cameraView.holder)
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
}
package com.store.qrcode.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.store.qrcode.R
import com.store.qrcode.databinding.ActivitySplashBinding
import com.store.qrcode.presentation.base.BaseActivity
import com.store.qrcode.presentation.ui.main.MainActivity
import com.github.ybq.android.spinkit.style.ThreeBounce
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    private val SPLASH_DISPLAY_LENGTH: Long = 1000
    var mDrawable: ThreeBounce? = null
    override fun initViewModel() {
        viewModel = getViewModel(SplashViewModel::class.java)
    }

    override val layoutId: Int
        get() = R.layout.activity_splash

    override fun initViews(savedInstanceState: Bundle?) {
        initAnimation()
        initDrawable()
        openMainScreen()
    }

    private fun openMainScreen() {
        /* New Handler to start the Menu-Activity
       * and close this Splash-Screen after some seconds.*/
        Handler(Looper.getMainLooper()).postDelayed({
            viewBinding.barView.clearAnimation()
            /* Create an Intent that will start the Menu-Activity. */
            val mainIntent = Intent(this, MainActivity::class.java)
            this.startActivity(mainIntent)
            this.finish()
        }, SPLASH_DISPLAY_LENGTH)
    }

    private fun initDrawable() {
        //Textview
        mDrawable = ThreeBounce()
        mDrawable?.setBounds(0, 0, 100, 100)
        //        mDrawable?.color = Color.BLACK
        mDrawable?.color = ContextCompat.getColor(this, R.color.purple_500)
        viewBinding.tvLoading.setCompoundDrawables(null, null, mDrawable, null)
    }

    private fun initAnimation() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.reversedscan)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                viewBinding.barView.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        viewBinding.barView.visibility = View.VISIBLE
        viewBinding.barView.startAnimation(animation)
    }

    override fun initObservers() {
    }

    override fun initListeners() {
    }

    override fun onClick(v: View?) {
    }

    override fun onResume() {
        super.onResume()
        mDrawable?.start()
    }

    override fun onStop() {
        super.onStop()
        mDrawable?.stop()
    }
}
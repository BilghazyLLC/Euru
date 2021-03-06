package io.euruapp.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.util.ConstantsUtils

/**
 * Splash screen activity
 */
class SplashActivity(override val layoutId: Int = R.layout.activity_splash) : BaseActivity() {


    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        Handler().postDelayed({
            ConstantsUtils.intentToActivity(
                this@SplashActivity,
                if (database.isLoggedIn) {
                    if (database.isPending) PendingRegistrationActivity::class.java else HomeActivity::class.java
                } else {
                    OnboardingActivity::class.java
                }
            )


            finish()
        }, 1000)
    }
}
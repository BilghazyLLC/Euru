package io.euruapp.view

import android.content.Intent
import android.os.Bundle
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.util.ConstantsUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen activity
 */
class SplashActivity(override val layoutId: Int = R.layout.activity_splash) : BaseActivity() {


    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        /*Handler().postDelayed({
            ConstantsUtils.intentToActivity(
                this@SplashActivity,
                when {
                    database.isLoggedIn -> {
                        if (database.isPending) PendingRegistrationActivity::class.java
                        else HomeActivity::class.java
                    }

                    else -> OnboardingActivity::class.java
                }
            )


            finish()
        }, 1000)*/

        ioScope.launch {
            delay(1000)
            uiScope.launch {
                ConstantsUtils.intentToActivity(
                    this@SplashActivity,
                    when {
                        database.isLoggedIn -> {
                            if (database.isPending) PendingRegistrationActivity::class.java
                            else HomeActivity::class.java
                        }

                        else -> OnboardingActivity::class.java
                    }
                )
                finish()
            }
        }
    }
}
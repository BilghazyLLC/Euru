package io.euruapp.core

import android.app.Application

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

import io.euruapp.core.injection.AppComponent
import io.euruapp.core.injection.ContextModule
import io.euruapp.core.injection.DaggerAppComponent
import io.euruapp.core.location.GPSTracker
import io.euruapp.model.User
import io.euruapp.modules.roomModule
import io.euruapp.util.ConstantsUtils
import io.euruapp.viewmodel.UserDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * [Application] class implementation for the Euru Android Client
 */
class EuruApplication : Application() {

    lateinit var component: AppComponent
    private lateinit var messaging: FirebaseMessaging
    private lateinit var database: UserDatabase

    override fun onCreate() {
        super.onCreate()

        //Firebase Init
        FirebaseApp.initializeApp(this)

        //Init Dagger
        component = DaggerAppComponent.builder()
            .contextModule(ContextModule(this))
            .build()
        component.inject(this)

        startKoin {
            androidContext(this@EuruApplication)

            modules(roomModule)
        }

        //Get messaging instance
        messaging = FirebaseMessaging.getInstance()

        //Register topic based on user's type
        database = UserDatabase(applicationContext)
        if (database.isLoggedIn) {
            when (database.user.type) {
                User.TYPE_BUSINESS -> {
                    messaging.subscribeToTopic(ConstantsUtils.TOPIC_BUSINESS)
                        .addOnCompleteListener {
                            ConstantsUtils.logResult("Registered on business topics successfully")
                        }.addOnFailureListener {
                            ConstantsUtils.logResult(it.localizedMessage)
                        }
                }

                else -> {
                    messaging.subscribeToTopic(ConstantsUtils.TOPIC_INDIVIDUAL_CUSTOMER)
                        .addOnCompleteListener {
                            ConstantsUtils.logResult("Registered on customer topics successfully")
                        }.addOnFailureListener {
                            ConstantsUtils.logResult(it.localizedMessage)
                        }
                }

            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        val tracker = GPSTracker(applicationContext as BaseActivity)
        tracker.stopUsingGPS()

        if (database.isLoggedIn) {
            messaging.unsubscribeFromTopic(
                if (database.user.type == User.TYPE_BUSINESS) ConstantsUtils.TOPIC_BUSINESS
                else ConstantsUtils.TOPIC_INDIVIDUAL_CUSTOMER
            ).addOnCompleteListener {
                ConstantsUtils.logResult("Unsubscribed from topic successfully")
            }.addOnFailureListener {
                ConstantsUtils.logResult(it.localizedMessage)
            }
        }
    }
}

package io.euruapp.core

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.euruapp.core.location.GPSTracker
import io.euruapp.model.User
import io.euruapp.room.RoomAppDatabase
import io.euruapp.util.ConstantsUtils
import io.euruapp.viewmodel.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    //Database instance for Users
    @Inject
    lateinit var database: UserDatabase

    val roomDb: RoomAppDatabase by inject { parametersOf(application as EuruApplication) }

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: StorageReference

    lateinit var tracker: GPSTracker
    lateinit var loggedInUser: User

    private val job = Job()
    val ioScope = CoroutineScope(Dispatchers.IO + job)
    val uiScope = CoroutineScope(Dispatchers.Main + job)

    val isNetworkAvailable: Boolean
        get() {
            val manager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var networkInfo: NetworkInfo? = null
            networkInfo = manager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    //Override by children
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        //init dagger
        (application as EuruApplication).component.inject(this)

        //Init firebase
        auth = FirebaseAuth.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = settings
        storage = FirebaseStorage.getInstance().reference

        //init GPS tracker
        tracker = GPSTracker(this)

        // Know which activity we are on
        ConstantsUtils.logResult("You are on the : ${this@BaseActivity::class.java.canonicalName}")

        // Observe user on every activity
        if (database.isLoggedIn) {
            roomDb.dao().getCurrentUser(database.user.key).observeForever {
                if (it != null) {
                    ConstantsUtils.logResult("Current user from Room Database: $it")
                    loggedInUser = it
                }
            }
        }

        //Always at the bottom
        onViewCreated(intent, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        job.cancel()
    }

    protected abstract fun onViewCreated(intent: Intent, instanceState: Bundle?)
}

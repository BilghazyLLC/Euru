package io.euruapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.util.bindView
import io.codelabs.widget.BaselineGridTextView
import io.codelabs.widget.ForegroundImageView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Business
import io.euruapp.model.User
import io.euruapp.util.ConstantsUtils
import io.euruapp.util.glide.GlideApp
import io.euruapp.viewmodel.UserDatabase
import pub.devrel.easypermissions.EasyPermissions
import java.util.HashMap

class HomeActivity(override val layoutId: Int = R.layout.activity_home) : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private val drawer: DrawerLayout by bindView(R.id.drawer)
    private val navView: NavigationView by bindView(R.id.nav_view)
    private val toolbar: Toolbar by bindView(R.id.toolbar)

    //Header
    private lateinit var avatar: ForegroundImageView
    private lateinit var logo: ForegroundImageView
    private lateinit var username: BaselineGridTextView
    private lateinit var type: BaselineGridTextView

    private var manager: FragmentManager? = null
    private var hasLocationPermission: Boolean = false

    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        //Send registration token to the server
        sendRegistration()

        println(database.user.toString())

        setSupportActionBar(toolbar)
       


        hasLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        val drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawer.addDrawerListener(drawerToggle)

        val headerView = navView.getHeaderView(0)
        navView!!.setNavigationItemSelectedListener(this)
        manager = supportFragmentManager

        setupUser(headerView)
        val menuItem = navView!!.menu.findItem(R.id.menu_home)
        if (database.user.type == User.TYPE_BUSINESS) {
            menuItem.title = "Dashboard"
            menuItem.setIcon(R.drawable.ic_dashboard)
            manager!!.beginTransaction().replace(R.id.frame, DashboardFragment.newInstance()).commit()
        } else {
            manager!!.beginTransaction().replace(R.id.frame, HomeFragment.newInstance()).commit()
        }
    }

    private fun sendRegistration() {
        //Send registration to server
        val user = UserDatabase(this).user
        if (user != null /*&& user.getType() == User.TYPE_BUSINESS*/) {

            val fb = FirebaseFirestore.getInstance()
            val s = FirebaseInstanceId.getInstance().token

            //Get the business model of the current service provider
            if (user.type == User.TYPE_BUSINESS) {
                fb.collection(ConstantsUtils.COLLECTION_BUSINESS)
                    .whereEqualTo("userUID", user.key)
                    .limit(1)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val result = task.result
                            if (result == null || result.isEmpty) return@addOnCompleteListener

                            //Get the document from the result
                            val snapshot = result.documents[0]
                            if (snapshot.exists()) {
                                val business = snapshot.toObject(Business::class.java) ?: return@addOnCompleteListener
                                //Create hash map
                                val hashMap = HashMap<Any, Any?>(0)
                                hashMap["key"] = s
                                hashMap["type"] = user.type
                                hashMap["uid"] = user.key
                                hashMap["name"] = user.name
                                hashMap["timestamp"] = System.currentTimeMillis().toString()
                                hashMap["service"] = business.category
                                hashMap["address"] = GeoPoint(tracker.latitude, tracker.longitude)

                                //Push data to database
                                FirebaseFirestore.getInstance().collection(ConstantsUtils.COLLECTION_TOKENS)
                                    .document(user.key)
                                    .set(hashMap)
                                    .addOnCompleteListener { task1 ->
                                        ConstantsUtils.logResult(s)
                                        ConstantsUtils.logResult(if (task1.isSuccessful) "Device Token uploaded" else "Failed to upload device token")
                                    }.addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }
                            }

                        }
                    }
                    .addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }
            } else {

                //Get the users with said key
                fb.collection(ConstantsUtils.COLLECTION_USERS)
                    .whereEqualTo("key", user.key)
                    .limit(1)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val result = task.result
                            if (result == null || result.isEmpty) return@addOnCompleteListener

                            //Get the document from the result
                            val snapshot = result.documents[0]
                            if (snapshot.exists()) {
                                //Create hash map
                                val hashMap = HashMap<Any, Any?>(0)
                                hashMap["key"] = s
                                hashMap["type"] = user.type
                                hashMap["uid"] = user.key
                                hashMap["timestamp"] = System.currentTimeMillis().toString()
                                hashMap["address"] = GeoPoint(tracker.latitude, tracker.longitude)

                                //Push data to database
                                FirebaseFirestore.getInstance().collection(ConstantsUtils.COLLECTION_TOKENS)
                                    .document(user.key)
                                    .set(hashMap)
                                    .addOnCompleteListener { task1 ->
                                        ConstantsUtils.logResult(s)
                                        ConstantsUtils.logResult(if (task1.isSuccessful) "Device Token uploaded" else "Failed to upload device token")
                                    }.addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }
                            }
                        }
                    }
                    .addOnFailureListener { e -> ConstantsUtils.logResult(e.localizedMessage) }
            }
        }
    }

    override fun onEnterAnimationComplete() {
        if (!hasLocationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EasyPermissions.requestPermissions(
                this@HomeActivity,
                "Allow location permission",
                RC_LOC_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else if (tracker.canGetLocation()) {
            println(tracker.location)
        }
    }

    private fun setupUser(headerView: View) {
        //Init view
        logo = headerView.findViewById(R.id.header_logo)
        avatar = headerView.findViewById(R.id.header_avatar)
        username = headerView.findViewById(R.id.header_username)
        type = headerView.findViewById(R.id.header_user_type)

        //Get login state
        if (database.isLoggedIn) {
            val user = database.user
            username.text = user?.getName() ?: "Enter username"
            var userType = User.TYPE_BUSINESS
            when (user.type) {
                User.TYPE_INDIVIDUAL -> userType = "Individual"
                User.TYPE_CUSTOMER -> userType = "Customer"
                User.TYPE_BUSINESS -> userType = "Business"
            }
            type.text = String.format("User Type: %s", userType)

            //Load logo
            GlideApp.with(this)
                .load(R.drawable.app_logo)
                .circleCrop()
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .priority(Priority.IMMEDIATE)
                .into(logo)

            //Load user profile,if any
            GlideApp.with(this)
                .load(if (user.profile != null && !TextUtils.isEmpty(user.profile)) user.profile else R.drawable.ic_player)
                .circleCrop()
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .fallback(R.drawable.ic_player)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(avatar)

        } else {
            headerView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_search)
            ConstantsUtils.intentToActivity(this@HomeActivity, SearchActivity::class.java)
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.menu_home -> {
                val fragment =
                    if (database.user.getType() == User.TYPE_BUSINESS) DashboardFragment.newInstance() else HomeFragment.newInstance()
                manager!!.beginTransaction().replace(R.id.frame, fragment).commit()
            }
            R.id.menu_profile -> {
                val fm =
                    if (database.user.getType() == User.TYPE_BUSINESS) ProfileFragment.newInstance() else UserFragment.newInstance()
                manager!!.beginTransaction().replace(R.id.frame, fm).commit()
            }
            R.id.menu_settings -> {
            }
            R.id.menu_about -> ConstantsUtils.intentToActivity(this@HomeActivity, OnboardingActivity::class.java)
            R.id.menu_logout -> AlertDialog.Builder(this)
                .setMessage("Do you wish to logout of " + getString(R.string.app_name) + "?")
                .setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    database.logout()
                    ConstantsUtils.intentTo(this@HomeActivity, AuthActivity::class.java)
                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create().show()
            R.id.menu_invite -> {
                val intentBuilder = ShareCompat.IntentBuilder.from(this@HomeActivity)
                    .setText(getString(R.string.app_name))
                    .setChooserTitle("Invite friends through...")
                    .setType("text/*")
                    .setSubject("Share Euru with your friends and get a 12% discount on any transaction")
                    .setStream(Uri.parse("https://play.google.com/store/apps/details?id=io.euru"))

                if (intentBuilder.intent.resolveActivity(packageManager) != null)
                    intentBuilder.startChooser()
                else
                    ConstantsUtils.showToast(this, "Sorry. You cannot share at this time. Please try again later")
            }

            R.id.menu_ratings -> {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.euruapp"))
                if (i.resolveActivity(packageManager) != null)
                    startActivity(Intent.createChooser(i, null))
                else
                    ConstantsUtils.showToast(this, "Sorry. You cannot rate at this time. Please try again later")
            }
        }// TODO: 10/20/2018 Replace fragment here

        //Close drawer
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        hasLocationPermission = requestCode == RC_LOC_PERMISSION
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val RC_LOC_PERMISSION = 2
    }
}

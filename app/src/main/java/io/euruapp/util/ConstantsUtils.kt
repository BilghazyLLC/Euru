package io.euruapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringDef
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.euruapp.R
import io.euruapp.core.BaseActivity
import io.euruapp.model.Category
import io.euruapp.model.RequestModel
import io.euruapp.model.User
import io.euruapp.view.HomeActivity
import io.euruapp.view.ProviderLoginActivity
import io.euruapp.viewmodel.UserDatabase
import java.util.*

/**
 * Contains constants that will be used in the [io.euruapp.core.EuruApplication]
 */
object ConstantsUtils {
    //Categories for services
    @ServiceCategory
    val categories = mutableListOf<Category>(

        Category(
            Category.SEPTIC_WASTE,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/service_uploads%2FWhatsApp%20Image%202018-11-05%20at%2012.14.50%20AM.jpeg?alt=media&token=f89a23e2-f17a-44f5-86a6-12274512f95b"
        ),
        Category(
            Category.AUTO_SERVICE,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/auto.png?alt=media&token=44e4d5f3-17ab-4e67-9c2a-ea74c263c4be" ),
        Category(
            Category.AIR_CONDITION_REPAIRS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/ac.jpg?alt=media&token=1a07eb4f-b7df-483d-b34e-00e63a955c35"
        ),
        Category(
            Category.BEAUTY,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/beauty.jpg?alt=media&token=a8e54cf5-0c7f-4010-9274-f8454388e505"
        ),
        Category(
            Category.BARBER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fbarber%202.jpg?alt=media&token=0aba1e10-7694-492d-9b53-678922fc3ffd"
        ),
        Category(
            Category.BAKER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fbaker.jpg?alt=media&token=7741973f-6440-4995-88ec-fc6f3b3eb513"
        ),
        Category(
            Category.CAR_RENTALS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/crentals.png?alt=media&token=74c7e72e-f49f-4e66-bd37-ef7bece3d27d"      ),

        Category(
            Category.CAR_WASH,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fcar%20wash.jpg?alt=media&token=0d93abce-b8c7-481a-8bbc-0a4e843f09f5"
        ),
        Category(
            Category.CAR_SPRAYER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fcar%20sprayer.jpg?alt=media&token=84736051-2d18-4a19-9839-60edd4725b0c"
        ),
        Category(
            Category.CATERER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fcaterer.jpg?alt=media&token=cfb962e2-8077-494f-af0c-1d40510cdd52"
        ),
        Category(
            Category.CARPENTER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fcarpenter.jpg?alt=media&token=24cba1a2-f201-4d69-a0fb-c282c052b11b"
        ),

        Category(
            Category.CLEANERS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/cleaners.jpg?alt=media&token=95f36a02-4817-4d17-b35d-53d9f21203ad"  ),
        Category(
            Category.DJ,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2FDJ.jpg?alt=media&token=14dfa725-79a6-4f8c-8808-8f03ef2a4a19"
        ),
        Category(
            Category.TAXI_DRIVERS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Ftaxidriver.jpg?alt=media&token=2a755f4f-f019-4d1d-b5e1-104a49ca3281"

        ),

        Category(
            Category.ELECTRICIAN,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Felectrician.jpg?alt=media&token=32d9a6e3-7278-4cec-a967-903cb635ec7f"
        ),
        Category(
            Category.HOTELS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/service_uploads%2FWhatsApp%20Image%202018-11-05%20at%2012.14.56%20AM.jpeg?alt=media&token=89eb8b88-0ddd-49a8-863e-2bd910cda3f4"
        ),
        Category(
            Category.EVENT_PLANNING,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/eventplaning.png?alt=media&token=10925e74-92b4-4ba6-a724-a61590cf4b35"
        ),
        Category(
            Category.PIZZA,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/service_uploads%2Fpizza.png?alt=media&token=9e0f52ee-056d-4e80-9afc-2179deaad328"
        ),
        Category(
            Category.PLUMBERS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fmason.jpg?alt=media&token=7f677392-958c-4893-8606-50dc84816a89"
        ),
        Category(
            Category.PAINTER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/painter.jpg?alt=media&token=384e0f8f-d966-4720-80dd-e3abb37b43b5"
        ),
        Category(
            Category.RESTAURANTS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Frestaurant.jpg?alt=media&token=7ce89aec-7e04-4898-b664-424ddcf36b44"
        ),
        Category(
            Category.SHOE_MAKER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fshoemaker.jpg?alt=media&token=067301f4-e0c3-41d9-be82-9aae7a265235"
        ),
        Category(
            Category.SEAMSTRESS,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fseamstress.jpg?alt=media&token=688775e2-39df-4da7-873d-2cc848a1b5fe"
        ),
        Category(
            Category.TEACHER,
            "https://firebasestorage.googleapis.com/v0/b/bilghazyllc.appspot.com/o/euru_latest_uploads%2Fteacher.jpg?alt=media&token=e19e63e4-6a1b-4dc4-9fa6-b093f3f2ac70"
        )


    )

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(NEW_JOBS, PENDING_JOBS, COMPLETED_JOBS)
    annotation class ProviderJobType

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class ServiceCategory

    const val DATABASE_NAME = "euru_users.db"
    const val COLLECTION_BUSINESS = "euru_business"
    const val COLLECTION_USERS = "euru_users"
    const val COLLECTION_PENDING_REGISTRATION = "euru_pending_approvals"
    const val COLLECTION_REQUESTS = "test_requests"
    const val COLLECTION_JOBS = "euru_jobs"
    const val COLLECTION_TOKENS = "euru_tokens"
    const val COMPLETED_JOBS = "completed"
    private const val COLLECTION_ERRORS = "euru_errors"
    const val PENDING_JOBS = "pending"
    const val NEW_JOBS = "new"
    const val TOPIC_INDIVIDUAL_CUSTOMER = "customer_topics"
    const val TOPIC_BUSINESS = "business_topics"
    const val DEFAULT_LAT = 5.53165340423584
    const val DEFAULT_LNG = -0.2589235007762909


    @JvmStatic
    fun showToast(context: Context, msg: Any?, isLong: Boolean = false) {
        //Toast a message to the screen
        Toast.makeText(context, msg.toString(), if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun logResult(msg: Any?, extraName: String = " "): Unit {
        //Log message to the console
        println("Euru Debugger: " + extraName + ":: " + msg.toString())
    }

    @JvmStatic
    fun logResult(msg: Any?): Unit {
        //Log message to the console
        println("Euru Debugger: " + msg.toString())
    }

    /*fun intentTo(host: Activity, target: Class<out BaseActivity>) {
        val intent = Intent(host, target)
        host.startActivity(intent)
        host.overridePendingTransition(R.anim.post_story_enter, R.anim.post_story_exit)
    }*/

    fun intentTo(host: Activity, target: Class<out BaseActivity>, b: Bundle? = null) {
        val intent = Intent(host, target)
        if (b != null)intent.putExtras(b)
        host.startActivity(intent)
        host.overridePendingTransition(R.anim.post_story_enter, R.anim.post_story_exit)
    }

    fun intentTo(host: Activity, target: Class<out BaseActivity>, rc: Int, b: Bundle) {
        val intent = Intent(host, target)
        intent.putExtras(b)
        host.startActivityForResult(intent, rc)
        host.overridePendingTransition(R.anim.post_story_enter, R.anim.post_story_exit)
    }

    fun intentToActivity(host: BaseActivity, target: Class<out Activity>) {
        val intent = Intent(host, target)
        host.startActivity(intent)
        host.overridePendingTransition(R.anim.post_story_enter, R.anim.post_story_exit)
    }

    fun uploadImage(uri: Uri, listener: OnCompleteListener<Uri>) {
        val reference = FirebaseStorage.getInstance().reference
        reference.child(System.currentTimeMillis().toString() + ".jpeg").putFile(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    result?.storage?.downloadUrl?.addOnCompleteListener(listener)
                }
            }

    }

    fun validateField(editText: EditText): Boolean {
        val s = editText.text.toString()
        return !TextUtils.isEmpty(s)
    }

    fun sendRequest(model: RequestModel?, service: String, listener: OnCompleteListener<Void>) {
        if (model == null) return

        val firestore = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val document = firestore.collection(COLLECTION_REQUESTS)
            .document(model.userKey).collection(dayOfWeek.toString()).document()

        //Set document id
        model.setDataKey(document.id)
        model.category = service

        //Add listener
        document.set(model).addOnCompleteListener(listener)
    }

    fun showUserSelectionDialog(context: Activity, database: UserDatabase) {
        val options = arrayOf<CharSequence>("Customer", "Service Provider")
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("Continue as Euru...")
        dialog.setCancelable(true)
        val alertDialog = dialog.setItems(options) { dialog1, which ->
            dialog1.dismiss()
            val user = database.user
            when (which) {
                0 -> {
                    user.setType(User.TYPE_CUSTOMER)
                    database.user = user
                }
                1 -> {
                    user.setType(User.TYPE_BUSINESS)
                    database.user = user
                }
            }
            intentTo(
                context,
                if (user.type == User.TYPE_BUSINESS) ProviderLoginActivity::class.java else HomeActivity::class.java
            )
            context.finish()
        }.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

    }

    fun sendErrorMessage(firestore: FirebaseFirestore, database: UserDatabase, e: Exception) {
        firestore.collection(COLLECTION_ERRORS).document(database.user.key)
            .set(
                mapOf(
                    "reason" to e.message,
                    "device" to Build.DEVICE,
                    "timestamp" to Date(System.currentTimeMillis())
                )
            ).addOnCompleteListener {
                logResult("Error code sent")
            }
    }

}

package io.euruapp.core

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import io.euruapp.viewmodel.UserDatabase
import javax.inject.Inject

abstract class BaseFragment : Fragment() {
    @Inject
    lateinit var database: UserDatabase

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: StorageReference

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        (requireActivity().application as EuruApplication).component.inject(this)

        //Init firebase
        auth = (requireActivity() as BaseActivity).auth
        firestore = (requireActivity() as BaseActivity).firestore
        storage = (requireActivity() as BaseActivity).storage

    }
}

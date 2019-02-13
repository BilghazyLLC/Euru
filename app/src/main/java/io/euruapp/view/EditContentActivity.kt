package io.euruapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.codelabs.util.bindView
import io.euruapp.R
import io.euruapp.core.BaseActivity
import java.util.*

class EditContentActivity(override val layoutId: Int = R.layout.activity_edit_content) : BaseActivity() {
    private val saveBtn: Button by bindView(R.id.save_data)
    private val contentData: TextInputEditText by bindView(R.id.edit_content_field)
    private val contentDataContainer: TextInputLayout by bindView(R.id.edit_content_field_container)

    private val id = 0
    override fun onViewCreated(intent: Intent, instanceState: Bundle?) {
        if (intent.hasExtra(EXTRA_CONTENT_NAME)) {
            val hint = intent.getStringExtra(EXTRA_CONTENT_NAME)
            contentDataContainer!!.hint = hint

            contentData!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    saveBtn!!.isEnabled = !TextUtils.isEmpty(s) && s.length > 4
                }

                override fun afterTextChanged(s: Editable) {

                }
            })
        }

        saveBtn.setOnClickListener { save() }
    }

    internal fun save() {
        val data = Intent()
        data.putExtra(BusinessActivity.EXTRA_DATA, Objects.requireNonNull<Editable>(contentData!!.text).toString())
        data.putExtra(EXTRA_CONTENT_ID, id)
        setResult(Activity.RESULT_OK, data)
        finishAfterTransition()
    }

    companion object {
        val EXTRA_CONTENT_NAME = "EXTRA_CONTENT_NAME"
        val EXTRA_CONTENT_ID = "EXTRA_CONTENT_ID"
    }
}

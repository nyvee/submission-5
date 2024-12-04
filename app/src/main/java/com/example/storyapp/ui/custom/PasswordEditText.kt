package com.example.storyapp.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.storyapp.R

class PasswordEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (id == R.id.ed_register_password || id == R.id.ed_login_password) {
                    if (s != null && s.length < 8) {
                        error = context.getString(R.string.password_too_short)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
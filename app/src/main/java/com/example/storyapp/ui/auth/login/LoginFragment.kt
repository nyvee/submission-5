package com.example.storyapp.ui.auth.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.storyapp.R
import com.example.storyapp.ui.auth.AuthViewModel
import com.example.storyapp.ui.auth.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var wrongCredentialsTextView: TextView
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(requireContext())).get(AuthViewModel::class.java)

        val loginButton: Button = view.findViewById(R.id.loginButton)
        val loginProgressBar: ProgressBar = view.findViewById(R.id.loginProgressBar)
        val goToRegisterButton: Button = view.findViewById(R.id.goToRegisterButton)
        emailEditText = view.findViewById(R.id.ed_login_email)
        passwordEditText = view.findViewById(R.id.ed_login_password)
        passwordVisibilityToggle = view.findViewById(R.id.passwordVisibilityToggle)
        wrongCredentialsTextView = view.findViewById(R.id.wrongCredentialsTextView)

        (activity as AppCompatActivity).supportActionBar?.hide()

        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginButton.isEnabled = false
            loginButton.text = ""
            loginButton.setBackgroundColor(resources.getColor(R.color.gray))
            context?.let { it1 -> ContextCompat.getColor(it1, R.color.white) }?.let { it2 ->
                loginProgressBar.indeterminateDrawable.setColorFilter(
                    it2, android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            loginProgressBar.visibility = View.VISIBLE


            lifecycleScope.launch {
                try {
                    viewModel.login(email, password) { isSuccess, message ->
                        loginButton.isEnabled = true
                        loginButton.text = "Login"
                        loginButton.setBackgroundColor(resources.getColor(R.color.black))
                        loginProgressBar.visibility = View.GONE
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                            val navOptions = NavOptions.Builder()
                                .setEnterAnim(R.anim.slide_in_right)
                                .setExitAnim(R.anim.slide_out_left)
                                .setPopEnterAnim(R.anim.slide_in_left)
                                .setPopExitAnim(R.anim.slide_out_right)
                                .build()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment, null, navOptions)
                        } else {
                            wrongCredentialsTextView.text = message
                        }
                    }
                } catch (e: Exception) {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    loginButton.setBackgroundColor(resources.getColor(R.color.gray))
                    loginProgressBar.visibility = View.GONE
                    wrongCredentialsTextView.text = e.message
                }
            }
        }

        goToRegisterButton.setOnClickListener {
            goToRegisterButton.isEnabled = false
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment, null, navOptions)
            goToRegisterButton.isEnabled = true
        }

        viewModel.loginState.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        viewModel.wrongCredentials.observe(viewLifecycleOwner) { wrongCredentials ->
            wrongCredentialsTextView.text = wrongCredentials
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
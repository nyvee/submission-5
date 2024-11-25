package com.example.storyapp.ui.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.MainActivity
import com.example.storyapp.R
import com.example.storyapp.ui.auth.AuthViewModel
import com.example.storyapp.ui.auth.AuthViewModelFactory
import com.example.storyapp.ui.auth.register.RegisterFragment
import com.example.storyapp.ui.home.HomeFragment
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var wrongCredentialsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(requireContext())).get(AuthViewModel::class.java)

        val loginButton: Button = view.findViewById(R.id.loginButton)
        val goToRegisterButton: Button = view.findViewById(R.id.goToRegisterButton)
        emailEditText = view.findViewById(R.id.ed_login_email)
        passwordEditText = view.findViewById(R.id.ed_login_password)
        wrongCredentialsTextView = view.findViewById(R.id.wrongCredentialsTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            lifecycleScope.launch {
                try {
                    viewModel.login(email, password) { isSuccess, message ->
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                            (activity as MainActivity).navigateToFragment(HomeFragment())
                        } else {
                            wrongCredentialsTextView.text = message
                        }
                    }
                } catch (e: Exception) {
                    wrongCredentialsTextView.text = e.message
                }
            }
        }

        goToRegisterButton.setOnClickListener {
            (activity as MainActivity).navigateToFragment(RegisterFragment())
        }

        viewModel.loginState.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                (activity as MainActivity).navigateToFragment(HomeFragment())
            }
        }

        viewModel.wrongCredentials.observe(viewLifecycleOwner) { wrongCredentials ->
            wrongCredentialsTextView.text = wrongCredentials
        }
    }
}
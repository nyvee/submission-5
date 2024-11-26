package com.example.storyapp.ui.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.storyapp.R
import com.example.storyapp.ui.auth.AuthViewModel
import com.example.storyapp.ui.auth.AuthViewModelFactory
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
        val loginProgressBar: ProgressBar = view.findViewById(R.id.loginProgressBar)
        val registerProgressBar: ProgressBar = view.findViewById(R.id.registerProgressBar)
        val goToRegisterButton: Button = view.findViewById(R.id.goToRegisterButton)
        emailEditText = view.findViewById(R.id.ed_login_email)
        passwordEditText = view.findViewById(R.id.ed_login_password)
        wrongCredentialsTextView = view.findViewById(R.id.wrongCredentialsTextView)

        (activity as AppCompatActivity).supportActionBar?.hide()

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginButton.isEnabled = false
            loginProgressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    viewModel.login(email, password) { isSuccess, message ->
                        loginButton.isEnabled = true
                        loginProgressBar.visibility = View.GONE
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            wrongCredentialsTextView.text = message
                        }
                    }
                } catch (e: Exception) {
                    loginButton.isEnabled = true
                    loginProgressBar.visibility = View.GONE
                    wrongCredentialsTextView.text = e.message
                }
            }
        }

        goToRegisterButton.setOnClickListener {
            goToRegisterButton.isEnabled = false
            registerProgressBar.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            goToRegisterButton.isEnabled = true
            registerProgressBar.visibility = View.GONE
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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
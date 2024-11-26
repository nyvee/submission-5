package com.example.storyapp.ui.auth.register

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

class RegisterFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registeredEmailTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(requireContext())).get(AuthViewModel::class.java)
        val registerButton: Button = view.findViewById(R.id.registerButton)
        val registerProgressBar: ProgressBar = view.findViewById(R.id.registerProgressBar)
        val loginProgressBar: ProgressBar = view.findViewById(R.id.loginProgressBar)
        val goToLoginButton: Button = view.findViewById(R.id.goToLoginButton)
        nameEditText = view.findViewById(R.id.ed_register_name)
        emailEditText = view.findViewById(R.id.ed_register_email)
        passwordEditText = view.findViewById(R.id.ed_register_password)
        registeredEmailTextView = view.findViewById(R.id.registeredEmailTextView)

        // Hide the toolbar
        (activity as AppCompatActivity).supportActionBar?.hide()

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerButton.isEnabled = false
            registerProgressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    viewModel.register(name, email, password) { isSuccess, message ->
                        registerButton.isEnabled = true
                        registerProgressBar.visibility = View.GONE
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        } else {
                            registeredEmailTextView.text = message
                        }
                    }
                } catch (e: Exception) {
                    registerButton.isEnabled = true
                    registerProgressBar.visibility = View.GONE
                    registeredEmailTextView.text = e.message
                }
            }
        }

        goToLoginButton.setOnClickListener {
            goToLoginButton.isEnabled = false
            loginProgressBar.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            goToLoginButton.isEnabled = true
            loginProgressBar.visibility = View.GONE
        }

        viewModel.registrationState.observe(viewLifecycleOwner) { isRegistered ->
            if (isRegistered) {
                registeredEmailTextView.text = "Registered Email: ${viewModel.registeredEmail.value}"
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the toolbar again
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
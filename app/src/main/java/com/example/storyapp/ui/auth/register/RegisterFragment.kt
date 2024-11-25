package com.example.storyapp.ui.auth.register

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
import com.example.storyapp.ui.auth.login.LoginFragment
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
        nameEditText = view.findViewById(R.id.ed_register_name)
        emailEditText = view.findViewById(R.id.ed_register_email)
        passwordEditText = view.findViewById(R.id.ed_register_password)
        registeredEmailTextView = view.findViewById(R.id.registeredEmailTextView)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            lifecycleScope.launch {
                try {
                    viewModel.register(name, email, password) { isSuccess, message ->
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                            (activity as MainActivity).navigateToFragment(LoginFragment())
                        } else {
                            registeredEmailTextView.text = message
                        }
                    }
                } catch (e: Exception) {
                    registeredEmailTextView.text = e.message
                }
            }
        }

        viewModel.registrationState.observe(viewLifecycleOwner) { isRegistered ->
            if (isRegistered) {
                registeredEmailTextView.text = "Registered Email: ${viewModel.registeredEmail.value}"
                (activity as MainActivity).navigateToFragment(LoginFragment())
            }
        }
    }
}
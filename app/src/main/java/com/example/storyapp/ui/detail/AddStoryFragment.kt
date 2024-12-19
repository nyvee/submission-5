package com.example.storyapp.ui.detail

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.data.remote.retrofit.RetrofitInstance
import com.example.storyapp.databinding.FragmentAddStoryBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File as JavaFile
import android.content.ContentResolver
import android.database.Cursor
import android.provider.OpenableColumns
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.example.storyapp.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

class AddStoryFragment : Fragment() {

    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val checkbox get() = binding.checkbox

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mainActivity = activity as AppCompatActivity
        mainActivity.supportActionBar?.apply {
            title = getString(R.string.add_story_title)
            setDisplayHomeAsUpEnabled(true)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            if (navController != null && !navController.popBackStack()) {
                requireActivity().onBackPressed()
            }
        }

        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        binding.cameraButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                Toast.makeText(requireContext(), "Not including current location", Toast.LENGTH_SHORT).show()
                currentLatitude = null
                currentLongitude = null
            }
        }

        binding.buttonAdd.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
            val token = sharedPref.getString("token", "")
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val description = binding.edAddDescription.text.toString()
            val imageUri = binding.previewImageView.tag as? Uri
            if (description.isNotEmpty() && imageUri != null) {
                uploadStory(token, description, imageUri, currentLatitude, currentLongitude)
            } else {
                Toast.makeText(requireContext(), "Please provide a description and select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                binding.previewImageView.setImageURI(uri)
                binding.previewImageView.tag = uri
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                binding.previewImageView.setImageURI(imageUri)
                binding.previewImageView.tag = imageUri
            } else {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                val tempFile = JavaFile.createTempFile("captured_image", ".jpg", requireContext().cacheDir)
                val outputStream = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                binding.previewImageView.setImageURI(Uri.fromFile(tempFile))
                binding.previewImageView.tag = Uri.fromFile(tempFile)
            }
        }
    }

    private fun getFileFromUri(uri: Uri): JavaFile? {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val fileName: String = getFileName(uri) ?: "temp_file"
        val tempFile = JavaFile.createTempFile(fileName, null, requireContext().cacheDir)
        tempFile.outputStream().use { outputStream ->
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return name
    }

    private fun uploadStory(token: String, description: String, imageUri: Uri, latitude: Double?, longitude: Double?) {
        val file = getFileFromUri(imageUri)
        if (file != null) {
            val compressedFile = compressImageFile(file)

            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), compressedFile)
            val body = MultipartBody.Part.createFormData("photo", compressedFile.name, requestFile)
            val descriptionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
            val latitudeBody = latitude?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toFloat().toString()) }
            val longitudeBody = longitude?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toFloat().toString()) }

            lifecycleScope.launch {
                try {
                    Log.d("UploadStory", "Sending request to API...")
                    val response = RetrofitInstance.api.addStory(
                        token = "Bearer $token",
                        description = descriptionBody,
                        photo = body,
                        latitude = latitudeBody,
                        longitude = longitudeBody
                    )

                    if (response.error) {
                        Toast.makeText(requireContext(), "Failed to upload story: ${response.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                        (activity as MainActivity).navigateWithAnimation(R.id.homeFragment)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to get file from URI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    Toast.makeText(requireContext(), "Location retrieved: $currentLatitude, $currentLongitude", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error getting location", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Location permission is not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImageFile(file: JavaFile): JavaFile {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var quality = 100
        val compressedFile = JavaFile(requireContext().cacheDir, "compressed_${file.name}")
        do {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            if (byteArray.size <= 1_000_000) {
                FileOutputStream(compressedFile).use { it.write(byteArray) }
                break
            }
            quality -= 5
        } while (quality > 0)
        return compressedFile
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as AppCompatActivity
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
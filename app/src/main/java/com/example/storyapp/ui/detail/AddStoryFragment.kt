package com.example.storyapp.ui.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

class AddStoryFragment : Fragment() {

    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.add_story_title)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        binding.buttonAdd.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
            val token = sharedPref.getString("token", "")
            if (token.isNullOrEmpty()) {
                Log.e("AddStoryFragment", "Token is empty or null")
                Toast.makeText(requireContext(), "Token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val description = binding.edAddDescription.text.toString()
            val imageUri = binding.previewImageView.tag as? Uri
            if (description.isNotEmpty() && imageUri != null) {
                uploadStory(token, description, imageUri)
            } else {
                Toast.makeText(requireContext(), "Please provide a description and select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                binding.previewImageView.setImageURI(uri)
                binding.previewImageView.tag = uri
            }
        }
    }

    private fun getFileFromUri(uri: Uri): JavaFile? {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val fileName: String? = getFileName(uri)
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

    private fun uploadStory(token: String, description: String, imageUri: Uri) {
        val file = getFileFromUri(imageUri)
        if (file != null) {
            val compressedFile = compressImageFile(file)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), compressedFile)
            val body = MultipartBody.Part.createFormData("photo", compressedFile.name, requestFile)
            val descriptionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.addStory(
                        token = "Bearer $token",
                        description = descriptionBody,
                        photo = body
                    )
                    if (response.error) {
                        Log.e("AddStoryFragment", "Failed to upload story: ${response.message}")
                        Log.e("AddStoryFragment", "Image: ${compressedFile.name}, Text: $description, Token: $token")
                        Toast.makeText(requireContext(), "Failed to upload story: ${response.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("AddStoryFragment", "Story uploaded successfully")
                        Log.d("AddStoryFragment", "Image: ${compressedFile.name}, Text: $description, Token: $token")
                        Toast.makeText(requireContext(), "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                } catch (e: Exception) {
                    Log.e("AddStoryFragment", "Error uploading story", e)
                    Log.e("AddStoryFragment", "Image: ${compressedFile.name}, Text: $description, Token: $token")
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to get file from URI", Toast.LENGTH_SHORT).show()
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
        _binding = null
    }
}
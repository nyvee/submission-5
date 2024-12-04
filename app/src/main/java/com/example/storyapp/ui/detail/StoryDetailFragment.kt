// StoryDetailFragment.kt
package com.example.storyapp.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.databinding.FragmentDetailStoryBinding
import com.example.storyapp.utils.DateFormat

class StoryDetailFragment : Fragment() {

    private var _binding: FragmentDetailStoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update the toolbar title and navigation
        val mainActivity = activity as AppCompatActivity
        mainActivity.supportActionBar?.apply {
            title = getString(R.string.story_detail_title)
            setDisplayHomeAsUpEnabled(true)
        }

        // Handle back navigation
        mainActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainActivity.onBackPressed()
        }

        // Populate story details
        val story = arguments?.getParcelable<Story>("story")
        story?.let {
            binding.tvDetailName.text = "by ${it.name}"
            binding.tvDetailDescription.text = it.description
            binding.tvDetailDate.text = DateFormat.format(it.createdAt)
            Glide.with(this).load(it.photoUrl).into(binding.ivDetailPhoto)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Revert toolbar configuration
        val mainActivity = activity as AppCompatActivity
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

}


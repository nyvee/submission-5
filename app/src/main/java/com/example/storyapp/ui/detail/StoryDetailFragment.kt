package com.example.storyapp.ui.detail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        val mainActivity = activity as AppCompatActivity
        mainActivity.supportActionBar?.apply {
            title = getString(R.string.story_detail_title)
            setDisplayHomeAsUpEnabled(true)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            if (navController != null && !navController.popBackStack()) {
                requireActivity().onBackPressed()
            }
        }

        val story = arguments?.getParcelable<Story>("story")
        story?.let {
            binding.tvDetailName.text = "by ${it.name}"
            binding.tvDetailDescription.text = it.description
            binding.tvDetailDate.text = DateFormat.format(it.createdAt)
            Glide.with(this).load(it.photoUrl).into(binding.ivDetailPhoto)
        }

        playAnimation()
    }

    private fun playAnimation() {
        val scaleX = ObjectAnimator.ofFloat(binding.ivDetailPhoto, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.ivDetailPhoto, View.SCALE_Y, 0f, 1f)
        scaleX.duration = 500
        scaleY.duration = 500

        val descriptionFade = ObjectAnimator.ofFloat(binding.tvDetailDescription, View.ALPHA, 0f, 1f).setDuration(1000)
        val nameFade = ObjectAnimator.ofFloat(binding.tvDetailName, View.ALPHA, 0f, 1f).setDuration(1000)
        val dateFade = ObjectAnimator.ofFloat(binding.tvDetailDate, View.ALPHA, 0f, 1f).setDuration(1000)

        val together = AnimatorSet().apply {
            playTogether(scaleX, scaleY, descriptionFade, nameFade, dateFade)
        }
        together.start()
    }



    override fun onDestroyView() {
        super.onDestroyView()

        val mainActivity = activity as AppCompatActivity
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}

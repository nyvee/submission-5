package com.example.storyapp.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.MainActivity
import com.example.storyapp.R
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: StoryAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = StoryRepository(requireContext())
        val factory = HomeViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        adapter = StoryAdapter { story -> navigateToDetail(story) }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel.stories.observe(viewLifecycleOwner, { stories ->
            adapter.submitList(stories)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchStories()
        }

        binding.fabAddStory.setOnClickListener {
            navigateToAddStory()
        }

        viewModel.fetchStories()
    }

    private fun navigateToDetail(story: Story) {
        val action = HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(story)
        (activity as MainActivity).navigateWithAnimation(action.actionId, action.arguments)
    }

    private fun navigateToAddStory() {
        (activity as MainActivity).navigateWithAnimation(R.id.addStoryFragment)
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
        }
    }
}
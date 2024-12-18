package com.example.storyapp.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.MainActivity
import com.example.storyapp.R
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.data.remote.retrofit.RetrofitInstance
import com.example.storyapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: StoryAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = "Bearer ${requireContext().getSharedPreferences("UserPrefs", 0).getString("token", "")}"
        val repository = StoryRepository(RetrofitInstance.api, token)
        viewModel = ViewModelProvider(this, HomeViewModelFactory(repository)).get(HomeViewModel::class.java)

        adapter = StoryAdapter { story -> navigateToDetail(story) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.getStoriesPaging().collectLatest { pagingData ->
                adapter.submitData(pagingData)
                println("PagingData received: ${pagingData}")
            }
        }

        binding.fabAddStory.setOnClickListener {
            navigateToAddStory()
        }
    }

    private fun navigateToDetail(story: Story) {
        val action = HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(story)
        (activity as MainActivity).navigateWithAnimation(action.actionId, action.arguments)
    }

    private fun navigateToAddStory() {
        (activity as MainActivity).navigateWithAnimation(R.id.addStoryFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

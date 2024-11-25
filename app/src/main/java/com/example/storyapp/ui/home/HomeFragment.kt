package com.example.storyapp.ui.home

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.storyapp.MainActivity
import com.example.storyapp.R

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoryAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = StoryAdapter { story ->
            // Temporarily remove navigation to DetailStoryFragment
        }
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.app_name)
        val factory = HomeViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        viewModel.stories.observe(viewLifecycleOwner, { stories ->
            adapter.submitList(stories)
        })
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefreshLayout.isRefreshing = false
        })
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchStories()
        }
        viewModel.fetchStories()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                (activity as MainActivity).logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
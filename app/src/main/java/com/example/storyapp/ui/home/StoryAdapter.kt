package com.example.storyapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.Story

class StoryAdapter(private val onItemClick: (Story) -> Unit) :
    PagingDataAdapter<Story, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.story_card, parent, false)
        return StoryViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        story?.let { holder.bind(it) }
    }

    class StoryViewHolder(itemView: View, private val onItemClick: (Story) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_item_name)
        private val photoImageView: ImageView = itemView.findViewById(R.id.iv_item_photo)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(story: Story) {
            nameTextView.text = story.name
            progressBar.visibility = View.VISIBLE

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(photoImageView)

            progressBar.visibility = View.GONE

            itemView.setOnClickListener { onItemClick(story) }
        }
    }

    class StoryDiffCallback : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }
}

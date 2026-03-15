package ru.netology.nework.presentation.users.jobs.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.domain.model.Job
import ru.netology.nework.util.DateUtils

class JobAdapter(
    private val onRemoveClick: ((Job) -> Unit)? = null
) : ListAdapter<Job, JobAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return JobViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JobViewHolder(
        private val binding: CardJobBinding,
        private val onRemoveClick: ((Job) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {
            binding.name.text = job.name
            binding.position.text = job.position

            val startDate = DateUtils.formatIsoDate(job.start.toString())
            val endDate = job.finish?.let { DateUtils.formatIsoDate(it.toString()) }
                ?: binding.root.context.getString(R.string.present_time)
            binding.startFinish.text = "$startDate — $endDate"

            job.link?.let {
                binding.link.visibility = View.VISIBLE
                binding.link.text = it.trim()
            } ?: run {
                binding.link.visibility = View.GONE
            }

            if (onRemoveClick != null) {
                binding.buttonRemoveJob.visibility = View.VISIBLE
                binding.buttonRemoveJob.setOnClickListener {
                    onRemoveClick.invoke(job)
                }
            } else {
                binding.buttonRemoveJob.visibility = View.GONE
            }
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Job, newItem: Job) = oldItem == newItem
    }
}
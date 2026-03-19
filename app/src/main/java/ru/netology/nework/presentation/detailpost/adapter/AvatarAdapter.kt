package ru.netology.nework.presentation.detailpost.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardAvatarBinding
import ru.netology.nework.domain.model.User
import ru.netology.nework.util.load

class AvatarAdapter : ListAdapter<User, AvatarAdapter.AvatarViewHolder>(AvatarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val binding = CardAvatarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AvatarViewHolder(
        private val binding: CardAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.avatar.load(
                url = user.avatar,
                placeholder = R.drawable.ic_account_circle_24,
                error = R.drawable.ic_account_circle_24,
                roundedCorners = 24
            )
        }
    }

    class AvatarDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.id == newItem.id || oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem == newItem
    }
}
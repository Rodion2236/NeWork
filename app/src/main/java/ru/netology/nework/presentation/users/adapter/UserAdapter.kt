package ru.netology.nework.presentation.users.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.domain.model.User
import ru.netology.nework.util.load

class UserAdapter(
    private val onUserClick: (User) -> Unit,
    private val showCheckbox: Boolean = false,
    private val onUserToggle: ((String, Boolean) -> Unit)? = null
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding, onUserClick, showCheckbox, onUserToggle)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUserClick: (User) -> Unit,
    private val showCheckbox: Boolean,
    private val onUserToggle: ((String, Boolean) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.authorName.text = user.name
        binding.authorLogin.text = user.login

        binding.authorAvatar.load(
            url = user.avatar,
            placeholder = R.drawable.ic_account_circle_24,
            error = R.drawable.ic_account_circle_24,
            roundedCorners = 24
        )

        binding.checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE

        if (showCheckbox && onUserToggle != null) {
            binding.checkBox.isEnabled = true
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = false
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onUserToggle(user.id, isChecked)
            }
            binding.root.setOnClickListener {
                binding.checkBox.isChecked = !binding.checkBox.isChecked
            }
        } else {
            binding.root.setOnClickListener { onUserClick(user) }
        }
    }
}
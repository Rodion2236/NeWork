package ru.netology.nework.fragments.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentDialogUserSelectionBinding
import ru.netology.nework.presentation.users.UsersViewModel
import ru.netology.nework.presentation.users.adapter.UserAdapter
import ru.netology.nework.util.BundleKeys

@AndroidEntryPoint
class UserSelectionDialogFragment : BottomSheetDialogFragment(R.layout.fragment_dialog_user_selection) {

    companion object { const val TAG = "UserSelectionDialog" }

    private val viewModel: UsersViewModel by viewModels()
    private var _binding: FragmentDialogUserSelectionBinding? = null
    private val binding get() = _binding!!
    private val selectedUserIds = mutableSetOf<String>()
    private lateinit var adapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDialogUserSelectionBinding.bind(view)

        adapter = UserAdapter(
            onUserClick = {},
            showCheckbox = true,
            onUserToggle = { userId, checked ->
                if (checked) selectedUserIds.add(userId) else selectedUserIds.remove(userId)
            }
        )
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUsers.adapter = adapter

        setupObservers()
        setupClicks()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users -> adapter.submitList(users) }
            }
        }
    }

    private fun setupClicks() {
        binding.buttonConfirm.setOnClickListener {
            setFragmentResult(BundleKeys.SELECTED_USER_IDS, Bundle().apply {
                putStringArrayList(BundleKeys.SELECTED_USER_IDS, ArrayList(selectedUserIds))
            })
            dismiss()
        }
        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
package com.udacity.project4.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentAuthenticationBinding

class AuthenticationFragment : BaseFragment() {
    override val _viewModel: AuthenticationViewModel
        get() = AuthenticationViewModel(requireActivity().application)

    private var _binding: FragmentAuthenticationBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentAuthenticationBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setObservers()
        return binding.root
    }

    private fun setObservers() {
        _viewModel.authenticationState.observe(viewLifecycleOwner) { authenticationState ->
            if (authenticationState == AuthenticationViewModel.AuthenticationState.AUTHENTICATED) {
                findNavController().navigate(AuthenticationFragmentDirections.toReminderListFragment())
            } else {
                launchSignInFlow()
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivity(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build()
        )
    }
}

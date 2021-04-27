package com.andresgarrido.memorygame

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andresgarrido.memorygame.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        val binding = FragmentHomeBinding.inflate(inflater)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        context?.let { viewModel.initSelectionAudio(it) }

        viewModel.isGameExit.observe(viewLifecycleOwner, { isExit ->
            if (isExit) {
                activity?.finish()
            }
        })

        viewModel.optionChosen.observe(viewLifecycleOwner, { option ->
            if (option != HomeViewModel.BoardType.NONE) {

                val action = HomeFragmentDirections.actionHomeFragmentToGameFragment(option)
                findNavController().navigate(action)
                viewModel.selectMenuItem(null, HomeViewModel.BoardType.NONE)
                viewModel.isStartGameVisible.postValue(true)
            }

        })

        return binding.root
    }
}
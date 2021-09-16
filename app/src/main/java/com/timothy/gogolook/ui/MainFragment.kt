package com.timothy.gogolook.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.timothy.gogolook.data.model.ResultOf
import com.timothy.gogolook.databinding.MainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainFragment : Fragment(), View.OnClickListener{
    private val mainViewModel: MainViewModel by viewModels()
    lateinit var binding :MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            button.setOnClickListener(this@MainFragment)
        }

        mainViewModel.imagesSearchResult.observe(viewLifecycleOwner){
            when(it){
                is ResultOf.Success ->{
                    val result = it.value
                    binding.message.text = "$result"
                }

                is ResultOf.Failure ->{
                    binding.message.text = it.message
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when(view){
            binding.button ->{
                mainViewModel.searchImages("flower+yellow")
                Timber.d("onClick")
            }
        }
    }
}
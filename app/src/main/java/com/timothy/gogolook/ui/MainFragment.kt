package com.timothy.gogolook.ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.gogolook.R
import com.timothy.gogolook.data.model.LoadingStatus
import com.timothy.gogolook.databinding.MainFragmentBinding
import com.timothy.gogolook.ui.adapters.ImageSearchResultListAdapter
import com.timothy.gogolook.ui.adapters.LayoutType
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainFragment : Fragment(), View.OnClickListener{

    private val mainViewModel: MainViewModel by viewModels()
    lateinit var binding :MainFragmentBinding

    private val adapter=ImageSearchResultListAdapter()

    private val gridLayoutManager:GridLayoutManager by lazy {
        GridLayoutManager(requireContext(),2)
    }
    private val linearLayoutManager:LinearLayoutManager by lazy {
        LinearLayoutManager(requireContext())
    }

    private val imeService:InputMethodManager by lazy {
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private var isGrid:Boolean? = null

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
            //recyclerView
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }

            //button settings
            with(displayBtnGroup){
                addOnButtonCheckedListener { _, checkedId, _ ->
                    if (checkedId == R.id.display_type_list) {
                        toggleRecyclerViewLayout(false)
                    } else if (checkedId == R.id.display_type_grid)
                        toggleRecyclerViewLayout(true)
                }

                check(if(isGrid==true) R.id.display_type_grid else R.id.display_type_list)
            }

            //search terms editText
            searchTermInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT) {
                    imeService.hideSoftInputFromWindow(view.windowToken, 0)
                    updateSearchTermsInput()
                    true
                } else {
                    false
                }
            }
            searchTermInput.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    imeService.hideSoftInputFromWindow(view.windowToken, 0)
                    updateSearchTermsInput()
                    true
                } else {
                    false
                }
            }

            btnRetry.setOnClickListener(this@MainFragment)
        }

        mainViewModel.pagedList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        mainViewModel.loadStatus.observe(viewLifecycleOwner){
            binding.progressBar.visibility =
                if(it is LoadingStatus.LOADING) View.VISIBLE else View.GONE

            binding.btnRetry.visibility =
                if(it is LoadingStatus.ERROR) View.VISIBLE else View.GONE
        }
    }

    private fun toggleRecyclerViewLayout(isGrid:Boolean){
        this.isGrid = isGrid
        adapter.setLayoutType( if(isGrid) LayoutType.Grid else LayoutType.Linear )

        //scroll to position
        val lastFirstVisiblePosition =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

        if(this.isGrid==true){
            binding.recyclerView.layoutManager = gridLayoutManager.also {
                it.scrollToPosition(lastFirstVisiblePosition)
            }
        }else{
            binding.recyclerView.layoutManager = linearLayoutManager.also {
                it.scrollToPosition(lastFirstVisiblePosition)
            }
        }
    }

    private fun updateSearchTermsInput(){
        binding.searchTermInput.text?.trim().toString().replace("\\s+","+").let {
            Timber.d(it)
            if (it.isNotBlank() && mainViewModel.loadStatus.value !is LoadingStatus.LOADING) {
                mainViewModel.updateSearchTerms(it)
            }
        }
    }

    override fun onClick(view: View?) {
        when(view){
            binding.btnRetry ->{
                mainViewModel.retry()
            }
        }
    }

}
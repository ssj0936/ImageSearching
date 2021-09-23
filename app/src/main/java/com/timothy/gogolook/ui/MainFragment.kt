package com.timothy.gogolook.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.timothy.gogolook.R
import com.timothy.gogolook.data.model.LoadingStatus
import com.timothy.gogolook.databinding.MainFragmentBinding
import com.timothy.gogolook.ui.adapters.ImageSearchResultListAdapter
import com.timothy.gogolook.ui.adapters.LayoutType
import com.timothy.gogolook.util.windowWidth
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar

val DEFAULT_LAYOUT_TYPE:LayoutType = LayoutType.Linear

@AndroidEntryPoint
class MainFragment : Fragment(), View.OnClickListener{

    private val mainViewModel: MainViewModel by viewModels()
    lateinit var binding :MainFragmentBinding

    //adapter for searching result
    private val searchResultAdapter = ImageSearchResultListAdapter()
    //adapter for history search terms
    private lateinit var historySearchTermsAdapter:ArrayAdapter<String>

    private val gridLayoutManager:GridLayoutManager by lazy {
        val width = windowWidth
        val gridWidth = resources.getDimension(R.dimen.recycler_view_item_image_length)

        GridLayoutManager(requireContext(), (width/gridWidth).toInt())
    }

    private val linearLayoutManager:LinearLayoutManager by lazy {
        LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    private var isGrid:Boolean = DEFAULT_LAYOUT_TYPE is LayoutType.Grid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            //recyclerView
            recyclerView.adapter = searchResultAdapter
            recyclerView.layoutManager = if(isGrid) gridLayoutManager else linearLayoutManager

            //layout button group settings
            with(displayBtnGroup){
                addOnButtonCheckedListener { _, checkedId, _ ->
                    if (checkedId == R.id.display_type_list) {
                        toggleRecyclerViewLayout(false)
                    } else if (checkedId == R.id.display_type_grid)
                        toggleRecyclerViewLayout(true)
                }

                //init default selection
                check(if(isGrid) R.id.display_type_grid else R.id.display_type_list)
            }

            //search terms editText
            searchTermInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT) {
                    submitSearchTerms()
                    true
                } else {
                    false
                }
            }
            searchTermInput.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitSearchTerms()
                    true
                } else {
                    false
                }
            }

            //history search terms
            historySearchTermsAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
            searchTermInput.setAdapter(historySearchTermsAdapter)
            searchTermInput.threshold = 1
            searchTermInput.setOnItemClickListener { _, _, _, _ ->
                submitSearchTerms()
            }

            searchTermInput.setOnTouchListener { _, _ ->
                if(!historySearchTermsAdapter.isEmpty && !searchTermInput.isPopupShowing)
                    searchTermInput.showDropDown()

                false
            }

            //retry button for fetching data fail
            btnRetry.setOnClickListener(this@MainFragment)
        }

        mainViewModel.pagedList.observe(viewLifecycleOwner){
            searchResultAdapter.submitList(it)
        }

        mainViewModel.loadStatus.observe(viewLifecycleOwner){ loadingStatus ->
            //showing snackbar for error message
            loadingStatus.message?.let {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            binding.progressBar.visibility =
                if(loadingStatus is LoadingStatus.Loading) View.VISIBLE else View.GONE

            binding.btnRetry.visibility =
                if(loadingStatus is LoadingStatus.Error) View.VISIBLE else View.GONE
        }

        mainViewModel.searchTermsHistory.observe(viewLifecycleOwner){ queue ->
            historySearchTermsAdapter.run {
                clear()
                queue.toList().asReversed().forEach { historySearchTermsAdapter.add(it) }
                notifyDataSetChanged()
            }
        }
    }

    private fun submitSearchTerms(){
        //hide ime
        val imeService = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imeService.hideSoftInputFromWindow(view?.windowToken ?: return, 0)

        //submit terms
        updateSearchTermsInput()
    }

    private fun toggleRecyclerViewLayout(isGrid:Boolean){
        this.isGrid = isGrid
        searchResultAdapter.setLayoutType( if(isGrid) LayoutType.Grid else LayoutType.Linear )

        //scroll to position
        val lastFirstVisiblePosition =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

        if(this.isGrid){
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
            if (it.isNotBlank() && mainViewModel.loadStatus.value !is LoadingStatus.Loading) {
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
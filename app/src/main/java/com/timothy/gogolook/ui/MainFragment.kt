package com.timothy.gogolook.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.timothy.gogolook.R
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.data.model.LoadingStatus
import com.timothy.gogolook.databinding.MainFragmentBinding
import com.timothy.gogolook.ui.adapters.ImageSearchResultListAdapter
import com.timothy.gogolook.ui.adapters.LayoutType
import com.timothy.gogolook.util.windowWidth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    private val animSet = AnimationSet(true).apply {
        addAnimation(AlphaAnimation(0f, 1f).apply {
            duration = 500
        })
        addAnimation(TranslateAnimation(0f, 0f, 100f, 0f).apply {
            duration = 500
        })
        fillAfter = true
    }

    private val gridLayoutManager: GridLayoutManager by lazy {
        val width = windowWidth
        val gridWidth = resources.getDimension(R.dimen.recycler_view_item_image_length)

        GridLayoutManager(requireContext(), (width / gridWidth).toInt())
    }

    private val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bindState(
            uiStateFlow = mainViewModel.uiState,
            searchHistoryStateFlow = mainViewModel.searchTermsHistory,
            pagingFlow = mainViewModel.pagingFlow
        )
    }

    private fun MainFragmentBinding.bindState(
        uiStateFlow: StateFlow<UIState>,
        searchHistoryStateFlow: StateFlow<List<String>>,
        pagingFlow: Flow<PagingData<HitsItem>>,
    ) {
        bindSearch(searchHistoryStateFlow = searchHistoryStateFlow)
        bindList(uiStateFlow = uiStateFlow, pagingFlow = pagingFlow)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun MainFragmentBinding.bindSearch(
        searchHistoryStateFlow: StateFlow<List<String>>
    ) {
        //adapter for history search terms
        val historySearchTermsAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)

        //group toggle buttons
        with(displayBtnGroup) {
            this.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    mainViewModel.toggleRecyclerViewLayout(isGrid = (checkedId == R.id.display_type_grid))
                    recyclerView.apply {
                        startAnimation(animSet)
                    }
                }
            }

            //init default selection
            this.check(if (mainViewModel.currentState.isGrid) R.id.display_type_grid else R.id.display_type_list)
        }

        with(searchTermInput) {
            threshold = 1
            setText(mainViewModel.currentState.searchTerms)
            setAdapter(historySearchTermsAdapter)

            //search terms editText
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT) {
                    submitSearchTerms()
                    true
                } else {
                    false
                }
            }
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitSearchTerms()
                    true
                } else {
                    false
                }
            }

            //history search terms
            setOnItemClickListener { _, _, _, _ ->
                submitSearchTerms()
            }

            setOnTouchListener { _, _ ->
                if (!historySearchTermsAdapter.isEmpty)
                    searchTermInput.showDropDown()

                false
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchHistoryStateFlow.collectLatest { historyTerms ->
                    historySearchTermsAdapter.run {
                        clear()
                        historySearchTermsAdapter.addAll(historyTerms.asReversed())
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun MainFragmentBinding.bindList(
        uiStateFlow: StateFlow<UIState>,
        pagingFlow: Flow<PagingData<HitsItem>>,
    ) {
        //adapter for searching result
        val searchResultAdapter = ImageSearchResultListAdapter()

        //recyclerView
        with(recyclerView) {
            adapter = searchResultAdapter
            layoutManager =
                if (mainViewModel.currentState.isGrid) gridLayoutManager else linearLayoutManager
        }

        //retry button for fetching data fail
        with(btnRetry) {
            setOnClickListener {
                searchResultAdapter.retry()
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //paging
                launch {
                    pagingFlow.collectLatest { pagingData ->
                        searchResultAdapter.submitData(pagingData)
                    }
                }

                launch {
                    searchResultAdapter.loadStateFlow.collectLatest { loadingState ->
                        val errorState = loadingState.source.append as? LoadState.Error
                            ?: loadingState.source.prepend as? LoadState.Error
                            ?: loadingState.append as? LoadState.Error
                            ?: loadingState.prepend as? LoadState.Error

                        progressBar.visibility =
                            if (loadingState.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                        binding.btnRetry.visibility =
                            if (errorState is LoadState.Error) View.VISIBLE else View.GONE

                        errorState?.error?.message?.let {msg ->
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    msg,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                        }
                    }
                }

                //viewModel isGrid
                launch {
                    uiStateFlow.map { it.isGrid }.distinctUntilChanged().collectLatest { isGrid ->
                        searchResultAdapter.setLayoutType(if (isGrid) LayoutType.Grid else LayoutType.Linear)

                        //scroll to position
                        val lastFirstVisiblePosition =
                            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

                        if (isGrid) {
                            binding.recyclerView.layoutManager = gridLayoutManager.apply {
                                scrollToPosition(lastFirstVisiblePosition)
                            }
                        } else {
                            binding.recyclerView.layoutManager = linearLayoutManager.apply {
                                scrollToPosition(lastFirstVisiblePosition)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun submitSearchTerms() {
        //hide ime
        val imeService =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imeService.hideSoftInputFromWindow(view?.windowToken ?: return, 0)

        //submit terms
        updateSearchTermsInput()
    }

    private fun updateSearchTermsInput() {
        binding.searchTermInput.text?.trim().toString().replace("\\s+", "+").let {
            if (it.isNotBlank()) {
                mainViewModel.updateSearchTerms(it)
            }
        }
    }
}
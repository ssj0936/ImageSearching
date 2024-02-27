package com.timothy.gogolook.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import com.google.android.material.snackbar.Snackbar
import com.timothy.gogolook.R
import com.timothy.gogolook.data.model.LoadingStatus
import com.timothy.gogolook.databinding.MainFragmentBinding
import com.timothy.gogolook.ui.adapters.ImageSearchResultListAdapter
import com.timothy.gogolook.ui.adapters.LayoutType
import com.timothy.gogolook.util.DEFAULT_LAYOUT_TYPE
import com.timothy.gogolook.util.windowWidth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainFragment : Fragment(), View.OnClickListener {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding

    //adapter for searching result
    private val searchResultAdapter = ImageSearchResultListAdapter()

    //adapter for history search terms
    private lateinit var historySearchTermsAdapter: ArrayAdapter<String>

    val animSet = AnimationSet(true).apply {
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

    @SuppressLint("ClickableViewAccessibility", "UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            //recyclerView
            recyclerView.adapter = searchResultAdapter
            recyclerView.layoutManager =
                if (mainViewModel.currentState.isGrid) gridLayoutManager else linearLayoutManager

            //layout button group settings
            with(displayBtnGroup) {
                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        mainViewModel.toggleRecyclerViewLayout(isGrid = (checkedId == R.id.display_type_grid))
                        recyclerView.apply {
                            startAnimation(animSet)
                        }
                    }
                }

                //init default selection
                check(if (mainViewModel.currentState.isGrid) R.id.display_type_grid else R.id.display_type_list)
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
            historySearchTermsAdapter =
                ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
            searchTermInput.setText(mainViewModel.currentState.searchTerms)
            searchTermInput.setAdapter(historySearchTermsAdapter)
            searchTermInput.threshold = 1
            searchTermInput.setOnItemClickListener { _, _, _, _ ->
                submitSearchTerms()
            }

            searchTermInput.setOnTouchListener { _, _ ->
                if (!historySearchTermsAdapter.isEmpty)
                    searchTermInput.showDropDown()

                false
            }

            //retry button for fetching data fail
            btnRetry.setOnClickListener(this@MainFragment)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //paging
                launch {
                    mainViewModel.pagingFlow.collectLatest { pagingData ->
                        searchResultAdapter.submitData(pagingData)
                    }
                }
                //viewModel loadState
                launch {
                    mainViewModel.uiState.map { it.loadState }.distinctUntilChanged().collectLatest { loadState ->
                        if (loadState is LoadingStatus.Error) {
                            loadState.message?.let { msg ->
                                Snackbar.make(
                                    requireActivity().findViewById(android.R.id.content),
                                    msg,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                        binding.progressBar.visibility =
                            if (loadState is LoadingStatus.Loading) View.VISIBLE else View.GONE

                        binding.btnRetry.visibility =
                            if (loadState is LoadingStatus.Error) View.VISIBLE else View.GONE
                    }
                }

                //viewModel isGrid
                launch {
                    mainViewModel.uiState.map { it.isGrid }.distinctUntilChanged().collectLatest { isGrid ->
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

                //history
                launch {
                    mainViewModel.searchTermsHistory.collectLatest { historyTerms ->
                        historySearchTermsAdapter.run {
                            clear()
                            historySearchTermsAdapter.addAll(historyTerms.asReversed())
                            notifyDataSetChanged()
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
            if (it.isNotBlank() && mainViewModel.currentState.loadState !is LoadingStatus.Loading) {
                mainViewModel.updateSearchTerms(it)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnRetry -> {
                searchResultAdapter.retry()
//                mainViewModel.retry()
            }
        }
    }
}
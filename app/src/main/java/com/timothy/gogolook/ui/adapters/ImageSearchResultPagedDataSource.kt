package com.timothy.gogolook.ui.adapters

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.timothy.gogolook.data.Repository
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.util.IMAGE_SEARCH_INITIAL_PAGE
import timber.log.Timber

class ImageSearchResultPagedDataSource (
    private val repository: Repository,
    private val searchTerms: String,
    private val onLoading:()->Unit = {},
    private val onLoadingFinish:()->Unit = {},
    private val onLoadingFail:(String?)->Unit = {},
): PagingSource<Int, HitsItem>() {
    override fun getRefreshKey(state: PagingState<Int, HitsItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HitsItem> {
        try {
            onLoading()
            val pageNumber = params.key?: IMAGE_SEARCH_INITIAL_PAGE
            val response = repository.getSearchImages(searchTerms,pageNumber)
            val items = response.body()!!.hits!!
            onLoadingFinish()
            return LoadResult.Page(
                data = response.body()!!.hits ?: emptyList(),
                prevKey = if(pageNumber==1) null else pageNumber-1,
                nextKey = if(items.isEmpty()) null else pageNumber+1
            )
        }catch (e:Exception){
            Timber.d(e)
            onLoadingFail(e.message)
        }

        return LoadResult.Page(data = emptyList(), null, null)
    }
}
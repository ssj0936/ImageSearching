package com.timothy.gogolook.ui.widget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.timothy.gogolook.R
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.ui.MainViewModel
import com.timothy.gogolook.ui.UIEvent
import com.timothy.gogolook.util.IMAGE_SEARCH_INITIAL_MULTIPLIER
import com.timothy.gogolook.util.IMAGE_SEARCH_PAGE_SIZE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import timber.log.Timber


@Composable
fun SearchResultList(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val dataList by mainViewModel.uiState.map { it.dataWrapper.dataList }.collectAsState(
        emptyList()
    )
    val isGrid by mainViewModel.uiState.map { it.isGrid }.collectAsState(initial = true)

    val lazyGridState = rememberLazyGridState()
    val animatableValue = remember { Animatable(1f) }

    val transitionTrigger: suspend CoroutineScope.() -> Unit = {
        animatableValue.snapTo(0f)
        animatableValue.animateTo(1f, tween(500))
    }

    //trigger transition animation on first Time
    LaunchedEffect(dataList.size, isGrid) {
        if (dataList.size == 0 || dataList.size > (IMAGE_SEARCH_PAGE_SIZE * IMAGE_SEARCH_INITIAL_MULTIPLIER))
            return@LaunchedEffect
        transitionTrigger()
    }

    //trigger on new page loaded
    LaunchedEffect(dataList.size) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }.collectLatest { index ->
            if (dataList.isNotEmpty() && index >= dataList.size - 10) {
                mainViewModel.setEvent(UIEvent.OnLoadNewPage)
            }
        }
    }

    //scroll to top for each new search round
    LaunchedEffect(Unit) {
        mainViewModel.uiState.map { it.dataWrapper.page }.distinctUntilChanged().filter { it == 1 }
            .collectLatest {
                lazyGridState.scrollToItem(0)
            }
    }

    //
    LaunchedEffect(true) {
        mainViewModel.setEvent(UIEvent.OnSearch(mainViewModel.currentState.searchTerms))
    }

    Box(modifier = modifier.alpha(animatableValue.value)) {
        LazyVerticalGrid(columns = GridCells.Fixed(if (isGrid) 2 else 1), state = lazyGridState) {
            itemsIndexed(items = dataList) { i, data ->
                if (isGrid) {
                    ItemGrid(item = data)
                } else
                    ItemLinear(item = data)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ItemGrid(
    modifier: Modifier = Modifier,
    item: HitsItem
) {
    Column(modifier = modifier) {
        GlideImage(
            contentScale = ContentScale.Crop,
            model = item.webformatURL,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(4.dp)
                .align(Alignment.CenterHorizontally),
            failure = placeholder(R.drawable.glide_error_image),
            transition = CrossFade
        )
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(id = R.string.image_info_id, item.id))
            item.tags?.let { Text(text = stringResource(id = R.string.image_info_tags, it)) }
            item.views?.let { Text(text = stringResource(id = R.string.image_info_views, it)) }
            item.downloads?.let {
                Text(
                    text = stringResource(
                        id = R.string.image_info_downloads,
                        it
                    )
                )
            }
            item.likes?.let { Text(text = stringResource(id = R.string.image_info_likes, it)) }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ItemLinear(
    modifier: Modifier = Modifier,
    item: HitsItem
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        GlideImage(
            contentScale = ContentScale.Crop,
            model = item.webformatURL,
            contentDescription = null,
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.recycler_view_item_image_length))
                .padding(4.dp),
            failure = placeholder(R.drawable.glide_error_image),
            transition = CrossFade
        )
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(id = R.string.image_info_id, item.id))
            item.tags?.let { Text(text = stringResource(id = R.string.image_info_tags, it)) }
            item.views?.let { Text(text = stringResource(id = R.string.image_info_views, it)) }
            item.downloads?.let {
                Text(
                    text = stringResource(
                        id = R.string.image_info_downloads,
                        it
                    )
                )
            }
            item.likes?.let { Text(text = stringResource(id = R.string.image_info_likes, it)) }
        }
    }
}
package com.timothy.gogolook.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.gogolook.R
import com.timothy.gogolook.ui.MainViewModel
import com.timothy.gogolook.ui.UIEvent
import com.timothy.gogolook.util.LAYOUT_TYPE_GRID
import com.timothy.gogolook.util.LAYOUT_TYPE_LINEAR
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),

    ) {
    Row(
        modifier = modifier
//            .height(intrinsicSize = IntrinsicSize.Max)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchInput(
            modifier = Modifier.weight(1f),
            initString = viewModel.currentState.searchTerms,
            onSearch = { viewModel.setEvent(UIEvent.OnSearch(it)) },
            onType = { viewModel.setEvent(UIEvent.OnType(it)) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        SegmentedButtons(options = layoutOptions)
    }
}

@Composable
fun SearchInput(
    modifier: Modifier = Modifier,
    initString: String,
    onSearch: (String) -> Unit = {},
    onType: (String) -> Unit = {}

) {
    var text by remember {
        mutableStateOf(initString)
    }
    var textFieldFocusState by remember {
        mutableStateOf(false)
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val trailingIcon = @Composable {
        IconButton(
            onClick = { text = "" },
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "",
                tint = Color.Black
            )
        }
    }
    Column(modifier = modifier) {
        TextField(
            modifier = Modifier.onFocusChanged {
                Timber.d("focus: ${it.hasFocus}, ${it.isFocused}")
                textFieldFocusState = it.hasFocus
            },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            value = text,
            onValueChange = {
                text = it
                onType(it)
            },
            label = { Text("SearchTerm") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch(text)
                    keyboardController?.hide()
                }
            ),
            trailingIcon = if (text.isEmpty()) null else trailingIcon,
        )

        if (textFieldFocusState) {
            AutoCompleteDropdown()
        }
    }
}

@Composable
fun AutoCompleteDropdown(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val dropdownContentList by viewModel.searchTermsHistoryMatchPrefix.collectAsState()

    Timber.d("dropdownContentList:$dropdownContentList")
    Box(modifier = modifier) {
        LazyColumn {
            items(items = dropdownContentList) { candidate ->
                Text(
                    text = candidate,
                    modifier = Modifier.clickable {
                        Timber.d(candidate)
                    }
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SearchInputEmptyPreview() {
    MaterialTheme {
        SearchInput(initString = "")
    }
}

@Preview(showSystemUi = true)
@Composable
fun SearchInputPreview() {
    MaterialTheme {
        SearchInput(initString = "Good")
    }
}

class ToggleButtonOption(val title: String, val value: Int, val iconId: Int?)

private val layoutOptions = listOf(
    ToggleButtonOption(
        title = "",
        value = LAYOUT_TYPE_LINEAR,
        iconId = R.drawable.ic_list_24
    ),
    ToggleButtonOption(
        title = "",
        value = LAYOUT_TYPE_GRID,
        iconId = R.drawable.ic_grid_24
    )
)

@Composable
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    options: List<ToggleButtonOption>,
    borderStrokeWidth: Dp = 1.dp,
    roundedCornerPercent: Int? = null,
    roundedCornerDp: Dp? = 14.dp,
    viewModel: MainViewModel = hiltViewModel()
) {
    if (options.isEmpty()) return

    val isGrid by viewModel.uiState.map { it.isGrid }.collectAsState(initial = true)

    Row(modifier = modifier) {
        options.forEachIndexed { i, option ->
            val selected =
                (isGrid && option.value == LAYOUT_TYPE_GRID) || (!isGrid && option.value == LAYOUT_TYPE_LINEAR)

            val shape = when (i) {
                0 -> if (roundedCornerPercent != null) {
                    RoundedCornerShape(
                        topStartPercent = roundedCornerPercent,
                        bottomStartPercent = roundedCornerPercent
                    )
                } else if (roundedCornerDp != null) {
                    RoundedCornerShape(
                        topStart = roundedCornerDp,
                        bottomStart = roundedCornerDp,
                    )
                } else {
                    return
                }

                options.lastIndex -> if (roundedCornerPercent != null) {
                    RoundedCornerShape(
                        topEndPercent = roundedCornerPercent,
                        bottomEndPercent = roundedCornerPercent
                    )
                } else if (roundedCornerDp != null) {
                    RoundedCornerShape(
                        topEnd = roundedCornerDp,
                        bottomEnd = roundedCornerDp,
                    )
                } else {
                    return
                }

                else -> RoundedCornerShape(0)
            }

            val offsetX =
                with(LocalDensity.current) { (if (i == 0) 0.dp else (-borderStrokeWidth * i)).roundToPx() }

            OutlinedButton(
                modifier = Modifier
                    .offset { IntOffset(offsetX, 0) },
                colors = ButtonDefaults.outlinedButtonColors().copy(
                    containerColor = if (selected)
                        MaterialTheme.colorScheme.primaryContainer
                    else ButtonDefaults.outlinedButtonColors().containerColor
                ),
                onClick = {
                    viewModel.setEvent(UIEvent.OnLayoutToggle(option.value == LAYOUT_TYPE_GRID))
                },
                shape = shape,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = borderStrokeWidth,
                    brush = SolidColor(
                        if (selected) Color.Transparent else MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (option.title.isNotEmpty())
                        Text(text = option.title)

                    option.iconId?.let {
                        Icon(painter = painterResource(id = option.iconId), contentDescription = "")
                    }
                }
            }
        }
    }
}
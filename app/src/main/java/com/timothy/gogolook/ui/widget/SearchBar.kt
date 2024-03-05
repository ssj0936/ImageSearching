package com.timothy.gogolook.ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timothy.gogolook.R
import com.timothy.gogolook.ui.MainViewModel
import com.timothy.gogolook.ui.UIEvent
import com.timothy.gogolook.util.LAYOUT_TYPE_GRID
import com.timothy.gogolook.util.LAYOUT_TYPE_LINEAR
import timber.log.Timber


@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchInput(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(4.dp))
        SegmentedButtons(options = layoutOptions)
    }
}

@Composable
fun SearchInput(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    var text by remember {
        mutableStateOf(viewModel.currentState.searchTerms)
    }
    val keyboardController = LocalSoftwareKeyboardController.current


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
    Box(modifier = modifier) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("SearchTerm") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    Timber.d("onSearch")
                    viewModel.setEvent(UIEvent.OnSearch(text))
                    keyboardController?.hide()
                }
            ),
            trailingIcon = if(text.isEmpty()) null else trailingIcon
        )
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
    roundedCornerPercent: Int = 50,
    viewModel: MainViewModel = hiltViewModel()
) {
    if (options.isEmpty()) return

    Row(modifier = modifier) {
        options.forEachIndexed { i, option ->
            val shape = when (i) {
                0 -> RoundedCornerShape(
                    topStartPercent = roundedCornerPercent,
                    bottomStartPercent = roundedCornerPercent
                )

                options.lastIndex -> RoundedCornerShape(
                    topEndPercent = roundedCornerPercent,
                    bottomEndPercent = roundedCornerPercent
                )

                else -> RoundedCornerShape(0)
            }

            val optionModifier = Modifier.offset(
                if (i == 0) 0.dp else (-borderStrokeWidth * i)
            )

            OutlinedButton(
                modifier = optionModifier,
                onClick = {
                    viewModel.setEvent(UIEvent.OnLayoutToggle(option.value == LAYOUT_TYPE_GRID))
                },
                shape = shape,
                border = ButtonDefaults.outlinedButtonBorder.copy(width = borderStrokeWidth)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if(option.title.isNotEmpty())
                        Text(text = option.title)

                    option.iconId?.let {
                        Icon(painter = painterResource(id = option.iconId), contentDescription = "")
                    }
                }
            }
        }
    }
}
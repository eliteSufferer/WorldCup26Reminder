package com.worldcup26.reminder.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Snackbar host whose snackbars can be swiped away in either direction. Duration is
 * controlled by the caller (we show them for ~2s), so swipe is the manual dismiss.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        val dismissState = rememberSwipeToDismissBoxState()
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                data.dismiss()
            }
        }
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            content = { Snackbar(data) },
        )
    }
}

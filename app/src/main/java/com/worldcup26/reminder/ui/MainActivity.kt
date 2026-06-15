package com.worldcup26.reminder.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.worldcup26.reminder.WorldCupApp
import com.worldcup26.reminder.ui.theme.WorldCupTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MatchesViewModel by viewModels {
        MatchesViewModel.Factory(
            (application as WorldCupApp).container.matchRepository
        )
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermissions()

        setContent {
            WorldCupTheme {
                val matches by viewModel.matches.collectAsState()
                val refreshing by viewModel.refreshing.collectAsState()
                MatchListScreen(
                    matches = matches,
                    refreshing = refreshing,
                    onRefresh = viewModel::refresh,
                    onToggleFollow = { viewModel.toggleFollow(it) },
                )
            }
        }

        // Populate on first launch when the cache is still empty.
        if (viewModel.matches.value.isEmpty()) viewModel.refresh()
    }

    private fun requestRuntimePermissions() {
        val perms = buildList {
            add(Manifest.permission.READ_CALENDAR)
            add(Manifest.permission.WRITE_CALENDAR)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        permissionLauncher.launch(perms)
    }
}

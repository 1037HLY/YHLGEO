package com.geosurvey.toolbox.presentation.utils

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(backDispatcher, lifecycleOwner) {
        val callback = object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                if (enabled) {
                    onBack()
                }
            }
        }

        backDispatcher?.addCallback(lifecycleOwner, callback)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                callback.remove()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            callback.remove()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

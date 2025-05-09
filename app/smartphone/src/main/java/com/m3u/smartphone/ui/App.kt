package com.m3u.smartphone.ui

import android.app.ActivityOptions
import android.content.Intent
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.m3u.smartphone.ui.common.connect.RemoteControlSheet
import com.m3u.smartphone.ui.common.connect.RemoteControlSheetValue
import com.m3u.core.architecture.preferences.hiltPreferences
import com.m3u.data.tv.model.RemoteDirection
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import com.m3u.smartphone.ui.business.channel.PlayerActivity
import com.m3u.smartphone.ui.material.model.LocalSpacing
import com.m3u.smartphone.ui.common.AppNavHost
import com.m3u.smartphone.ui.common.Scaffold
import com.m3u.smartphone.ui.material.components.Destination
import com.m3u.smartphone.ui.material.components.FontFamilies
import com.m3u.smartphone.ui.material.components.SnackHost

@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = hiltViewModel(),
) {
    val onBackPressedDispatcher = checkNotNull(
        LocalOnBackPressedDispatcherOwner.current
    ).onBackPressedDispatcher

    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()

    val shouldDispatchBackStack by remember {
        derivedStateOf {
            with(entry) {
                this != null && destination.route in Destination.Root.entries.map { it.name }
            }
        }
    }

    val onBackPressed: (() -> Unit) = {
        onBackPressedDispatcher.onBackPressed()
    }

    // for tvs
    val broadcastCodeOnTv by viewModel.broadcastCodeOnTv.collectAsStateWithLifecycle()

    // for smartphones
    val remoteControlSheetValue by viewModel.remoteControlSheetValue.collectAsStateWithLifecycle()

    AppImpl(
        navController = navController,
        onBackPressed = onBackPressed.takeUnless { shouldDispatchBackStack },
        checkTvCodeOnSmartphone = viewModel::checkTvCodeOnSmartphone,
        forgetTvCodeOnSmartphone = viewModel::forgetTvCodeOnSmartphone,
        broadcastCodeOnTv = broadcastCodeOnTv,
        isRemoteControlSheetVisible = viewModel.isConnectSheetVisible,
        remoteControlSheetValue = remoteControlSheetValue,
        onRemoteDirection = viewModel::onRemoteDirection,
        openRemoteControlSheet = { viewModel.isConnectSheetVisible = true },
        onCode = { viewModel.code = it },
        onDismissRequest = {
            viewModel.code = ""
            viewModel.isConnectSheetVisible = false
        },
        modifier = modifier
    )
}

@Composable
private fun AppImpl(
    navController: NavHostController,
    isRemoteControlSheetVisible: Boolean,
    remoteControlSheetValue: RemoteControlSheetValue,
    broadcastCodeOnTv: String?,
    onBackPressed: (() -> Unit)?,
    openRemoteControlSheet: () -> Unit,
    onCode: (String) -> Unit,
    checkTvCodeOnSmartphone: () -> Unit,
    forgetTvCodeOnSmartphone: () -> Unit,
    onRemoteDirection: (RemoteDirection) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val preferences = hiltPreferences()

    val entry by navController.currentBackStackEntryAsState()

    val rootDestination by remember {
        derivedStateOf {
            Destination.Root.of(entry?.destination?.route)
        }
    }

    val navigateToChannel: () -> Unit = {
        if (!preferences.zappingMode || !PlayerActivity.isInPipMode) {
            val options = ActivityOptions.makeCustomAnimation(
                context,
                0,
                0
            )
            context.startActivity(
                Intent(context, PlayerActivity::class.java),
                options.toBundle()
            )
        }
    }

    Scaffold(
        rootDestination = rootDestination,
        onBackPressed = onBackPressed,
        navigateToChannel =navigateToChannel,
        navigateToRootDestination = {
            navController.navigate(it.name, navOptions {
                popUpTo(it.name) {
                    inclusive = true
                }
            })
        },
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        AppNavHost(
            navController = navController,
            navigateToRootDestination = { navController.navigate(it.name) },
            navigateToChannel = navigateToChannel,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        )
        // snack-host area
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small, Alignment.End),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(contentPadding)
                .padding(spacing.medium)
        ) {
            SnackHost(Modifier.weight(1f))
            AnimatedVisibility(
                visible = preferences.remoteControl,
                enter = scaleIn(initialScale = 0.65f) + fadeIn(),
                exit = scaleOut(targetScale = 0.65f) + fadeOut()
            ) {
                FloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = spacing.none,
                        pressedElevation = spacing.none,
                        focusedElevation = spacing.extraSmall,
                        hoveredElevation = spacing.extraSmall
                    ),
                    onClick = openRemoteControlSheet
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SettingsRemote,
                        contentDescription = "remote control"
                    )
                }
            }
        }

        RemoteControlSheet(
            value = remoteControlSheetValue,
            visible = isRemoteControlSheetVisible,
            onCode = onCode,
            checkTvCodeOnSmartphone = checkTvCodeOnSmartphone,
            forgetTvCodeOnSmartphone = forgetTvCodeOnSmartphone,
            onRemoteDirection = onRemoteDirection,
            onDismissRequest = onDismissRequest
        )

        Crossfade(
            targetState = broadcastCodeOnTv,
            label = "broadcast-code-on-tv",
            modifier = Modifier
                .padding(spacing.medium)
                .align(Alignment.BottomEnd)
        ) { code ->
            if (code != null) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamilies.JetbrainsMono
                )
            }
        }
    }
}

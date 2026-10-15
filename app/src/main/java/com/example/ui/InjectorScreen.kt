package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.FileStatus
import com.example.ui.components.ConsoleLogSection
import com.example.ui.components.DestinationFolderCard
import com.example.ui.components.GameSelectorDialog
import com.example.ui.components.GameTargetCard
import com.example.ui.components.ManagedFileCard
import com.example.ui.components.ScopedStorageExplainerDialog
import com.example.ui.theme.GamingAmberWarning
import com.example.ui.theme.GamingBorder
import com.example.ui.theme.GamingDarkBg
import com.example.ui.theme.GamingGreenSuccess
import com.example.ui.theme.GamingNeonBlue
import com.example.ui.theme.GamingOrangePrimary
import com.example.ui.theme.GamingRedError
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.GamingSurfaceElevated
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary

@Composable
fun InjectorScreen(
    viewModel: InjectorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // SAF File Picker (Multiple Documents)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        viewModel.onFilesSelected(uris)
    }

    // SAF Destination Folder Picker (Document Tree)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            viewModel.onDestinationFolderSelected(treeUri)
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.setToast(null)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GamingDarkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            GamingDarkBg,
                            Color(0xFF06090F)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top App Bar / Hero Branding
                item {
                    HeaderBrandingSection(
                        isOperationInProgress = state.isOperationInProgress,
                        onRefreshClick = { viewModel.refreshGameStatus() }
                    )
                }

                // Game Target Section (Selected Game, Version, Open Game, Select Game)
                item {
                    GameTargetCard(
                        game = state.selectedGame,
                        onSelectGameClick = { viewModel.setShowGameSelector(true) },
                        onOpenGameClick = { viewModel.openFreeFire() }
                    )
                }

                // Destination Folder & Scoped Storage Card
                item {
                    DestinationFolderCard(
                        destinationState = state.destinationState,
                        onConfigureClick = { viewModel.setShowStorageExplainer(true) }
                    )
                }

                // Managed Files Header & Actions (Add File, Inject All, Remove All)
                item {
                    ManagedFilesControlHeader(
                        totalFiles = state.totalManagedCount,
                        injectedCount = state.injectedCount,
                        readyCount = state.readyCount,
                        isOperationInProgress = state.isOperationInProgress,
                        onAddFileClick = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onInjectAllClick = { viewModel.injectAll() },
                        onRemoveAllClick = { viewModel.requestRemoveAll() }
                    )
                }

                // Managed Files List
                if (state.managedFiles.isEmpty()) {
                    item {
                        EmptyFilesPlaceholder(
                            onAddFileClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                        )
                    }
                } else {
                    items(state.managedFiles, key = { it.id }) { file ->
                        ManagedFileCard(
                            file = file,
                            isOperationInProgress = state.isOperationInProgress,
                            onInjectClick = { viewModel.injectFile(file.id) },
                            onRemoveClick = { viewModel.requestRemoveFile(file) },
                            onDiscardClick = { viewModel.discardManagedFile(file.id) }
                        )
                    }
                }

                // Console & Operations Log Section
                item {
                    ConsoleLogSection(logs = state.logs)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Modal: Scoped Storage Explainer Dialog
    if (state.showStorageExplainer) {
        ScopedStorageExplainerDialog(
            game = state.selectedGame,
            isAccessible = state.destinationState.isAccessible,
            onLaunchFolderPicker = {
                viewModel.setShowStorageExplainer(false)
                folderPickerLauncher.launch(null)
            },
            onDismiss = { viewModel.setShowStorageExplainer(false) }
        )
    }

    // Modal: Game Selector Dialog
    if (state.showGameSelector) {
        GameSelectorDialog(
            selectedGame = state.selectedGame,
            availableGames = state.availableGames,
            onSelectGame = { viewModel.selectGame(it) },
            onDismiss = { viewModel.setShowGameSelector(false) }
        )
    }

    // Modal: Single File Removal Confirmation Dialog
    state.filePendingRemoval?.let { file ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelRemoveFile() },
            containerColor = GamingSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = GamingRedError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm File Removal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to remove '${file.displayName}' from the destination folder?\n\nThis will only delete the file placed by FF DATA INJECTOR. Unrelated game files will NOT be touched.",
                    fontSize = 13.sp,
                    color = GamingTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeRemoveFile() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingRedError,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_remove_button")
                ) {
                    Text("REMOVE FILE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelRemoveFile() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GamingBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GamingTextMuted)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Modal: Remove All Confirmation Dialog
    if (state.showRemoveAllDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelRemoveAll() },
            containerColor = GamingSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = GamingRedError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remove All Injected Files",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                }
            },
            text = {
                Text(
                    text = "This will delete all files currently placed in the destination folder by this application.\n\nOnly managed files tracked by FF DATA INJECTOR will be removed.",
                    fontSize = 13.sp,
                    color = GamingTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeRemoveAll() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingRedError,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_remove_all_button")
                ) {
                    Text("REMOVE ALL (${state.injectedCount})", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelRemoveAll() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GamingBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GamingTextMuted)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun HeaderBrandingSection(
    isOperationInProgress: Boolean,
    onRefreshClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GamingSurfaceElevated),
        border = BorderStroke(1.dp, GamingBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // App Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0E17))
                    .border(1.2.dp, GamingNeonBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ff_logo),
                    contentDescription = "FF Data Injector Logo",
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FF DATA INJECTOR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GamingTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Professional Android Storage & Data Manager",
                    fontSize = 11.sp,
                    color = GamingNeonBlue
                )
            }

            if (isOperationInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = GamingOrangePrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("refresh_status_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Status",
                        tint = GamingTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagedFilesControlHeader(
    totalFiles: Int,
    injectedCount: Int,
    readyCount: Int,
    isOperationInProgress: Boolean,
    onAddFileClick: () -> Unit,
    onInjectAllClick: () -> Unit,
    onRemoveAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MANAGED FILES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingTextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$totalFiles / 10 slots • $injectedCount injected • Max 250 MB/file",
                    fontSize = 11.sp,
                    color = GamingTextMuted
                )
            }

            // ADD FILE BUTTON
            Button(
                onClick = onAddFileClick,
                enabled = !isOperationInProgress && totalFiles < 10,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamingOrangePrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("add_file_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ADD FILE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Secondary batch actions if files are present
        if (totalFiles > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (readyCount > 0) {
                    OutlinedButton(
                        onClick = onInjectAllClick,
                        enabled = !isOperationInProgress,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GamingNeonBlue.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GamingNeonBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inject_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "INJECT ALL ($readyCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (injectedCount > 0) {
                    OutlinedButton(
                        onClick = onRemoveAllClick,
                        enabled = !isOperationInProgress,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GamingRedError.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GamingRedError),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("remove_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "REMOVE ALL ($injectedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFilesPlaceholder(
    onAddFileClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GamingSurfaceElevated),
        border = BorderStroke(1.dp, GamingBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF132338))
                    .border(1.dp, GamingNeonBlue.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = GamingNeonBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No Files Selected",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GamingTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap 'ADD FILE' to choose up to 10 files (max 250 MB each) via the Storage Access Framework.",
                fontSize = 12.sp,
                color = GamingTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddFileClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamingOrangePrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("empty_state_add_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SELECT FILES FROM STORAGE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

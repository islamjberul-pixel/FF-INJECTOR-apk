package com.example.ui

import com.example.model.DestinationState
import com.example.model.GameTarget
import com.example.model.LogEntry
import com.example.model.ManagedFile

data class InjectorUiState(
    val selectedGame: GameTarget = GameTarget.defaultTargets().first(),
    val availableGames: List<GameTarget> = GameTarget.defaultTargets(),
    val destinationState: DestinationState = DestinationState(),
    val managedFiles: List<ManagedFile> = emptyList(),
    val isOperationInProgress: Boolean = false,
    val logs: List<LogEntry> = emptyList(),
    val toastMessage: String? = null,
    val showStorageExplainer: Boolean = false,
    val showGameSelector: Boolean = false,
    val filePendingRemoval: ManagedFile? = null,
    val showRemoveAllDialog: Boolean = false
) {
    val totalManagedCount: Int get() = managedFiles.size
    val injectedCount: Int get() = managedFiles.count { it.status == com.example.model.FileStatus.INJECTED }
    val readyCount: Int get() = managedFiles.count { it.status == com.example.model.FileStatus.READY }
}

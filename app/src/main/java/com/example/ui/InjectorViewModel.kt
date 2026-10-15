package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileRepository
import com.example.data.FileSelectionResult
import com.example.data.GameManager
import com.example.data.InjectResult
import com.example.data.RemoveResult
import com.example.data.StorageManager
import com.example.model.DestinationState
import com.example.model.FileStatus
import com.example.model.GameTarget
import com.example.model.LogEntry
import com.example.model.ManagedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InjectorViewModel(application: Application) : AndroidViewModel(application) {

    private val gameManager = GameManager(application)
    private val storageManager = StorageManager(application)
    private val repository = FileRepository(application)

    private val _uiState = MutableStateFlow(InjectorUiState())
    val uiState: StateFlow<InjectorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val savedGameId = repository.getSelectedGameId()
        val allGames = GameTarget.defaultTargets().map { gameManager.checkGameTarget(it) }
        val currentSelected = allGames.find { it.id == savedGameId } ?: allGames.first()

        val savedFiles = repository.loadManagedFiles()
        val savedTreeUriStr = repository.getSavedDestinationTreeUri()
        val savedFolderDisplay = repository.getSavedDestinationDisplayName()

        val destState = if (!savedTreeUriStr.isNullOrBlank()) {
            storageManager.evaluateDestinationTree(Uri.parse(savedTreeUriStr), currentSelected)
        } else {
            DestinationState(
                targetFolderPath = currentSelected.targetSubPath,
                treeUriString = null,
                folderDisplayName = savedFolderDisplay,
                isAccessible = false,
                isVerifiedTarget = false,
                isScopedStorageRestricted = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R,
                statusMessage = "Target destination: ${currentSelected.targetSubPath}. Authorize folder to allow file injection."
            )
        }

        _uiState.update {
            it.copy(
                selectedGame = currentSelected,
                availableGames = allGames,
                destinationState = destState,
                managedFiles = savedFiles
            )
        }

        addLog("INIT", "FF DATA INJECTOR initialized. Target package: ${currentSelected.packageName}")
        if (currentSelected.isInstalled) {
            addLog("GAME", "${currentSelected.displayName} detected: v${currentSelected.versionName}", isSuccess = true)
        } else {
            addLog("GAME", "${currentSelected.displayName} (${currentSelected.packageName}) is NOT installed on this device.", isError = true)
        }

        if (destState.isAccessible) {
            addLog("STORAGE", "Destination folder verified and writable: ${destState.folderDisplayName}", isSuccess = true)
        } else {
            addLog("SECURITY", "Destination access pending. Android Scoped Storage requires explicit folder authorization.")
        }
    }

    fun refreshGameStatus() {
        val updatedGames = _uiState.value.availableGames.map { gameManager.checkGameTarget(it) }
        val currentId = _uiState.value.selectedGame.id
        val updatedSelected = updatedGames.find { it.id == currentId } ?: updatedGames.first()

        _uiState.update {
            it.copy(
                availableGames = updatedGames,
                selectedGame = updatedSelected
            )
        }
        addLog("GAME", "Refreshed package status for ${updatedSelected.packageName}. Installed: ${updatedSelected.isInstalled}")
    }

    fun selectGame(target: GameTarget) {
        val checked = gameManager.checkGameTarget(target)
        repository.saveSelectedGameId(checked.id)

        // Re-evaluate current destination against new target
        val currentTreeUri = _uiState.value.destinationState.treeUriString
        val newDestState = if (!currentTreeUri.isNullOrBlank()) {
            storageManager.evaluateDestinationTree(Uri.parse(currentTreeUri), checked)
        } else {
            DestinationState(
                targetFolderPath = checked.targetSubPath,
                treeUriString = null,
                folderDisplayName = null,
                isAccessible = false,
                isVerifiedTarget = false,
                isScopedStorageRestricted = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R,
                statusMessage = "Target path: ${checked.targetSubPath}. Permission required."
            )
        }

        _uiState.update {
            it.copy(
                selectedGame = checked,
                destinationState = newDestState,
                showGameSelector = false
            )
        }

        addLog("GAME", "Selected active target: ${checked.displayName} (${checked.packageName})")
    }

    fun onFilesSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return

        var addedCount = 0
        var currentFiles = _uiState.value.managedFiles.toMutableList()

        for (uri in uris) {
            when (val result = storageManager.processSelectedUri(uri, currentFiles)) {
                is FileSelectionResult.Success -> {
                    currentFiles.add(result.file)
                    addedCount++
                    addLog("FILE", "Added '${result.file.displayName}' (${result.file.formattedSize})")
                }
                is FileSelectionResult.Error -> {
                    addLog("FILE", result.message, isError = true)
                    setToast(result.message)
                }
            }
        }

        if (addedCount > 0) {
            _uiState.update { it.copy(managedFiles = currentFiles) }
            repository.saveManagedFiles(currentFiles)
            setToast("Added $addedCount file(s) to managed queue.")
        }
    }

    fun onDestinationFolderSelected(treeUri: Uri) {
        val target = _uiState.value.selectedGame
        val eval = storageManager.evaluateDestinationTree(treeUri, target)

        repository.saveDestinationTreeUri(eval.treeUriString, eval.folderDisplayName)

        _uiState.update {
            it.copy(
                destinationState = eval,
                showStorageExplainer = false
            )
        }

        if (eval.isAccessible) {
            addLog("SAF", "Destination granted: ${eval.folderDisplayName} (Write Verified)", isSuccess = true)
            setToast("Destination verified and ready for file injection.")
        } else {
            addLog("SAF_ERROR", eval.statusMessage, isError = true)
            setToast(eval.statusMessage)
        }
    }

    fun injectFile(fileId: String) {
        val state = _uiState.value
        val file = state.managedFiles.find { it.id == fileId } ?: return

        if (!state.destinationState.isAccessible) {
            val errorMsg = "Cannot inject: Destination folder is not accessible. Android Scoped Storage blocked write permissions."
            updateFileStatus(fileId, FileStatus.BLOCKED_STORAGE, 0f, errorMsg)
            addLog("INJECT_FAIL", errorMsg, isError = true)
            _uiState.update { it.copy(showStorageExplainer = true) }
            setToast("Destination folder inaccessible. Please configure target directory.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            updateFileStatus(fileId, FileStatus.INJECTING, 0.05f, "Initiating streaming copy...")
            addLog("INJECT", "Starting injection: '${file.displayName}' (${file.formattedSize})")

            val destTreeUri = state.destinationState.treeUriString
            val result = storageManager.injectFile(
                file = file,
                destTreeUriString = destTreeUri,
                target = state.selectedGame,
                onProgress = { bytesCopied, totalBytes ->
                    val frac = if (totalBytes > 0) (bytesCopied.toFloat() / totalBytes).coerceIn(0f, 1f) else 0.5f
                    val percent = (frac * 100).toInt()
                    val copiedFormatted = ManagedFile.formatBytes(bytesCopied)
                    val totalFormatted = ManagedFile.formatBytes(totalBytes)
                    updateFileStatus(
                        fileId,
                        FileStatus.INJECTING,
                        frac,
                        "Copying bytes: $copiedFormatted / $totalFormatted ($percent%)"
                    )
                }
            )

            when (result) {
                is InjectResult.Success -> {
                    updateFileStatus(
                        fileId = fileId,
                        status = FileStatus.INJECTED,
                        progress = 1f,
                        message = "Injected & Verified (${ManagedFile.formatBytes(result.verifiedBytes)})",
                        destFileName = result.fileName,
                        timestamp = System.currentTimeMillis(),
                        verifiedBytes = result.verifiedBytes
                    )
                    addLog("INJECT_SUCCESS", "Successfully injected & verified '${result.fileName}' (${ManagedFile.formatBytes(result.verifiedBytes)})", isSuccess = true)
                    setToast("Successfully injected '${file.displayName}'")
                }
                is InjectResult.Failure -> {
                    updateFileStatus(fileId, FileStatus.FAILED, 0f, result.reason)
                    addLog("INJECT_ERROR", "Failed to inject '${file.displayName}': ${result.reason}", isError = true)
                    setToast("Injection failed: ${result.reason}")
                }
            }

            _uiState.update { it.copy(isOperationInProgress = false) }
            repository.saveManagedFiles(_uiState.value.managedFiles)
        }
    }

    fun injectAll() {
        val pendingFiles = _uiState.value.managedFiles.filter {
            it.status == FileStatus.READY || it.status == FileStatus.FAILED || it.status == FileStatus.BLOCKED_STORAGE
        }

        if (pendingFiles.isEmpty()) {
            setToast("No pending files ready to inject.")
            return
        }

        viewModelScope.launch {
            for (file in pendingFiles) {
                injectFile(file.id)
            }
        }
    }

    fun requestRemoveFile(file: ManagedFile) {
        _uiState.update { it.copy(filePendingRemoval = file) }
    }

    fun cancelRemoveFile() {
        _uiState.update { it.copy(filePendingRemoval = null) }
    }

    fun executeRemoveFile() {
        val file = _uiState.value.filePendingRemoval ?: return
        _uiState.update { it.copy(filePendingRemoval = null, isOperationInProgress = true) }

        viewModelScope.launch {
            val destTreeUri = _uiState.value.destinationState.treeUriString
            val result = storageManager.removeInjectedFile(file, destTreeUri)

            when (result) {
                is RemoveResult.Success -> {
                    updateFileStatus(file.id, FileStatus.REMOVED, 0f, "Removed from destination")
                    addLog("REMOVE", "Removed '${result.fileName}' from destination", isSuccess = true)
                    setToast("Removed '${file.displayName}' successfully.")
                }
                is RemoveResult.Failure -> {
                    addLog("REMOVE_ERROR", "Could not remove '${file.displayName}': ${result.reason}", isError = true)
                    setToast("Failed to remove: ${result.reason}")
                }
            }

            _uiState.update { it.copy(isOperationInProgress = false) }
            repository.saveManagedFiles(_uiState.value.managedFiles)
        }
    }

    fun discardManagedFile(fileId: String) {
        val updated = _uiState.value.managedFiles.filterNot { it.id == fileId }
        _uiState.update { it.copy(managedFiles = updated) }
        repository.saveManagedFiles(updated)
        addLog("DISCARD", "Removed file from managed list.")
    }

    fun requestRemoveAll() {
        val injected = _uiState.value.managedFiles.filter { it.status == FileStatus.INJECTED }
        if (injected.isEmpty()) {
            setToast("No injected files to remove.")
            return
        }
        _uiState.update { it.copy(showRemoveAllDialog = true) }
    }

    fun cancelRemoveAll() {
        _uiState.update { it.copy(showRemoveAllDialog = false) }
    }

    fun executeRemoveAll() {
        val injected = _uiState.value.managedFiles.filter { it.status == FileStatus.INJECTED }
        _uiState.update { it.copy(showRemoveAllDialog = false, isOperationInProgress = true) }

        viewModelScope.launch {
            val destTreeUri = _uiState.value.destinationState.treeUriString
            var removedCount = 0

            for (file in injected) {
                val res = storageManager.removeInjectedFile(file, destTreeUri)
                if (res is RemoveResult.Success) {
                    removedCount++
                    updateFileStatus(file.id, FileStatus.REMOVED, 0f, "Removed from destination")
                }
            }

            addLog("REMOVE_ALL", "Removed $removedCount injected files safely.", isSuccess = true)
            setToast("Removed $removedCount files from destination.")
            _uiState.update { it.copy(isOperationInProgress = false) }
            repository.saveManagedFiles(_uiState.value.managedFiles)
        }
    }

    fun openFreeFire() {
        val target = _uiState.value.selectedGame
        if (!target.isInstalled) {
            addLog("LAUNCH", "${target.displayName} is not installed. Opening Google Play Store...")
            val res = gameManager.openStorePage(target.packageName)
            if (res.isFailure) {
                setToast("Could not open Play Store: ${res.exceptionOrNull()?.localizedMessage}")
            }
            return
        }

        val launchResult = gameManager.launchGame(target.packageName)
        if (launchResult.isSuccess) {
            addLog("LAUNCH", "Launched ${target.displayName} successfully.", isSuccess = true)
            setToast("Opening ${target.displayName}...")
        } else {
            val err = launchResult.exceptionOrNull()?.localizedMessage ?: "Launch failed"
            addLog("LAUNCH_ERROR", "Failed to launch game: $err", isError = true)
            setToast("Failed to launch game: $err")
        }
    }

    private fun updateFileStatus(
        fileId: String,
        status: FileStatus,
        progress: Float,
        message: String,
        destFileName: String? = null,
        timestamp: Long? = null,
        verifiedBytes: Long? = null
    ) {
        val updated = _uiState.value.managedFiles.map { f ->
            if (f.id == fileId) {
                f.copy(
                    status = status,
                    progress = progress,
                    statusMessage = message,
                    destinationFileName = destFileName ?: f.destinationFileName,
                    injectedTimestamp = timestamp ?: f.injectedTimestamp,
                    verifiedByteSize = verifiedBytes ?: f.verifiedByteSize
                )
            } else {
                f
            }
        }
        _uiState.update { it.copy(managedFiles = updated) }
    }

    fun setShowStorageExplainer(show: Boolean) {
        _uiState.update { it.copy(showStorageExplainer = show) }
    }

    fun setShowGameSelector(show: Boolean) {
        _uiState.update { it.copy(showGameSelector = show) }
    }

    fun setToast(message: String?) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    private fun addLog(tag: String, message: String, isError: Boolean = false, isSuccess: Boolean = false) {
        val entry = LogEntry(
            tag = tag,
            message = message,
            isError = isError,
            isSuccess = isSuccess
        )
        _uiState.update {
            val newLogs = (listOf(entry) + it.logs).take(100)
            it.copy(logs = newLogs)
        }
    }
}

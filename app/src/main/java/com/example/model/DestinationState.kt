package com.example.model

data class DestinationState(
    val targetFolderPath: String = GameTarget.DEFAULT_DESTINATION_SUBPATH,
    val treeUriString: String? = null,
    val folderDisplayName: String? = null,
    val isAccessible: Boolean = false,
    val isVerifiedTarget: Boolean = false,
    val isScopedStorageRestricted: Boolean = true,
    val statusMessage: String = "Destination folder not yet granted. Scoped storage requires user authorization."
)

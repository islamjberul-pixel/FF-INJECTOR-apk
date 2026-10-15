package com.example.model

data class GameTarget(
    val id: String,
    val displayName: String,
    val packageName: String,
    val targetSubPath: String,
    val isInstalled: Boolean,
    val versionName: String? = null,
    val versionCode: Long = 0,
    val isPrimary: Boolean = true
) {
    companion object {
        const val PACKAGE_FREE_FIRE = "com.dts.freefireth"
        const val PACKAGE_FREE_FIRE_MAX = "com.dts.freefiremax"
        const val DEFAULT_DESTINATION_SUBPATH = "Android/data/com.dts.freefireth/files/"

        fun defaultTargets(): List<GameTarget> = listOf(
            GameTarget(
                id = "freefire_std",
                displayName = "Free Fire (Official)",
                packageName = PACKAGE_FREE_FIRE,
                targetSubPath = "Android/data/com.dts.freefireth/files/",
                isInstalled = false,
                isPrimary = true
            ),
            GameTarget(
                id = "freefire_max",
                displayName = "Free Fire MAX",
                packageName = PACKAGE_FREE_FIRE_MAX,
                targetSubPath = "Android/data/com.dts.freefiremax/files/",
                isInstalled = false,
                isPrimary = false
            )
        )
    }
}

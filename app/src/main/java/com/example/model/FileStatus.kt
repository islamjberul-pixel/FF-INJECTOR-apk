package com.example.model

enum class FileStatus(val label: String) {
    READY("Ready"),
    INJECTING("Injecting"),
    INJECTED("Injected"),
    REMOVED("Removed"),
    FAILED("Failed"),
    BLOCKED_STORAGE("Blocked by OS")
}

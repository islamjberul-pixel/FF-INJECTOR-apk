package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.model.GameTarget

class GameManager(private val context: Context) {

    fun checkGameTarget(target: GameTarget): GameTarget {
        return try {
            val pm = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(target.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(target.packageName, 0)
            }

            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            target.copy(
                isInstalled = true,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = versionCode
            )
        } catch (_: PackageManager.NameNotFoundException) {
            target.copy(
                isInstalled = false,
                versionName = null,
                versionCode = 0
            )
        } catch (_: Exception) {
            target.copy(
                isInstalled = false,
                versionName = null,
                versionCode = 0
            )
        }
    }

    fun launchGame(packageName: String): Result<Boolean> {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("No launch intent available for $packageName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openStorePage(packageName: String): Result<Boolean> {
        return try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
            Result.success(true)
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

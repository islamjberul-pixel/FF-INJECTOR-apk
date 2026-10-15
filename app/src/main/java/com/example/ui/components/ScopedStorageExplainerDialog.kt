package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameTarget
import com.example.ui.theme.GamingAmberWarning
import com.example.ui.theme.GamingBorder
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
fun ScopedStorageExplainerDialog(
    game: GameTarget,
    isAccessible: Boolean,
    onLaunchFolderPicker: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamingSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Storage Security",
                    tint = GamingNeonBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Storage Access & Android Security",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Why this destination folder is required:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingNeonBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Free Fire stores its user data and resource files in its official package directory:",
                    fontSize = 12.sp,
                    color = GamingTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = GamingSurfaceElevated),
                    border = BorderStroke(1.dp, GamingBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = game.targetSubPath,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = GamingOrangePrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Android 11+ Scoped Storage policy explanation
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    border = BorderStroke(1.dp, GamingAmberWarning.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Policy Alert",
                                tint = GamingAmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Android 11+ Scoped Storage Policy",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamingAmberWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Starting with Android 11 (API 30), Google enforces strict sandboxing: the system folder picker (SAF) directly prevents apps from selecting 'Android/data' and 'Android/obb'.\n\n" +
                                    "• FF DATA INJECTOR complies 100% with Android security and never uses root, memory hacking, or OS bypasses.\n" +
                                    "• If the Android OS blocks access to Android/data, the application transparently reports write denial.\n" +
                                    "• You can also authorize any test or backup folder (e.g. Download/FF_Data) to verify real streaming copy and file removal.",
                            fontSize = 11.sp,
                            color = GamingTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isAccessible)
                        "Status: Destination write access is currently verified and granted."
                    else
                        "Status: Permission not yet authorized or restricted by the operating system.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAccessible) GamingGreenSuccess else GamingAmberWarning
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onLaunchFolderPicker,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GamingOrangePrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("launch_folder_picker_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SELECT FOLDER IN SYSTEM PICKER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GamingBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GamingTextMuted
                )
            ) {
                Text(text = "CLOSE", fontSize = 11.sp)
            }
        }
    )
}

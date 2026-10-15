package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.model.DestinationState
import com.example.ui.theme.GamingAmberWarning
import com.example.ui.theme.GamingBorder
import com.example.ui.theme.GamingGreenSuccess
import com.example.ui.theme.GamingNeonBlue
import com.example.ui.theme.GamingRedError
import com.example.ui.theme.GamingSurfaceElevated
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary

@Composable
fun DestinationFolderCard(
    destinationState: DestinationState,
    onConfigureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GamingSurfaceElevated),
        border = BorderStroke(1.dp, GamingBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Folder Icon, Destination Label, Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (destinationState.isAccessible) GamingGreenSuccess.copy(alpha = 0.2f)
                            else if (destinationState.isScopedStorageRestricted) GamingRedError.copy(alpha = 0.2f)
                            else GamingAmberWarning.copy(alpha = 0.2f)
                        )
                ) {
                    Icon(
                        imageVector = if (destinationState.isAccessible) Icons.Default.VerifiedUser
                        else if (destinationState.isScopedStorageRestricted) Icons.Default.Lock
                        else Icons.Default.Folder,
                        contentDescription = "Destination Status",
                        tint = if (destinationState.isAccessible) GamingGreenSuccess
                        else if (destinationState.isScopedStorageRestricted) GamingRedError
                        else GamingAmberWarning,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DESTINATION FOLDER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = destinationState.targetFolderPath,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = GamingNeonBlue
                    )
                }

                // Status Pill
                val statusColor = when {
                    destinationState.isAccessible -> GamingGreenSuccess
                    destinationState.isScopedStorageRestricted -> GamingRedError
                    else -> GamingAmberWarning
                }
                val statusText = when {
                    destinationState.isAccessible -> "WRITABLE"
                    destinationState.isScopedStorageRestricted -> "BLOCKED"
                    else -> "PENDING"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Diagnostic message
            Text(
                text = destinationState.statusMessage,
                fontSize = 12.sp,
                color = GamingTextSecondary,
                lineHeight = 16.sp
            )

            if (destinationState.folderDisplayName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selected Directory: ${destinationState.folderDisplayName}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GamingTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button to Open Explainer & Folder Picker
            Button(
                onClick = onConfigureClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (destinationState.isAccessible) Color(0xFF1E293B)
                    else Color(0xFF0F2642),
                    contentColor = GamingNeonBlue
                ),
                border = BorderStroke(1.dp, GamingNeonBlue.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("destination_setup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Configure Destination",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (destinationState.isAccessible) "FOLDER PERMISSION GRANTED (TAP TO CHANGE)"
                    else "GRANT FOLDER ACCESS / STORAGE DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileStatus
import com.example.model.ManagedFile
import com.example.ui.theme.GamingAmberWarning
import com.example.ui.theme.GamingBorder
import com.example.ui.theme.GamingGreenSuccess
import com.example.ui.theme.GamingNeonBlue
import com.example.ui.theme.GamingOrangePrimary
import com.example.ui.theme.GamingRedError
import com.example.ui.theme.GamingSurfaceElevated
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary

@Composable
fun ManagedFileCard(
    file: ManagedFile,
    isOperationInProgress: Boolean,
    onInjectClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GamingSurfaceElevated),
        border = BorderStroke(1.dp, GamingBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: File icon, Name, Size, Discard button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF132338))
                        .border(1.dp, GamingNeonBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "File Icon",
                        tint = GamingNeonBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GamingTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = file.formattedSize,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GamingNeonBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Status badge
                        val statusColor = when (file.status) {
                            FileStatus.READY -> GamingAmberWarning
                            FileStatus.INJECTING -> GamingNeonBlue
                            FileStatus.INJECTED -> GamingGreenSuccess
                            FileStatus.REMOVED -> GamingTextMuted
                            FileStatus.FAILED, FileStatus.BLOCKED_STORAGE -> GamingRedError
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(0.8.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = file.status.label.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                // Discard / Remove from list button
                IconButton(
                    onClick = onDiscardClick,
                    enabled = !isOperationInProgress,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("discard_file_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Discard File",
                        tint = GamingTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Progress bar during injection
            AnimatedVisibility(visible = file.status == FileStatus.INJECTING) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    LinearProgressIndicator(
                        progress = { file.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GamingOrangePrimary,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status message
            Text(
                text = file.statusMessage,
                fontSize = 11.sp,
                color = when (file.status) {
                    FileStatus.FAILED, FileStatus.BLOCKED_STORAGE -> GamingRedError
                    FileStatus.INJECTED -> GamingGreenSuccess
                    FileStatus.INJECTING -> GamingNeonBlue
                    else -> GamingTextSecondary
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: INJECT and REMOVE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // INJECT button
                Button(
                    onClick = onInjectClick,
                    enabled = !isOperationInProgress && file.status != FileStatus.INJECTING,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingOrangePrimary,
                        disabledContainerColor = GamingOrangePrimary.copy(alpha = 0.3f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inject_button_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Inject",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (file.status == FileStatus.INJECTED) "RE-INJECT" else "INJECT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // REMOVE button (active if previously injected)
                OutlinedButton(
                    onClick = onRemoveClick,
                    enabled = !isOperationInProgress && file.status == FileStatus.INJECTED,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (file.status == FileStatus.INJECTED) GamingRedError.copy(alpha = 0.6f)
                        else GamingBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GamingRedError,
                        disabledContentColor = GamingTextMuted
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("remove_button_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REMOVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

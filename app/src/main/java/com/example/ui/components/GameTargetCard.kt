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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameTarget
import com.example.ui.theme.GamingAmberWarning
import com.example.ui.theme.GamingBorder
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingGreenSuccess
import com.example.ui.theme.GamingNeonBlue
import com.example.ui.theme.GamingOrangeHover
import com.example.ui.theme.GamingOrangePrimary
import com.example.ui.theme.GamingRedError
import com.example.ui.theme.GamingSurfaceElevated
import com.example.ui.theme.GamingTextMuted
import com.example.ui.theme.GamingTextPrimary
import com.example.ui.theme.GamingTextSecondary

@Composable
fun GameTargetCard(
    game: GameTarget,
    onSelectGameClick: () -> Unit,
    onOpenGameClick: () -> Unit,
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
            // Header Row: Game Badge, Name, Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Game Icon Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GamingOrangePrimary, Color(0xFFB45309))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Game Icon",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamingTextPrimary
                    )
                    Text(
                        text = game.packageName,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = GamingNeonBlue
                    )
                }

                // Installation Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (game.isInstalled) GamingGreenSuccess.copy(alpha = 0.15f)
                            else GamingRedError.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (game.isInstalled) GamingGreenSuccess.copy(alpha = 0.5f)
                            else GamingRedError.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (game.isInstalled) GamingGreenSuccess else GamingRedError)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (game.isInstalled) "INSTALLED" else "NOT INSTALLED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (game.isInstalled) GamingGreenSuccess else GamingRedError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Version info banner if installed
            if (game.isInstalled && game.versionName != null) {
                Text(
                    text = "Installed Version: v${game.versionName} (Build ${game.versionCode})",
                    fontSize = 11.sp,
                    color = GamingTextSecondary
                )
            } else {
                Text(
                    text = "Official Free Fire APK not detected. You can still manage data files or install via Play Store.",
                    fontSize = 11.sp,
                    color = GamingTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: SELECT GAME & OPEN FREE FIRE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectGameClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GamingNeonBlue.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GamingNeonBlue
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Select Game",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SELECT GAME",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onOpenGameClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamingOrangePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("open_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Open Free Fire",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (game.isInstalled) "OPEN FREE FIRE" else "GET ON PLAY STORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

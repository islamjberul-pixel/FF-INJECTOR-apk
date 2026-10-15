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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogEntry
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
fun ConsoleLogSection(
    logs: List<LogEntry>,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Console Logs",
                    tint = GamingNeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OPERATION CONSOLE & LOGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamingTextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${logs.size} entries",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GamingNeonBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Log Console Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF04070D))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "Console ready. No operations executed yet.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = GamingTextMuted,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        reverseLayout = false
                    ) {
                        items(logs, key = { it.id }) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = log.formattedTime,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = GamingTextMuted
                                )
                                Text(
                                    text = "[${log.tag}]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.isError) GamingRedError
                                    else if (log.isSuccess) GamingGreenSuccess
                                    else GamingOrangePrimary
                                )
                                Text(
                                    text = log.message,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = if (log.isError) GamingRedError.copy(alpha = 0.9f)
                                    else GamingTextSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

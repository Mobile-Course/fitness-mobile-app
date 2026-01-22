package com.fitness.app.ui.screens.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val bgColor = Color(0xFFF0F4F8)
    val accentDark = Color(0xFF343E4E)
    val cardBg = Color.White

    var pushNotifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = accentDark
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
        )

        Divider(color = Color(0xFFE2E8F0))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Notifications Card
            PreferenceCard(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                accentDark = accentDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Push Notifications", color = accentDark, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appearance Card
            PreferenceCard(
                icon = Icons.Outlined.LightMode,
                title = "Appearance",
                accentDark = accentDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Dark Mode", color = accentDark, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Units Card
            PreferenceCard(
                icon = Icons.Outlined.Language,
                title = "Units",
                accentDark = accentDark
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Metric (kg, km)", color = accentDark)
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Goal Card
            PreferenceCard(
                icon = Icons.Outlined.Lock,
                title = "Weekly Goal",
                accentDark = accentDark
            ) {
                Text(
                    text = "How many workouts per week?",
                    color = accentDark,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFF8FAFC)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "3 workouts per week", color = accentDark)
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PreferenceCard(
    icon: ImageVector,
    title: String,
    accentDark: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = accentDark, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentDark,
                        fontSize = 20.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

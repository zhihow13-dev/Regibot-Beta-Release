package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BallType
import com.example.model.BerryType
import com.example.model.CurveDirection
import com.example.model.PriorityMode
import com.example.model.ThrowStyle
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.viewmodel.RegiBotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RegiBotViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    var priorityExpanded by remember { mutableStateOf(false) }
    var throwStyleExpanded by remember { mutableStateOf(false) }
    var ballExpanded by remember { mutableStateOf(false) }
    var berryExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "REGIBOT CONFIGURATION",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanNeon,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Fine-tune inference speed, cycle timing, fast catch, and anti-detection parameters",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Real Game Integration Hook
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, if (settings.realGameIntegration) CyanNeon.copy(alpha = 0.6f) else ObsidianBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (settings.realGameIntegration) CyanNeon else AmberEnergy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (settings.realGameIntegration) "REAL POKÉMON GO HOOK" else "OFFLINE SIMULATION MODE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (settings.realGameIntegration)
                                        "Target: com.nianticlabs.pokemongo (Live Gestures & Screen Capture)"
                                    else
                                        "Kinematics calibration mode without game connection",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.realGameIntegration,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(realGameIntegration = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanNeon,
                                checkedTrackColor = CyanNeon.copy(alpha = 0.3f),
                                uncheckedThumbColor = AmberEnergy,
                                uncheckedTrackColor = ObsidianBorder
                            )
                        )
                    }
                }
            }
        }

        // Section: Vision & Automation Pipeline
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMPUTER VISION & INFERENCE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Cycle Interval Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cycle Interval", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("${settings.cycleIntervalMs} ms", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                        }
                        Text(
                            "Delay between frame processing passes. Lower = faster reaction, Higher = safer cadence.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.cycleIntervalMs.toFloat(),
                            onValueChange = { viewModel.setCycleInterval(it.toLong()) },
                            valueRange = 400f..3000f,
                            steps = 25,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanNeon,
                                activeTrackColor = CyanNeon,
                                inactiveTrackColor = ObsidianBorder
                            ),
                            modifier = Modifier.testTag("cycle_interval_slider")
                        )
                    }

                    // Results Per Inference
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Results Per Inference", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("${settings.resultsPerInference} targets", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanGlow)
                        }
                        Text(
                            "Maximum candidates evaluated in a single visual recognition frame.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.resultsPerInference.toFloat(),
                            onValueChange = { viewModel.updateSettings(settings.copy(resultsPerInference = it.toInt())) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanNeon,
                                activeTrackColor = CyanNeon,
                                inactiveTrackColor = ObsidianBorder
                            )
                        )
                    }
                }
            }
        }

        // Section: Catch Mechanics & Fast Catch
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = AmberEnergy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CATCH ENGINE & FAST CATCH",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Fast Catch Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fast Catch Skip", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                "Pulls ball tray drawer and exits encounter immediately after throw, saving 15 seconds per capture.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.fastCatchEnabled,
                            onCheckedChange = { viewModel.setFastCatchEnabled(it) },
                            modifier = Modifier.testTag("fast_catch_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberEnergy,
                                checkedTrackColor = ObsidianBorder
                            )
                        )
                    }

                    // Throw Kinematics Style Dropdown
                    ExposedDropdownMenuBox(
                        expanded = throwStyleExpanded,
                        onExpandedChange = { throwStyleExpanded = !throwStyleExpanded }
                    ) {
                        OutlinedTextField(
                            value = settings.throwStyle.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Throw Physics Technique") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = throwStyleExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = throwStyleExpanded,
                            onDismissRequest = { throwStyleExpanded = false }
                        ) {
                            ThrowStyle.values().forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(style.displayName, fontWeight = FontWeight.Bold)
                                            Text(style.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setThrowStyle(style)
                                        throwStyleExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Throw Power Velocity Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Throw Velocity & Height", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text(String.format("%.2fx", settings.throwPowerBoost), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberEnergy)
                        }
                        Text(
                            "Calibrates touch velocity for distant wild Pokémon. 1.0x = standard, 1.25x+ = high arc.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.throwPowerBoost,
                            onValueChange = { viewModel.setThrowPowerBoost(it) },
                            valueRange = 0.8f..1.4f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberEnergy,
                                activeTrackColor = AmberEnergy,
                                inactiveTrackColor = ObsidianBorder
                            )
                        )
                    }

                    // Priority Mode Dropdown
                    ExposedDropdownMenuBox(
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = !priorityExpanded }
                    ) {
                        OutlinedTextField(
                            value = settings.priorityMode.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority Workflow") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            PriorityMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(mode.title, fontWeight = FontWeight.Bold)
                                            Text(mode.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setPriorityMode(mode)
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Ball Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = ballExpanded,
                        onExpandedChange = { ballExpanded = !ballExpanded }
                    ) {
                        OutlinedTextField(
                            value = settings.ballType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ball Selector") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ballExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = ballExpanded,
                            onDismissRequest = { ballExpanded = false }
                        ) {
                            BallType.values().forEach { ball ->
                                DropdownMenuItem(
                                    text = { Text(ball.displayName) },
                                    onClick = {
                                        viewModel.setBallType(ball)
                                        ballExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Berry Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = berryExpanded,
                        onExpandedChange = { berryExpanded = !berryExpanded }
                    ) {
                        OutlinedTextField(
                            value = settings.berryType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Auto Berry Feeding") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = berryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = berryExpanded,
                            onDismissRequest = { berryExpanded = false }
                        ) {
                            BerryType.values().forEach { berry ->
                                DropdownMenuItem(
                                    text = { Text(berry.displayName) },
                                    onClick = {
                                        viewModel.setBerryType(berry)
                                        berryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Anti-Detection & Humanization
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANTI-DETECTION HUMANIZATION",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Random Jitter
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Random Delay Jitter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("±${settings.humanizeJitterMs} ms", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                        Text(
                            "Injects stochastic timing variation into touch dispatches to eliminate mechanical signatures.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.humanizeJitterMs.toFloat(),
                            onValueChange = { viewModel.setJitter(it.toInt()) },
                            valueRange = 50f..500f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldSuccess,
                                activeTrackColor = EmeraldSuccess,
                                inactiveTrackColor = ObsidianBorder
                            )
                        )
                    }

                    // Touch Variance
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Touch Dispersion Radius", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                            Text("±${settings.tapAccuracyVariancePx} px", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                        Text(
                            "Slight pixel randomness around target centers simulating fingertip surface contact.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.tapAccuracyVariancePx.toFloat(),
                            onValueChange = { viewModel.setAccuracyVariance(it.toInt()) },
                            valueRange = 4f..32f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldSuccess,
                                activeTrackColor = EmeraldSuccess,
                                inactiveTrackColor = ObsidianBorder
                            )
                        )
                    }
                }
            }
        }

        // Section: Session Actions
        item {
            Button(
                onClick = { viewModel.saveCurrentSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_session_button")
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color.Black
                )
            ) {
                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE & ARCHIVE SESSION TO LOCAL DATABASE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

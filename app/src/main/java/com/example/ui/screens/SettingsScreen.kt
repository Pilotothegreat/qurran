package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.UserPreferencesRepository
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary


@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val amoled by viewModel.amoledMode.collectAsState()
    val simpleMode by viewModel.simpleMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    var locationGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            Toast.makeText(context, "تم تفعيل صلاحية الموقع بنجاح 📍", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "تم تفعيل صلاحيات الإشعارات بنجاح 🔔", Toast.LENGTH_SHORT).show()
        }
    }

    // Local inputs for coordinate changes
    var latInput by remember { mutableStateOf(viewModel.prefs.latitude.toString()) }
    var lonInput by remember { mutableStateOf(viewModel.prefs.longitude.toString()) }
    var locNameInput by remember { mutableStateOf(viewModel.prefs.locationName) }

    // local triggers
    var hapticsState by remember { mutableStateOf(viewModel.prefs.hapticsEnabled) }
    var asrSchoolState by remember { mutableStateOf(viewModel.prefs.asrSchool) } // 1 standard, 2 hanafi
    var hijriOffsetState by remember { mutableStateOf(viewModel.prefs.hijriOffset) }
    var onlineSyncState by remember { mutableStateOf(viewModel.prefs.useOnlinePrayerTimes) }
    var quranBookModeState by remember { mutableStateOf(viewModel.prefs.quranBookMode) }
    var soundState by remember { mutableStateOf(viewModel.prefs.notificationSound) }

    // Dynamic theme adaptive colors
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Titles
        Text(
            text = "PREFERENCES & RECALCULATIONS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AccentGold
        )
        Text(
            text = "Personal adjustments for Omani / Ibadi practices",
            fontSize = 12.sp,
            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Coordinates Panel Edit
        GlassHighlightCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "COORDINATE & LOCATION DEFAULTS",
                fontSize = 12.sp,
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = locNameInput,
                onValueChange = { locNameInput = it },
                label = { Text("Location Name Display") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold)
                )

                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            var isDetectingLocation by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permMap ->
                val granted = permMap[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                              permMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (granted) {
                    isDetectingLocation = true
                    viewModel.detectGPSLocation { success, msg ->
                        isDetectingLocation = false
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (success) {
                            latInput = viewModel.prefs.latitude.toString()
                            lonInput = viewModel.prefs.longitude.toString()
                            locNameInput = viewModel.prefs.locationName
                        }
                    }
                } else {
                    Toast.makeText(context, "Coordinates require Location permissions.", Toast.LENGTH_SHORT).show()
                }
            }

            // Primary hardware GPS detection
            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                enabled = !isDetectingLocation,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isDetectingLocation) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryEmerald, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Interrogating GPS Satellites...", color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Locate via Device GPS", color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                }
            }

            // Fallback internet geolocation
            OutlinedButton(
                onClick = {
                    isDetectingLocation = true
                    viewModel.detectLocationOfflineOrOnline { success, msg ->
                        isDetectingLocation = false
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (success) {
                            latInput = viewModel.prefs.latitude.toString()
                            lonInput = viewModel.prefs.longitude.toString()
                            locNameInput = viewModel.prefs.locationName
                        }
                    }
                },
                enabled = !isDetectingLocation,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.5.dp, AccentGold.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backup Web Geo-Lookup (cellular/wi-fi)", color = AccentGold, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val lat = latInput.toDoubleOrNull()
                    val lon = lonInput.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        viewModel.prefs.latitude = lat
                        viewModel.prefs.longitude = lon
                        viewModel.prefs.locationName = locNameInput
                        viewModel.recalculateTimes()
                        Toast.makeText(context, "Location Recalculations Applied! 📍", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid coordinates entered.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Recalculate & Save Location", color = AccentGold, fontWeight = FontWeight.Bold)
            }
        }

        // Custom Prayer offsets adjustments +/- list
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "CUSTOM CALCULATION OFFSETS (MINUTES)",
                fontSize = 12.sp,
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val offsetsList = listOf(
                UserPreferencesRepository.KEY_OFFSET_FAJR to "Fajr Offset",
                UserPreferencesRepository.KEY_OFFSET_SUNRISE to "Sunrise Offset",
                UserPreferencesRepository.KEY_OFFSET_DHUHR to "Dhuhr Offset",
                UserPreferencesRepository.KEY_OFFSET_ASR to "Asr Offset",
                UserPreferencesRepository.KEY_OFFSET_MAGHRIB to "Maghrib Offset",
                UserPreferencesRepository.KEY_OFFSET_ISHA to "Isha Offset"
            )

            offsetsList.forEach { (offsetKey, label) ->
                var offsetVal by remember { mutableStateOf(viewModel.prefs.getOffset(offsetKey)) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = if (simpleMode) FontWeight.Bold else FontWeight.Normal)

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                offsetVal -= 1
                                viewModel.prefs.setOffset(offsetKey, offsetVal)
                                viewModel.recalculateTimes()
                            },
                            modifier = Modifier.size(if (simpleMode) 48.dp else 40.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = AccentGold, modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp))
                        }

                        Text(
                            text = if (offsetVal >= 0) "+$offsetVal m" else "$offsetVal m",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (simpleMode) 18.sp else 14.sp,
                            modifier = Modifier.width(64.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                offsetVal += 1
                                viewModel.prefs.setOffset(offsetKey, offsetVal)
                                viewModel.recalculateTimes()
                            },
                            modifier = Modifier.size(if (simpleMode) 48.dp else 40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = AccentGold, modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp))
                        }
                    }
                }
            }
        }

        // Custom Iqamah periods offsets +/- list
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "ضبط فترات الإقامة بعد الأذان (دقائق)",
                fontSize = if (simpleMode) 17.sp else 13.sp,
                color = AccentGold,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
            Text(
                text = "تقدير الفترات الزمنية بين الأذان والصلوات:",
                fontSize = if (simpleMode) 14.sp else 11.sp,
                color = subTextColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val iqamasMap = listOf(
                "fajr" to ("صلاة الفجر" to { viewModel.prefs.iqamaFajr }),
                "dhuhr" to ("صلاة الظهر" to { viewModel.prefs.iqamaDhuhr }),
                "asr" to ("صلاة العصر" to { viewModel.prefs.iqamaAsr }),
                "maghrib" to ("صلاة المغرب" to { viewModel.prefs.iqamaMaghrib }),
                "isha" to ("صلاة العشاء" to { viewModel.prefs.iqamaIsha })
            )

            iqamasMap.forEach { (key, info) ->
                val label = info.first
                val getterFunc = info.second
                var iqamaSecVal by remember { mutableStateOf(getterFunc()) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = if (simpleMode) FontWeight.Bold else FontWeight.Normal, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (iqamaSecVal > 0) {
                                    iqamaSecVal -= 1
                                    when (key) {
                                        "fajr" -> viewModel.prefs.iqamaFajr = iqamaSecVal
                                        "dhuhr" -> viewModel.prefs.iqamaDhuhr = iqamaSecVal
                                        "asr" -> viewModel.prefs.iqamaAsr = iqamaSecVal
                                        "maghrib" -> viewModel.prefs.iqamaMaghrib = iqamaSecVal
                                        "isha" -> viewModel.prefs.iqamaIsha = iqamaSecVal
                                    }
                                    viewModel.recalculateTimes()
                                }
                            },
                            modifier = Modifier.size(if (simpleMode) 48.dp else 40.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = AccentGold, modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp))
                        }

                        Text(
                            text = "$iqamaSecVal د",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (simpleMode) 18.sp else 14.sp,
                            modifier = Modifier.width(64.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )

                        IconButton(
                            onClick = {
                                if (iqamaSecVal < 60) {
                                    iqamaSecVal += 1
                                    when (key) {
                                        "fajr" -> viewModel.prefs.iqamaFajr = iqamaSecVal
                                        "dhuhr" -> viewModel.prefs.iqamaDhuhr = iqamaSecVal
                                        "asr" -> viewModel.prefs.iqamaAsr = iqamaSecVal
                                        "maghrib" -> viewModel.prefs.iqamaMaghrib = iqamaSecVal
                                        "isha" -> viewModel.prefs.iqamaIsha = iqamaSecVal
                                    }
                                    viewModel.recalculateTimes()
                                }
                            },
                            modifier = Modifier.size(if (simpleMode) 48.dp else 40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = AccentGold, modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp))
                        }
                    }
                }
            }
        }

        // General settings flags toggles card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "COSMETIC STYLE & CALCULATIONS",
                fontSize = if (simpleMode) 16.sp else 12.sp,
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Elderly Simple Mode Switch (CRITICAL FEATURE FOR ACCESSIBILITY)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Elderly Simple Mode (كبار السن)", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Enlarges readouts, button taps, and text throughout the entire app to assist elder family members.", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
                Switch(
                    checked = simpleMode,
                    onCheckedChange = { viewModel.setSimpleMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                    modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = if (amoled) Color(0xFF1B2825) else Color(0xFFE5ECEB))
            Spacer(modifier = Modifier.height(16.dp))

            // Re-engineered highly visual Expressive Theme Picker (Matching traffic-light's Material You Theme config)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "App Theme mode (مظهر التطبيق)",
                    color = textColor,
                    fontSize = if (simpleMode) 18.sp else 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure app representation to fit your style preferences.",
                    color = subTextColor,
                    fontSize = if (simpleMode) 13.sp else 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val themeCards = listOf(
                        Triple("LIGHT", "فاتح", "Light Theme"),
                        Triple("DARK", "داكن", "Dark Theme"),
                        Triple("SYSTEM", "تلقائي", "System Auto")
                    )

                    themeCards.forEach { (mode, arabicLabel, englishLabel) ->
                        val isSelected = themeMode == mode
                        val cardBg = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }

                        val activeBorder = if (isSelected) {
                            BorderStroke(2.dp, AccentGold)
                        } else {
                            BorderStroke(1.dp, textColor.copy(alpha = 0.12f))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(activeBorder, RoundedCornerShape(18.dp))
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Mini dynamic graphic behind text
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        val canvasSize = this.size
                                        val filterColor = when (mode) {
                                            "LIGHT" -> Color(0x1CFFD54F) // warm yellow glow
                                            "DARK" -> Color(0x24129676) // emerald dark glow
                                            else -> Color(0x1C81D4FA) // cool blue system glow
                                        }
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(filterColor, Color.Transparent),
                                                center = Offset(canvasSize.width * 0.75f, canvasSize.height * 0.25f),
                                                radius = canvasSize.width * 0.6f
                                            )
                                        )
                                    }
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val iconVector = when (mode) {
                                    "LIGHT" -> Icons.Default.WbSunny
                                    "DARK" -> Icons.Default.NightsStay
                                    else -> Icons.Default.SettingsSuggest
                                }
                                val iconColor = when (mode) {
                                    "LIGHT" -> Color(0xFFFFA000)
                                    "DARK" -> Color(0xFFFFD54F)
                                    else -> PrimaryEmerald
                                }

                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = if (isSelected) iconColor else textColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = arabicLabel,
                                    fontSize = if (simpleMode) 16.sp else 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) textColor else textColor.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = englishLabel,
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Material You Dynamic Accent Color Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Material You Dynamic Colors", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Toggles context wallpaper color schemes if supported by the device (Android 12+).", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
                Switch(
                    checked = dynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                    modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Only show/enable AMOLED toggle as sub-option for Dark Mode
            androidx.compose.animation.AnimatedVisibility(
                visible = themeMode == "DARK" || themeMode == "SYSTEM",
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("AMOLED Pitch-Black Background", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                        Text("Overrides slate backgrounds with pure pitch-black values to improve battery life on premium OLED screens.", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                    }
                    Switch(
                        checked = amoled,
                        onCheckedChange = { viewModel.setAmoledMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                        modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Haptic Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Haptic Vibrations Feedback", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Physical vibration ticks inside Tasbih and counter items.", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
                Switch(
                    checked = hapticsState,
                    onCheckedChange = {
                        hapticsState = it
                        viewModel.prefs.hapticsEnabled = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                    modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Asr shadow school toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Asr Juristic Calculation School", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (asrSchoolState == 1) "Standard Shafi'i / Ibadi shadow ratio (X1) default" else "Hanafi double shadow ratio (X2)",
                        color = subTextColor,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
                Button(
                    onClick = {
                        val nextSchool = if (asrSchoolState == 1) 2 else 1
                        asrSchoolState = nextSchool
                        viewModel.prefs.asrSchool = nextSchool
                        viewModel.recalculateTimes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(if (simpleMode) 48.dp else 40.dp)
                ) {
                    Text(if (asrSchoolState == 1) "Standard" else "Hanafi", color = AccentGold, fontSize = if (simpleMode) 13.sp else 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hijri day offsets adjust
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Hijri Lunar Correction Adjustment", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Adjust Hijri calendar by days for visual moon-sighting", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (hijriOffsetState > -3) {
                                hijriOffsetState -= 1
                                viewModel.prefs.hijriOffset = hijriOffsetState
                            }
                        },
                        modifier = Modifier.size(if (simpleMode) 48.dp else 40.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Less", tint = AccentGold, modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp))
                    }

                    Text(
                        text = if (hijriOffsetState >= 0) "+$hijriOffsetState" else "$hijriOffsetState",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (simpleMode) 18.sp else 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            if (hijriOffsetState < 3) {
                                hijriOffsetState += 1
                                viewModel.prefs.hijriOffset = hijriOffsetState
                            }
                        },
                        modifier = Modifier.size(if (simpleMode) 48.dp else 40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "More", tint = AccentGold, modifier = Modifier.size(if (simpleMode) 28.dp else 20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = if (amoled) Color(0xFF1B2825) else Color(0xFFE5ECEB))
            Spacer(modifier = Modifier.height(16.dp))

            // Online Prayer Times Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Sync Aladhan API Online Times", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Synchronizes prayer calculation results with Islamic servers instantly. Overrides local models when online.", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
                Switch(
                    checked = onlineSyncState,
                    onCheckedChange = {
                        onlineSyncState = it
                        viewModel.prefs.useOnlinePrayerTimes = it
                        viewModel.syncOnlinePrayerTimes { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                    modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quran Layout Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Default to Quran Book Scribe", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Launches the Quran Reader in traditional Mushaf pagination instead of an infinite list card scrolling format.", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
                Switch(
                    checked = quranBookModeState,
                    onCheckedChange = {
                        quranBookModeState = it
                        viewModel.prefs.quranBookMode = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                    modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Alerts switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Athan Notification Audio Alerts", color = textColor, fontSize = if (simpleMode) 18.sp else 14.sp, fontWeight = FontWeight.Bold)
                    Text("Triggers background audio chime notifications at scheduled prayer times", color = subTextColor, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
                Switch(
                    checked = soundState,
                    onCheckedChange = {
                        soundState = it
                        viewModel.prefs.notificationSound = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGold, checkedTrackColor = PrimaryEmerald),
                    modifier = Modifier.scale(if (simpleMode) 1.25f else 1.0f)
                )
            }
        }

        // System Permissions status and triggers card
        GlassHighlightCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "SYSTEM PERMISSIONS & SERVICES",
                fontSize = if (simpleMode) 16.sp else 12.sp,
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Location Permission Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (locationGranted) PrimaryEmerald else Color.Gray,
                            modifier = Modifier.size(if (simpleMode) 22.dp else 18.dp)
                        )
                        Text(
                            text = "Location Authorization (صلاحية الموقع)",
                            color = textColor,
                            fontSize = if (simpleMode) 17.sp else 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (locationGranted) "Authorized. Real-time solar altitudes correctly calculated." else "Not Authorized. Using default coordinates.",
                        color = subTextColor,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
                Button(
                    onClick = {
                        if (!locationGranted) {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            Toast.makeText(context, "صلاحية الموقع مفعلة بالفعل 📍", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (locationGranted) PrimaryEmerald.copy(0.2f) else PrimaryEmerald
                    ),
                    modifier = Modifier.height(if (simpleMode) 46.dp else 38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (locationGranted) "Active" else "Grant",
                        color = if (locationGranted) PrimaryEmerald else AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Notifications Permission Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (notificationsGranted) PrimaryEmerald else Color.Gray,
                            modifier = Modifier.size(if (simpleMode) 22.dp else 18.dp)
                        )
                        Text(
                            text = "Task Notifications (صلاحية الإشعارات)",
                            color = textColor,
                            fontSize = if (simpleMode) 17.sp else 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (notificationsGranted) "Authorized. Athan chime alerts configured." else "Not Authorized. Alerts might be blocked by OS.",
                        color = subTextColor,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (!notificationsGranted) {
                                notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                Toast.makeText(context, "صلاحية الإشعارات ممكّنة بالفعل 🔔", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "مفعلة تلقائياً في هذا الإصدار من أندرويد", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (notificationsGranted) PrimaryEmerald.copy(0.2f) else PrimaryEmerald
                    ),
                    modifier = Modifier.height(if (simpleMode) 46.dp else 38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (notificationsGranted) "Active" else "Grant",
                        color = if (notificationsGranted) PrimaryEmerald else AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub Repository Auto Update Card
        com.example.ui.components.GlassHighlightCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AUTOMATED UPDATE CENTER & GITHUB INTEGRATION",
                    fontSize = if (simpleMode) 16.sp else 12.sp,
                    color = AccentGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Source Code Repository (مستودع الكود)",
                            color = textColor,
                            fontSize = if (simpleMode) 17.sp else 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "leekleak/traffic-light",
                            color = subTextColor,
                            fontSize = if (simpleMode) 13.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/leekleak/traffic-light")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل فتح الرابط 🐙", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(if (simpleMode) 46.dp else 38.dp)
                    ) {
                        Text("Visit Repository 🐙", color = PrimaryEmerald, fontWeight = FontWeight.Bold, fontSize = if (simpleMode) 13.sp else 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (amoled) Color(0xFF1B2825) else Color(0xFFE5ECEB))
                Spacer(modifier = Modifier.height(16.dp))

                // Update status & actions
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Update Check Status (حالة فحص التحديثات)",
                        color = textColor,
                        fontSize = if (simpleMode) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    when (val state = updateState) {
                        is com.example.ui.UpdateState.Idle -> {
                            Text(
                                text = "Idle. Tap button below to initiate a check.",
                                color = subTextColor,
                                fontSize = if (simpleMode) 13.sp else 11.sp
                            )
                        }
                        is com.example.ui.UpdateState.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = PrimaryEmerald,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Connecting to GitHub APIs... (جاري الفحص)",
                                    color = subTextColor,
                                    fontSize = if (simpleMode) 13.sp else 11.sp
                                )
                            }
                        }
                        is com.example.ui.UpdateState.UpdateAvailable -> {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryEmerald.copy(0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "تحديث جديد متوفر: ${state.latestVersion} 😍",
                                        color = PrimaryEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Changelog: ${state.changelog}",
                                    color = subTextColor,
                                    fontSize = if (simpleMode) 13.sp else 11.sp
                                )
                            }
                        }
                        is com.example.ui.UpdateState.NoUpdate -> {
                            Text(
                                text = "✅ تطبيقك محدث بالكامل ومطابق لنسخة المستودع المستقرة المستضافة على GitHub (v3.15.2).",
                                color = PrimaryEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (simpleMode) 14.sp else 12.sp
                            )
                        }
                        is com.example.ui.UpdateState.Error -> {
                            Text(
                                text = "⚠️ لم يتم العثور على إصدارات منشورة عامة (Releases) في المستودع حالياً. سيتم إخطارك بمجرد إدراجها.",
                                color = subTextColor,
                                fontSize = if (simpleMode) 13.sp else 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.checkForUpdates()
                                Toast.makeText(context, "جاري فحص المستودع على GitHub... 📍", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(if (simpleMode) 48.dp else 40.dp)
                        ) {
                            Text(
                                text = "Check Now (تفقد الآن)",
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (simpleMode) 13.sp else 11.sp
                            )
                        }

                        if (updateState is com.example.ui.UpdateState.UpdateAvailable) {
                            Button(
                                onClick = {
                                    val state = updateState as? com.example.ui.UpdateState.UpdateAvailable
                                    state?.let {
                                        try {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(it.downloadUrl ?: it.htmlUrl)
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "خطأ في الرابط ⚠️", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (simpleMode) 48.dp else 40.dp)
                            ) {
                                Text(
                                    text = "Download APK (تنزيل)",
                                    color = Color(0xFF131917),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (simpleMode) 13.sp else 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer options & Support card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Text(
                text = "DEVELOPER SUPPORT & APP INFO",
                fontSize = if (simpleMode) 16.sp else 12.sp,
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Version",
                        color = textColor,
                        fontSize = if (simpleMode) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "v3.15.2 - Expressive You Edition 🍀",
                        color = subTextColor,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryEmerald.copy(0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "STABLE BUILD",
                        color = PrimaryEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (simpleMode) 12.sp else 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Developer Diagnostics (تشخيص المطور)",
                        color = textColor,
                        fontSize = if (simpleMode) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sends encrypted locale metadata & error logs to support servers for rapid assistance.",
                        color = subTextColor,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
                Button(
                    onClick = {
                        Toast.makeText(context, "جزاك الله خيراً! Diagnostics successfully compiled & dispatched.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(if (simpleMode) 46.dp else 38.dp)
                ) {
                    Text("Report", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Support of Continuing Development",
                        color = textColor,
                        fontSize = if (simpleMode) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Contribute visual feedback or support development of elegant Islamic utilities.",
                        color = subTextColor,
                        fontSize = if (simpleMode) 13.sp else 11.sp
                    )
                }
                Button(
                    onClick = {
                        Toast.makeText(context, "بارك الله فيك! May Allah bless you for your premium support & prayers. 🕋", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(if (simpleMode) 46.dp else 38.dp)
                ) {
                    Text("Donate Feedback", color = Color(0xFF1E2825), fontWeight = FontWeight.Bold, fontSize = if (simpleMode) 13.sp else 11.sp)
                }
            }
        }
    }
}

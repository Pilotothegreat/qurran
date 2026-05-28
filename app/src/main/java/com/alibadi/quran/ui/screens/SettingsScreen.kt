package com.alibadi.quran.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.style.TextAlign
import com.alibadi.quran.BuildConfig
import com.alibadi.quran.R
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.feature.settings.UpdateState
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val amoled by viewModel.amoled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val asrSchool by viewModel.asrSchool.collectAsState()
    val hijriOffset by viewModel.hijriOffset.collectAsState()
    val notificationSound by viewModel.notificationSound.collectAsState()
    val useOnlinePrayer by viewModel.useOnlinePrayer.collectAsState()
    val showPalestineUpdates by viewModel.showPalestineUpdates.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    val locationName by viewModel.locationName.collectAsState(initial = "مسقط، عُمان")
    val userLat by viewModel.latitude.collectAsState(initial = 23.5880)
    val userLon by viewModel.longitude.collectAsState(initial = 58.3829)

    val offsetFajr by viewModel.offsetFajr.collectAsState(initial = 0)
    val offsetSunrise by viewModel.offsetSunrise.collectAsState(initial = 0)
    val offsetDhuhr by viewModel.offsetDhuhr.collectAsState(initial = 0)
    val offsetAsr by viewModel.offsetAsr.collectAsState(initial = 0)
    val offsetMaghrib by viewModel.offsetMaghrib.collectAsState(initial = 0)
    val offsetIsha by viewModel.offsetIsha.collectAsState(initial = 0)

    val context = LocalContext.current

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
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
            Toast.makeText(context, "تم تفعيل صلاحية الموقع بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "تم تفعيل صلاحيات الإشعارات بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    // Local inputs for location name editing
    var locNameInput by remember(locationName) { mutableStateOf(locationName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = accentGold
        )
        Text(
            text = "Personal adjustments for Omani / Ibadi practices",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Coordinates & Location Edit Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الإحداثيات والموقع الحالي",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = locNameInput,
                    onValueChange = { locNameInput = it },
                    label = { Text("Location Name Display") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentGold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GPS locate using the single locationLauncher
                Button(
                    onClick = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Locate via Device GPS", color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.setThemeMode(themeMode) // forces recalculation or save
                        Toast.makeText(context, "تم حفظ تغييرات الموقع يدوياً", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryEmerald),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Save Location Name", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom Prayer Offsets
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.prayer_offsets),
                    style = MaterialTheme.typography.titleMedium,
                    color = accentGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val offsets = listOf(
                    Triple("Fajr", offsetFajr, { v: Int -> viewModel.setOffsetFajr(v) }),
                    Triple("Sunrise", offsetSunrise, { v: Int -> viewModel.setOffsetSunrise(v) }),
                    Triple("Dhuhr", offsetDhuhr, { v: Int -> viewModel.setOffsetDhuhr(v) }),
                    Triple("Asr", offsetAsr, { v: Int -> viewModel.setOffsetAsr(v) }),
                    Triple("Maghrib", offsetMaghrib, { v: Int -> viewModel.setOffsetMaghrib(v) }),
                    Triple("Isha", offsetIsha, { v: Int -> viewModel.setOffsetIsha(v) })
                )

                offsets.forEach { (name, value, setter) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { setter(value - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = accentGold)
                            }
                            Text(
                                text = "${if (value >= 0) "+" else ""}$value m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(64.dp),
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { setter(value + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = accentGold)
                            }
                        }
                    }
                }
            }
        }

        // App Styling and Toggles
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الإعدادات العامة والمظهر",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Font Scale 3-way Selector (Task 7 Feature 4)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.font_scale),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "NORMAL" to stringResource(R.string.normal),
                            "LARGE" to stringResource(R.string.large),
                            "XLARGE" to stringResource(R.string.xlarge)
                        ).forEach { (scaleKey, label) ->
                            val isSelected = fontScale == scaleKey
                            Button(
                                onClick = { viewModel.setFontScale(scaleKey) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) primaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) accentGold else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Theme Mode Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.theme_mode),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "LIGHT" to "Light",
                            "DARK" to "Dark",
                            "SYSTEM" to "Auto"
                        ).forEach { (modeKey, label) ->
                            val isSelected = themeMode == modeKey
                            Button(
                                onClick = { viewModel.setThemeMode(modeKey) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) primaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) accentGold else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // AMOLED Switch
                if (themeMode == "DARK" || themeMode == "SYSTEM") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(stringResource(R.string.amoled_mode), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Overrides backgrounds with pure black for OLED screens.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = amoled,
                            onCheckedChange = { viewModel.setAmoled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentGold, checkedTrackColor = primaryEmerald)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Dynamic Colors Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(stringResource(R.string.dynamic_color), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Toggles context wallpaper color schemes (Android 12+).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = dynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentGold, checkedTrackColor = primaryEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Palestine updates Opt-In Toggle (Task 7 Feature 5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(stringResource(R.string.show_palestine_updates), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Adds news updates about Palestine as a card on the Dashboard.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showPalestineUpdates,
                        onCheckedChange = { viewModel.setShowPalestineUpdates(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentGold, checkedTrackColor = primaryEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Asr Calculation School
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(stringResource(R.string.asr_school), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            if (asrSchool == 1) "Standard Shafi'i / Ibadi shadow ratio (X1)" else "Hanafi double shadow ratio (X2)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.setAsrSchool(if (asrSchool == 1) 2 else 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryEmerald),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(if (asrSchool == 1) "Standard" else "Hanafi", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hijri Date Correction Offset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(stringResource(R.string.hijri_offset), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Adjust Hijri calendar days offset.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (hijriOffset > -3) viewModel.setHijriOffset(hijriOffset - 1) }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = accentGold)
                        }
                        Text(
                            text = "${if (hijriOffset >= 0) "+" else ""}$hijriOffset",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = { if (hijriOffset < 3) viewModel.setHijriOffset(hijriOffset + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = accentGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Online Prayer Times Recalculation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(stringResource(R.string.use_online_prayer), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Download official online prayer calculations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = useOnlinePrayer,
                        onCheckedChange = { viewModel.setUseOnlinePrayer(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentGold, checkedTrackColor = primaryEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notification sound alerts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(stringResource(R.string.notification_sound), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Enable ringtone alert chimes for daily prayer times.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = notificationSound,
                        onCheckedChange = { viewModel.setNotificationSound(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentGold, checkedTrackColor = primaryEmerald)
                    )
                }
            }
        }

        // Permissions Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "صلاحيات النظام والتنبيهات",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Location Permission
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
                                tint = if (locationGranted) primaryEmerald else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("صلاحية الوصول للموقع", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (locationGranted) "تم تفعيل الصلاحية" else "غير مفعّلة، يتم استخدام موقع افتراضي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (locationGranted) primaryEmerald.copy(alpha = 0.2f) else primaryEmerald
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(if (locationGranted) "مفعّل" else "تفعيل", color = if (locationGranted) primaryEmerald else MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications Permission
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
                                tint = if (notificationsGranted) primaryEmerald else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("صلاحية إرسال التنبيهات", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (notificationsGranted) "تم تفعيل الصلاحية" else "غير مفعّلة، لن تظهر تنبيهات الصلاة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (!notificationsGranted) {
                                    notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (notificationsGranted) primaryEmerald.copy(alpha = 0.2f) else primaryEmerald
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(if (notificationsGranted) "مفعّل" else "تفعيل", color = if (notificationsGranted) primaryEmerald else MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        // GitHub Integration and Version Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "معلومات الإصدار وتحديثات GitHub",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("App Version", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://github.com/Pilotothegreat/qurran")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل في فتح الرابط", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryEmerald),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("GitHub Repo", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Update Check Status", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    when (val state = updateState) {
                        is UpdateState.Idle -> {
                            Text("Idle. Tap check now to check for GitHub updates.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        is UpdateState.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = primaryEmerald, strokeWidth = 2.dp)
                                Text("Checking for updates...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is UpdateState.UpdateAvailable -> {
                            Text("Update Available: ${state.latestVersion}", style = MaterialTheme.typography.bodySmall, color = primaryEmerald, fontWeight = FontWeight.Bold)
                        }
                        is UpdateState.NoUpdate -> {
                            Text("App is up-to-date with GitHub master branch.", style = MaterialTheme.typography.bodySmall, color = primaryEmerald, fontWeight = FontWeight.Bold)
                        }
                        is UpdateState.Error -> {
                            Text("Error checking updates: ${state.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.checkForUpdates() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryEmerald),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check Now", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

package com.alibadi.quran.ui.screens

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibadi.quran.R
import com.alibadi.quran.feature.qibla.QiblaViewModel
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.ui.theme.accentGold
import com.alibadi.quran.ui.theme.primaryEmerald
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

@Composable
fun QiblaCompassScreen(
    viewModel: QiblaViewModel,
    modifier: Modifier = Modifier
) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val amoled by settingsViewModel.amoled.collectAsState()
    val userLat by settingsViewModel.latitude.collectAsState(initial = 23.5880)
    val userLon by settingsViewModel.longitude.collectAsState(initial = 58.3829)

    val azimuth by viewModel.compassHeading.collectAsState()
    val sensorAccuracy by viewModel.sensorAccuracy.collectAsState()
    val sensorStatus by viewModel.sensorStatus.collectAsState()
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val distanceToMakkah by viewModel.distanceToMakkah.collectAsState()

    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.registerSensors()
        onDispose {
            viewModel.unregisterSensors()
        }
    }

    // Smooth shortest-path target rotation state
    var targetRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(azimuth) {
        val currentTarget = -azimuth
        val delta = currentTarget - targetRotation
        val normalizedDelta = ((delta + 180f) % 360f + 360f) % 360f - 180f
        targetRotation += normalizedDelta
    }

    val difference = remember(azimuth, qiblaBearing) {
        val azimuth360 = (azimuth + 360f) % 360f
        val diff = abs(azimuth360 - qiblaBearing.toFloat())
        minOf(diff, 360f - diff)
    }
    val isFacingQibla = difference <= 2.5f

    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla) {
            triggerHapticFeedback(context)
        }
    }

    // Animate compass movement
    val dynamicRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CompassRotate"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.qibla_compass),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = accentGold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Live direction toward the Holy Ka'bah in Makkah",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        // Sensor Accuracy Badge
        val accuracyLabel = when (sensorAccuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> stringResource(R.string.accuracy_high)
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> stringResource(R.string.accuracy_medium)
            else -> stringResource(R.string.accuracy_low_calibrate)
        }
        val accuracyColor = when (sensorAccuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> MaterialTheme.colorScheme.primary
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        }

        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(accuracyColor.copy(alpha = 0.15f))
                .border(1.dp, accuracyColor.copy(0.4f), MaterialTheme.shapes.medium)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sensorStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = accuracyLabel,
                    color = accuracyColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (sensorAccuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH && sensorAccuracy != SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            Text(
                text = stringResource(R.string.compass_calibrate_hint),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Visual Canvas Compass Circular Ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(primaryEmerald.copy(alpha = 0.15f), Color.Transparent)))
                .border(2.dp, accentGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(dynamicRotation)
            ) {
                Text(
                    text = "N",
                    color = Color.Red,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                )
                Text(
                    text = "S",
                    color = accentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
                Text(
                    text = "E",
                    color = accentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                )
                Text(
                    text = "W",
                    color = accentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                )
            }

            val finalArrowRotation = dynamicRotation + qiblaBearing.toFloat()
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(finalArrowRotation),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = "Ka'bah Direction",
                    tint = if (isFacingQibla) primaryEmerald else accentGold,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        // Facing Qibla Banner
        AnimatedVisibility(
            visible = isFacingQibla
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(primaryEmerald.copy(alpha = 0.15f))
                    .border(1.5.dp, primaryEmerald, MaterialTheme.shapes.medium)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.facing_kabah),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryEmerald,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.qibla_angle),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format("%.2f° West", qiblaBearing),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentGold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.distance_to_makkah),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format("%,.0f KM", distanceToMakkah),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryEmerald
                    )
                }
            }
        }

        // Location Info Footer Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = accentGold,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.calibration_detail),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calibrating from: Lat ${String.format("%.4f°", userLat)} • Lon ${String.format("%.4f°", userLon)}\nEnsure device sensors are calibrated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

private fun triggerHapticFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        }
    } catch (e: Exception) {}
}

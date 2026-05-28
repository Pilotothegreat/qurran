package com.example.ui.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHighlightCard
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build

private val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
private val PrimaryEmerald: Color @Composable get() = MaterialTheme.colorScheme.primary

@Composable
fun QiblaCompassScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val amoled = viewModel.prefs.amoledMode
    val context = LocalContext.current

    // Coordinates for Makkah (Kaba)
    val makkahLat = 21.4225
    val makkahLon = 39.8262

    val userLat = viewModel.prefs.latitude
    val userLon = viewModel.prefs.longitude

    // Live sensor-based compass azimuth rotation state
    var azimuth by remember { mutableStateOf(0f) }
    var sensorStatus by remember { mutableStateOf("Initializing sensors...") }

    // Smooth shortest-path target rotation state
    var targetRotation by remember { mutableStateOf(0f) }

    LaunchedEffect(azimuth) {
        val currentTarget = -azimuth
        val delta = currentTarget - targetRotation
        val normalizedDelta = ((delta + 180f) % 360f + 360f) % 360f - 180f
        targetRotation += normalizedDelta
    }

    // Bearing calculation from coordinates
    val qiblaBearing = remember(userLat, userLon) {
        calculateQiblaBearing(userLat, userLon, makkahLat, makkahLon)
    }

    val distanceToMakkah = remember(userLat, userLon) {
        calculateDistance(userLat, userLon, makkahLat, makkahLon)
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

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        val useRotationVector = rotationSensor != null
        
        val accelerometerReading = FloatArray(3)
        val magnetometerReading = FloatArray(3)
        val rMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                try {
                    if (useRotationVector && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuthRad = orientation[0]
                        azimuth = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        sensorStatus = "Using Rotation Vector Sensor (High Precision)"
                    } else if (!useRotationVector) {
                        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            System.arraycopy(event.values, 0, accelerometerReading, 0, event.values.size)
                        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                            System.arraycopy(event.values, 0, magnetometerReading, 0, event.values.size)
                        }
                        
                        val success = SensorManager.getRotationMatrix(rMatrix, null, accelerometerReading, magnetometerReading)
                        if (success) {
                            SensorManager.getOrientation(rMatrix, orientationAngles)
                            val azimuthRad = orientationAngles[0]
                            azimuth = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                            sensorStatus = "Using Accelerometer & Magnetometer (Standard Precision)"
                        }
                    }
                } catch (e: Exception) {
                    // Fallback or ignore corrupted/buggy emulated sensor frames
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (useRotationVector) {
            sensorManager?.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            sensorStatus = "Using Rotation Vector Sensor (High Precision)"
        } else {
            if (accelerometer != null && magnetometer != null) {
                sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                sensorManager?.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
                sensorStatus = "Using Accelerometer & Magnetometer (Standard Precision)"
            } else {
                sensorStatus = "Compass sensors unavailable on this device."
            }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Animate compass movement for buttery-smooth rotation sweeps
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
        // Upper Title Details
        Text(
            text = "QIBLA COMPASS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AccentGold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Live direction toward the Holy Ka'bah in Makkah",
            fontSize = 12.sp,
            color = if (amoled) Color(0xFF90A49F) else Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        // Sensor status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (amoled) Color(0xFF0F1E19) else Color(0xFFE2EFF0))
                .border(1.dp, AccentGold.copy(0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = sensorStatus,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (amoled) Color(0xFF86A39B) else PrimaryEmerald,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Visual Canvas Compass Circular Ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0x1A129676), Color.Transparent)))
                .border(2.dp, AccentGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Rotatable Cardinal Dial (N, S, E, W)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(dynamicRotation)
            ) {
                Text(
                    text = "N",
                    color = Color(0xFFE53935), // Red for North
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
                )
                Text(
                    text = "S",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                )
                Text(
                    text = "E",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
                )
                Text(
                    text = "W",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
                )
            }

            // Beautiful Compass Overlap Needle pointing towards Makkah (Bearing angle + current heading)
            val finalArrowRotation = dynamicRotation + qiblaBearing.toFloat()
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(finalArrowRotation),
                contentAlignment = Alignment.Center
            ) {
                // Draws high quality geometric Arrow pointing upwards
                Icon(
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = "Ka'bah Direction",
                    tint = if (isFacingQibla) Color(0xFF129676) else AccentGold,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        // Aligned Qibla Banner
        androidx.compose.animation.AnimatedVisibility(
            visible = isFacingQibla,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF129676).copy(alpha = 0.15f))
                    .border(1.5.dp, Color(0xFF129676), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "FACING KA'BAH 🕋",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF129676),
                    letterSpacing = 1.sp
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Info details card
        GlassHighlightCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QIBLA ANGLE FROM NORTH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amoled) Color(0xFF90A49F) else Color.DarkGray
                    )
                    Text(
                        text = String.format("%.2f° West", qiblaBearing),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentGold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DISTANCE TO MAKKAH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amoled) Color(0xFF90A49F) else Color.DarkGray
                    )
                    Text(
                        text = String.format("%,.0f KM", distanceToMakkah),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (amoled) Color.White else PrimaryEmerald
                    )
                }
            }
        }

        // Location Info Footer Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            isDark = amoled
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Coordinates alignment calibration",
                        fontSize = 13.sp,
                        color = if (amoled) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calibrating from: Lat ${String.format("%.4f°", userLat)} • Lon ${String.format("%.4f°", userLon)}\nEnsure device calibration sensors are clean for precise S24 gyroscope alignment.",
                        fontSize = 11.sp,
                        color = if (amoled) Color.LightGray else Color.DarkGray,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// Spherical trigonometry calculation
private fun calculateQiblaBearing(
    lat: Double, lon: Double,
    mLat: Double, mLon: Double
): Double {
    val phi1 = Math.toRadians(lat)
    val phi2 = Math.toRadians(mLat)
    val lam1 = Math.toRadians(lon)
    val lam2 = Math.toRadians(mLon)

    val deltaLam = lam2 - lam1
    val y = sin(deltaLam) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLam)
    
    var bearing = Math.toDegrees(atan2(y, x))
    bearing = (bearing + 360.0) % 360.0
    return bearing
}

// Accurate Haversine Distance calculation
private fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    
    val c = 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
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


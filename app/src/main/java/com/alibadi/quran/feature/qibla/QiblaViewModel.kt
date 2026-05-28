package com.alibadi.quran.feature.qibla

import android.app.Application
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alibadi.quran.core.data.AppPreferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.*

class QiblaViewModel(
    private val application: Application,
    private val prefs: AppPreferencesDataStore
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _compassHeading = MutableStateFlow(0f)
    val compassHeading: StateFlow<Float> = _compassHeading.asStateFlow()

    private val _sensorAccuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val sensorAccuracy: StateFlow<Int> = _sensorAccuracy.asStateFlow()

    private val _sensorStatus = MutableStateFlow("Initializing sensors...")
    val sensorStatus: StateFlow<String> = _sensorStatus.asStateFlow()

    private val _qiblaBearing = MutableStateFlow(0.0)
    val qiblaBearing: StateFlow<Double> = _qiblaBearing.asStateFlow()

    private val _distanceToMakkah = MutableStateFlow(0.0)
    val distanceToMakkah: StateFlow<Double> = _distanceToMakkah.asStateFlow()

    private var userLat = 23.5880
    private var userLon = 58.3829

    // Coordinates for Makkah (Kaba)
    private val makkahLat = 21.4225
    private val makkahLon = 39.8262

    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val useRotationVector = rotationSensor != null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    init {
        viewModelScope.launch {
            // Get coordinates once or watch changes
            prefs.latitude.collect { lat ->
                userLat = lat
                recalculateQiblaData()
            }
        }
        viewModelScope.launch {
            prefs.longitude.collect { lon ->
                userLon = lon
                recalculateQiblaData()
            }
        }
    }

    private fun recalculateQiblaData() {
        _qiblaBearing.value = calculateQiblaBearing(userLat, userLon, makkahLat, makkahLon)
        _distanceToMakkah.value = calculateDistance(userLat, userLon, makkahLat, makkahLon)
    }

    fun registerSensors() {
        if (sensorManager == null) {
            _sensorStatus.value = "Sensor service unavailable"
            return
        }

        if (useRotationVector) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            _sensorStatus.value = "Using Rotation Vector Sensor (High Precision)"
        } else {
            if (accelerometer != null && magnetometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
                _sensorStatus.value = "Using Accelerometer & Magnetometer"
            } else {
                _sensorStatus.value = "Compass sensors unavailable on this device."
            }
        }
    }

    fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        try {
            if (useRotationVector && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthRad = orientation[0]
                val magneticHeading = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                
                // Correct for geomagnetic declination to find true north
                val geoField = GeomagneticField(
                    userLat.toFloat(), userLon.toFloat(), 0f,
                    System.currentTimeMillis()
                )
                val declination = geoField.declination
                val correctedHeading = (magneticHeading + declination + 360f) % 360f

                _compassHeading.value = correctedHeading
                _sensorStatus.value = "Using Rotation Vector Sensor (High Precision)"
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
                    val magneticHeading = Math.toDegrees(azimuthRad.toDouble()).toFloat()

                    val geoField = GeomagneticField(
                        userLat.toFloat(), userLon.toFloat(), 0f,
                        System.currentTimeMillis()
                    )
                    val declination = geoField.declination
                    val correctedHeading = (magneticHeading + declination + 360f) % 360f

                    _compassHeading.value = correctedHeading
                    _sensorStatus.value = "Using Accelerometer & Magnetometer"
                }
            }
        } catch (e: Exception) {
            // Squelch
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            _sensorAccuracy.value = accuracy
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensors()
    }

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

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

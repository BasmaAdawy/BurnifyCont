package com.example.burnify.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs

class CompassViewModel(application: Application) : AndroidViewModel(application) {

    private val compassData = MutableLiveData<CompassSample>()
    private var compassSample = CompassSample()
    // Holds the last recorded direction to check for significant changes
    private var lastDirection: Float = 0f

    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Listener for compass (rotation vector) sensor events
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                // Convert rotation vector to rotation matrix and then to azimuth in degrees
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val normalizedAzimuth = ((azimuthInDegrees + 360) % 360 ).toFloat() // Normalize to [0, 360]
                compassSample = CompassSample()
                // Update if the direction has changed by more than 1 degree
                if (abs(normalizedAzimuth - lastDirection) > 1) {
                    lastDirection = normalizedAzimuth
                    // Add sample to CompassMeasurements and post the updated list
                    //compassMeasurements.addSample(CompassSample(normalizedAzimuth))
                    compassSample.setAngle(normalizedAzimuth)
                    compassData.postValue(compassSample)
                }else {
                    // Add the previous sample to the data list if the direction has not changed
                    compassSample.setAngle(lastDirection)
                    compassData.postValue(compassSample)
                }

            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    init {
        initializeCompassListener()
    }

    private fun initializeCompassListener() {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        rotationSensor?.let {
            // Register the listener with the UI delay setting
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the listener when the ViewModel is cleared
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun getCompassData(): LiveData<CompassSample> {return compassData}

    // Retrieve the stored compass samples if needed
}

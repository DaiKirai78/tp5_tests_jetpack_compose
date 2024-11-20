package com.example.tests_jetpack_compose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PodometreActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodometreScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PodometreScreen() {
    var stepCount by remember { mutableStateOf(0) }
    var isPermissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var initialStepCount by remember { mutableStateOf(0) }
    var isInitialStepCaptured by remember { mutableStateOf(false) }

    // Demander la permission d'activité directement dans la Composable
    LaunchedEffect(Unit) {
        // Demander la permission ici
        val permissionStatus = requestActivityRecognitionPermission(context)
        isPermissionGranted = permissionStatus
    }

    // UI pour afficher le nombre de pas ou un message d'erreur
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isPermissionGranted) {
            Text("Nombre de pas: $stepCount", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("Permission non accordée ou capteur non disponible.", style = MaterialTheme.typography.bodyLarge)
        }
    }

    // Gestion du capteur de podomètre
    LaunchedEffect(key1 = isPermissionGranted) {
        if (isPermissionGranted) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

            stepSensor?.let { sensor ->
                sensorManager.registerListener(object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                            val currentStepCount = event.values[0].toInt()

                            // Capturer la valeur initiale au début
                            if (!isInitialStepCaptured) {
                                initialStepCount = currentStepCount
                                isInitialStepCaptured = true
                            }

                            // Calculer les pas depuis l'ouverture de la page
                            stepCount = currentStepCount - initialStepCount
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }, sensor, SensorManager.SENSOR_DELAY_UI)
            } ?: run {
                Toast.makeText(context, "Capteur de podomètre non disponible", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// Fonction pour demander la permission directement
@RequiresApi(Build.VERSION_CODES.Q)
private fun requestActivityRecognitionPermission(context: Context): Boolean {
    // Vérifier si la permission est déjà accordée
    return if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
        true
    } else {
        // Demander la permission
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            1
        )
        false
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PodometreScreen()
}

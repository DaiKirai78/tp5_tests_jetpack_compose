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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun StockageScreen() {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val scope = rememberCoroutineScope()
    val database by remember { mutableStateOf(StepDatabase.getDatabase(context)) }
    val stepDao = database.stepDao()

    var stepCount by remember { mutableStateOf(0) }
    var stepsToday by remember { mutableStateOf(0) } // Utiliser remember pour stocker la valeur persistante
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var isPermissionGranted by remember { mutableStateOf(false) }
    var allStepsRecords by remember { mutableStateOf<List<StepRecord>>(emptyList()) }

    // Demander les permissions
    LaunchedEffect(Unit) {
        isPermissionGranted = requestActivityRecognitionPermission(context)
    }

    // Charger les données existantes de la base de données lors de la composition de l'écran
    LaunchedEffect(today) {
        if (isPermissionGranted) {
            // Vérifier si des pas sont déjà enregistrés pour aujourd'hui
            scope.launch {
                try {
                    val record = stepDao.getStepsForDate(today)
                    // Si des données existent, utiliser cette valeur
                    stepsToday = record?.steps ?: 0
                    stepCount = stepsToday // Initialiser stepCount avec la valeur de stepsToday
                } catch (e: Exception) {
                    Toast.makeText(context, "Erreur lors de la récupération des données.", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    // Charger toutes les données lorsque l'écran est chargé
    LaunchedEffect(Unit) {
        scope.launch {
            val allRecords = stepDao.getAllSteps()
            allStepsRecords = allRecords // Charger toutes les données dans la liste
        }
    }

    // Activer le capteur de pas
    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted && stepSensor != null) {
            sensorManager.registerListener(object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                        val totalSteps = event.values[0].toInt()
                        // Si stepCount est égal à zéro, cela signifie qu'on commence le comptage
                        if (stepCount == 0) {
                            stepCount = totalSteps // Initialiser avec la première valeur détectée
                        }
                        // Calculer les pas supplémentaires effectués depuis la dernière mise à jour
                        val stepsDelta = totalSteps - stepCount
                        stepCount = totalSteps

                        // Ajouter ces pas à stepsToday
                        stepsToday += stepsDelta

                        // Mettre à jour la base de données avec la nouvelle valeur
                        scope.launch {
                            stepDao.insertOrUpdateStep(StepRecord(today, stepsToday))
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (stepSensor == null) {
            Toast.makeText(context, "Capteur de podomètre non disponible", Toast.LENGTH_LONG).show()
        }
    }

    // Interface utilisateur
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isPermissionGranted) {
            Text("Nombre de pas aujourd'hui : $stepsToday", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                scope.launch {
                    val allSteps = stepDao.getAllSteps()
                    allStepsRecords = allSteps // Récupérer toutes les données de pas
                }
            }) {
                Text("Afficher toutes les données")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Afficher toutes les données
            allStepsRecords.forEach { record ->
                Text("${record.date}: ${record.steps} pas", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text("Permission non accordée ou capteur non disponible.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Fonction pour demander la permission
@RequiresApi(Build.VERSION_CODES.Q)
private fun requestActivityRecognitionPermission(context: Context): Boolean {
    return if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
        true
    } else {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            1
        )
        false
    }
}

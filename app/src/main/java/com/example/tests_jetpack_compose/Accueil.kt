package com.example.tests_jetpack_compose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*

class Acceuil : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AcceuilScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AcceuilScreen() {
    // Configuration du NavController
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerContent = { DrawerContent(navController) }, // Passer le NavController à DrawerContent
        content = {
            // Contenu principal avec la navigation
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(navController) // Accueil principal
                }
                composable("podometre") {
                    PodometreScreen() // Page Podomètre
                }
                composable("stockage") {
                    StockageScreen() // Page Podomètre
                }
            }
        }
    )
}

@Composable
fun DrawerContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.onSecondary)
            .padding(16.dp)
    ) {
        // Menu du Drawer
        DrawerMenuItem("Accueil", navController)
        DrawerMenuItem("Podomètre", navController)
        DrawerMenuItem("Stocker les données", navController)
        DrawerMenuItem("Graphique", navController)
        DrawerMenuItem("Background Process", navController)
        DrawerMenuItem("Notifications", navController)
        DrawerMenuItem("Couleurs", navController)
    }
}

@Composable
fun DrawerMenuItem(text: String, navController: NavController) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .padding(vertical = 22.dp)
            .clickable {
                if (text == "Podomètre") {
                    // Naviguer vers la page Podomètre
                    navController.navigate("podometre")
                } else if (text == "Accueil") {
                    navController.navigate("home")
                } else if (text == "Stocker les données") {
                    navController.navigate("stockage")
                }
                // Ajoute ici la navigation pour les autres éléments si besoin
            }
    )
}

@Composable
fun HomeScreen(navController: NavController) {
    // Contenu principal de l'accueil
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Explorez les tests dans le drawer en haut à gauche",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true)
@Composable
fun DefaultPreviewAccueil() {
    AcceuilScreen()
}

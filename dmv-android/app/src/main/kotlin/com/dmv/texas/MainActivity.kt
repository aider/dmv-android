package com.dmv.texas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dmv.texas.ui.navigation.NavGraph
import com.dmv.texas.ui.navigation.Screen
import com.dmv.texas.ui.theme.DMVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DMVTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Import.route,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

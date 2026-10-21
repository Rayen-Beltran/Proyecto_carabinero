package com.example.proyectocarabinero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.proyectocarabinero.ui.AppNav
import com.example.proyectocarabinero.ui.theme.ProyectoCarabineroTheme
import com.example.proyectocarabinero.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoCarabineroTheme {
            //    AppNav(viewModel<MainViewModel>())
            }
        }
    }
}
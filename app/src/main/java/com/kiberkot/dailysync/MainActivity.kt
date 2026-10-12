package com.kiberkot.dailysync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kiberkot.dailysync.navigation.AppNavigation
import com.kiberkot.dailysync.ui.theme.DailySyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailySyncTheme {
                AppNavigation()
            }
        }
    }
}
package com.kiberkot.dailysync.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.ui.components.DailySyncLogo
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

/** Respaldo del splash para las composiciones fuera de la actividad y los fallos recuperables. */
@Composable
internal fun InicioSesionScreen(error: String?, onReintentar: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brand.Background).systemBarsPadding().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        DailySyncLogo(Modifier.size(80.dp))
        Spacer(Modifier.height(20.dp))
        Text("DailySync", fontFamily = Brand.Font, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Brand.Ink)
        Spacer(Modifier.height(16.dp))
        if (error == null) CircularProgressIndicator(Modifier.size(24.dp), color = Brand.Teal, strokeWidth = 2.dp)
        else {
            Text(error, fontFamily = Brand.Font, fontSize = 16.sp, color = Brand.Muted)
            TextButton(onClick = onReintentar) {
                Text("Reintentar", fontFamily = Brand.Font, fontSize = 16.sp, color = Brand.Teal)
            }
        }
    }
}

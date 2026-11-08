package com.kiberkot.dailysync.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.R

/** Vector original de la marca, independiente de la resolución de pantalla. */
@Composable
fun DailySyncLogo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.ic_dailysync_logo), contentDescription = null,
        modifier = modifier.size(48.dp))
}

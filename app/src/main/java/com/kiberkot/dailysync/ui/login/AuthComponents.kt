package com.kiberkot.dailysync.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand

@Composable
internal fun CampoAcceso(
    etiqueta: String, valor: String, onCambio: (String) -> Unit, placeholder: String,
    icono: Int, error: String?, enabled: Boolean, tag: String,
    keyboardOptions: KeyboardOptions, keyboardActions: KeyboardActions,
    transformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(16.dp)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // La etiqueta accesible está en el campo para que no se anuncie dos veces.
        TextoAcceso(etiqueta, 12, Brand.Muted, FontWeight.Medium, Modifier.clearAndSetSemantics {})
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).background(Brand.Field, shape)
                .border(if (focused) 1.5.dp else 1.dp,
                    if (error != null) Brand.Error else if (focused) Brand.Teal else Brand.Border, shape)
                .padding(start = 14.dp, end = if (trailingIcon == null) 14.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(icono), contentDescription = null,
                modifier = Modifier.size(18.dp), tint = Brand.Teal.copy(alpha = 0.85f))
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = valor, onValueChange = com.kiberkot.dailysync.ui.components.conActividadSesion(onCambio), enabled = enabled, singleLine = true,
                textStyle = estiloAcceso(14).copy(color = Brand.Ink),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Brand.Teal),
                interactionSource = interaction, keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions, visualTransformation = transformation,
                modifier = Modifier.weight(1f).padding(vertical = 14.dp).testTag(tag).semantics {
                    contentDescription = etiqueta
                    if (error != null) error(error)
                },
                decorationBox = { campo ->
                    Box {
                        if (valor.isEmpty()) TextoAcceso(placeholder, 14, Color(0xFF78968F))
                        campo()
                    }
                }
            )
            trailingIcon?.invoke()
        }
        error?.let {
            TextoAcceso(it, 11, Brand.Error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
    }
}

private const val ACCESS_TEXT_SCALE = 1.2f

internal fun estiloAcceso(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Brand.Font,
    fontSize = (size * ACCESS_TEXT_SCALE).sp,
    lineHeight = (size * 1.45f * ACCESS_TEXT_SCALE).sp,
    fontWeight = weight, letterSpacing = (-0.15f * ACCESS_TEXT_SCALE).sp
)

@Composable
internal fun TextoAcceso(text: String, size: Int, color: Color,
    weight: FontWeight = FontWeight.Normal, modifier: Modifier = Modifier,
    letterSpacing: Float = -0.15f, textAlign: TextAlign? = null) {
    Text(text, modifier = modifier, color = color,
        style = estiloAcceso(size, weight).copy(letterSpacing = (letterSpacing * ACCESS_TEXT_SCALE).sp), textAlign = textAlign)
}

@Composable
internal fun FondoAcceso(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFF1FBF8), Color(0xFFEFFBF7))))
        drawCircle(Brush.radialGradient(listOf(Color(0xFFAFF5E5).copy(alpha = 0.62f), Color.Transparent),
            center = Offset(size.width * 0.94f, size.height * 0.13f), radius = size.width * 0.68f),
            radius = size.width * 0.9f, center = Offset(size.width * 0.94f, size.height * 0.13f))
        drawCircle(Brush.radialGradient(listOf(Color(0xFFB6F2DC).copy(alpha = 0.5f), Color.Transparent),
            center = Offset(size.width * 0.10f, size.height * 0.88f), radius = size.width * 0.72f),
            radius = size.width, center = Offset(size.width * 0.10f, size.height * 0.88f))
        val step = 28.dp.toPx()
        val limite = size.height * 0.55f
        var y = 0f
        while (y < limite) {
            drawLine(Brand.Teal.copy(alpha = 0.035f * (1f - y / limite)), Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
            y += step
        }
        var x = 0f
        while (x < size.width) {
            drawLine(Brush.verticalGradient(listOf(Brand.Teal.copy(alpha = 0.035f), Color.Transparent), endY = limite),
                Offset(x, 0f), Offset(x, limite), 0.5.dp.toPx())
            x += step
        }
    }
}

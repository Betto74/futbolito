package com.example.fucho

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fucho.ui.theme.FuchoTheme

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import dev.ricknout.composesensors.accelerometer.isAccelerometerSensorAvailable
import dev.ricknout.composesensors.accelerometer.rememberAccelerometerSensorValueAsState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuchoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    AccelerometerDemo()
                }
            }
        }
    }
}

@Composable
fun AccelerometerDemo() {
    if (isAccelerometerSensorAvailable()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val sensorValue by rememberAccelerometerSensorValueAsState()
            val (ax, ay, _) = sensorValue.value

            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            var center by remember { mutableStateOf(Offset(width / 2, height / 2)) }
            var velocityX by remember { mutableStateOf(0f) }
            var velocityY by remember { mutableStateOf(0f) }

            val contentColor = LocalContentColor.current
            val radius = with(LocalDensity.current) { 10.dp.toPx() }

            val goalWidth = width * 0.3f // 30% del ancho de la pantalla
            val goalHeight = 50f // Altura de la portería

            val speedMultiplier = 0.5f // Factor de ajuste de aceleración
            val friction = 0.98f // Para reducir la velocidad con el tiempo

            // Definir márgenes superior e inferior
            val topMargin = height * 0.1f
            val bottomMargin = height * 0.9f

            // Aplicar aceleración al movimiento
            velocityX += -ax * speedMultiplier
            velocityY += ay * speedMultiplier

            // Aplicar fricción para reducir la velocidad con el tiempo
            velocityX *= friction
            velocityY *= friction

            // Calcular la nueva posición
            var newX = center.x + velocityX
            var newY = center.y + velocityY

            // Si la bola toca los bordes, invertir la dirección (rebote)
            if (newX - radius < 0f || newX + radius > width) {
                velocityX = -velocityX * 0.8f // Rebote con pérdida de energía
                newX = center.x + velocityX
            }
            if (newY - radius < topMargin || newY + radius > bottomMargin) {
                velocityY = -velocityY * 0.8f // Rebote con pérdida de energía
                newY = center.y + velocityY
            }

            center = Offset(newX, newY)

            // Definir posiciones de las porterías
            val goalTop = Offset((width - goalWidth) / 2, topMargin)
            val goalBottom = Offset((width - goalWidth) / 2, bottomMargin - goalHeight)

            // Verificar si la bola toca alguna portería
            val inGoalTop = newY - radius <= goalHeight + topMargin &&
                    newX in goalTop.x..(goalTop.x + goalWidth)
            val inGoalBottom = newY + radius >= (bottomMargin - goalHeight) &&
                    newX in goalBottom.x..(goalBottom.x + goalWidth)

            if (inGoalTop || inGoalBottom) {
                center = Offset(width / 2, height / 2) // Reiniciar la bola al centro
                velocityX = 0f
                velocityY = 0f
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Dibujar la bola
                drawCircle(
                    color = contentColor,
                    radius = radius,
                    center = center,
                )

                // Dibujar porterías
                drawRect(
                    color = Color.Red,
                    topLeft = goalTop,
                    size = Size(goalWidth, goalHeight)
                )
                drawRect(
                    color = Color.Blue,
                    topLeft = goalBottom,
                    size = Size(goalWidth, goalHeight)
                )
            }
        }
    }
}



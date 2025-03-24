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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.ricknout.composesensors.accelerometer.isAccelerometerSensorAvailable
import dev.ricknout.composesensors.accelerometer.rememberAccelerometerSensorValueAsState
import kotlinx.coroutines.delay

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


        //variables que almacenan los goles de cada lado.
        var scoreTop by remember { mutableStateOf(0) }
        var scoreBottom by remember { mutableStateOf(0) }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val sensorValue by rememberAccelerometerSensorValueAsState()

            //valores del acelerometro (no se utiliza z porque es un plano 2d)
            val (ax, ay, _) = sensorValue.value

            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()


            var center by remember { mutableStateOf(Offset(width / 2, height / 2)) }
            var velocityX by remember { mutableStateOf(0f) }
            var velocityY by remember { mutableStateOf(0f) }

            val contentColor = LocalContentColor.current
            val radius = with(LocalDensity.current) { 10.dp.toPx() }

            //Ancho de la porteria y de los bordes
            val goalWidth = width * 0.3f
            val borderThickness = 50f

            //Reducimos la altura en un 20 porciento
            val topMargin = height * 0.2f
            val bottomMargin = height * 0.8f

            val speedMultiplier = 0.3f // Aumentar la velocidad

            val minVelocityThreshold = 0.3f // Velocidad mínima antes de detenerse

            // Aplicar aceleración y fricción
            velocityX += -ax * speedMultiplier
            velocityY += ay * speedMultiplier


            var newX = center.x + velocityX
            var newY = center.y + velocityY

            // Rebote en paredes laterales
            if (newX - radius < borderThickness || newX + radius > width - borderThickness) {
                if (kotlin.math.abs(velocityX) > minVelocityThreshold) {
                    // se invierte la velocidad en X y se reduce un 40% para simular la pérdida de energía en el rebote.
                    velocityX = -velocityX * 0.6f
                } else {
                    // Si la velocidad es muy pequeña, la pelota se detiene
                    velocityX = 0f
                }
                newX = center.x + velocityX
            }

            // Rebote en paredes superior e inferior (excepto en las porterías)
            val isTouchingTopBorder = newY - radius < topMargin &&
                    //El operador .. en Kotlin crea un rango
                    !(newX in (width - goalWidth) / 2..(width + goalWidth) / 2) // Verifica que no esté dentro de la portería, ya que ahí no debe rebotar.
            val isTouchingBottomBorder = newY + radius > bottomMargin &&
                    !(newX in (width - goalWidth) / 2..(width + goalWidth) / 2)

            if (isTouchingTopBorder || isTouchingBottomBorder) {
                if (kotlin.math.abs(velocityY) > minVelocityThreshold) {
                    velocityY = -velocityY * 0.6f // Disminuir rebote
                } else {
                    velocityY = 0f
                }
                newY = center.y + velocityY
            }

            // Limitar la posición dentro del campo de juego (min y maximo)
            newX = newX.coerceIn(borderThickness + radius, width - borderThickness - radius)
            newY = newY.coerceIn(topMargin + radius, bottomMargin - radius)

            // Asignar la nueva posición a la pelota
            center = Offset(newX, newY)

            // definir las coordenadas de las porterias
            val goalTop = Offset((width - goalWidth) / 2, topMargin - borderThickness)
            val goalBottom = Offset((width - goalWidth) / 2, bottomMargin)

            //Verificar si la pelota se encuentra en el area de a porteria
            val inGoalTop = newY - radius <= topMargin &&
                    newX in goalTop.x..(goalTop.x + goalWidth)
            val inGoalBottom = newY + radius >= bottomMargin &&
                    newX in goalBottom.x..(goalBottom.x + goalWidth)

            if (inGoalTop) {
                scoreBottom++ // Gol en la portería superior
                center = Offset(width / 2, height / 2)//Reiniciar pelota
                velocityX = 0f
                velocityY = 0f
            } else if (inGoalBottom) {
                scoreTop++ // Gol en la portería inferior
                center = Offset(width / 2, height / 2)
                velocityX = 0f
                velocityY = 0f
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = contentColor, radius = radius, center = center)

                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(0f, topMargin),
                        size = Size(borderThickness, bottomMargin - topMargin)
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(width - borderThickness, topMargin),
                        size = Size(borderThickness, bottomMargin - topMargin)
                    )

                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(0f, topMargin - borderThickness),
                        size = Size((width - goalWidth) / 2, borderThickness)
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset((width + goalWidth) / 2, topMargin - borderThickness),
                        size = Size((width - goalWidth) / 2, borderThickness)
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(0f, bottomMargin),
                        size = Size((width - goalWidth) / 2, borderThickness)
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset((width + goalWidth) / 2, bottomMargin),
                        size = Size((width - goalWidth) / 2, borderThickness)
                    )

                    drawRect(
                        color = Color.Red,
                        topLeft = goalTop,
                        size = Size(goalWidth, borderThickness)
                    )
                    drawRect(
                        color = Color.Blue,
                        topLeft = goalBottom,
                        size = Size(goalWidth, borderThickness)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔴 $scoreTop - $scoreBottom 🔵",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

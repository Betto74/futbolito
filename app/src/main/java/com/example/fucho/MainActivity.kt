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
        var scoreTop by remember { mutableStateOf(0) }
        var scoreBottom by remember { mutableStateOf(0) }

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

            val goalWidth = width * 0.3f
            val borderThickness = 50f

            val topMargin = height * 0.2f
            val bottomMargin = height * 0.8f

            val speedMultiplier = 0.5f
            val friction = 0.98f

            velocityX += -ax * speedMultiplier
            velocityY += ay * speedMultiplier

            velocityX *= friction
            velocityY *= friction

            var newX = center.x + velocityX
            var newY = center.y + velocityY

            if (newX - radius < borderThickness || newX + radius > width - borderThickness) {
                velocityX = -velocityX * 0.8f
                newX = center.x + velocityX
            }

            val isTouchingTopBorder = newY - radius < topMargin &&
                    !(newX in (width - goalWidth) / 2..(width + goalWidth) / 2)
            val isTouchingBottomBorder = newY + radius > bottomMargin &&
                    !(newX in (width - goalWidth) / 2..(width + goalWidth) / 2)

            if (isTouchingTopBorder || isTouchingBottomBorder) {
                velocityY = -velocityY * 0.8f
                newY = center.y + velocityY
            }

            newY = newY.coerceIn(topMargin + radius, bottomMargin - radius)

            center = Offset(newX, newY)

            val goalTop = Offset((width - goalWidth) / 2, topMargin - borderThickness)
            val goalBottom = Offset((width - goalWidth) / 2, bottomMargin)

            val inGoalTop = newY - radius <= topMargin &&
                    newX in goalTop.x..(goalTop.x + goalWidth)
            val inGoalBottom = newY + radius >= bottomMargin &&
                    newX in goalBottom.x..(goalBottom.x + goalWidth)


            if (inGoalTop) {
                scoreBottom++ // Gol en la portería superior
                center = Offset(width / 2, height / 2)
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


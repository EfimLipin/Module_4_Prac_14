package com.example.module_4_prac_14

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.module_4_prac_14.theme.Module_4_Prac_14Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Module_4_Prac_14Theme(
                darkTheme = true
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1A2E)
                ) {
                    CompassScreen()
                }
            }
        }
    }
}

@Composable
fun CompassScreen(
    viewModel: MainViewModel = viewModel()
) {
    val azimuth by viewModel.azimuth.collectAsState()
    val sensorAvailable by viewModel.sensorAvailable.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startSensor()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopSensor()
                else -> { }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!sensorAvailable) {
        SensorErrorContent()
    } else {
        CompassContent(azimuth = azimuth)
    }
}

@Composable
private fun CompassContent(azimuth: Float) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "azimuth_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Компас",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(300.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF16213E))
            )

            CompassDial()

            CompassNeedle(
                rotation = -animatedAzimuth,
                modifier = Modifier.size(240.dp)
            )

            Text(
                text = "N",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE94560),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Азимут",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Text(
                    text = "${azimuth.toInt()}°",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = getDirectionName(azimuth),
                    fontSize = 18.sp,
                    color = Color(0xFFE94560)
                )
            }
        }
    }
}

@Composable
private fun CompassDial() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.95f

        for (i in 0 until 360 step 10) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val lineLength = if (i % 30 == 0) 20f else 10f
            val lineColor = if (i % 90 == 0) Color(0xFFE94560) else Color.White.copy(alpha = 0.5f)

            val startX = center.x + (radius - lineLength) * kotlin.math.sin(angle)
            val startY = center.y - (radius - lineLength) * kotlin.math.cos(angle)
            val endX = center.x + radius * kotlin.math.sin(angle)
            val endY = center.y - radius * kotlin.math.cos(angle)

            drawLine(
                color = lineColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (i % 30 == 0) 3f else 1.5f
            )
        }
    }
}

@Composable
private fun CompassNeedle(
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.rotate(rotation)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val needleLength = size.minDimension * 0.4f
        val needleWidth = 30f

        val northPath = Path().apply {
            moveTo(center.x, center.y - needleLength)
            lineTo(center.x - needleWidth / 2, center.y)
            lineTo(center.x + needleWidth / 2, center.y)
            close()
        }

        val southPath = Path().apply {
            moveTo(center.x, center.y + needleLength)
            lineTo(center.x - needleWidth / 2, center.y)
            lineTo(center.x + needleWidth / 2, center.y)
            close()
        }

        drawPath(northPath, color = Color(0xFFE94560))
        drawPath(southPath, color = Color(0xFF4A5568))

        drawCircle(
            color = Color.White,
            radius = 15f,
            center = center
        )
    }
}

@Composable
private fun SensorErrorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Устройство не поддерживает\nдатчик ориентации",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE94560),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Для работы компаса необходимы\nакселерометр и магнитометр",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

private fun getDirectionName(azimuth: Float): String {
    return when {
        azimuth >= 337.5 || azimuth < 22.5 -> "Север"
        azimuth >= 22.5 && azimuth < 67.5 -> "Северо-Восток"
        azimuth >= 67.5 && azimuth < 112.5 -> "Восток"
        azimuth >= 112.5 && azimuth < 157.5 -> "Юго-Восток"
        azimuth >= 157.5 && azimuth < 202.5 -> "Юг"
        azimuth >= 202.5 && azimuth < 247.5 -> "Юго-Запад"
        azimuth >= 247.5 && azimuth < 292.5 -> "Запад"
        azimuth >= 292.5 && azimuth < 337.5 -> "Северо-Запад"
        else -> ""
    }
}

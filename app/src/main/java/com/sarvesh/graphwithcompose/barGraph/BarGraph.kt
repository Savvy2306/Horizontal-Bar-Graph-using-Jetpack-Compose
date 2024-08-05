package com.sarvesh.graphwithcompose.barGraph

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Top
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round
import com.sarvesh.graphwithcompose.enums.BarTypeEnum

/*this function is used to show simple bar graph with respect to value.
 */

@Composable
fun BarGraph(
    graphBarData: List<Float>,
    xAxisScaleData: List<Int>,
    barDataList: List<Int>,
    height: Dp,
    roundType: BarTypeEnum,
    barWidth: Dp,
    barColor: Color,
    barArrangement: Arrangement.Horizontal
) {
    val barData by remember { mutableStateOf(barDataList + 0) }
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp

    // Bottom height of the X-Axis Scale
    val xAxisScaleHeight = 40.dp
    val yAxisScaleSpacing by remember { mutableFloatStateOf(100f) }
    val yAxisTextWidth by remember { mutableStateOf(100.dp) }

    val barShape = getBarShape(roundType)
    val textPaint = createTextPaint()

    // Y coordinates for y-axis scale
    val yCoordinates = mutableListOf<Float>()
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val lineHeightXAxis = 10.dp
    val horizontalLineHeight = 5.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        YAxisScale(
            barData = barData,
            height = height,
            xAxisScaleHeight = xAxisScaleHeight,
            yAxisScaleSpacing = yAxisScaleSpacing,
            textPaint = textPaint,
            yCoordinates = yCoordinates,
            pathEffect = pathEffect
        )

        GraphContent(
            width = width,
            yAxisTextWidth = yAxisTextWidth,
            height = height,
            xAxisScaleHeight = xAxisScaleHeight,
            graphBarData = graphBarData,
            xAxisScaleData = xAxisScaleData,
            barShape = barShape,
            barWidth = barWidth,
            barColor = barColor,
            barArrangement = barArrangement,
            lineHeightXAxis = lineHeightXAxis,
            horizontalLineHeight = horizontalLineHeight
        )
    }
}

@Composable
fun getBarShape(roundType: BarTypeEnum): Shape {
    return when (roundType) {
        BarTypeEnum.CIRCULAR_TYPE -> CircleShape
        BarTypeEnum.TOP_ROUNDED_TYPE -> RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
    }
}

@Composable
fun createTextPaint(): Paint {
    val density = LocalDensity.current
    return remember(density) {
        Paint().apply {
            color = Color.Black.hashCode()
            textAlign = Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() }
        }
    }
}

@Composable
fun YAxisScale(
    barData: List<Int>,
    height: Dp,
    xAxisScaleHeight: Dp,
    yAxisScaleSpacing: Float,
    textPaint: Paint,
    yCoordinates: MutableList<Float>,
    pathEffect: PathEffect
) {
    Column(
        modifier = Modifier
            .padding(top = xAxisScaleHeight, end = 3.dp)
            .height(height)
            .fillMaxWidth(),
        horizontalAlignment = CenterHorizontally
    ) {
        Canvas(modifier = Modifier.padding(bottom = 10.dp).fillMaxSize()) {
            val yAxisScaleText = (barData.maxOrNull() ?: 0) / 3f
            (0..3).forEach { i ->
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        round((barData.minOrNull() ?: 0) + yAxisScaleText * i).toString(),
                        30f,
                        size.height - yAxisScaleSpacing - i * size.height / 3f,
                        textPaint
                    )
                }
                yCoordinates.add(size.height - yAxisScaleSpacing - i * size.height / 3f)
            }

            (1..3).forEach {
                drawLine(
                    start = Offset(x = yAxisScaleSpacing + 30f, y = yCoordinates[it]),
                    end = Offset(x = size.width, y = yCoordinates[it]),
                    color = Color.Gray,
                    strokeWidth = 5f,
                    pathEffect = pathEffect
                )
            }
        }
    }
}

@Composable
fun GraphContent(
    width: Dp,
    yAxisTextWidth: Dp,
    height: Dp,
    xAxisScaleHeight: Dp,
    graphBarData: List<Float>,
    xAxisScaleData: List<Int>,
    barShape: Shape,
    barWidth: Dp,
    barColor: Color,
    barArrangement: Arrangement.Horizontal,
    lineHeightXAxis: Dp,
    horizontalLineHeight: Dp
) {
    Box(
        modifier = Modifier
            .padding(start = 50.dp)
            .width(width - yAxisTextWidth)
            .height(height + xAxisScaleHeight),
        contentAlignment = BottomCenter
    ) {
        Row(
            modifier = Modifier.width(width - yAxisTextWidth),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = barArrangement
        ) {
            graphBarData.forEachIndexed { index, value ->
                var animationTriggered by remember { mutableStateOf(false) }
                val graphBarHeight by animateFloatAsState(
                    targetValue = if (animationTriggered) value else 0f,
                    animationSpec = tween(durationMillis = 1000, delayMillis = 0), label = ""
                )
                LaunchedEffect(true) {
                    animationTriggered = true
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Top,
                    horizontalAlignment = CenterHorizontally
                ) {
                    Bar(
                        height = height,
                        barShape = barShape,
                        barWidth = barWidth,
                        barColor = barColor,
                        graphBarHeight = graphBarHeight
                    )

                    XAxisScale(
                        xAxisScaleHeight = xAxisScaleHeight,
                        lineHeightXAxis = lineHeightXAxis,
                        horizontalLineHeight = horizontalLineHeight,
                        xAxisLabel = xAxisScaleData[index]
                    )
                }
            }
        }

        HorizontalLineOnXAxis(horizontalLineHeight = horizontalLineHeight, xAxisScaleHeight = xAxisScaleHeight)
    }
}

@Composable
fun Bar(
    height: Dp,
    barShape: Shape,
    barWidth: Dp,
    barColor: Color,
    graphBarHeight: Float
) {
    Box(
        modifier = Modifier
            .padding(bottom = 5.dp)
            .clip(barShape)
            .width(barWidth)
            .height(height - 10.dp)
            .background(Color.Transparent),
        contentAlignment = BottomCenter
    ) {
        Box(
            modifier = Modifier
                .clip(barShape)
                .fillMaxWidth()
                .fillMaxHeight(graphBarHeight)
                .background(barColor)
        )
    }
}

@Composable
fun XAxisScale(
    xAxisScaleHeight: Dp,
    lineHeightXAxis: Dp,
    horizontalLineHeight: Dp,
    xAxisLabel: Int
) {
    Column(
        modifier = Modifier.height(xAxisScaleHeight),
        verticalArrangement = Top,
        horizontalAlignment = CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                .width(horizontalLineHeight)
                .height(lineHeightXAxis)
                .background(Color.Gray)
        )

        Text(
            modifier = Modifier.padding(bottom = 3.dp),
            text = xAxisLabel.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
    }
}

@Composable
fun HorizontalLineOnXAxis(
    horizontalLineHeight: Dp,
    xAxisScaleHeight: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        horizontalAlignment = CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = xAxisScaleHeight + 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .fillMaxWidth()
                .height(horizontalLineHeight)
                .background(Color.Gray)
        )
    }
}

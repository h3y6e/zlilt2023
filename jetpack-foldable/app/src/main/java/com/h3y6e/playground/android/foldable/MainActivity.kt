package com.h3y6e.playground.android.foldable

import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.h3y6e.playground.android.foldable.ui.theme.FoldableTheme
import kotlinx.coroutines.flow.Flow

class MainActivity : ComponentActivity() {
    private lateinit var windowInfoTracker: WindowInfoTracker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        windowInfoTracker = WindowInfoTracker.getOrCreate(this@MainActivity)

        setContent {
            FoldableTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    FoldingFeatureExample(windowInfoTracker.windowLayoutInfo(this@MainActivity))
                }
            }
        }
    }
}

@Composable
fun FoldingFeatureExample(windowLayoutInfo: Flow<WindowLayoutInfo>) {
    var isSeparating by remember { mutableStateOf(false) }
    var orientation by remember { mutableStateOf(FoldingFeature.Orientation.VERTICAL) }
    var state by remember { mutableStateOf(FoldingFeature.State.FLAT) }
    var occlusionType by remember { mutableStateOf(FoldingFeature.OcclusionType.NONE) }
    var bounds by remember { mutableStateOf(Rect(0, 0, 0, 0)) }

    val density = LocalDensity.current.density

    LaunchedEffect(windowLayoutInfo) {
        windowLayoutInfo.collect { newLayoutInfo ->
            val displayFeatures = newLayoutInfo.displayFeatures
            if (displayFeatures.isNotEmpty()) {
                val foldingFeature = displayFeatures.first() as FoldingFeature
                isSeparating = foldingFeature.isSeparating
                orientation = foldingFeature.orientation
                state = foldingFeature.state
                occlusionType = foldingFeature.occlusionType
                bounds = foldingFeature.bounds
            }
        }
    }

    /*
     * 画面の中央に折りたたみ線を描画する
     *
     * `Bounds`はスクリーンに対する座標であるため、カメラ周りのギャップ(=縦にした時の通知バーの高さ)を考慮する必要がある
     * ここでは簡単のため、その大きさを50dpとして処理を行う (motorola razr 40 ultraの場合)
     */
    // 通知バーの高さ
    val notificationBarHeight = 50.dp
    // offset
    val offsetModifier =
        if (orientation == FoldingFeature.Orientation.VERTICAL)
            Modifier.absoluteOffset(x = -notificationBarHeight, y = 0.dp)
        else
            Modifier.absoluteOffset(x = 0.dp, y = -notificationBarHeight)

    Canvas(modifier = offsetModifier) {
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(
                x = bounds.left.toFloat(),
                y = bounds.top.toFloat()
            ),
            size = Size(
                width = bounds.width().toFloat() + 1,
                height = bounds.height().toFloat() + 1
            ),
        )
    }

    if (isSeparating) {
        if (orientation == FoldingFeature.Orientation.HORIZONTAL) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Green)
                        .fillMaxWidth()
                        .height((bounds.top / density).dp - notificationBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Top Side",
                        color = Color.White,
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color.Blue)
                        .fillMaxWidth()
                        .height((bounds.bottom / density).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bottom Side",
                        color = Color.White,
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Magenta)
                        .fillMaxHeight()
                        .width((bounds.left / density).dp - notificationBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Left Side",
                        color = Color.White,
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color.Red)
                        .fillMaxHeight()
                        .width((bounds.right / density).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Right Side",
                        color = Color.White,
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    Text(
        text = "isSeparating: $isSeparating\n" +
                "orientation: $orientation\n" +
                "state: $state\n" +
                "occlusionType: $occlusionType\n" +
                "bounds: $bounds\n",
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
        fontSize = 28.sp,
        lineHeight = 38.sp,
    )
}
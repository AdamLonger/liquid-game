package com.firethings.liquidmingler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.firethings.liquidmingler.ui.theme.LiquidMinglerTheme
import com.firethings.liquidmingler.utils.TCls
import com.firethings.liquidmingler.utils.solveMulti

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        solveMulti(
            arrayOf(
                arrayOf(TCls.BLUE, TCls.BLUE, TCls.YELLOW, TCls.GREEN),
                arrayOf(TCls.YELLOW, TCls.YELLOW, TCls.BLUE, TCls.YELLOW),
                arrayOf(TCls.GREEN, TCls.GREEN, TCls.GREEN, TCls.BLUE),
                Array(4){TCls.EMPTY},
                Array(4){TCls.EMPTY}
            )
        )
        setContent {
            LiquidMinglerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Game(startScene)
                }
            }
        }
    }
}

package com.xaymaca.sit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.xaymaca.sit.ui.launch.LaunchScreen
import com.xaymaca.sit.ui.nav.NavGraph
import com.xaymaca.sit.ui.theme.SITTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SITTheme {
                var launchComplete by remember { mutableStateOf(false) }
                if (!launchComplete) {
                    LaunchScreen(onComplete = { launchComplete = true })
                } else {
                    NavGraph()
                }
            }
        }
    }
}

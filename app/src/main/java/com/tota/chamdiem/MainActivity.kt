package com.tota.chamdiem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tota.chamdiem.ui.AppRoot
import com.tota.chamdiem.ui.theme.ChamDiemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ChamDiemTheme {
                AppRoot()
            }
        }
    }
}

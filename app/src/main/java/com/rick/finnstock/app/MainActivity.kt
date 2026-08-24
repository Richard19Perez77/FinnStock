package com.rick.finnstock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.rick.finnstock.feature.stocks.presentation.mvi.StocksRoute
import com.rick.finnstock.ui.theme.FinnStockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinnStockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StocksRoute(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

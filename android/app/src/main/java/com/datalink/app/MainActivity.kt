
package com.datalink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.datalink.app.navigation.AppNavigation
import com.datalink.app.ui.theme.DataLinkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DataLinkTheme {
                AppNavigation()
            }
        }
    }
}

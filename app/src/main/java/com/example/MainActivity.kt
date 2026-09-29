package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.admin.AdminMainScreen
import com.example.ui.customer.CustomerMainScreen
import com.example.ui.delivery.DeliveryMainScreen
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.UserRole
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vendor.VendorMainScreen

class MainActivity : ComponentActivity() {
    private val viewModel: KhushbooViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KhushbooAppRoot(viewModel)
            }
        }
    }
}

@Composable
fun KhushbooAppRoot(viewModel: KhushbooViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()

    when (currentRole) {
        UserRole.CUSTOMER -> CustomerMainScreen(viewModel)
        UserRole.VENDOR -> VendorMainScreen(viewModel)
        UserRole.DELIVERY -> DeliveryMainScreen(viewModel)
        UserRole.ADMIN -> AdminMainScreen(viewModel)
    }
}

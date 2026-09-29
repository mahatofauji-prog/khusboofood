package com.example.ui.delivery

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.example.R
import com.example.data.room.OrderEntity
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMainScreen(viewModel: KhushbooViewModel) {
    val orders by viewModel.orders.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = LIGHT_YELLOW,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        KhushbooBrandHeader(
                            subtitle = "🛵 Delivery Fleet Partner",
                            brandSize = BrandSize.COMPACT
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.setRole(UserRole.CUSTOMER) }) {
                            Icon(Icons.Default.Storefront, contentDescription = "Switch to Customer", tint = ORANGE, modifier = Modifier.size(22.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PRIMARY_YELLOW,
                        titleContentColor = TEXT,
                        actionIconContentColor = ORANGE
                    )
                )
                HorizontalDivider(color = BORDER, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(innerPadding).padding(16.dp)) {
            Text("Assigned Deliveries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            Spacer(modifier = Modifier.height(12.dp))

            val availableOrders = orders.filter { it.status == "READY_FOR_PICKUP" || it.status == "PICKED_UP" || it.status == "OUT_FOR_DELIVERY" }

            if (availableOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active delivery requests right now.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableOrders) { order ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                                    Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("Customer: ${order.customerName}")
                                Text("Delivery Address: ${order.address}")
                                Text("Status: ${order.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerMobile}"))
                                        context.startActivity(intent)
                                    }) {
                                        Icon(Icons.Default.Phone, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call (${order.customerMobile})")
                                    }

                                    if (order.status == "READY_FOR_PICKUP") {
                                        Button(onClick = { viewModel.updateOrderStatus(order.id, "OUT_FOR_DELIVERY", "Delivery Partner") }) {
                                            Text("Start Delivery")
                                        }
                                    } else if (order.status == "OUT_FOR_DELIVERY") {
                                        Button(onClick = { viewModel.updateOrderStatus(order.id, "DELIVERED", "Delivery Partner") }) {
                                            Text("Mark Delivered")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

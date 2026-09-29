package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.room.BusinessEntity
import com.example.data.room.OrderEntity
import com.example.data.room.ProductEntity
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(viewModel: KhushbooViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()
    val businesses by viewModel.businesses.collectAsState()

    Scaffold(
        containerColor = APP_BACKGROUND,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        KhushbooBrandHeader(
                            subtitle = "👑 Super Admin Control Center",
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
                        titleContentColor = TEXT_PRIMARY,
                        actionIconContentColor = ORANGE
                    )
                )
                HorizontalDivider(color = BORDER, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = BORDER, thickness = 1.dp)
                NavigationBar(containerColor = PRIMARY_YELLOW) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DARK_ORANGE,
                        selectedTextColor = DARK_ORANGE,
                        unselectedIconColor = ORANGE,
                        unselectedTextColor = TEXT_SECONDARY,
                        indicatorColor = SECTION_BACKGROUND
                    )

                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("Overview", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("Vendors", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("Orders", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("Analytics", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> AdminOverviewTab(orders, products, businesses)
                1 -> AdminVendorsTab(viewModel, businesses)
                2 -> AdminOrdersTab(viewModel, orders)
                3 -> AdminAnalyticsTab(orders, businesses)
            }
        }
    }
}

@Composable
fun AdminOverviewTab(orders: List<OrderEntity>, products: List<ProductEntity>, businesses: List<BusinessEntity>) {
    val totalRevenue = orders.sumOf { it.totalAmount }
    val pendingVendors = businesses.filter { it.status == "PENDING_APPROVAL" }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("Marketplace Control Center", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Platform GMV", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("₹${totalRevenue.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ORANGE)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pending Approvals", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("${pendingVendors.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DARK_ORANGE)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active Businesses", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("${businesses.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Products", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("${products.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                }
            }
        }
    }
}

@Composable
fun AdminVendorsTab(viewModel: KhushbooViewModel, businesses: List<BusinessEntity>) {
    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("Vendor / Business Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        if (businesses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No vendor applications registered yet.", color = TEXT_SECONDARY)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(businesses) { b ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(b.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                                Surface(
                                    color = if (b.status == "APPROVED") SuccessGreen else ORANGE,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(b.status, color = WHITE, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Owner: ${b.ownerName} (${b.ownerMobile})", fontSize = 12.sp, color = TEXT_PRIMARY)
                            Text("Category: ${b.category} | ${b.address}, ${b.city}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (b.status != "APPROVED") {
                                    Button(
                                        onClick = { viewModel.updateBusinessStatus(b.id, "APPROVED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = WHITE),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Approve Vendor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (b.status == "APPROVED") {
                                    Button(
                                        onClick = { viewModel.updateBusinessStatus(b.id, "SUSPENDED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = WHITE),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Suspend Vendor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun AdminOrdersTab(viewModel: KhushbooViewModel, orders: List<OrderEntity>) {
    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("All Marketplace Orders (${orders.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed on platform yet.", color = TEXT_SECONDARY)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Order #${order.id} (Vendor #${order.vendorId})", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = ORANGE)
                            }
                            Text("Customer: ${order.customerName} (${order.customerMobile})", fontSize = 12.sp, color = TEXT_PRIMARY)
                            Text("Address: ${order.address}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            Text("Status: ${order.status.replace("_", " ")}", fontSize = 12.sp, color = ORANGE, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(orders: List<OrderEntity>, businesses: List<BusinessEntity>) {
    val totalRevenue = orders.sumOf { it.totalAmount }
    val commissionAmount = totalRevenue * 0.05 // 5% marketplace commission

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("Platform Commission & Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Platform Revenue (5% Commission)", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                Text("₹${commissionAmount.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ORANGE)
                Text("Based on total platform sales of ₹${totalRevenue.toInt()}", fontSize = 12.sp, color = TEXT_SECONDARY)
            }
        }
    }
}

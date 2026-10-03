package com.example.ui.vendor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.room.OrderEntity
import com.example.data.room.ProductEntity
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorMainScreen(viewModel: KhushbooViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val vendorProducts by viewModel.vendorProducts.collectAsState()
    val vendorOrders by viewModel.vendorOrders.collectAsState()
    val businesses by viewModel.businesses.collectAsState()
    val selectedVendorId by viewModel.selectedVendorId.collectAsState()

    val currentBusiness = businesses.find { it.id == selectedVendorId } ?: businesses.firstOrNull()

    Scaffold(
        containerColor = APP_BACKGROUND,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        KhushbooBrandHeader(
                            subtitle = "🏪 ${currentBusiness?.name ?: "Vendor Outlet"} (#$selectedVendorId)",
                            brandSize = BrandSize.COMPACT
                        )
                    },
                    actions = {
                        var vendorDropdownExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { vendorDropdownExpanded = true }) {
                            Icon(Icons.Default.Store, contentDescription = "Select Vendor", tint = ORANGE)
                        }
                        DropdownMenu(expanded = vendorDropdownExpanded, onDismissRequest = { vendorDropdownExpanded = false }) {
                            businesses.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.name} (#${b.id})") },
                                    onClick = {
                                        viewModel.selectVendorId(b.id)
                                        vendorDropdownExpanded = false
                                    }
                                )
                            }
                        }

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
                        selectedIconColor = BRIGHT_GOLD,
                        selectedTextColor = BRIGHT_GOLD,
                        unselectedIconColor = InactiveIconColor,
                        unselectedTextColor = InactiveIconColor,
                        indicatorColor = NAV_INDICATOR_COLOR
                    )

                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text("Products", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
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
                        label = { Text("Reports", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> VendorDashboardTab(vendorOrders, vendorProducts)
                1 -> VendorProductsTab(viewModel, vendorProducts, selectedVendorId)
                2 -> VendorOrdersTab(viewModel, vendorOrders)
                3 -> VendorReportsTab(vendorOrders)
            }
        }
    }
}

@Composable
fun VendorDashboardTab(orders: List<OrderEntity>, products: List<ProductEntity>) {
    val totalRevenue = orders.sumOf { it.totalAmount }
    val pendingCount = orders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Vendor Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Revenue", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("₹${totalRevenue.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ORANGE)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pending Orders", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("$pendingCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DARK_ORANGE)
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
                    Text("Total Products", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("${products.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Orders", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("${orders.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Recent Vendor Orders", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(8.dp))

        if (orders.isEmpty()) {
            Text("No orders received yet.", color = TEXT_SECONDARY, modifier = Modifier.padding(vertical = 12.dp))
        } else {
            orders.take(5).forEach { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                    border = BorderStroke(1.dp, BORDER)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                            Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = ORANGE)
                        }
                        Text("Customer: ${order.customerName} (${order.customerMobile})", fontSize = 12.sp, color = TEXT_SECONDARY)
                        Text("Status: ${order.status.replace("_", " ")}", fontSize = 12.sp, color = ORANGE, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorProductsTab(viewModel: KhushbooViewModel, products: List<ProductEntity>, vendorId: Long) {
    var showAddDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sweets") }
    var price by remember { mutableStateOf("150") }
    var stock by remember { mutableStateOf("50") }
    var description by remember { mutableStateOf("Freshly prepared authentic delicacy.") }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Products (${products.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Product", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No products listed yet.", color = TEXT_SECONDARY)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("Category: ${product.category} | Stock: ${product.stock}", fontSize = 12.sp, color = TEXT_SECONDARY)
                                val displayPrice = if (product.priceStandard > 0) product.priceStandard else product.price250g
                                Text("₹$displayPrice", fontWeight = FontWeight.Bold, color = ORANGE)
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Vendor Product") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Sweets/Fast Food)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock Quantity") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addProduct(
                                ProductEntity(
                                    vendorId = vendorId,
                                    name = name,
                                    category = category,
                                    priceStandard = price.toDoubleOrNull() ?: 150.0,
                                    price250g = price.toDoubleOrNull() ?: 150.0,
                                    stock = stock.toIntOrNull() ?: 50,
                                    description = description,
                                    imageUrl = "sweets"
                                )
                            )
                            showAddDialog = false
                            name = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            },
            containerColor = CARD_BACKGROUND
        )
    }
}

@Composable
fun VendorOrdersTab(viewModel: KhushbooViewModel, orders: List<OrderEntity>) {
    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("Vendor Orders (${orders.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders assigned to your vendor account yet.", color = TEXT_SECONDARY)
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
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = ORANGE)
                            }
                            Text("Customer: ${order.customerName} (${order.customerMobile})", fontSize = 13.sp, color = TEXT_PRIMARY)
                            Text("Address: ${order.address}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            Text("Items: ${order.itemsSummary}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Current Status: ${order.status.replace("_", " ")}", fontWeight = FontWeight.Bold, color = ORANGE)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order.id, "PROCESSING", "Vendor") },
                                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Processing", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order.id, "PACKED", "Vendor") },
                                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Packed", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order.id, "READY_FOR_PICKUP", "Vendor") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DARK_ORANGE, contentColor = WHITE),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Ready Pickup", fontSize = 10.sp)
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
fun VendorReportsTab(orders: List<OrderEntity>) {
    val totalSales = orders.sumOf { it.totalAmount }
    val deliveredOrders = orders.filter { it.status == "DELIVERED" }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("Vendor Sales Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Completed Sales", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                Text("₹${totalSales.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ORANGE)
                Text("Delivered Orders: ${deliveredOrders.size} / ${orders.size}", fontSize = 12.sp, color = TEXT_SECONDARY)
            }
        }
    }
}

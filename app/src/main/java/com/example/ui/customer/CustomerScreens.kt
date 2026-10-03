package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.room.AddressEntity
import com.example.data.room.BusinessEntity
import com.example.data.room.CartItemEntity
import com.example.data.room.OrderEntity
import com.example.data.room.ProductEntity
import com.example.ui.main.CustomerScreen
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.OnboardingStep
import com.example.ui.main.StoreWithDistance
import com.example.ui.main.UserRole
import com.example.ui.theme.*
import com.example.util.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMainScreen(viewModel: KhushbooViewModel) {
    val onboardingStep by viewModel.onboardingStep.collectAsState()
    val currentScreen by viewModel.customerScreen.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedProductId by viewModel.selectedProductId.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val trackingOrderId by viewModel.selectedOrderIdForTracking.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val showLocationPicker by viewModel.showLocationPicker.collectAsState()
    val conflict by viewModel.cartConflict.collectAsState()

    // If onboarding is incomplete, show the Onboarding Flow
    if (onboardingStep != OnboardingStep.COMPLETED) {
        CustomerOnboardingFlow(viewModel)
        return
    }

    // Location selection bottom sheet
    if (showLocationPicker) {
        LocationSelectionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showLocationPicker.value = false }
        )
    }

    // Multi-Store Cart Conflict Dialog
    if (conflict != null) {
        AlertDialog(
            onDismissRequest = { viewModel.resolveCartConflict(false) },
            containerColor = CARD_BACKGROUND,
            title = {
                Text("Replace Cart Items?", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            },
            text = {
                Text(
                    "Your cart already contains items from ${conflict?.existingStoreName}. Food delivery orders can only be placed from one store at a time. Would you like to clear your cart and add items from this store?",
                    color = TEXT_SECONDARY,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resolveCartConflict(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Clear Cart & Add Item", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.resolveCartConflict(false) },
                    border = BorderStroke(1.dp, BORDER),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TEXT_SECONDARY)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = APP_BACKGROUND,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.showLocationPicker.value = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, BRIGHT_GOLD, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_uploaded_logo),
                                    contentDescription = "Khushboo Food Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Deliver to",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TEXT_SECONDARY
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(13.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${profile?.selectedLocality ?: "Purulia Town"}, ${profile?.selectedCity ?: "Purulia"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TEXT_PRIMARY,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.SEARCH) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        }

                        Box(modifier = Modifier.clickable { viewModel.navigateCustomerTo(CustomerScreen.CART) }) {
                            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.CART) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                            }
                            if (cartItems.isNotEmpty()) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(1.dp),
                                    containerColor = BRIGHT_GOLD,
                                    contentColor = DarkText
                                ) {
                                    Text("${cartItems.sumOf { it.quantity }}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PRIMARY_YELLOW,
                        titleContentColor = TEXT_PRIMARY,
                        actionIconContentColor = BRIGHT_GOLD
                    )
                )
                HorizontalDivider(color = BORDER, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = BORDER, thickness = 1.dp)
                NavigationBar(
                    containerColor = PRIMARY_YELLOW,
                    modifier = Modifier.height(62.dp)
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BRIGHT_GOLD,
                        selectedTextColor = BRIGHT_GOLD,
                        unselectedIconColor = InactiveIconColor,
                        unselectedTextColor = InactiveIconColor,
                        indicatorColor = NAV_INDICATOR_COLOR
                    )

                    NavigationBarItem(
                        selected = currentScreen == CustomerScreen.HOME,
                        onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(22.dp)) },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = if (currentScreen == CustomerScreen.HOME) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = currentScreen == CustomerScreen.LISTING,
                        onClick = { viewModel.selectCategory("All") },
                        icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu", modifier = Modifier.size(22.dp)) },
                        label = { Text("Menu", fontSize = 10.sp, fontWeight = if (currentScreen == CustomerScreen.LISTING) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = currentScreen == CustomerScreen.SEARCH,
                        onClick = { viewModel.navigateCustomerTo(CustomerScreen.SEARCH) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(22.dp)) },
                        label = { Text("Search", fontSize = 10.sp, fontWeight = if (currentScreen == CustomerScreen.SEARCH) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = currentScreen == CustomerScreen.CART,
                        onClick = { viewModel.navigateCustomerTo(CustomerScreen.CART) },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(22.dp)) },
                        label = { Text("Cart", fontSize = 10.sp, fontWeight = if (currentScreen == CustomerScreen.CART) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = currentScreen == CustomerScreen.MORE_MENU,
                        onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More", modifier = Modifier.size(22.dp)) },
                        label = { Text("More", fontSize = 10.sp, fontWeight = if (currentScreen == CustomerScreen.MORE_MENU) FontWeight.Bold else FontWeight.Normal) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                CustomerScreen.HOME -> CustomerHomeScreen(viewModel, products)
                CustomerScreen.LISTING -> CustomerListingScreen(viewModel, products, selectedCategory)
                CustomerScreen.SEARCH -> CustomerSearchScreen(viewModel, products)
                CustomerScreen.MORE_MENU -> CustomerMenuScreen(viewModel)
                CustomerScreen.PRODUCT_DETAIL -> {
                    val product = products.find { it.id == selectedProductId } ?: products.firstOrNull()
                    if (product != null) {
                        CustomerProductDetailScreen(viewModel, product)
                    } else {
                        Text("Product not found", modifier = Modifier.padding(16.dp), color = TEXT_SECONDARY)
                    }
                }
                CustomerScreen.CART -> CustomerCartScreen(viewModel, cartItems)
                CustomerScreen.CHECKOUT -> CustomerCheckoutScreen(viewModel, cartItems)
                CustomerScreen.ORDER_TRACKING -> {
                    val orders by viewModel.orders.collectAsState()
                    val order = orders.find { it.id == trackingOrderId } ?: orders.firstOrNull()
                    if (order != null) {
                        CustomerTrackingScreen(viewModel, order)
                    } else {
                        Text("Order not found", modifier = Modifier.padding(16.dp), color = TEXT_SECONDARY)
                    }
                }
                CustomerScreen.MY_ORDERS -> CustomerMyOrdersScreen(viewModel)
                CustomerScreen.PROFILE -> CustomerProfileScreen(viewModel)
                CustomerScreen.CREATE_BUSINESS_ACCOUNT -> CustomerCreateBusinessScreen(viewModel)
                CustomerScreen.WISHLIST -> CustomerWishlistScreen(viewModel)
                CustomerScreen.ADDRESSES -> CustomerAddressesScreen(viewModel)
                CustomerScreen.COUPONS -> CustomerCouponsScreen(viewModel)
                CustomerScreen.NOTIFICATIONS -> CustomerNotificationsScreen(viewModel)
                CustomerScreen.HELP_SUPPORT -> CustomerHelpSupportScreen(viewModel)
                CustomerScreen.ABOUT -> CustomerAboutScreen(viewModel)
                CustomerScreen.REVIEWS -> CustomerAboutScreen(viewModel)
                CustomerScreen.SELECT_LOCATION -> {
                    LocationSelectionDialog(viewModel = viewModel, onDismiss = { viewModel.navigateCustomerTo(CustomerScreen.HOME) })
                }
            }
        }
    }
}

// ==========================================
// CUSTOMER HOME SCREEN
// ==========================================
@Composable
fun CustomerHomeScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val storesWithDistance by viewModel.storesWithDistance.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Active Delivery Location Bar
        item {
            Surface(
                color = SECTION_BACKGROUND,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showLocationPicker.value = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(GOLD_CONTAINER, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Delivering to ${profile?.selectedLocality ?: "Purulia Town"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TEXT_PRIMARY
                            )
                            Text(
                                text = profile?.selectedAddress ?: "Main Market Road, Purulia Town",
                                fontSize = 11.sp,
                                color = TEXT_SECONDARY,
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = "CHANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BRIGHT_GOLD,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            HorizontalDivider(color = BORDER)
        }

        // Search Bar Banner
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search sweets, barfi, cakes, lassi, snacks...", color = TEXT_MUTED) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BRIGHT_GOLD) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TEXT_SECONDARY)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CARD_BACKGROUND,
                        unfocusedContainerColor = CARD_BACKGROUND,
                        focusedBorderColor = BRIGHT_GOLD,
                        unfocusedBorderColor = BORDER,
                        focusedTextColor = TEXT_PRIMARY,
                        unfocusedTextColor = TEXT_PRIMARY
                    ),
                    singleLine = true
                )
            }
        }

        // Category Chips
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Explore Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                val categories = listOf("All", "Sweets", "Bengali Sweets", "Barfi", "Laddu", "Rasgulla", "Cakes & Pastries", "Beverages", "Fast Food")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = (selectedCategory == cat)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = CARD_BACKGROUND,
                                labelColor = TEXT_PRIMARY,
                                selectedContainerColor = PRIMARY_GOLD,
                                selectedLabelColor = DarkText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BORDER,
                                selectedBorderColor = BRIGHT_GOLD
                            )
                        )
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER_GOLD)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text("KHUSHBOO SPECIAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BRIGHT_GOLD, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Special Kulhad Lassi & Kaju Katli", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                        Text("Prepared with pure desi ghee & 100% fresh milk.", fontSize = 12.sp, color = TEXT_SECONDARY)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.selectCategory("Sweets") },
                            colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Order Now", fontWeight = FontWeight.Bold, color = DarkText)
                        }
                    }
                }
            }
        }

        // Popular Products List
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Popular Delicacies Near You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                        Text("Instant delivery from nearby kitchens", fontSize = 11.sp, color = TEXT_SECONDARY)
                    }
                    TextButton(onClick = { viewModel.selectCategory("All") }) {
                        Text("See All", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            val filteredProducts = if (searchQuery.isBlank()) {
                products
            } else {
                products.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    val isAvailable = viewModel.isProductAvailableAtLocation(product)
                    val store = viewModel.getStoreForProduct(product)
                    val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

                    ProductCard(
                        product = product,
                        storeName = store?.name ?: "Khushboo Outlet",
                        distanceKm = distanceKm,
                        isAvailable = isAvailable,
                        onClick = { viewModel.selectProduct(product.id) },
                        onAddToCart = {
                            val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                            val variant = if (product.priceStandard > 0) "Standard" else "250g"
                            viewModel.addToCart(product, variant, price)
                        }
                    )
                }
            }
        }

        // Trust Features (Why Khushboo Food?)
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Why Khushboo Food?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FeatureBadge(icon = Icons.Default.Eco, title = "100% Fresh", desc = "Prepared daily")
                    FeatureBadge(icon = Icons.Default.Verified, title = "Pure Ghee", desc = "Top quality")
                    FeatureBadge(icon = Icons.Default.FlashOn, title = "Fast Delivery", desc = "20-30 mins")
                }
            }
        }

        // Explore Nearby Stores Section (Placed directly below Why Khushboo Food?)
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stores Delivering To You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TEXT_PRIMARY
                        )
                        Text(
                            text = "Nearby verified kitchens & sweet shops",
                            fontSize = 11.sp,
                            color = TEXT_SECONDARY
                        )
                    }
                    Text(
                        text = "${storesWithDistance.count { it.isAvailable }} Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BRIGHT_GOLD
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(storesWithDistance) { item ->
                        StoreCard(item = item, onClick = {
                            viewModel.selectCategory("All")
                        })
                    }
                }
            }
        }

        // Location & Outlet Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Main Outlet & Hub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TEXT_PRIMARY)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("KHUSHBOO FOOD, Sweets & Fast Food Center", color = TEXT_PRIMARY)
                    Text("📍 Main Market Road, Purulia Town", fontSize = 12.sp, color = TEXT_SECONDARY)
                    Text("📞 Helpline: 6365839460", fontSize = 12.sp, color = BRIGHT_GOLD)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/owjmnmnfdecTksj5A?g_st=aw"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = DarkText)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Google Maps", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:6365839460"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BRIGHT_GOLD),
                            border = BorderStroke(1.dp, BRIGHT_GOLD)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = BRIGHT_GOLD)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Outlet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("© Khushboo Food Marketplace", fontSize = 12.sp, color = TEXT_SECONDARY)
                Text("Location-Based Multi-Store Food Delivery Network", fontSize = 11.sp, color = TEXT_MUTED)
            }
        }
    }
}

// ==========================================
// STORE CARD COMPONENT
// ==========================================
@Composable
fun StoreCard(item: StoreWithDistance, onClick: () -> Unit) {
    val store = item.store
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
        border = BorderStroke(1.dp, if (item.isAvailable) BORDER_GOLD else BORDER)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (item.isAvailable) Color(0xFF142416) else Color(0xFF261818),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (item.isAvailable) SUCCESS_GREEN else ERROR_RED)
                ) {
                    Text(
                        text = if (item.isAvailable) "DELIVERING NOW" else "UNAVAILABLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isAvailable) SUCCESS_GREEN else ERROR_RED,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("${store.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = store.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TEXT_PRIMARY,
                maxLines = 1
            )

            Text(
                text = store.category,
                fontSize = 11.sp,
                color = TEXT_SECONDARY,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BORDER)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(LocationHelper.formatDistance(item.distanceKm), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BRIGHT_GOLD)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TEXT_SECONDARY, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${item.estimatedMinutes} mins", fontSize = 11.sp, color = TEXT_SECONDARY)
                }
            }
        }
    }
}

// ==========================================
// PRODUCT CARD COMPONENT
// ==========================================
@Composable
fun ProductCard(
    product: ProductEntity,
    storeName: String = "Khushboo Outlet",
    distanceKm: Double = 0.0,
    isAvailable: Boolean = true,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(175.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
        border = BorderStroke(1.dp, BORDER)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = getProductImageRes(product.name)),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                if (!isAvailable) {
                    Surface(
                        color = Color(0xCC0B0B0B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Outside Delivery Area",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ERROR_RED,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = storeName,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = BRIGHT_GOLD,
                maxLines = 1
            )

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                fontSize = 13.5.sp,
                color = TEXT_PRIMARY
            )

            Text(
                text = product.category,
                fontSize = 10.5.sp,
                color = TEXT_SECONDARY
            )

            Spacer(modifier = Modifier.height(4.dp))

            val displayPrice = if (product.priceStandard > 0) product.priceStandard else product.price250g
            val variantLabel = if (product.priceStandard > 0) "" else " (250g)"

            Text(
                text = "₹$displayPrice$variantLabel",
                fontWeight = FontWeight.Bold,
                color = BRIGHT_GOLD,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isAvailable) {
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                }
            } else {
                OutlinedButton(
                    onClick = { /* Disabled */ },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BORDER),
                    colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = TEXT_MUTED)
                ) {
                    Text("Unavailable", fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// MENU, CATEGORY & SEARCH SCREENS
// ==========================================
@Composable
fun CustomerListingScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>, selectedCategory: String) {
    val filtered = if (selectedCategory == "All") products else products.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("$selectedCategory Menu (${filtered.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items found in this category.", color = TEXT_SECONDARY)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { product ->
                    val isAvailable = viewModel.isProductAvailableAtLocation(product)
                    val store = viewModel.getStoreForProduct(product)
                    val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

                    ProductCard(
                        product = product,
                        storeName = store?.name ?: "Khushboo Outlet",
                        distanceKm = distanceKm,
                        isAvailable = isAvailable,
                        onClick = { viewModel.selectProduct(product.id) },
                        onAddToCart = {
                            val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                            val variant = if (product.priceStandard > 0) "Standard" else "250g"
                            viewModel.addToCart(product, variant, price)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerSearchScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>) {
    var query by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search sweets, food, snacks...", color = TEXT_MUTED) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BRIGHT_GOLD) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TEXT_SECONDARY)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = BRIGHT_GOLD,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        val categories = listOf("All", "Sweets", "Barfi", "Laddu", "Bengali Sweets", "Fast Food", "Beverages")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSel = (selectedCat == cat)
                FilterChip(
                    selected = isSel,
                    onClick = { selectedCat = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CARD_BACKGROUND,
                        labelColor = TEXT_PRIMARY,
                        selectedContainerColor = PRIMARY_GOLD,
                        selectedLabelColor = DarkText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSel,
                        borderColor = BORDER,
                        selectedBorderColor = BRIGHT_GOLD
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val results = products.filter {
            (selectedCat == "All" || it.category.equals(selectedCat, ignoreCase = true)) &&
                    (query.isBlank() || it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true))
        }

        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching delicacies found.", color = TEXT_SECONDARY)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(results) { product ->
                    val isAvailable = viewModel.isProductAvailableAtLocation(product)
                    val store = viewModel.getStoreForProduct(product)
                    val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

                    ProductCard(
                        product = product,
                        storeName = store?.name ?: "Khushboo Outlet",
                        distanceKm = distanceKm,
                        isAvailable = isAvailable,
                        onClick = { viewModel.selectProduct(product.id) },
                        onAddToCart = {
                            val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                            val variant = if (product.priceStandard > 0) "Standard" else "250g"
                            viewModel.addToCart(product, variant, price)
                        }
                    )
                }
            }
        }
    }
}

// Product Details Screen
@Composable
fun CustomerProductDetailScreen(viewModel: KhushbooViewModel, product: ProductEntity) {
    val wishlist by viewModel.wishlist.collectAsState()
    val isWishlisted = wishlist.any { it.productId == product.id }
    val store = viewModel.getStoreForProduct(product)
    val isAvailable = viewModel.isProductAvailableAtLocation(product)
    val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

    var selectedVariant by remember { mutableStateOf(if (product.priceStandard > 0) "Standard" else "250g") }
    var currentPrice by remember {
        mutableStateOf(if (product.priceStandard > 0) product.priceStandard else product.price250g)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Product Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                Icon(
                    if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) ERROR_RED else BRIGHT_GOLD
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = getProductImageRes(product.name)),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Store & Distance Badge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER_GOLD)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(store?.name ?: "Khushboo Food Main Outlet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BRIGHT_GOLD)
                    Text(store?.address ?: "Main Market Road, Purulia", fontSize = 11.sp, color = TEXT_SECONDARY)
                }
                Surface(
                    color = if (isAvailable) Color(0xFF142416) else Color(0xFF261818),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (isAvailable) "Within Delivery Range (${LocationHelper.formatDistance(distanceKm)})" else "Outside Range",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) SUCCESS_GREEN else ERROR_RED,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Text("Category: ${product.category}", color = TEXT_SECONDARY)
        Spacer(modifier = Modifier.height(6.dp))
        Text("₹$currentPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BRIGHT_GOLD)
        Spacer(modifier = Modifier.height(10.dp))
        Text(product.description, style = MaterialTheme.typography.bodyMedium, color = TEXT_PRIMARY, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (product.priceStandard == 0.0) {
            Text("Select Variant / Weight", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("250g" to product.price250g, "500g" to product.price500g, "1kg" to product.price1kg).forEach { (variant, price) ->
                    val isSel = selectedVariant == variant
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedVariant = variant
                            currentPrice = price
                        },
                        label = { Text("$variant (₹$price)", fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CARD_BACKGROUND,
                            labelColor = TEXT_PRIMARY,
                            selectedContainerColor = PRIMARY_GOLD,
                            selectedLabelColor = DarkText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = BORDER,
                            selectedBorderColor = BRIGHT_GOLD
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (isAvailable) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.addToCart(product, selectedVariant, currentPrice) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CARD_BACKGROUND, contentColor = BRIGHT_GOLD),
                    border = BorderStroke(1.dp, BRIGHT_GOLD)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BRIGHT_GOLD)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Cart", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        viewModel.addToCart(product, selectedVariant, currentPrice)
                        viewModel.navigateCustomerTo(CustomerScreen.CART)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Buy Now", fontWeight = FontWeight.Bold, color = DarkText)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261818)),
                border = BorderStroke(1.dp, ERROR_RED)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unavailable for Delivery at Selected Address", fontWeight = FontWeight.Bold, color = ERROR_RED, fontSize = 13.sp)
                    Text("Please change your delivery location to a closer area to order from this store.", fontSize = 11.sp, color = TEXT_SECONDARY, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ==========================================
// CART & CHECKOUT WITH REALTIME LOCATION VALIDATION
// ==========================================
@Composable
fun CustomerCartScreen(viewModel: KhushbooViewModel, cartItems: List<CartItemEntity>) {
    val validation = viewModel.validateCartForCheckout()
    val profile by viewModel.profile.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Cart (${cartItems.sumOf { it.quantity }} items)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            if (cartItems.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Clear Cart", color = ERROR_RED, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = TEXT_MUTED)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your cart is empty", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY, fontSize = 16.sp)
                    Text("Add delicious sweets & snacks from stores near you", color = TEXT_SECONDARY, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) },
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore Delicacies", fontWeight = FontWeight.Bold, color = DarkText)
                    }
                }
            }
        } else {
            // Active Store Info Banner
            val storeName = cartItems.firstOrNull()?.storeName ?: "Khushboo Food"
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER_GOLD)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ordering from: $storeName", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                        Text("Delivering to: ${profile?.selectedLocality ?: "Purulia"}", fontSize = 11.sp, color = TEXT_SECONDARY)
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY, fontSize = 14.sp)
                                Text("Variant: ${item.variant} | ₹${item.price}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateCartQty(item.id, item.quantity - 1) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BRIGHT_GOLD)
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY, modifier = Modifier.padding(horizontal = 6.dp))
                                IconButton(onClick = { viewModel.updateCartQty(item.id, item.quantity + 1) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = BRIGHT_GOLD)
                                }
                                IconButton(onClick = { viewModel.removeFromCart(item.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ERROR_RED)
                                }
                            }
                        }
                    }
                }
            }

            val subtotal = cartItems.sumOf { it.price * it.quantity }
            val deliveryFee = validation.deliveryFee
            val grandTotal = subtotal + deliveryFee

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item Total", color = TEXT_PRIMARY, fontSize = 13.sp)
                        Text("₹$subtotal", color = TEXT_PRIMARY, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Partner Fee (${LocationHelper.formatDistance(validation.distanceKm)})", color = TEXT_SECONDARY, fontSize = 12.sp)
                        Text(if (deliveryFee == 0.0) "FREE" else "₹$deliveryFee", color = if (deliveryFee == 0.0) SUCCESS_GREEN else BRIGHT_GOLD, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BORDER)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY, fontSize = 15.sp)
                        Text("₹$grandTotal", fontWeight = FontWeight.ExtraBold, color = BRIGHT_GOLD, fontSize = 16.sp)
                    }
                }
            }

            if (!validation.isValid && validation.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1616)),
                    border = BorderStroke(1.dp, ERROR_RED)
                ) {
                    Text(
                        text = "⚠️ ${validation.errorMessage}",
                        color = ERROR_RED,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.navigateCustomerTo(CustomerScreen.CHECKOUT) },
                enabled = validation.isValid,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText,
                    disabledContainerColor = CARD_BACKGROUND,
                    disabledContentColor = TEXT_MUTED
                )
            ) {
                Text("Proceed to Checkout", fontWeight = FontWeight.ExtraBold, color = if (validation.isValid) DarkText else TEXT_MUTED, fontSize = 15.sp)
            }
        }
    }
}

// Checkout Screen
@Composable
fun CustomerCheckoutScreen(viewModel: KhushbooViewModel, cartItems: List<CartItemEntity>) {
    val profile by viewModel.profile.collectAsState()
    val validation = viewModel.validateCartForCheckout()

    var name by remember(profile) { mutableStateOf(profile?.name ?: "Rahul Sharma") }
    var mobile by remember(profile) { mutableStateOf(profile?.mobile ?: "6365839460") }
    var paymentMethod by remember { mutableStateOf("UPI / Online Payment") }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = validation.deliveryFee
    val grandTotal = subtotal + deliveryFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.CART) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Order Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Delivery Address Card with Change Affordance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER_GOLD)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delivery Location", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                    }
                    TextButton(onClick = { viewModel.showLocationPicker.value = true }) {
                        Text("Change", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Text(profile?.selectedAddress ?: "Main Market Road, Purulia", fontSize = 12.5.sp, color = TEXT_SECONDARY)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(color = GOLD_CONTAINER, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "Distance to ${validation.store?.name ?: "Store"}: ${LocationHelper.formatDistance(validation.distanceKm)}",
                        fontSize = 10.sp,
                        color = BRIGHT_GOLD,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = BRIGHT_GOLD,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Contact Phone") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = BRIGHT_GOLD,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Mode", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(8.dp))

        listOf("UPI / Online Payment", "Credit / Debit Card", "Cash on Delivery (COD)").forEach { method ->
            val isSelected = paymentMethod == method
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { paymentMethod = method },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) GOLD_CONTAINER else CARD_BACKGROUND),
                border = BorderStroke(1.dp, if (isSelected) BRIGHT_GOLD else BORDER)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { paymentMethod = method },
                        colors = RadioButtonDefaults.colors(selectedColor = BRIGHT_GOLD)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(method, color = TEXT_PRIMARY, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val summary = cartItems.joinToString(", ") { "${it.productName} (${it.variant}) x${it.quantity}" }
                viewModel.placeOrder(
                    customerName = name,
                    customerMobile = mobile,
                    address = profile?.selectedAddress ?: "Purulia Town",
                    itemsSummary = summary,
                    itemTotal = subtotal,
                    deliveryFee = deliveryFee,
                    discountAmount = 0.0,
                    totalAmount = grandTotal,
                    paymentMethod = paymentMethod,
                    deliveryMode = validation.deliveryMode
                ) { orderId ->
                    viewModel.viewOrderTracking(orderId)
                }
            },
            enabled = validation.isValid && name.isNotBlank() && mobile.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
        ) {
            Text("Place Order (₹$grandTotal)", fontWeight = FontWeight.ExtraBold, color = DarkText, fontSize = 15.sp)
        }
    }
}

// ==========================================
// ORDER TRACKING SCREEN
// ==========================================
@Composable
fun CustomerTrackingScreen(viewModel: KhushbooViewModel, order: OrderEntity) {
    val context = LocalContext.current
    val statuses = listOf(
        "CONFIRMED" to "Order Confirmed",
        "STORE_ACCEPTED" to "Store Accepted",
        "PREPARING" to "Preparing Fresh Delicacies",
        "READY_FOR_PICKUP" to "Ready for Pickup",
        "DELIVERY_ASSIGNED" to "Delivery Partner Assigned",
        "PICKED_UP" to "Picked Up",
        "OUT_FOR_DELIVERY" to "Out for Delivery",
        "DELIVERED" to "Delivered"
    )

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MY_ORDERS) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Live Order #${order.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live ETA Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GOLD_CONTAINER),
            border = BorderStroke(1.5.dp, BRIGHT_GOLD),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(CARD_BACKGROUND, CircleShape)
                        .border(1.dp, BRIGHT_GOLD, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Estimated Delivery Time", fontSize = 11.sp, color = TEXT_SECONDARY)
                    Text("${order.estimatedMinutes} Minutes", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BRIGHT_GOLD)
                    Text("Status: ${order.status.replace("_", " ")}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Delivery Partner Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Assigned Delivery Fleet", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TEXT_MUTED)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SECTION_BACKGROUND, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BRIGHT_GOLD)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(order.deliveryPartnerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TEXT_PRIMARY)
                            Text("Delivery Partner • Verified", fontSize = 11.sp, color = SUCCESS_GREEN)
                        }
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.deliveryPartnerPhone}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = DarkText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Timeline Progress
        Text("ORDER PROGRESS TIMELINE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TEXT_MUTED)
        Spacer(modifier = Modifier.height(8.dp))

        val currentIndex = statuses.indexOfFirst { it.first == order.status }

        statuses.forEachIndexed { index, (_, label) ->
            val isCompleted = index <= currentIndex || currentIndex == -1 && index == 0
            Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(if (isCompleted) BRIGHT_GOLD else SECTION_BACKGROUND, CircleShape)
                        .border(1.dp, if (isCompleted) BRIGHT_GOLD else BORDER, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DarkText, modifier = Modifier.size(13.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal, color = if (isCompleted) TEXT_PRIMARY else TEXT_MUTED, fontSize = 13.sp)
            }
        }
    }
}

// ==========================================
// MY ORDERS SCREEN
// ==========================================
@Composable
fun CustomerMyOrdersScreen(viewModel: KhushbooViewModel) {
    val orders by viewModel.orders.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("My Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed yet.", color = TEXT_SECONDARY)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.viewOrderTracking(order.id) },
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("₹${order.totalAmount}", fontWeight = FontWeight.ExtraBold, color = BRIGHT_GOLD)
                            }
                            Text("Store: ${order.storeName}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Items: ${order.itemsSummary}", fontSize = 13.sp, maxLines = 1, color = TEXT_PRIMARY)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = GOLD_CONTAINER,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        order.status.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BRIGHT_GOLD,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("Track Order →", color = BRIGHT_GOLD, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CUSTOMER MENU, ADDRESSES & SETTINGS
// ==========================================
@Composable
fun CustomerMenuScreen(viewModel: KhushbooViewModel) {
    val profile by viewModel.profile.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateCustomerTo(CustomerScreen.PROFILE) },
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(SECTION_BACKGROUND)
                        .border(1.dp, BRIGHT_GOLD, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile?.name ?: "Rahul Sharma", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TEXT_PRIMARY)
                    Text(profile?.mobile ?: "+91 6365839460", fontSize = 12.sp, color = TEXT_SECONDARY)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BRIGHT_GOLD)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Business Account Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateCustomerTo(CustomerScreen.CREATE_BUSINESS_ACCOUNT) },
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.5.dp, BRIGHT_GOLD)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Register as Food Store Seller", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                    Text("List your outlet & reach customers across your city", fontSize = 11.sp, color = TEXT_SECONDARY)
                }
                Surface(
                    color = PRIMARY_GOLD,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply", color = DarkText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("MY SHOPPING & ORDERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_MUTED, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column {
                MenuItemRow(icon = Icons.Default.ReceiptLong, title = "My Orders", subtitle = "View current & past orders") {
                    viewModel.navigateCustomerTo(CustomerScreen.MY_ORDERS)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.Favorite, title = "Wishlist (${wishlist.size})", subtitle = "Your saved favorite delicacies") {
                    viewModel.navigateCustomerTo(CustomerScreen.WISHLIST)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.ShoppingCart, title = "Cart", subtitle = "Review items & checkout") {
                    viewModel.navigateCustomerTo(CustomerScreen.CART)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("SAVED ADDRESSES & DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_MUTED, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column {
                MenuItemRow(icon = Icons.Default.LocationOn, title = "My Saved Addresses", subtitle = "Manage delivery addresses & GPS pins") {
                    viewModel.navigateCustomerTo(CustomerScreen.ADDRESSES)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.LocalOffer, title = "Coupons & Offers", subtitle = "Active promo codes & discounts") {
                    viewModel.navigateCustomerTo(CustomerScreen.COUPONS)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.Notifications, title = "Notifications (${notifications.size})", subtitle = "Order alerts & deals") {
                    viewModel.navigateCustomerTo(CustomerScreen.NOTIFICATIONS)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("SUPPORT & ROLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_MUTED, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column {
                MenuItemRow(icon = Icons.Default.Help, title = "Help & Support", subtitle = "FAQs, contact & customer care") {
                    viewModel.navigateCustomerTo(CustomerScreen.HELP_SUPPORT)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.Info, title = "About Khushboo Food", subtitle = "Outlet story & legal info") {
                    viewModel.navigateCustomerTo(CustomerScreen.ABOUT)
                }
                HorizontalDivider(color = BORDER)
                Box {
                    MenuItemRow(icon = Icons.Default.AdminPanelSettings, title = "Switch App Role", subtitle = "Vendor, Delivery Partner or Admin Panel") {
                        roleMenuExpanded = true
                    }
                    DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Customer App (Active)") }, onClick = { roleMenuExpanded = false })
                        DropdownMenuItem(text = { Text("Switch to Vendor App") }, onClick = { roleMenuExpanded = false; viewModel.setRole(UserRole.VENDOR) })
                        DropdownMenuItem(text = { Text("Switch to Delivery Fleet") }, onClick = { roleMenuExpanded = false; viewModel.setRole(UserRole.DELIVERY) })
                        DropdownMenuItem(text = { Text("Switch to Super Admin") }, onClick = { roleMenuExpanded = false; viewModel.setRole(UserRole.ADMIN) })
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TEXT_PRIMARY)
            Text(subtitle, fontSize = 11.sp, color = TEXT_SECONDARY)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TEXT_MUTED, modifier = Modifier.size(18.dp))
    }
}

// Business Registration Screen
@Composable
fun CustomerCreateBusinessScreen(viewModel: KhushbooViewModel) {
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("Rahul Sharma") }
    var ownerMobile by remember { mutableStateOf("6365839460") }
    var ownerEmail by remember { mutableStateOf("rahul.sharma@gmail.com") }
    var category by remember { mutableStateOf("Sweets & Fast Food") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("Main Road, Purulia") }
    var city by remember { mutableStateOf("Purulia") }
    var state by remember { mutableStateOf("West Bengal") }
    var pincode by remember { mutableStateOf("723101") }
    var deliveryRadius by remember { mutableStateOf("10.0") }
    var applicationSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Register Seller Store", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (applicationSubmitted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.5.dp, BRIGHT_GOLD)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Application Submitted Successfully!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TEXT_PRIMARY)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Status: PENDING APPROVAL", fontWeight = FontWeight.Bold, color = BRIGHT_GOLD, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your store registration for '$businessName' has been sent for admin verification.", textAlign = TextAlign.Center, fontSize = 12.sp, color = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setRole(UserRole.VENDOR) },
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Text("Open Vendor Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Store Details & Mandatory Location", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Store / Outlet Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = ownerMobile, onValueChange = { ownerMobile = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Store Address (Full Street & Area)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("PIN") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = deliveryRadius, onValueChange = { deliveryRadius = it }, label = { Text("Delivery Radius (in km)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (businessName.isNotBlank() && address.isNotBlank()) {
                                viewModel.registerBusiness(
                                    BusinessEntity(
                                        name = businessName,
                                        ownerName = ownerName,
                                        ownerMobile = ownerMobile,
                                        ownerEmail = ownerEmail,
                                        category = category,
                                        description = description,
                                        address = address,
                                        locality = city,
                                        city = city,
                                        state = state,
                                        pincode = pincode,
                                        deliveryRadius = deliveryRadius.toDoubleOrNull() ?: 10.0,
                                        status = "APPROVED"
                                    )
                                ) {
                                    applicationSubmitted = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Text("Submit Store Registration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Addresses Screen
@Composable
fun CustomerAddressesScreen(viewModel: KhushbooViewModel) {
    val addresses by viewModel.addresses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("Home") }
    var fullAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Purulia") }
    var pincode by remember { mutableStateOf("723101") }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
                }
                Text("Saved Addresses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Address", tint = BRIGHT_GOLD)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(addresses) { addr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                    border = BorderStroke(1.dp, if (addr.isDefault) BRIGHT_GOLD else BORDER)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BRIGHT_GOLD)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${addr.title} ${if (addr.isDefault) "(Active Delivery Pin)" else ""}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                            Text(addr.fullAddress, fontSize = 12.sp, color = TEXT_SECONDARY)
                        }
                        IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ERROR_RED)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Delivery Address", color = TEXT_PRIMARY) },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Label (Home/Work/Other)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = fullAddress, onValueChange = { fullAddress = it }, label = { Text("Full Address Line") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("PIN Code") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullAddress.isNotBlank()) {
                            viewModel.addAddress(
                                title = title,
                                fullAddress = fullAddress,
                                houseNumber = "",
                                street = "",
                                locality = city,
                                city = city,
                                state = "West Bengal",
                                pincode = pincode,
                                lat = LocationHelper.DEFAULT_LAT,
                                lng = LocationHelper.DEFAULT_LNG
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Save Address")
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

// Profile Screen
@Composable
fun CustomerProfileScreen(viewModel: KhushbooViewModel) {
    val profile by viewModel.profile.collectAsState()
    var name by remember(profile) { mutableStateOf(profile?.name ?: "Rahul Sharma") }
    var mobile by remember(profile) { mutableStateOf(profile?.mobile ?: "6365839460") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "rahul.sharma@gmail.com") }
    var address by remember(profile) { mutableStateOf(profile?.defaultAddress ?: "Main Market Road, Purulia Town") }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, BRIGHT_GOLD, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_uploaded_logo),
                contentDescription = "Profile Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(profile?.name ?: "Rahul Sharma", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Text(profile?.mobile ?: "+91 6365839460", color = TEXT_SECONDARY)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Text(if (isEditing) "Cancel" else "Edit", color = BRIGHT_GOLD)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (isEditing) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Default Address") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.updateProfile(name, mobile, email, address)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Name: $name", color = TEXT_PRIMARY)
                    Text("Mobile: $mobile", color = TEXT_PRIMARY)
                    Text("Email: $email", color = TEXT_PRIMARY)
                    Text("Delivery Pin: $address", color = TEXT_SECONDARY)
                }
            }
        }
    }
}

// Wishlist Screen
@Composable
fun CustomerWishlistScreen(viewModel: KhushbooViewModel) {
    val wishlist by viewModel.wishlist.collectAsState()
    val products by viewModel.products.collectAsState()
    val wishlistedProducts = products.filter { p -> wishlist.any { it.productId == p.id } }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("My Wishlist (${wishlistedProducts.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (wishlistedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your wishlist is empty", color = TEXT_SECONDARY)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishlistedProducts) { product ->
                    val isAvailable = viewModel.isProductAvailableAtLocation(product)
                    val store = viewModel.getStoreForProduct(product)
                    val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

                    ProductCard(
                        product = product,
                        storeName = store?.name ?: "Khushboo Outlet",
                        distanceKm = distanceKm,
                        isAvailable = isAvailable,
                        onClick = { viewModel.selectProduct(product.id) },
                        onAddToCart = {
                            val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                            val variant = if (product.priceStandard > 0) "Standard" else "250g"
                            viewModel.addToCart(product, variant, price)
                        }
                    )
                }
            }
        }
    }
}

// Coupons Screen
@Composable
fun CustomerCouponsScreen(viewModel: KhushbooViewModel) {
    val coupons by viewModel.coupons.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Coupons & Offers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(coupons) { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                    border = BorderStroke(1.dp, BORDER_GOLD)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coupon.code, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BRIGHT_GOLD)
                            Text("Get ${coupon.discountPercent}% OFF up to ₹${coupon.maxDiscount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                            Text("Min order: ₹${coupon.minOrderValue}", fontSize = 11.sp, color = TEXT_SECONDARY)
                        }
                        Button(
                            onClick = { viewModel.navigateCustomerTo(CustomerScreen.CART) },
                            colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                        ) {
                            Text("Use Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Notifications Screen
@Composable
fun CustomerNotificationsScreen(viewModel: KhushbooViewModel) {
    val notifications by viewModel.notifications.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No new notifications.", color = TEXT_SECONDARY)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BRIGHT_GOLD)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(notif.title, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text(notif.message, fontSize = 12.sp, color = TEXT_SECONDARY)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Help & Support Screen
@Composable
fun CustomerHelpSupportScreen(viewModel: KhushbooViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("Help & Support", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Need Assistance?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Our customer care helpline is active 7 days a week from 8 AM to 10 PM.", fontSize = 12.sp, color = TEXT_SECONDARY)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:6365839460"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Helpline 6365839460", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("FREQUENTLY ASKED QUESTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TEXT_MUTED)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "How does location-based ordering work?" to "We calculate your distance to each store using GPS. You can only order from stores within their delivery radius.",
            "Can I order from multiple stores in one cart?" to "Each order is dedicated to a single store to ensure fast, uncompromised delivery quality.",
            "How is the delivery fee calculated?" to "Delivery fees are calculated based on your distance from the store (free for orders above ₹499).",
            "How do I track my active delivery partner?" to "Open the 'My Orders' section and tap on your order to view the live timeline and driver details."
        ).forEach { (q, a) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Q: $q", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                    Text("A: $a", fontSize = 12.sp, color = TEXT_SECONDARY)
                }
            }
        }
    }
}

// About Screen
@Composable
fun CustomerAboutScreen(viewModel: KhushbooViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Text("About Khushboo Food", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                KhushbooBrandHeader(
                    subtitle = "Location-Based Sweets & Food Marketplace",
                    brandSize = BrandSize.LARGE
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Khushboo Food connects food lovers with authentic local sweets shops, pure desi ghee kitchens, and restaurants within their neighborhood for lightning-fast delivery.",
                    textAlign = TextAlign.Center,
                    fontSize = 12.5.sp,
                    color = TEXT_PRIMARY,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Main Outlet & Kitchen Hub", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(4.dp))
                Text("📍 Main Branch, Main Market Road, Purulia Town, West Bengal 723101", fontSize = 12.sp, color = TEXT_PRIMARY)
                Text("📞 Phone: 6365839460", fontSize = 12.sp, color = BRIGHT_GOLD)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/owjmnmnfdecTksj5A?g_st=aw"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open on Google Maps", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helpers
fun getProductImageRes(productName: String): Int {
    return when {
        productName.contains("Lassi", ignoreCase = true) -> R.drawable.img_prod_kulhad_lassi_1790683930543
        productName.contains("Katli", ignoreCase = true) || productName.contains("Barfi", ignoreCase = true) -> R.drawable.img_prod_kaju_katli_1790683943334
        productName.contains("Cake", ignoreCase = true) || productName.contains("Pastry", ignoreCase = true) -> R.drawable.img_prod_cake_pastry_1790683967769
        productName.contains("Pizza", ignoreCase = true) || productName.contains("Burger", ignoreCase = true) || productName.contains("Chat", ignoreCase = true) -> R.drawable.img_prod_pizza_burger_1790683979853
        else -> R.drawable.img_prod_gulab_jamun_1790683956047
    }
}

@Composable
fun FeatureBadge(icon: ImageVector, title: String, desc: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(SECTION_BACKGROUND, CircleShape)
                .border(1.dp, BORDER_GOLD, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, color = TEXT_PRIMARY)
        Text(desc, fontSize = 10.sp, color = TEXT_SECONDARY, textAlign = TextAlign.Center)
    }
}

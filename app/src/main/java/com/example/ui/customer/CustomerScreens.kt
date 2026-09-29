package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.ui.main.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMainScreen(viewModel: KhushbooViewModel) {
    val currentScreen by viewModel.customerScreen.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedProductId by viewModel.selectedProductId.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val trackingOrderId by viewModel.selectedOrderIdForTracking.collectAsState()

    Scaffold(
        containerColor = APP_BACKGROUND,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        KhushbooBrandHeader()
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.SEARCH) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = ORANGE, modifier = Modifier.size(20.dp))
                        }

                        Box(modifier = Modifier.clickable { viewModel.navigateCustomerTo(CustomerScreen.CART) }) {
                            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.CART) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = ORANGE, modifier = Modifier.size(20.dp))
                            }
                            if (cartItems.isNotEmpty()) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(1.dp),
                                    containerColor = ORANGE,
                                    contentColor = WHITE
                                ) {
                                    Text("${cartItems.sumOf { it.quantity }}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ORANGE, modifier = Modifier.size(20.dp))
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
                NavigationBar(
                    containerColor = PRIMARY_YELLOW,
                    modifier = Modifier.height(62.dp)
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DARK_ORANGE,
                        selectedTextColor = DARK_ORANGE,
                        unselectedIconColor = ORANGE,
                        unselectedTextColor = TEXT_SECONDARY,
                        indicatorColor = SECTION_BACKGROUND
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
                        Text("Product not found", modifier = Modifier.padding(16.dp))
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
                        Text("Order not found", modifier = Modifier.padding(16.dp))
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
            }
        }
    }
}

@Composable
fun CustomerHomeScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Search bar banner
        item {
            Surface(
                color = SECTION_BACKGROUND,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Fresh Sweets, Delicious Food & More",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_PRIMARY
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search sweets, cakes, lassi, pizza...", color = TEXT_SECONDARY) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ORANGE) },
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
                            focusedBorderColor = ORANGE,
                            unfocusedBorderColor = BORDER,
                            focusedTextColor = TEXT_PRIMARY,
                            unfocusedTextColor = TEXT_PRIMARY
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Categories Row
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    "Explore Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                                selectedContainerColor = ORANGE,
                                selectedLabelColor = WHITE
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BORDER,
                                selectedBorderColor = ORANGE
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        Text("KHUSHBOO SPECIAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ORANGE)
                        Text("Special Kulhad Lassi & Kaju Katli", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                        Text("Prepared with pure desi ghee & 100% fresh milk.", fontSize = 13.sp, color = TEXT_SECONDARY)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.selectCategory("Sweets") },
                            colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                        ) {
                            Text("Order Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Popular Products
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Popular & Best Sellers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                    TextButton(onClick = { viewModel.selectCategory("All") }) {
                        Text("See All", color = ORANGE, fontWeight = FontWeight.Bold)
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
                    ProductCard(product = product, onClick = { viewModel.selectProduct(product.id) }, onAddToCart = {
                        val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                        val variant = if (product.priceStandard > 0) "Standard" else "250g"
                        viewModel.addToCart(product, variant, price)
                    })
                }
            }
        }

        // Why Khushboo Food
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Why Khushboo Food?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FeatureBadge(icon = Icons.Default.Eco, title = "100% Fresh", desc = "Prepared daily")
                    FeatureBadge(icon = Icons.Default.Verified, title = "Pure Ghee", desc = "Top quality")
                    FeatureBadge(icon = Icons.Default.FlashOn, title = "Fast Delivery", desc = "Hot & hygienic")
                }
            }
        }

        // Location & Contact Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Visit Our Outlet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TEXT_PRIMARY)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("KHUSHBOO FOOD, Sweets & Fast Food Center", color = TEXT_PRIMARY)
                    Text("📞 Contact: 6365839460", color = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/owjmnmnfdecTksj5A?g_st=aw"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = WHITE)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Google Maps", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:6365839460"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ORANGE),
                            border = BorderStroke(1.dp, ORANGE)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = ORANGE)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call 6365839460", fontWeight = FontWeight.Bold)
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
                Text("Authentic Sweets & Multi-Vendor Delicacies", fontSize = 11.sp, color = TEXT_SECONDARY)
            }
        }
    }
}

// Compact Premium Customer Menu Screen
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
        // User Profile Header Card
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
                        .background(SECTION_BACKGROUND),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ORANGE, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile?.name ?: "Rahul Sharma", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TEXT_PRIMARY)
                    Text(profile?.mobile ?: "+91 6365839460", fontSize = 12.sp, color = TEXT_SECONDARY)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ORANGE)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent Create Business Account Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateCustomerTo(CustomerScreen.CREATE_BUSINESS_ACCOUNT) },
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.5.dp, ORANGE)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = ORANGE, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Create Business Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                    Text("Register your sweet shop / food outlet as a vendor", fontSize = 11.sp, color = TEXT_SECONDARY)
                }
                Surface(
                    color = ORANGE,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply", color = WHITE, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Section 1: Orders & Shopping
        Text("MY SHOPPING & ORDERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_SECONDARY, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
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
                MenuItemRow(icon = Icons.Default.LocalShipping, title = "Track Order", subtitle = "Live status of active deliveries") {
                    viewModel.navigateCustomerTo(CustomerScreen.ORDER_TRACKING)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.Favorite, title = "Wishlist (${wishlist.size})", subtitle = "Your saved favorite delicacies") {
                    viewModel.navigateCustomerTo(CustomerScreen.WISHLIST)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.ShoppingCart, title = "Cart", subtitle = "Review cart items & checkout") {
                    viewModel.navigateCustomerTo(CustomerScreen.CART)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Section 2: Account & Settings
        Text("ACCOUNT & SAVED DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_SECONDARY, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column {
                MenuItemRow(icon = Icons.Default.Person, title = "My Profile", subtitle = "Edit name, email, phone & photo") {
                    viewModel.navigateCustomerTo(CustomerScreen.PROFILE)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.LocationOn, title = "My Addresses", subtitle = "Manage home & office delivery addresses") {
                    viewModel.navigateCustomerTo(CustomerScreen.ADDRESSES)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.LocalOffer, title = "Coupons & Offers", subtitle = "Explore active promo codes & discounts") {
                    viewModel.navigateCustomerTo(CustomerScreen.COUPONS)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.Notifications, title = "Notifications (${notifications.size})", subtitle = "Order alerts & deals") {
                    viewModel.navigateCustomerTo(CustomerScreen.NOTIFICATIONS)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Section 3: Support & App Roles
        Text("SUPPORT & ROLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_SECONDARY, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column {
                MenuItemRow(icon = Icons.Default.Help, title = "Help & Support", subtitle = "FAQs, chat & customer care") {
                    viewModel.navigateCustomerTo(CustomerScreen.HELP_SUPPORT)
                }
                HorizontalDivider(color = BORDER)
                MenuItemRow(icon = Icons.Default.Info, title = "About Khushboo Food", subtitle = "Outlet location, contact & story") {
                    viewModel.navigateCustomerTo(CustomerScreen.ABOUT)
                }
                HorizontalDivider(color = BORDER)
                Box {
                    MenuItemRow(icon = Icons.Default.AdminPanelSettings, title = "Switch App Role", subtitle = "Vendor, Delivery or Admin Panel") {
                        roleMenuExpanded = true
                    }
                    DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Customer App (Active)") },
                            onClick = { roleMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to Vendor App") },
                            onClick = { roleMenuExpanded = false; viewModel.setRole(UserRole.VENDOR) }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to Delivery Partner") },
                            onClick = { roleMenuExpanded = false; viewModel.setRole(UserRole.DELIVERY) }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to Super Admin") },
                            onClick = { roleMenuExpanded = false; viewModel.setRole(UserRole.ADMIN) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = BorderStroke(1.dp, ErrorRed)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Account", fontWeight = FontWeight.Bold)
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
        Icon(icon, contentDescription = null, tint = ORANGE, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TEXT_PRIMARY)
            Text(subtitle, fontSize = 11.sp, color = TEXT_SECONDARY)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TEXT_SECONDARY, modifier = Modifier.size(18.dp))
    }
}

// Business Registration Screen
@Composable
fun CustomerCreateBusinessScreen(viewModel: KhushbooViewModel) {
    val context = LocalContext.current
    val businesses by viewModel.businesses.collectAsState()

    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("Rahul Sharma") }
    var ownerMobile by remember { mutableStateOf("6365839460") }
    var ownerEmail by remember { mutableStateOf("rahul.sharma@gmail.com") }
    var category by remember { mutableStateOf("Sweets & Fast Food") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Sweets City") }
    var state by remember { mutableStateOf("State") }
    var pincode by remember { mutableStateOf("110001") }
    var openingTime by remember { mutableStateOf("09:00 AM") }
    var closingTime by remember { mutableStateOf("10:00 PM") }
    var gstNumber by remember { mutableStateOf("") }
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
            }
            Text("Register Business Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (applicationSubmitted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.5.dp, ORANGE)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = ORANGE, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Application Submitted Successfully!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TEXT_PRIMARY)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Status: PENDING APPROVAL", fontWeight = FontWeight.Bold, color = ORANGE, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your business registration for '$businessName' has been sent to Super Admin for verification. You will be notified once approved.", textAlign = TextAlign.Center, fontSize = 12.sp, color = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setRole(UserRole.VENDOR) },
                        colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                    ) {
                        Text("Open Vendor Dashboard Preview", fontWeight = FontWeight.Bold)
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
                    Text("Business Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business / Outlet Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ownerMobile,
                            onValueChange = { ownerMobile = it },
                            label = { Text("Mobile Number") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ownerEmail,
                            onValueChange = { ownerEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Business Category (e.g. Sweets & Fast Food)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Business Description & Specialties") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Outlet Address Line") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it },
                            label = { Text("PIN Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it },
                            label = { Text("Opening Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it },
                            label = { Text("Closing Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it },
                        label = { Text("GST Number (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                                        city = city,
                                        state = state,
                                        pincode = pincode,
                                        openingTime = openingTime,
                                        closingTime = closingTime,
                                        gstNumber = gstNumber,
                                        status = "PENDING_APPROVAL"
                                    )
                                ) {
                                    applicationSubmitted = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                    ) {
                        Text("Submit Business Application", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Search Screen
@Composable
fun CustomerSearchScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>) {
    var query by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search sweets, barfi, cakes, lassi...", color = TEXT_SECONDARY) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ORANGE) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TEXT_SECONDARY)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            ),
            singleLine = true
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
                        selectedContainerColor = ORANGE,
                        selectedLabelColor = WHITE
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSel,
                        borderColor = BORDER,
                        selectedBorderColor = ORANGE
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
                    ProductCard(product = product, onClick = { viewModel.selectProduct(product.id) }, onAddToCart = {
                        val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                        val variant = if (product.priceStandard > 0) "Standard" else "250g"
                        viewModel.addToCart(product, variant, price)
                    })
                }
            }
        }
    }
}

// Listing Screen
@Composable
fun CustomerListingScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>, category: String) {
    val filtered = if (category == "All") products else products.filter { it.category.equals(category, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("Our Menu - $category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { product ->
                ProductCard(product = product, onClick = { viewModel.selectProduct(product.id) }, onAddToCart = {
                    val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                    val variant = if (product.priceStandard > 0) "Standard" else "250g"
                    viewModel.addToCart(product, variant, price)
                })
            }
        }
    }
}

// Product Detail Screen
@Composable
fun CustomerProductDetailScreen(viewModel: KhushbooViewModel, product: ProductEntity) {
    val wishlist by viewModel.wishlist.collectAsState()
    val isWishlisted = wishlist.any { it.productId == product.id }

    var selectedVariant by remember { mutableStateOf(if (product.priceStandard > 0) "Standard" else "250g") }
    var currentPrice by remember {
        mutableStateOf(
            if (product.priceStandard > 0) product.priceStandard else product.price250g
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = getProductImageRes(product.name)),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { viewModel.toggleWishlist(product.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(WHITE, CircleShape)
            ) {
                Icon(
                    if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) ErrorRed else ORANGE
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Text("Category: ${product.category}", color = TEXT_SECONDARY)
        Spacer(modifier = Modifier.height(8.dp))
        Text("₹$currentPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ORANGE)
        Spacer(modifier = Modifier.height(12.dp))
        Text(product.description, style = MaterialTheme.typography.bodyMedium, color = TEXT_PRIMARY)
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
                            selectedContainerColor = ORANGE,
                            selectedLabelColor = WHITE
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = BORDER,
                            selectedBorderColor = ORANGE
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.addToCart(product, selectedVariant, currentPrice) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Cart", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    viewModel.addToCart(product, selectedVariant, currentPrice)
                    viewModel.navigateCustomerTo(CustomerScreen.CART)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DARK_ORANGE, contentColor = WHITE)
            ) {
                Text("Buy Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Cart Screen
@Composable
fun CustomerCartScreen(viewModel: KhushbooViewModel, cartItems: List<CartItemEntity>) {
    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("My Cart (${cartItems.size} items)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your cart is empty", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                    Button(
                        onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) },
                        modifier = Modifier.padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                    ) {
                        Text("Start Shopping", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("Variant: ${item.variant} | ₹${item.price}", fontSize = 12.sp, color = TEXT_SECONDARY)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateCartQty(item.id, item.quantity - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = ORANGE)
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                IconButton(onClick = { viewModel.updateCartQty(item.id, item.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = ORANGE)
                                }
                                IconButton(onClick = { viewModel.removeFromCart(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }

            val subtotal = cartItems.sumOf { it.price * it.quantity }
            val deliveryCharge = 30.0
            val grandTotal = subtotal + deliveryCharge

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = TEXT_PRIMARY)
                        Text("₹$subtotal", color = TEXT_PRIMARY)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Charge", color = TEXT_PRIMARY)
                        Text("₹$deliveryCharge", color = TEXT_PRIMARY)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BORDER)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                        Text("₹$grandTotal", fontWeight = FontWeight.Bold, color = ORANGE)
                    }
                }
            }

            Button(
                onClick = { viewModel.navigateCustomerTo(CustomerScreen.CHECKOUT) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
            ) {
                Text("Proceed to Checkout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Checkout Screen
@Composable
fun CustomerCheckoutScreen(viewModel: KhushbooViewModel, cartItems: List<CartItemEntity>) {
    var name by remember { mutableStateOf("Rahul Sharma") }
    var mobile by remember { mutableStateOf("6365839460") }
    var address by remember { mutableStateOf("House 42, Main Market Road, Near Temple") }
    var paymentMethod by remember { mutableStateOf("UPI / Online Payment") }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val grandTotal = subtotal + 30.0

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Delivery Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CARD_BACKGROUND,
                unfocusedContainerColor = CARD_BACKGROUND,
                focusedBorderColor = ORANGE,
                unfocusedBorderColor = BORDER,
                focusedTextColor = TEXT_PRIMARY,
                unfocusedTextColor = TEXT_PRIMARY
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Method", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(8.dp))

        listOf("UPI / Online Payment", "Credit / Debit Card", "Cash on Delivery (COD)").forEach { method ->
            val isSelected = paymentMethod == method
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { paymentMethod = method },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) LIGHT_YELLOW else CARD_BACKGROUND),
                border = BorderStroke(1.dp, if (isSelected) ORANGE else BORDER)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { paymentMethod = method },
                        colors = RadioButtonDefaults.colors(selectedColor = ORANGE)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(method, color = TEXT_PRIMARY, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val summary = cartItems.joinToString(", ") { "${it.productName} (${it.variant}) x${it.quantity}" }
                viewModel.placeOrder(
                    customerName = name,
                    customerMobile = mobile,
                    address = address,
                    itemsSummary = summary,
                    totalAmount = grandTotal,
                    paymentMethod = paymentMethod
                ) { orderId ->
                    viewModel.viewOrderTracking(orderId)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
        ) {
            Text("Place Order (₹$grandTotal)", fontWeight = FontWeight.Bold)
        }
    }
}

// Order Tracking Screen
@Composable
fun CustomerTrackingScreen(viewModel: KhushbooViewModel, order: OrderEntity) {
    val statuses = listOf(
        "ORDER_PLACED" to "Order Placed",
        "CONFIRMED" to "Confirmed",
        "PROCESSING" to "Processing",
        "PACKED" to "Packed",
        "READY_FOR_PICKUP" to "Ready for Pickup",
        "PICKED_UP" to "Picked Up",
        "OUT_FOR_DELIVERY" to "Out for Delivery",
        "DELIVERED" to "Delivered"
    )

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Order Tracking #${order.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status: ${order.status.replace("_", " ")}", fontWeight = FontWeight.Bold, color = ORANGE)
                Text("Amount: ₹${order.totalAmount} | Payment: ${order.paymentMethod}", color = TEXT_PRIMARY)
                Text("Deliver to: ${order.address}", color = TEXT_SECONDARY)
                Text("Items: ${order.itemsSummary}", color = TEXT_SECONDARY)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Timeline", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        val currentIndex = statuses.indexOfFirst { it.first == order.status }

        statuses.forEachIndexed { index, (key, label) ->
            val isCompleted = index <= currentIndex
            Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(if (isCompleted) ORANGE else SECTION_BACKGROUND, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = WHITE, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal, color = TEXT_PRIMARY)
            }
        }
    }
}

// My Orders Screen
@Composable
fun CustomerMyOrdersScreen(viewModel: KhushbooViewModel) {
    val orders by viewModel.orders.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Text("My Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed yet.", color = TEXT_SECONDARY)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = ORANGE)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Items: ${order.itemsSummary}", fontSize = 13.sp, maxLines = 1, color = TEXT_SECONDARY)
                            Text("Status: ${order.status.replace("_", " ")}", fontSize = 12.sp, color = ORANGE)
                        }
                    }
                }
            }
        }
    }
}

// Profile Screen
@Composable
fun CustomerProfileScreen(viewModel: KhushbooViewModel) {
    val profile by viewModel.profile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.name ?: "Rahul Sharma") }
    var mobile by remember(profile) { mutableStateOf(profile?.mobile ?: "6365839460") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "rahul.sharma@gmail.com") }
    var address by remember(profile) { mutableStateOf(profile?.defaultAddress ?: "House 42, Main Market Road, Near Temple") }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_uploaded_logo),
            contentDescription = "Profile Logo",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
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
                        Text(if (isEditing) "Cancel" else "Edit", color = ORANGE)
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
                        colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Name: $name", color = TEXT_PRIMARY)
                    Text("Mobile: $mobile", color = TEXT_PRIMARY)
                    Text("Email: $email", color = TEXT_PRIMARY)
                    Text("Address: $address", color = TEXT_SECONDARY)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.navigateCustomerTo(CustomerScreen.ADDRESSES) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SECTION_BACKGROUND, contentColor = TEXT_PRIMARY)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = ORANGE)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Saved Addresses", fontWeight = FontWeight.Bold)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
            }
            Text("My Wishlist (${wishlistedProducts.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (wishlistedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TEXT_SECONDARY, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your wishlist is empty", color = TEXT_PRIMARY, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { viewModel.selectCategory("All") },
                        modifier = Modifier.padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                    ) {
                        Text("Explore Delicacies", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishlistedProducts) { product ->
                    ProductCard(product = product, onClick = { viewModel.selectProduct(product.id) }, onAddToCart = {
                        val price = if (product.priceStandard > 0) product.priceStandard else product.price250g
                        val variant = if (product.priceStandard > 0) "Standard" else "250g"
                        viewModel.addToCart(product, variant, price)
                    })
                }
            }
        }
    }
}

// Saved Addresses Screen
@Composable
fun CustomerAddressesScreen(viewModel: KhushbooViewModel) {
    val addresses by viewModel.addresses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("Home") }
    var addressLine by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Sweets City") }
    var pincode by remember { mutableStateOf("110001") }

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
                }
                Text("Saved Addresses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Address", tint = ORANGE)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(addresses) { addr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                    border = BorderStroke(1.dp, BORDER)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ORANGE)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${addr.title} ${if (addr.isDefault) "(Default)" else ""}", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                            Text("${addr.addressLine}, ${addr.city} - ${addr.pincode}", fontSize = 12.sp, color = TEXT_SECONDARY)
                        }
                        IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Delivery Address") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (Home/Office)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addressLine, onValueChange = { addressLine = it }, label = { Text("Address Line") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("PIN Code") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addressLine.isNotBlank()) {
                            viewModel.addAddress(title, addressLine, city, pincode)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
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

// Coupons Screen
@Composable
fun CustomerCouponsScreen(viewModel: KhushbooViewModel) {
    val coupons by viewModel.coupons.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
            }
            Text("Coupons & Offers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(coupons) { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                    border = BorderStroke(1.dp, ORANGE)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = ORANGE, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coupon.code, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ORANGE)
                            Text("Get ${coupon.discountPercent}% OFF up to ₹${coupon.maxDiscount}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                            Text("Min order: ₹${coupon.minOrderValue}", fontSize = 11.sp, color = TEXT_SECONDARY)
                        }
                        Button(
                            onClick = { viewModel.navigateCustomerTo(CustomerScreen.CART) },
                            colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
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
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ORANGE)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
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
                Text("Need Immediate Assistance?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Our customer care helpline is active 7 days a week from 8 AM to 10 PM.", fontSize = 12.sp, color = TEXT_SECONDARY)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:6365839460"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Helpline 6365839460", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("FREQUENTLY ASKED QUESTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TEXT_SECONDARY)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "How long does delivery take?" to "Usually within 30–45 minutes depending on distance and order size.",
            "Are sweets made in pure desi ghee?" to "Yes, all Khushboo Food traditional sweets use 100% pure desi ghee.",
            "How do I track my active order?" to "Open the 'Track Order' option in the Menu to view live timeline status.",
            "Can I apply to become a vendor?" to "Yes! Click 'Create Business Account' in the menu and submit your registration details."
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

// About Khushboo Food Screen
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ORANGE)
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
                    subtitle = "Sweets & Fast Food Marketplace Platform",
                    brandSize = BrandSize.LARGE
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Khushboo Food is committed to delivering authentic Indian sweets, pure desi ghee delicacies, fresh kulhad lassi, cakes, and fast food directly to your doorstep.", textAlign = TextAlign.Center, fontSize = 12.sp, color = TEXT_PRIMARY)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Outlet Location & Contact", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(4.dp))
                Text("📍 Main Branch, Sweets City, Main Market Road", fontSize = 12.sp, color = TEXT_PRIMARY)
                Text("📞 Phone: 6365839460", fontSize = 12.sp, color = TEXT_SECONDARY)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/owjmnmnfdecTksj5A?g_st=aw"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Location on Google Maps", fontWeight = FontWeight.Bold)
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
fun ProductCard(product: ProductEntity, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
        border = BorderStroke(1.dp, BORDER)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = getProductImageRes(product.name)),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 14.sp, color = TEXT_PRIMARY)
            Text(product.category, fontSize = 11.sp, color = TEXT_SECONDARY)
            Spacer(modifier = Modifier.height(4.dp))
            val displayPrice = if (product.priceStandard > 0) product.priceStandard else product.price250g
            val variantLabel = if (product.priceStandard > 0) "" else " (250g)"
            Text("₹$displayPrice$variantLabel", fontWeight = FontWeight.Bold, color = ORANGE, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = WHITE)
            ) {
                Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FeatureBadge(icon: ImageVector, title: String, desc: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SECTION_BACKGROUND, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ORANGE)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, color = TEXT_PRIMARY)
        Text(desc, fontSize = 10.sp, color = TEXT_SECONDARY, textAlign = TextAlign.Center)
    }
}

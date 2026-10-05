package com.example.ui.customer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.testTag
import com.example.R
import com.example.data.room.AddressEntity
import com.example.data.room.BusinessEntity
import com.example.data.room.CartItemEntity
import com.example.data.room.CategoryEntity
import com.example.data.room.OrderEntity
import com.example.data.room.ProductEntity
import com.example.ui.main.CustomerScreen
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.OnboardingStep
import com.example.ui.main.PermissionStatus
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

data class HeroBannerItem(
    val imageRes: Int,
    val tag: String,
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val targetCategory: String
)

val HERO_BANNERS = listOf(
    HeroBannerItem(
        imageRes = R.drawable.hero_signature_1791067902606,
        tag = "KHUSHBOO SIGNATURE",
        title = "Authentic Taste. Delivered.",
        subtitle = "Royal feast & authentic delicacies from top local outlets",
        buttonText = "Explore Now",
        targetCategory = "All"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_sweets_1791067917652,
        tag = "INDIAN SWEETS",
        title = "Sweetness for Every Moment",
        subtitle = "Pure Desi Ghee Kaju Katli, Gulab Jamun & Barfi",
        buttonText = "Order Sweets",
        targetCategory = "Sweets"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_street_food_1791067932481,
        tag = "STREET FOOD CORNER",
        title = "Crispy Samosas & Tangy Chaat",
        subtitle = "Freshly made hot snacks delivered piping hot",
        buttonText = "Explore Chaat",
        targetCategory = "Fast Food"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_bengali_1791067952360,
        tag = "BENGALI SPECIAL",
        title = "Authentic Taste of Bengal",
        subtitle = "Melt-in-mouth Nolen Gur Sandesh & Spongy Rasgulla",
        buttonText = "View Bengali",
        targetCategory = "Bengali Sweets"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_biryani_1791067966981,
        tag = "BIRYANI LOVERS",
        title = "Rich. Aromatic. Delicious.",
        subtitle = "Clay handi dum biryani with rich saffron aroma",
        buttonText = "Order Biryani",
        targetCategory = "Fast Food"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_desserts_1791067982877,
        tag = "DESSERT SPECIAL",
        title = "Cakes, Pastries & Desserts",
        subtitle = "Rich chocolate truffle & gourmet baked treats",
        buttonText = "View Desserts",
        targetCategory = "Cakes & Pastries"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_drinks_1791067995788,
        tag = "DRINKS & REFRESHMENTS",
        title = "Special Kulhad Lassi & Shakes",
        subtitle = "Traditional Punjabi lassi with thick clotted malai",
        buttonText = "Order Drinks",
        targetCategory = "Beverages"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_local_market_1791068024182,
        tag = "LOCAL FOOD MARKET",
        title = "Nearby Verified Store Hubs",
        subtitle = "Discover popular authentic kitchens in your city",
        buttonText = "Discover Stores",
        targetCategory = "All"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_festive_1791068038180,
        tag = "FESTIVE FOOD SPREAD",
        title = "Mithai & Celebration Hampers",
        subtitle = "Pure ghee laddus and gift boxes for festivities",
        buttonText = "Gift Sweets",
        targetCategory = "Sweets"
    ),
    HeroBannerItem(
        imageRes = R.drawable.hero_marketplace_1791068053933,
        tag = "KHUSHBOO MARKETPLACE",
        title = "Connecting Local Flavor with You",
        subtitle = "Lightning-fast doorstep food delivery in 20-30 mins",
        buttonText = "Explore Menu",
        targetCategory = "All"
    )
)

@Composable
fun KhushbooHeroCarousel(
    viewModel: KhushbooViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { HERO_BANNERS.size })

    // Auto-scroll every 4.5 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4500L)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % HERO_BANNERS.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(210.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
            border = BorderStroke(1.dp, BORDER_GOLD)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val banner = HERO_BANNERS[page]
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Hero Image
                        Image(
                            painter = painterResource(id = banner.imageRes),
                            contentDescription = banner.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Rich Gradient Overlay for maximum readability and black+gold polish
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0x770B0B0B),
                                            Color(0xEE0B0B0B)
                                        ),
                                        startY = 60f
                                    )
                                )
                        )

                        // Text & CTA Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Surface(
                                color = Color(0xCC000000),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.8.dp, BRIGHT_GOLD)
                            ) {
                                Text(
                                    text = banner.tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BRIGHT_GOLD,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = banner.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TEXT_PRIMARY,
                                maxLines = 1
                            )

                            Text(
                                text = banner.subtitle,
                                fontSize = 11.5.sp,
                                color = TEXT_SECONDARY,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.selectCategory(banner.targetCategory) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PRIMARY_GOLD,
                                    contentColor = DarkText
                                )
                            ) {
                                Text(
                                    text = banner.buttonText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = DarkText,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dot Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HERO_BANNERS.indices.forEach { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 8.dp else 6.dp)
                        .background(
                            color = if (isSelected) BRIGHT_GOLD else InactiveIconColor.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .border(
                            width = if (isSelected) 0.5.dp else 0.dp,
                            color = if (isSelected) DarkText else Color.Transparent,
                            shape = CircleShape
                        )
                )
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
        // 1. Location Header
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
                                .size(32.dp)
                                .background(GOLD_CONTAINER, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Deliver to",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TEXT_SECONDARY
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍 ", fontSize = 13.sp)
                                Text(
                                    text = profile?.selectedLocality ?: "Purulia Town",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TEXT_PRIMARY,
                                    maxLines = 1
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(16.dp))
                            }
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

        // 2. HERO IMAGE CAROUSEL (ABOVE THE SEARCH BAR - ONLY ONE IMAGE AT A TIME)
        item {
            KhushbooHeroCarousel(viewModel = viewModel)
        }

        // 3. Search Bar Banner
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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

        // 4. Explore Categories (Premium Circular Food-Category Layout)
        item {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Explore Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TEXT_PRIMARY
                        )
                        Text(
                            text = "Fresh Indian sweets, fast food & treats",
                            fontSize = 11.sp,
                            color = TEXT_SECONDARY
                        )
                    }
                    TextButton(onClick = { viewModel.selectCategory("All", 0L) }) {
                        Text(
                            text = "View All",
                            color = BRIGHT_GOLD,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val categoriesList by viewModel.categories.collectAsState()

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriesList, key = { it.categoryId }) { cat ->
                        val isSelected = (selectedCategory == cat.name)
                        CircularCategoryItem(
                            category = cat,
                            isSelected = isSelected,
                            onClick = { viewModel.selectCategory(cat) }
                        )
                    }
                }
            }
        }

        // 5. Popular Delicacies Near You List
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
            val baseProducts = if (searchQuery.isBlank()) {
                if (selectedCategory == "All") products else products.filter {
                    it.category.equals(selectedCategory, ignoreCase = true) ||
                            (selectedCategory == "Indian Sweets" && it.category.contains("Sweets", ignoreCase = true))
                }
            } else {
                products.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
            }
            // Home should show only stores/products that can deliver to the selected location
            val filteredProducts = baseProducts.filter { viewModel.isProductAvailableAtLocation(it) }

            if (filteredProducts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                    border = BorderStroke(1.dp, BORDER)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No items available in this category for your area.", fontSize = 13.sp, color = TEXT_SECONDARY)
                    }
                }
            } else {
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
                                if (product.hasValidPrice) {
                                    val price = product.price!!
                                    val variant = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard"
                                    viewModel.addToCart(product, variant, price, 1)
                                } else {
                                    viewModel.selectProduct(product.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 6. Trust Features (Why Khushboo Food?)
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

        // 7. PRODUCT SECTION BELOW "WHY KHUSBOO FOOD" (Vertical Product Feed)
        // Immediately after "Why Khusboo Food", display real available products from the existing database
        val realAvailableProducts = products.filter { it.isAvailable }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "All Available Products",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TEXT_PRIMARY
                        )
                        Text(
                            text = "Freshly prepared delicacies ready for instant delivery",
                            fontSize = 11.5.sp,
                            color = TEXT_SECONDARY
                        )
                    }
                    Surface(
                        color = Color(0xFF142416),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SUCCESS_GREEN)
                    ) {
                        Text(
                            text = "${realAvailableProducts.size} Items Available",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SUCCESS_GREEN,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (realAvailableProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                    border = BorderStroke(1.dp, BORDER)
                ) {
                    Text(
                        text = "No products currently available in inventory.",
                        fontSize = 13.sp,
                        color = TEXT_SECONDARY,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(realAvailableProducts, key = { it.id }) { product ->
                val isAvailable = viewModel.isProductAvailableAtLocation(product)
                val store = viewModel.getStoreForProduct(product)
                val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

                VerticalProductCard(
                    product = product,
                    storeName = store?.name ?: "Khushboo Outlet",
                    distanceKm = distanceKm,
                    isAvailable = isAvailable,
                    onClick = { viewModel.selectProduct(product.id) },
                    onAddToCart = {
                        if (product.hasValidPrice && product.isAvailable) {
                            val price = product.price!!
                            val variant = if (product.unit.equals("kg", ignoreCase = true)) "1kg" else (if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard")
                            viewModel.addToCart(product, variant, price, 1)
                            Toast.makeText(context, "Added ${product.name} to cart", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.selectProduct(product.id)
                        }
                    }
                )
            }
        }

        // 8. Explore Nearby Stores Section (Placed below vertical product feed)
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
                    val storesDelivering = storesWithDistance.filter { it.isAvailable && it.distanceKm <= it.store.deliveryRadius }
                    Text(
                        text = "${storesDelivering.size} Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BRIGHT_GOLD
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val storesDelivering = storesWithDistance.filter { it.isAvailable && it.distanceKm <= it.store.deliveryRadius }
                if (storesDelivering.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Text(
                            text = "No stores currently delivering to this exact location radius. You can change your location pin above.",
                            fontSize = 12.sp,
                            color = TEXT_SECONDARY,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(storesDelivering) { item ->
                            StoreCard(item = item, onClick = {
                                viewModel.selectCategory("All")
                            })
                        }
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
        border = BorderStroke(1.dp, BORDER_GOLD)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF142416),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, SUCCESS_GREEN)
                ) {
                    Text(
                        text = "DELIVERING NOW",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SUCCESS_GREEN,
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

// Currency Formatter Utility
fun formatCurrency(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        "₹${amount.toInt()}"
    } else {
        val formatted = String.format(Locale.getDefault(), "%.2f", amount)
        "₹$formatted"
    }
}

// ==========================================
// VERTICAL PRODUCT CARD COMPONENT (Feed below Why Khushboo Food)
// ==========================================
@Composable
fun VerticalProductCard(
    product: ProductEntity,
    storeName: String = "Khushboo Outlet",
    distanceKm: Double = 0.0,
    isAvailable: Boolean = true,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .clickable { onClick() }
            .testTag("vertical_product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
        border = BorderStroke(1.dp, BORDER)
    ) {
        Column {
            // Prominent Food Image with object-fit / cover and rounded corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                Image(
                    painter = painterResource(id = getProductImageRes(product.name)),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Overlays: Availability Badge & Distance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAvailable && product.isAvailable) {
                        Surface(
                            color = Color(0xE6142416),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SUCCESS_GREEN)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SUCCESS_GREEN, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Available Now",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SUCCESS_GREEN
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xE62A1515),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ERROR_RED)
                        ) {
                            Text(
                                text = "Currently Unavailable",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ERROR_RED,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (distanceKm > 0.0) {
                        Surface(
                            color = Color(0xE6121212),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, BORDER_GOLD)
                        ) {
                            Text(
                                text = LocationHelper.formatDistance(distanceKm),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BRIGHT_GOLD,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Product Details and Action
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TEXT_PRIMARY,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.category,
                        fontSize = 12.sp,
                        color = TEXT_SECONDARY
                    )
                    Text(
                        text = " • $storeName",
                        fontSize = 11.5.sp,
                        color = BRIGHT_GOLD,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.hasValidPrice) {
                            val formattedPrice = formatCurrency(product.price!!)
                            val unitSuffix = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "/${product.unit}" else ""
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedPrice,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BRIGHT_GOLD,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = unitSuffix,
                                    fontSize = 12.5.sp,
                                    color = TEXT_SECONDARY,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Price unavailable",
                                fontWeight = FontWeight.Medium,
                                color = TEXT_MUTED,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Add to Cart Action Button
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("add_to_cart_${product.id}"),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = DarkText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add to Cart",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
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

            if (product.hasValidPrice) {
                val formattedPrice = if (product.price!! % 1.0 == 0.0) {
                    product.price.toInt().toString()
                } else {
                    String.format(java.util.Locale.getDefault(), "%.1f", product.price)
                }
                val unitSuffix = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "/${product.unit}" else ""
                Text(
                    text = "₹$formattedPrice$unitSuffix",
                    fontWeight = FontWeight.Bold,
                    color = BRIGHT_GOLD,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Price unavailable",
                    fontWeight = FontWeight.Medium,
                    color = TEXT_MUTED,
                    fontSize = 12.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
            ) {
                Text("+ Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
            }
        }
    }
}

// ==========================================
// MENU, CATEGORY & SEARCH SCREENS
// ==========================================
@Composable
fun CustomerListingScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>, selectedCategory: String) {
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }

    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val categoriesList by viewModel.categories.collectAsState()
    val filtered = filterProductsForCategory(products, selectedCategory, selectedCategoryId)

    Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND)) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateCustomerTo(CustomerScreen.HOME) },
                modifier = Modifier
                    .size(40.dp)
                    .background(CARD_BACKGROUND, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedCategory == "All") "All Menu Items" else selectedCategory,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY
                )
                Text(
                    text = "${filtered.size} items available",
                    fontSize = 12.sp,
                    color = TEXT_SECONDARY
                )
            }
        }

        // Horizontal Circular Category Selector for quick category switching
        if (categoriesList.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(76.dp)
                            .clickable { viewModel.selectCategory("All", 0L) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(if (selectedCategory == "All") GOLD_CONTAINER else SECTION_BACKGROUND)
                                .border(
                                    width = if (selectedCategory == "All") 2.dp else 1.2.dp,
                                    color = if (selectedCategory == "All") BRIGHT_GOLD else BORDER,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.RestaurantMenu,
                                contentDescription = "All",
                                tint = if (selectedCategory == "All") BRIGHT_GOLD else TEXT_SECONDARY,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All",
                            color = if (selectedCategory == "All") BRIGHT_GOLD else TEXT_PRIMARY,
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedCategory == "All") FontWeight.ExtraBold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                items(categoriesList, key = { it.categoryId }) { cat ->
                    val isSelected = (selectedCategory == cat.name)
                    CircularCategoryItem(
                        category = cat,
                        isSelected = isSelected,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }
            HorizontalDivider(color = BORDER, modifier = Modifier.padding(top = 8.dp))
        }

        // Category Product Listing
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = TEXT_MUTED,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No items currently available in $selectedCategory",
                        color = TEXT_PRIMARY,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Check back soon as local kitchens update their daily menus",
                        color = TEXT_SECONDARY,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.selectCategory("All", 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Text("Explore All Items", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { product ->
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
                            if (product.hasValidPrice) {
                                val price = product.price!!
                                val variant = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard"
                                viewModel.addToCart(product, variant, price, 1)
                            } else {
                                viewModel.selectProduct(product.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerSearchScreen(viewModel: KhushbooViewModel, products: List<ProductEntity>) {
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }
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

        val categories = listOf("All", "Indian Sweets", "Drinks", "Chaat / Snacks", "Cakes & Pastries", "Fast Food")
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
            (selectedCat == "All" || it.category.equals(selectedCat, ignoreCase = true) || (selectedCat == "Indian Sweets" && it.category.contains("Sweets", ignoreCase = true))) &&
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
                            if (product.hasValidPrice) {
                                val price = product.price!!
                                val variant = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard"
                                viewModel.addToCart(product, variant, price, 1)
                            } else {
                                viewModel.selectProduct(product.id)
                            }
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
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }
    val wishlist by viewModel.wishlist.collectAsState()
    val isWishlisted = wishlist.any { it.productId == product.id }
    val store = viewModel.getStoreForProduct(product)
    val isAvailable = viewModel.isProductAvailableAtLocation(product)
    val distanceKm = viewModel.getStoreDistanceKm(product.vendorId)

    val isWeightBased = product.unit.equals("kg", ignoreCase = true)
    val hasPrice = product.hasValidPrice

    var selectedWeightLabel by remember(product.id) { mutableStateOf("1kg") }
    var selectedMultiplier by remember(product.id) { mutableDoubleStateOf(1.0) }
    var quantity by remember(product.id) { mutableIntStateOf(1) }

    // Dynamic price calculation
    val dynamicUnitPrice = if (hasPrice) (product.price!! * selectedMultiplier) else 0.0
    val dynamicTotalPrice = dynamicUnitPrice * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Navigation Header
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

        // Large Product Image
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = getProductImageRes(product.name)),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Availability Badge Overlay
            Surface(
                color = if (isAvailable && product.isAvailable) Color(0xE6142416) else Color(0xE62A1515),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isAvailable && product.isAvailable) SUCCESS_GREEN else ERROR_RED),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isAvailable && product.isAvailable) SUCCESS_GREEN else ERROR_RED, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isAvailable && product.isAvailable) "Available Now" else "Out of Stock",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable && product.isAvailable) SUCCESS_GREEN else ERROR_RED
                    )
                }
            }
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
                    color = Color(0xFF142416),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, SUCCESS_GREEN)
                ) {
                    Text(
                        "Delivery Range • ${LocationHelper.formatDistance(distanceKm)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SUCCESS_GREEN,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Product Name
        Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)

        // Category Name
        Text("Category: ${product.category}", color = TEXT_SECONDARY, fontSize = 13.5.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // Price & Unit Display
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasPrice) {
                val formattedBasePrice = formatCurrency(product.price!!)
                Text(formattedBasePrice, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = BRIGHT_GOLD)
                val unitLabel = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "/${product.unit}" else ""
                Text(unitLabel, style = MaterialTheme.typography.titleMedium, color = TEXT_SECONDARY, modifier = Modifier.padding(start = 4.dp))
            } else {
                Text("Price unavailable", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_MUTED)
                Text(" • Unit: ${product.unit}", color = TEXT_MUTED, fontSize = 13.sp, modifier = Modifier.padding(start = 6.dp))
            }
        }

        // Product Description (gracefully hide if empty)
        if (product.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Product Description", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(product.description, style = MaterialTheme.typography.bodyMedium, color = TEXT_SECONDARY, lineHeight = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight Selector for products sold by weight (kg)
        if (hasPrice && isWeightBased) {
            Text("Select Weight Option", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY, fontSize = 14.sp)
            Text(
                "Dynamic price calculated from base price of ${formatCurrency(product.price!!)}/kg",
                fontSize = 11.5.sp,
                color = TEXT_SECONDARY
            )
            Spacer(modifier = Modifier.height(8.dp))

            val weightOptions = listOf(
                "250g" to 0.25,
                "500g" to 0.5,
                "1kg" to 1.0,
                "2kg" to 2.0
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weightOptions.forEach { (label, mult) ->
                    val isSel = selectedWeightLabel == label
                    val calculatedWeightAmount = product.calculateWeightPrice(mult)
                    val formattedAmount = formatCurrency(calculatedWeightAmount)
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedWeightLabel = label
                            selectedMultiplier = mult
                        },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(label, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                                Text(formattedAmount, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = if (isSel) DarkText else BRIGHT_GOLD)
                            }
                        },
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
        }

        // Quantity Selector [ - ] Quantity [ + ]
        if (hasPrice) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity Selector", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                    Text(
                        if (isWeightBased) "Quantity of $selectedWeightLabel" else "Quantity (${product.unit})",
                        fontSize = 11.sp,
                        color = TEXT_SECONDARY
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(CARD_BACKGROUND, RoundedCornerShape(8.dp))
                        .border(1.dp, BORDER_GOLD, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BRIGHT_GOLD)
                    }
                    Text(
                        text = "$quantity",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = TEXT_PRIMARY,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = BRIGHT_GOLD)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated Subtotal Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val portionDesc = if (isWeightBased) "$quantity × $selectedWeightLabel" else "$quantity ${product.unit}"
                    Text("Total Price ($portionDesc):", color = TEXT_PRIMARY, fontSize = 13.sp)
                    val formattedTotal = formatCurrency(dynamicTotalPrice)
                    Text(formattedTotal, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = BRIGHT_GOLD)
                }
            }
        } else {
            // Price unavailable notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF262015)),
                border = BorderStroke(1.dp, BRIGHT_GOLD.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("⚠️ Pricing Notice", fontWeight = FontWeight.Bold, color = BRIGHT_GOLD, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Price has not been provided by seller for this item. Ordering will be enabled once seller updates pricing.",
                        color = TEXT_PRIMARY,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: ADD TO CART & BUY NOW
        if (hasPrice) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val variant = if (isWeightBased) selectedWeightLabel else (if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard")
                        viewModel.addToCart(product, variant, dynamicUnitPrice, quantity)
                        Toast.makeText(context, "Added $quantity × ${product.name} ($variant) to cart", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("detail_add_to_cart"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CARD_BACKGROUND, contentColor = BRIGHT_GOLD),
                    border = BorderStroke(1.dp, BRIGHT_GOLD)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BRIGHT_GOLD)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD TO CART", fontWeight = FontWeight.Bold, color = BRIGHT_GOLD)
                }
                Button(
                    onClick = {
                        val variant = if (isWeightBased) selectedWeightLabel else (if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard")
                        viewModel.addToCart(product, variant, dynamicUnitPrice, quantity)
                        viewModel.navigateCustomerTo(CustomerScreen.CHECKOUT)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("detail_buy_now"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("BUY NOW", fontWeight = FontWeight.ExtraBold, color = DarkText)
                }
            }
        } else {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = CARD_BACKGROUND,
                    disabledContentColor = TEXT_MUTED
                )
            ) {
                Text("Price Unavailable (Checkout Restricted)", fontWeight = FontWeight.Bold, color = TEXT_MUTED)
            }
        }
    }
}

// ==========================================
// CART & CHECKOUT WITH REALTIME LOCATION VALIDATION
// ==========================================
@Composable
fun CustomerCartScreen(viewModel: KhushbooViewModel, cartItems: List<CartItemEntity>) {
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }
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
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }
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
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }
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

// ==========================================
// ADDRESSES SCREEN (Requirement 10)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAddressesScreen(viewModel: KhushbooViewModel) {
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.PROFILE)
    }
    val addresses by viewModel.addresses.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<AddressEntity?>(null) }

    var label by remember { mutableStateOf("Home") }
    var fullAddress by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Purulia") }
    var state by remember { mutableStateOf("West Bengal") }
    var pincode by remember { mutableStateOf("723101") }
    var isDefault by remember { mutableStateOf(false) }

    fun openDialog(address: AddressEntity? = null) {
        editingAddress = address
        if (address != null) {
            label = address.label
            fullAddress = address.fullAddress
            locality = address.locality
            city = address.city
            state = address.state
            pincode = address.postalCode
            isDefault = address.isDefault
        } else {
            label = "Home"
            fullAddress = ""
            locality = ""
            city = "Purulia"
            state = "West Bengal"
            pincode = "723101"
            isDefault = addresses.isEmpty()
        }
        showAddEditDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.PROFILE) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
                }
                Text(
                    text = "My Addresses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY
                )
            }
            Button(
                onClick = { openDialog(null) },
                colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DarkText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (addresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOff, contentDescription = null, tint = TEXT_MUTED, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No saved delivery addresses yet", color = TEXT_PRIMARY, fontWeight = FontWeight.Bold)
                    Text("Add an address for fast checkout & accurate delivery fee", color = TEXT_SECONDARY, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { openDialog(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Text("Add Delivery Address", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(addresses) { addr ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                        border = BorderStroke(1.5.dp, if (addr.isDefault) BRIGHT_GOLD else BORDER),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (addr.isDefault) GOLD_CONTAINER else SECTION_BACKGROUND,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (addr.isDefault) BRIGHT_GOLD else BORDER)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                when (addr.label.lowercase()) {
                                                    "work" -> Icons.Default.Work
                                                    "other" -> Icons.Default.Place
                                                    else -> Icons.Default.Home
                                                },
                                                contentDescription = null,
                                                tint = BRIGHT_GOLD,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = addr.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BRIGHT_GOLD
                                            )
                                        }
                                    }

                                    if (addr.isDefault) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = PRIMARY_GOLD,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Default",
                                                color = DarkText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { openDialog(addr) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TEXT_SECONDARY, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteAddress(addr.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ERROR_RED, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = addr.fullAddress,
                                color = TEXT_PRIMARY,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            if (addr.locality.isNotBlank() || addr.city.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${addr.locality}, ${addr.city}, ${addr.state} - ${addr.postalCode}",
                                    color = TEXT_SECONDARY,
                                    fontSize = 12.sp
                                )
                            }

                            if (!addr.isDefault) {
                                Spacer(modifier = Modifier.height(10.dp))
                                TextButton(
                                    onClick = { viewModel.setDefaultAddress(addr.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Set as Default Address", color = BRIGHT_GOLD, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        val labelOptions = listOf("Home", "Work", "Other")

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            containerColor = CARD_BACKGROUND,
            title = {
                Text(
                    text = if (editingAddress == null) "Add New Address" else "Edit Address",
                    color = TEXT_PRIMARY,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Address Label", fontSize = 12.sp, color = TEXT_SECONDARY, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        labelOptions.forEach { opt ->
                            FilterChip(
                                selected = label == opt,
                                onClick = { label = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PRIMARY_GOLD,
                                    selectedLabelColor = DarkText,
                                    containerColor = SECTION_BACKGROUND,
                                    labelColor = TEXT_PRIMARY
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = label == opt,
                                    borderColor = BORDER,
                                    selectedBorderColor = BRIGHT_GOLD
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        label = { Text("Flat / House / Street Address") },
                        placeholder = { Text("e.g. House 42, Main Market Road") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BRIGHT_GOLD,
                            unfocusedBorderColor = BORDER,
                            focusedTextColor = TEXT_PRIMARY,
                            unfocusedTextColor = TEXT_PRIMARY
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text("Locality / Landmark") },
                        placeholder = { Text("e.g. Near Big Temple") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BRIGHT_GOLD,
                            unfocusedBorderColor = BORDER,
                            focusedTextColor = TEXT_PRIMARY,
                            unfocusedTextColor = TEXT_PRIMARY
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BRIGHT_GOLD,
                                unfocusedBorderColor = BORDER,
                                focusedTextColor = TEXT_PRIMARY,
                                unfocusedTextColor = TEXT_PRIMARY
                            )
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) pincode = it
                            },
                            label = { Text("PIN Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BRIGHT_GOLD,
                                unfocusedBorderColor = BORDER,
                                focusedTextColor = TEXT_PRIMARY,
                                unfocusedTextColor = TEXT_PRIMARY
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors = CheckboxDefaults.colors(checkedColor = PRIMARY_GOLD, checkmarkColor = DarkText)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set as default delivery address", fontSize = 13.sp, color = TEXT_PRIMARY)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullAddress.isNotBlank()) {
                            val current = editingAddress
                            if (current != null) {
                                viewModel.updateAddress(
                                    current.copy(
                                        label = label,
                                        fullAddress = fullAddress,
                                        locality = locality,
                                        city = city,
                                        state = state,
                                        postalCode = pincode,
                                        isDefault = isDefault
                                    )
                                )
                            } else {
                                viewModel.addCustomerAddress(
                                    label = label,
                                    fullAddress = fullAddress,
                                    locality = locality,
                                    city = city,
                                    state = state,
                                    postalCode = pincode,
                                    isDefault = isDefault
                                )
                            }
                            showAddEditDialog = false
                        }
                    },
                    enabled = fullAddress.isNotBlank() && pincode.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Save Address", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            }
        )
    }
}

// ==========================================
// PROFILE SCREEN (Requirements 6, 8, 9, 12)
// ==========================================
@Composable
fun CustomerProfileScreen(viewModel: KhushbooViewModel) {
    BackHandler {
        viewModel.navigateCustomerTo(CustomerScreen.HOME)
    }
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    var showPhotoChoiceDialog by remember { mutableStateOf(false) }
    var showCameraExplanationDialog by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoUriString by remember(profile) { mutableStateOf(profile?.profilePhoto ?: "") }

    // Camera capture launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            viewModel.updateProfilePhoto("custom_captured_avatar")
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setCameraPermissionGranted(isGranted)
        if (isGranted) {
            takePhotoLauncher.launch(null)
        }
    }

    // Photo Picker launcher (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUriString = uri.toString()
            viewModel.updateProfilePhoto(uri.toString())
        }
    }

    // Phone Change state
    val isChangingMobile by viewModel.isChangingMobileDialog.collectAsState()
    val pendingMobile by viewModel.pendingNewMobile.collectAsState()
    val pendingOtp by viewModel.pendingMobileOtp.collectAsState()
    val pendingError by viewModel.pendingMobileError.collectAsState()
    val pendingCooldown by viewModel.pendingMobileCooldownSeconds.collectAsState()
    var enteredOtpCode by remember(pendingOtp) { mutableStateOf(pendingOtp) }
    var newPhoneInput by remember { mutableStateOf("") }
    var showPhoneInputDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateCustomerTo(CustomerScreen.MORE_MENU) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
                }
                Text("My Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
            }
            TextButton(onClick = { isEditing = !isEditing }) {
                Text(if (isEditing) "Done" else "Edit Profile", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Avatar with Camera badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(GOLD_CONTAINER)
                    .border(2.5.dp, BRIGHT_GOLD, CircleShape)
                    .clickable { showPhotoChoiceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_uploaded_logo),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(PRIMARY_GOLD)
                    .border(1.5.dp, DarkText, CircleShape)
                    .clickable { showPhotoChoiceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = DarkText, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (profile?.name.isNullOrBlank()) "Khushboo Customer" else profile!!.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TEXT_PRIMARY
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Mobile Number + Verified Badge (Requirement 9)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (profile?.mobileNumber.isNullOrBlank()) "No phone linked" else "+91 ${profile!!.mobileNumber}",
                color = TEXT_SECONDARY,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            if (profile?.mobileVerified == true) {
                Surface(
                    color = SUCCESS_GREEN.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SUCCESS_GREEN)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SUCCESS_GREEN, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Verified", color = SUCCESS_GREEN, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Personal Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TEXT_PRIMARY)
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.updateCustomerProfile(name, email)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Name", color = TEXT_MUTED, fontSize = 13.sp)
                        Text(if (profile?.name.isNullOrBlank()) "Not set" else profile!!.name, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BORDER)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email", color = TEXT_MUTED, fontSize = 13.sp)
                        Text(if (profile?.email.isNullOrBlank()) "Not set" else profile!!.email, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BORDER)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mobile Number", color = TEXT_MUTED, fontSize = 13.sp)
                            Text("+91 ${profile?.mobileNumber ?: ""}", color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                        }
                        TextButton(onClick = { showPhoneInputDialog = true }) {
                            Text("Change Phone", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation to Addresses & Orders
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("My Saved Addresses", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
                    supportingContent = { Text(profile?.selectedAddress ?: "Manage your home & work delivery locations", fontSize = 12.sp, color = TEXT_SECONDARY) },
                    leadingContent = {
                        Box(modifier = Modifier.size(36.dp).background(GOLD_CONTAINER, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BRIGHT_GOLD)
                        }
                    },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BRIGHT_GOLD) },
                    modifier = Modifier.clickable { viewModel.navigateCustomerTo(CustomerScreen.ADDRESSES) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(color = BORDER)
                ListItem(
                    headlineContent = { Text("My Orders", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
                    supportingContent = { Text("View past orders, receipts & live order status", fontSize = 12.sp, color = TEXT_SECONDARY) },
                    leadingContent = {
                        Box(modifier = Modifier.size(36.dp).background(GOLD_CONTAINER, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BRIGHT_GOLD)
                        }
                    },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BRIGHT_GOLD) },
                    modifier = Modifier.clickable { viewModel.navigateCustomerTo(CustomerScreen.MY_ORDERS) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permissions Status Card (Requirements 8 & 12)
        val locPerm by viewModel.locationPermissionState.collectAsState()
        val notifPerm by viewModel.notificationPermissionState.collectAsState()
        val camPerm by viewModel.cameraPermissionState.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
            border = BorderStroke(1.dp, BORDER),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("App Permissions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TEXT_PRIMARY)
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("System Settings", color = BRIGHT_GOLD, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                PermissionStatusRow(
                    icon = Icons.Default.LocationOn,
                    title = "Location Access",
                    desc = "Used to detect nearby food stores",
                    isGranted = locPerm == PermissionStatus.GRANTED
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BORDER)
                PermissionStatusRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    desc = "Used for real-time delivery status",
                    isGranted = notifPerm == PermissionStatus.GRANTED
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BORDER)
                PermissionStatusRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera",
                    desc = "Used only when taking profile photo",
                    isGranted = camPerm == PermissionStatus.GRANTED
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log Out Button
        OutlinedButton(
            onClick = { showLogoutConfirm = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, ERROR_RED),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ERROR_RED)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = ERROR_RED)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Photo Option Dialog (Requirement 6)
    if (showPhotoChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoChoiceDialog = false },
            containerColor = CARD_BACKGROUND,
            title = { Text("Change Profile Photo", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
            text = {
                Column {
                    Text("Select how you would like to update your profile photo:", fontSize = 13.sp, color = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoChoiceDialog = false
                                showCameraExplanationDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BRIGHT_GOLD)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Take Photo", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("Open camera to snap a new picture", fontSize = 11.sp, color = TEXT_SECONDARY)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoChoiceDialog = false
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        colors = CardDefaults.cardColors(containerColor = SECTION_BACKGROUND),
                        border = BorderStroke(1.dp, BORDER)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = BRIGHT_GOLD)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Choose from Gallery", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                                Text("Select an image from device gallery", fontSize = 11.sp, color = TEXT_SECONDARY)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoChoiceDialog = false }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            }
        )
    }

    // Camera Explanation Dialog before requesting permission (Requirement 6)
    if (showCameraExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showCameraExplanationDialog = false },
            containerColor = CARD_BACKGROUND,
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(36.dp)) },
            title = { Text("Camera Access Needed", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
            text = {
                Text(
                    "Camera access is needed to take your profile photo.",
                    color = TEXT_SECONDARY,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraExplanationDialog = false
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            takePhotoLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Continue to Camera", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraExplanationDialog = false }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            }
        )
    }

    // Change Mobile Input Dialog (Requirement 9)
    if (showPhoneInputDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneInputDialog = false },
            containerColor = CARD_BACKGROUND,
            title = { Text("Change Mobile Number", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
            text = {
                Column {
                    Text("Enter your new 10-digit Indian mobile number. An OTP code will be sent for verification.", fontSize = 13.sp, color = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = newPhoneInput,
                        onValueChange = {
                            if (it.length <= 10 && it.all { c -> c.isDigit() }) newPhoneInput = it
                        },
                        prefix = { Text("+91 ", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold) },
                        label = { Text("New Mobile Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestMobileChange(newPhoneInput)
                        showPhoneInputDialog = false
                    },
                    enabled = newPhoneInput.length == 10 && newPhoneInput.matches(Regex("^[6-9]\\d{9}$")),
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Send OTP", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneInputDialog = false }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            }
        )
    }

    // OTP Verification Dialog for Phone Change (Requirement 9)
    if (isChangingMobile) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMobileChange() },
            containerColor = CARD_BACKGROUND,
            title = { Text("Verify New Mobile Number", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
            text = {
                Column {
                    Text("Enter the 6-digit OTP sent to +91 $pendingMobile", fontSize = 13.sp, color = TEXT_SECONDARY)
                    Spacer(modifier = Modifier.height(12.dp))

                    // SMS verification banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GOLD_CONTAINER),
                        border = BorderStroke(1.dp, BORDER_GOLD)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = BRIGHT_GOLD)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SMS Code: $pendingOtp", fontWeight = FontWeight.Bold, color = BRIGHT_GOLD)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = enteredOtpCode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) enteredOtpCode = it
                        },
                        label = { Text("6-Digit OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pendingError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pendingError!!, color = ERROR_RED, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Didn't receive code?", fontSize = 11.sp, color = TEXT_MUTED)
                        if (pendingCooldown > 0) {
                            Text("Resend (${pendingCooldown}s)", color = TEXT_MUTED, fontSize = 12.sp)
                        } else {
                            TextButton(onClick = { viewModel.resendMobileChangeOtp() }) {
                                Text("Resend OTP", color = BRIGHT_GOLD, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.verifyMobileChange(enteredOtpCode) },
                    enabled = enteredOtpCode.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Verify & Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissMobileChange() }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = CARD_BACKGROUND,
            title = { Text("Log Out?", fontWeight = FontWeight.Bold, color = TEXT_PRIMARY) },
            text = { Text("Are you sure you want to log out of Khushboo Food? You will need to verify your phone on next login.", color = TEXT_SECONDARY) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ERROR_RED, contentColor = Color.White)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = TEXT_SECONDARY)
                }
            }
        )
    }
}

@Composable
fun PermissionStatusRow(
    icon: ImageVector,
    title: String,
    desc: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                Text(desc, fontSize = 11.sp, color = TEXT_SECONDARY)
            }
        }
        Surface(
            color = if (isGranted) SUCCESS_GREEN.copy(alpha = 0.15f) else TEXT_MUTED.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, if (isGranted) SUCCESS_GREEN else BORDER)
        ) {
            Text(
                text = if (isGranted) "Granted" else "Not Enabled",
                color = if (isGranted) SUCCESS_GREEN else TEXT_MUTED,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
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
                            if (product.hasValidPrice) {
                                val price = product.price!!
                                val variant = if (product.unit.isNotBlank() && !product.unit.equals("NOT PROVIDED", ignoreCase = true)) "1 ${product.unit}" else "Standard"
                                viewModel.addToCart(product, variant, price, 1)
                            } else {
                                viewModel.selectProduct(product.id)
                            }
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
    val clean = productName.trim().lowercase(java.util.Locale.ROOT)
    return when {
        clean == "special kulhad lassi" || clean.contains("lassi") -> R.drawable.img_prod_kulhad_lassi_1791068738451
        clean == "fruit chat" || clean.contains("fruit chat") -> R.drawable.img_prod_fruit_chat_1791068751675
        clean == "gulab jamun" -> R.drawable.img_prod_gulab_jamun_1791068764302
        clean == "sohan papdi" -> R.drawable.img_prod_sohan_papdi_1791068775391
        clean == "pista barfi" -> R.drawable.img_prod_pista_barfi_1791068786980
        clean == "kaju katli" -> R.drawable.img_prod_kaju_katli_1791068799420
        clean == "bundi laddu" -> R.drawable.img_prod_bundi_laddu_1791068813467
        clean == "besan laddu" -> R.drawable.img_prod_besan_laddu_1791068826192
        clean == "milk cake" -> R.drawable.img_prod_milk_cake_1791068837979
        clean == "doda burfi" -> R.drawable.img_prod_doda_burfi_1791068849177
        clean == "ilaichi barfi" -> R.drawable.img_prod_ilaichi_barfi_1791068860606
        clean == "chum chum" -> R.drawable.img_prod_chum_chum_1791068873447
        clean == "bengali rasgulla" -> R.drawable.img_prod_bengali_rasgulla_1791068887214
        clean == "sponge rasgulla" -> R.drawable.img_prod_sponge_rasgulla_1791068901167
        clean == "delicious cake" -> R.drawable.img_prod_delicious_cake_1791068914099
        clean == "pastry" -> R.drawable.img_prod_pastry_1791068925551
        clean == "pizza" -> R.drawable.img_prod_pizza_1791068937979
        clean == "burger" -> R.drawable.img_prod_burger_1791068950542
        else -> R.drawable.img_prod_gulab_jamun_1791068764302
    }
}

fun getCategoryImageRes(categoryName: String): Int {
    val clean = categoryName.trim().lowercase(java.util.Locale.ROOT)
    return when {
        clean.contains("sweet") -> R.drawable.hero_sweets_1791067917652
        clean.contains("drink") || clean.contains("lassi") -> R.drawable.img_prod_kulhad_lassi_1791068738451
        clean.contains("chaat") || clean.contains("snack") -> R.drawable.hero_street_food_1791067932481
        clean.contains("cake") || clean.contains("pastr") -> R.drawable.img_prod_delicious_cake_1791068914099
        clean.contains("pizza") -> R.drawable.img_prod_pizza_1791068937979
        clean.contains("burger") -> R.drawable.img_prod_burger_1791068950542
        clean.contains("bengali") -> R.drawable.hero_bengali_1791067952360
        clean.contains("dessert") -> R.drawable.hero_desserts_1791067982877
        clean.contains("fast") -> R.drawable.img_prod_pizza_burger_1790683979853
        clean.contains("traditional") -> R.drawable.hero_signature_1791067902606
        else -> R.drawable.hero_sweets_1791067917652
    }
}

fun filterProductsForCategory(
    products: List<ProductEntity>,
    categoryName: String,
    categoryId: Long?
): List<ProductEntity> {
    if (categoryName.equals("All", ignoreCase = true) || (categoryId == 0L)) {
        return products
    }

    return products.filter { p ->
        when (categoryId) {
            1L -> p.categoryId == 1L || p.category.equals("Indian Sweets", ignoreCase = true)
            2L -> p.categoryId == 2L || p.category.equals("Drinks", ignoreCase = true)
            3L -> p.categoryId == 3L || p.category.equals("Chaat / Snacks", ignoreCase = true)
            4L -> p.categoryId == 4L || p.category.equals("Cakes & Pastries", ignoreCase = true)
            5L -> p.categoryId == 5L || p.category.equals("Pizza", ignoreCase = true)
            6L -> p.categoryId == 6L || p.category.equals("Burgers", ignoreCase = true)
            7L -> p.name in listOf("Bengali Rasgulla", "Sponge Rasgulla", "Chum Chum") || p.category.contains("Bengali", ignoreCase = true)
            8L -> p.name in listOf("Gulab Jamun", "Bengali Rasgulla", "Sponge Rasgulla", "Chum Chum", "Delicious Cake", "Pastry") || p.category.contains("Dessert", ignoreCase = true)
            9L -> p.categoryId in listOf(5L, 6L) || p.name in listOf("Pizza", "Burger") || p.category.contains("Fast Food", ignoreCase = true)
            10L -> p.name in listOf("Gulab Jamun", "Sohan Papdi", "Special Kulhad Lassi", "Besan Laddu", "Bundi Laddu") || p.category.contains("Traditional", ignoreCase = true)
            else -> {
                if (categoryId != null && p.categoryId == categoryId) return@filter true
                val cName = categoryName.trim().lowercase(java.util.Locale.ROOT)
                when {
                    cName.contains("sweet") -> p.categoryId == 1L || p.category.contains("Sweets", ignoreCase = true)
                    cName.contains("drink") -> p.categoryId == 2L || p.category.contains("Drinks", ignoreCase = true)
                    cName.contains("chaat") || cName.contains("snack") -> p.categoryId == 3L || p.category.contains("Chaat", ignoreCase = true)
                    cName.contains("cake") || cName.contains("pastr") -> p.categoryId == 4L || p.category.contains("Cakes", ignoreCase = true)
                    cName.contains("pizza") -> p.categoryId == 5L || p.category.contains("Pizza", ignoreCase = true)
                    cName.contains("burger") -> p.categoryId == 6L || p.category.contains("Burger", ignoreCase = true)
                    cName.contains("bengali") -> p.name in listOf("Bengali Rasgulla", "Sponge Rasgulla", "Chum Chum") || p.category.contains("Bengali", ignoreCase = true)
                    cName.contains("dessert") -> p.name in listOf("Gulab Jamun", "Bengali Rasgulla", "Sponge Rasgulla", "Chum Chum", "Delicious Cake", "Pastry") || p.category.contains("Dessert", ignoreCase = true)
                    cName.contains("fast") -> p.categoryId in listOf(5L, 6L) || p.name in listOf("Pizza", "Burger") || p.category.contains("Fast Food", ignoreCase = true)
                    cName.contains("traditional") -> p.name in listOf("Gulab Jamun", "Sohan Papdi", "Special Kulhad Lassi", "Besan Laddu", "Bundi Laddu") || p.category.contains("Traditional", ignoreCase = true)
                    else -> p.category.equals(categoryName, ignoreCase = true)
                }
            }
        }
    }
}

@Composable
fun CircularCategoryItem(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageRes = getCategoryImageRes(category.name)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(76.dp)
            .clickable(
                onClick = onClick,
                role = androidx.compose.ui.semantics.Role.Button
            )
            .testTag("category_item_${category.categoryId}")
    ) {
        // Outer Container with subtle dark/gold glow & shadow
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle premium radial gold glow
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isSelected) {
                                listOf(
                                    BRIGHT_GOLD.copy(alpha = 0.50f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    BRIGHT_GOLD.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            }
                        ),
                        shape = CircleShape
                    )
            )

            // Perfectly circular food image with thin Gold border & dark inner background
            Surface(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(
                            width = if (isSelected) 2.dp else 1.2.dp,
                            brush = if (isSelected) {
                                Brush.linearGradient(listOf(BORDER_BRIGHT_GOLD, BRIGHT_GOLD))
                            } else {
                                Brush.linearGradient(listOf(BORDER_GOLD, Color(0xFF554422)))
                            }
                        ),
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = SECTION_BACKGROUND,
                shadowElevation = 4.dp
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = category.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category name: white/light text, small but clearly readable, center aligned, max 2 lines
        Text(
            text = category.name,
            color = if (isSelected) BRIGHT_GOLD else TEXT_PRIMARY,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
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

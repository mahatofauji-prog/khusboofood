package com.example.ui.customer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.main.KhushbooViewModel
import com.example.ui.main.OnboardingStep
import com.example.ui.theme.*
import com.example.util.GeoLocationResult
import com.example.util.LocationHelper

@Composable
fun CustomerOnboardingFlow(viewModel: KhushbooViewModel) {
    val step by viewModel.onboardingStep.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
    ) {
        when (step) {
            OnboardingStep.WELCOME -> WelcomeScreen(viewModel)
            OnboardingStep.MOBILE_INPUT -> MobileInputScreen(viewModel)
            OnboardingStep.OTP_VERIFY -> OtpVerifyScreen(viewModel)
            OnboardingStep.LOCATION_SETUP, OnboardingStep.LOCATION_PERMISSION -> LocationPermissionScreen(viewModel)
            OnboardingStep.CONFIRM_LOCATION -> ConfirmLocationScreen(viewModel)
            OnboardingStep.NOTIFICATION_PERMISSION -> NotificationPermissionScreen(viewModel)
            OnboardingStep.COMPLETED -> { /* Transitioned to Home */ }
        }
    }
}

// ==========================================
// 1. WELCOME SCREEN
// ==========================================
@Composable
fun WelcomeScreen(viewModel: KhushbooViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, BRIGHT_GOLD, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_uploaded_logo),
                    contentDescription = "Khushboo Food Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            KhushbooBrandHeader(
                brandSize = BrandSize.LARGE,
                subtitle = "✨ Taste Of India"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to Khusboo Food",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Discover authentic Indian sweets, pure desi ghee snacks, and gourmet dishes from verified local stores in Purulia.",
                style = MaterialTheme.typography.bodyLarge,
                color = TEXT_SECONDARY,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OnboardingFeaturePill(icon = Icons.Default.Storefront, label = "Nearby Stores")
                OnboardingFeaturePill(icon = Icons.Default.FlashOn, label = "Fast Delivery")
                OnboardingFeaturePill(icon = Icons.Default.Verified, label = "100% Pure")
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.startOnboarding() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = DarkText)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                fontSize = 11.sp,
                color = TEXT_MUTED,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OnboardingFeaturePill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(SECTION_BACKGROUND, CircleShape)
                .border(1.dp, BORDER_GOLD, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_SECONDARY)
    }
}

// ==========================================
// 2. MOBILE NUMBER SCREEN (Requirement 1)
// ==========================================
@Composable
fun MobileInputScreen(viewModel: KhushbooViewModel) {
    var mobileText by remember { mutableStateOf("") }
    val error by viewModel.authError.collectAsState()
    val isValidIndian = mobileText.length == 10 && mobileText.matches(Regex("^[6-9]\\d{9}$"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, BRIGHT_GOLD, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_uploaded_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Khusboo Food",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter your mobile number to continue",
                fontSize = 14.sp,
                color = TEXT_SECONDARY
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = mobileText,
                onValueChange = {
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        mobileText = it
                    }
                },
                prefix = {
                    Text("+91 ", fontWeight = FontWeight.Bold, color = BRIGHT_GOLD, fontSize = 16.sp)
                },
                placeholder = { Text("10-digit mobile number", color = TEXT_MUTED) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = BRIGHT_GOLD)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CARD_BACKGROUND,
                    unfocusedContainerColor = CARD_BACKGROUND,
                    focusedBorderColor = BRIGHT_GOLD,
                    unfocusedBorderColor = BORDER,
                    focusedTextColor = TEXT_PRIMARY,
                    unfocusedTextColor = TEXT_PRIMARY
                )
            )

            if (mobileText.isNotEmpty() && mobileText.length < 10) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${10 - mobileText.length} more digits needed",
                    color = TEXT_MUTED,
                    fontSize = 12.sp
                )
            } else if (mobileText.length == 10 && !isValidIndian) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚠️ Mobile number must start with 6, 7, 8, or 9",
                    color = ERROR_RED,
                    fontSize = 12.sp
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ $error",
                    color = ERROR_RED,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.sendOtp(mobileText) },
                enabled = isValidIndian,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText,
                    disabledContainerColor = CARD_BACKGROUND,
                    disabledContentColor = TEXT_MUTED
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isValidIndian) DarkText else TEXT_MUTED
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = if (isValidIndian) DarkText else TEXT_MUTED
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 3. OTP VERIFICATION SCREEN (Requirement 2)
// ==========================================
@Composable
fun OtpVerifyScreen(viewModel: KhushbooViewModel) {
    val mobile by viewModel.inputMobile.collectAsState()
    val generatedOtp by viewModel.generatedOtp.collectAsState()
    val cooldown by viewModel.resendCooldownSeconds.collectAsState()
    var otpText by remember { mutableStateOf(generatedOtp) }
    val error by viewModel.authError.collectAsState()

    val formattedMobile = if (mobile.length == 10) {
        "+91 ${mobile.take(5)} ${mobile.drop(5)}"
    } else {
        "+91 $mobile"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = { viewModel.startOnboarding() },
                modifier = Modifier
                    .size(40.dp)
                    .background(CARD_BACKGROUND, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BRIGHT_GOLD)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Verify your mobile number",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "OTP sent to $formattedMobile",
                fontSize = 14.sp,
                color = TEXT_SECONDARY
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Simulated OTP Alert Banner for seamless testing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GOLD_CONTAINER),
                border = BorderStroke(1.dp, BORDER_GOLD)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, tint = BRIGHT_GOLD)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("SMS Verification Code:", fontSize = 11.sp, color = TEXT_SECONDARY)
                        Text(generatedOtp, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BRIGHT_GOLD, letterSpacing = 2.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = otpText,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otpText = it
                    }
                },
                placeholder = { Text("Enter 6-digit OTP", color = TEXT_MUTED) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = BRIGHT_GOLD)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CARD_BACKGROUND,
                    unfocusedContainerColor = CARD_BACKGROUND,
                    focusedBorderColor = BRIGHT_GOLD,
                    unfocusedBorderColor = BORDER,
                    focusedTextColor = TEXT_PRIMARY,
                    unfocusedTextColor = TEXT_PRIMARY
                )
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ $error",
                    color = ERROR_RED,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Didn't receive code?", fontSize = 12.sp, color = TEXT_SECONDARY)
                if (cooldown > 0) {
                    Text(
                        text = "Resend OTP (${cooldown}s)",
                        color = TEXT_MUTED,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    TextButton(onClick = { viewModel.sendOtp(mobile) }) {
                        Text("Resend OTP", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.verifyOtp(otpText) },
                enabled = otpText.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText,
                    disabledContainerColor = CARD_BACKGROUND,
                    disabledContentColor = TEXT_MUTED
                )
            ) {
                Text(
                    text = "Verify",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (otpText.length == 6) DarkText else TEXT_MUTED
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (otpText.length == 6) DarkText else TEXT_MUTED
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 4. LOCATION SETUP SCREEN (Requirement 3)
// ==========================================
@Composable
fun LocationPermissionScreen(viewModel: KhushbooViewModel) {
    val context = LocalContext.current
    var showManualPicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectGpsLocation(context)
        } else {
            viewModel.setLocationPermissionGranted(false)
            showManualPicker = true
        }
    }

    if (showManualPicker) {
        ManualLocationPickerSheet(
            onLocationSelected = { loc ->
                viewModel.setManualLocation(loc)
            },
            onCustomLocationConfirmed = { area, city, pincode ->
                viewModel.setCustomManualLocation(area, city, pincode)
            },
            onDismiss = { showManualPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(GOLD_CONTAINER, CircleShape)
                    .border(2.dp, BRIGHT_GOLD, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BRIGHT_GOLD,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Set your delivery location",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Allow location access so we can show food and stores that deliver to you.",
                fontSize = 14.sp,
                color = TEXT_SECONDARY,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Stores delivering directly to your area", fontSize = 13.sp, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Accurate delivery time estimates", fontSize = 13.sp, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Distance-based low delivery fee", fontSize = 13.sp, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted || coarseGranted) {
                        viewModel.detectGpsLocation(context)
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText
                )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = DarkText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use Current Location", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showManualPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BORDER_GOLD),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BRIGHT_GOLD)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = BRIGHT_GOLD)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enter Location Manually", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 5. CONFIRM LOCATION SCREEN (Requirement 3)
// ==========================================
@Composable
fun ConfirmLocationScreen(viewModel: KhushbooViewModel) {
    val result by viewModel.detectedLocationResult.collectAsState()
    var showManualPicker by remember { mutableStateOf(false) }

    if (showManualPicker) {
        ManualLocationPickerSheet(
            onLocationSelected = { loc ->
                viewModel.setManualLocation(loc)
                showManualPicker = false
            },
            onCustomLocationConfirmed = { area, city, pincode ->
                viewModel.setCustomManualLocation(area, city, pincode)
                showManualPicker = false
            },
            onDismiss = { showManualPicker = false }
        )
    }

    val loc = result ?: GeoLocationResult(
        latitude = LocationHelper.DEFAULT_LAT,
        longitude = LocationHelper.DEFAULT_LNG,
        formattedAddress = LocationHelper.DEFAULT_ADDRESS,
        locality = LocationHelper.DEFAULT_LOCALITY,
        city = LocationHelper.DEFAULT_CITY,
        state = LocationHelper.DEFAULT_STATE,
        postalCode = LocationHelper.DEFAULT_PINCODE
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Is this your delivery location?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Please verify your delivery address to view available stores, delicacies, and delivery timings.",
                fontSize = 13.sp,
                color = TEXT_SECONDARY
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.5.dp, BORDER_GOLD),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = GOLD_CONTAINER,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BORDER_GOLD)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delivery Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BRIGHT_GOLD)
                            }
                        }

                        TextButton(onClick = { showManualPicker = true }) {
                            Text("Change Location", color = BRIGHT_GOLD, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = loc.locality,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_PRIMARY
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = loc.formattedAddress,
                        fontSize = 14.sp,
                        color = TEXT_SECONDARY,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BORDER)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("City / State", fontSize = 11.sp, color = TEXT_MUTED)
                            Text("${loc.city}, ${loc.state}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PIN Code", fontSize = 11.sp, color = TEXT_MUTED)
                            Text(loc.postalCode, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BRIGHT_GOLD)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.confirmDeliveryLocation(loc) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = DarkText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Location", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showManualPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BORDER),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TEXT_SECONDARY)
            ) {
                Text("Change Location", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 6. NOTIFICATION PERMISSION SCREEN (Requirement 5)
// ==========================================
@Composable
fun NotificationPermissionScreen(viewModel: KhushbooViewModel) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.finishNotificationStep(isGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(GOLD_CONTAINER, CircleShape)
                    .border(2.dp, BRIGHT_GOLD, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = BRIGHT_GOLD,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Stay updated",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Get notifications about your orders, delivery status and important updates.",
                fontSize = 15.sp,
                color = TEXT_SECONDARY,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                border = BorderStroke(1.dp, BORDER),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(GOLD_CONTAINER, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Moped, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Real-Time Order Updates", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                            Text("Live preparation, pickup & delivery alerts", fontSize = 11.sp, color = TEXT_SECONDARY)
                        }
                    }
                    HorizontalDivider(color = BORDER)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).background(GOLD_CONTAINER, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Special Sweets & Food Offers", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TEXT_PRIMARY)
                            Text("Exclusive discounts on festive sweets & fast food", fontSize = 11.sp, color = TEXT_SECONDARY)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            viewModel.finishNotificationStep(true)
                        } else {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        viewModel.finishNotificationStep(true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PRIMARY_GOLD,
                    contentColor = DarkText
                )
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = DarkText)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allow Notifications",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = {
                    viewModel.finishNotificationStep(false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Maybe Later",
                    fontSize = 14.sp,
                    color = TEXT_SECONDARY
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// MANUAL LOCATION PICKER SHEET (Requirement 4)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualLocationPickerSheet(
    onLocationSelected: (LocationHelper.KnownLocation) -> Unit,
    onCustomLocationConfirmed: ((area: String, city: String, pincode: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isManualInputMode by remember { mutableStateOf(false) }

    var customArea by remember { mutableStateOf("") }
    var customCity by remember { mutableStateOf("Purulia") }
    var customPincode by remember { mutableStateOf("723101") }

    val filtered = LocationHelper.POPULAR_LOCATIONS.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true) ||
                it.state.contains(searchQuery, ignoreCase = true) ||
                it.pincode.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SECTION_BACKGROUND,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BORDER_GOLD) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualInputMode) "Enter Location Details" else "Select Delivery Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TEXT_SECONDARY)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isManualInputMode) {
                Text(
                    text = "Enter your locality, city, and PIN code below:",
                    fontSize = 13.sp,
                    color = TEXT_SECONDARY
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customArea,
                    onValueChange = { customArea = it },
                    label = { Text("Area / Locality / Street") },
                    placeholder = { Text("e.g. Main Market, Near Bus Stand") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
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
                        value = customCity,
                        onValueChange = { customCity = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BRIGHT_GOLD,
                            unfocusedBorderColor = BORDER,
                            focusedTextColor = TEXT_PRIMARY,
                            unfocusedTextColor = TEXT_PRIMARY
                        )
                    )
                    OutlinedTextField(
                        value = customPincode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) customPincode = it
                        },
                        label = { Text("PIN Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BRIGHT_GOLD,
                            unfocusedBorderColor = BORDER,
                            focusedTextColor = TEXT_PRIMARY,
                            unfocusedTextColor = TEXT_PRIMARY
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onCustomLocationConfirmed?.invoke(customArea, customCity, customPincode)
                        onDismiss()
                    },
                    enabled = customArea.isNotBlank() && customPincode.length == 6,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_GOLD, contentColor = DarkText)
                ) {
                    Text("Confirm Address", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { isManualInputMode = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose from Popular Locations", color = BRIGHT_GOLD)
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by area, city or PIN code...", color = TEXT_MUTED) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BRIGHT_GOLD) },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("POPULAR SERVICE LOCATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_MUTED)
                    TextButton(onClick = { isManualInputMode = true }) {
                        Text("Type Address", color = BRIGHT_GOLD, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.forEach { loc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLocationSelected(loc)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(containerColor = CARD_BACKGROUND),
                            border = BorderStroke(1.dp, BORDER)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GOLD_CONTAINER, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(loc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TEXT_PRIMARY)
                                    Text("${loc.city}, ${loc.state} - ${loc.pincode}", fontSize = 12.sp, color = TEXT_SECONDARY)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// LOCATION SELECTION DIALOG FOR HOME SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionDialog(
    viewModel: KhushbooViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val addresses by viewModel.addresses.collectAsState()
    val profile by viewModel.profile.collectAsState()
    var showManualPicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectGpsLocation(context)
            val result = viewModel.detectedLocationResult.value
            if (result != null) {
                viewModel.confirmDeliveryLocation(result)
            }
        } else {
            showManualPicker = true
        }
    }

    if (showManualPicker) {
        ManualLocationPickerSheet(
            onLocationSelected = { loc ->
                viewModel.setManualLocation(loc)
                val res = viewModel.detectedLocationResult.value
                if (res != null) {
                    viewModel.confirmDeliveryLocation(res)
                }
                showManualPicker = false
                onDismiss()
            },
            onDismiss = { showManualPicker = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SECTION_BACKGROUND,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BORDER_GOLD) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Delivery Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TEXT_SECONDARY)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Use GPS Location Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (fineGranted || coarseGranted) {
                            viewModel.detectGpsLocation(context)
                            val res = viewModel.detectedLocationResult.value
                            if (res != null) {
                                viewModel.confirmDeliveryLocation(res)
                            }
                            onDismiss()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = GOLD_CONTAINER),
                border = BorderStroke(1.dp, BORDER_GOLD)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use Current GPS Location", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BRIGHT_GOLD)
                        Text("Auto-detect using device GPS", fontSize = 12.sp, color = TEXT_SECONDARY)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BRIGHT_GOLD)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search or Manual Selection Button
            OutlinedButton(
                onClick = { showManualPicker = true },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BORDER),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TEXT_PRIMARY)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = BRIGHT_GOLD)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search by Area, City or PIN Code")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("SAVED ADDRESSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TEXT_MUTED)

            Spacer(modifier = Modifier.height(8.dp))

            if (addresses.isEmpty()) {
                Text("No saved addresses yet.", fontSize = 12.sp, color = TEXT_SECONDARY, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                addresses.forEach { addr ->
                    val isCurrent = (profile?.selectedAddress == addr.fullAddress) || addr.isDefault
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectDeliveryAddress(addr)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(containerColor = if (isCurrent) GOLD_CONTAINER else CARD_BACKGROUND),
                        border = BorderStroke(1.dp, if (isCurrent) BRIGHT_GOLD else BORDER)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (addr.title.equals("Work", ignoreCase = true)) Icons.Default.Work else Icons.Default.Home,
                                contentDescription = null,
                                tint = if (isCurrent) BRIGHT_GOLD else TEXT_SECONDARY
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(addr.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TEXT_PRIMARY)
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = PRIMARY_GOLD, shape = RoundedCornerShape(4.dp)) {
                                            Text("Delivering Here", color = DarkText, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(addr.fullAddress, fontSize = 12.sp, color = TEXT_SECONDARY, maxLines = 2)
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BRIGHT_GOLD, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

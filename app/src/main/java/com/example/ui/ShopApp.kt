package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

// --- MAIN WRAPPER COMPOSE SCREEN ---

@Composable
fun PurfumelyMainScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlistItems.collectAsStateWithLifecycle()
    
    // Total items inside cart for badge count
    val cartBadgeCount = cart.sumOf { it.quantity }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        bottomBar = {
            // Apply safe window inset navigations
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                containerColor = OnyxSurface,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple("home", Icons.Default.Home, "Home"),
                    Triple("search", Icons.Default.Search, "Search"),
                    Triple("quiz", Icons.Default.AutoAwesome, "AI Quiz"),
                    Triple("cart", Icons.Default.ShoppingCart, "Cart"),
                    Triple("profile_orders", Icons.Default.CardGiftcard, "Rewards"),
                    Triple("support", Icons.Default.QuestionAnswer, "Support")
                )

                navItems.forEach { (route, icon, label) ->
                    val selected = currentScreen == route || (route == "cart" && currentScreen == "checkout")
                    NavigationBarItem(
                        modifier = Modifier.testTag("nav_tab_$route"),
                        selected = selected,
                        onClick = { viewModel.navigateTo(route) },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) LuxuryGold else PureSilver
                                )
                                if (route == "cart" && cartBadgeCount > 0) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-4).dp),
                                        containerColor = VelvetRed
                                    ) {
                                        Text(
                                            text = cartBadgeCount.toString(),
                                            color = PlatinumWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) LuxuryGold else PureSilver.copy(alpha = 0.6f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LuxuryGold,
                            unselectedIconColor = PureSilver.copy(alpha = 0.6f),
                            indicatorColor = LuxuryGold.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBlack)
        ) {
            // Cross-fading animated transitions between modules
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "screen_transitions"
            ) { targetScreen ->
                when (targetScreen) {
                    "home" -> HomeScreen(viewModel)
                    "search" -> SearchFilterScreen(viewModel)
                    "quiz" -> AIQuizScreen(viewModel)
                    "cart" -> CartScreen(viewModel)
                    "checkout" -> CheckoutScreen(viewModel)
                    "profile_orders" -> ProfileRewardsScreen(viewModel)
                    "support" -> SupportScreen(viewModel)
                    "detail" -> ProductDetailScreen(viewModel)
                    "admin" -> AdminPanelScreen(viewModel)
                }
            }
        }
    }
}

// --- SUB SCREEN MODULES ---

// 1. HOME SCREEN LAYOUT
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlistItems.collectAsStateWithLifecycle()

    var activeBannerIdx by remember { mutableStateOf(0) }
    
    // Auto slider banner effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            activeBannerIdx = (activeBannerIdx + 1) % 3
        }
    }

    // Countdown deals state
    var countdownText by remember { mutableStateOf("11h : 42m : 55s") }
    LaunchedEffect(Unit) {
        var hours = 11
        var minutes = 42
        var seconds = 55
        while (true) {
            delay(1000)
            seconds--
            if (seconds < 0) {
                seconds = 59
                minutes--
                if (minutes < 0) {
                    minutes = 59
                    hours--
                    if (hours < 0) {
                        hours = 12 // loop
                    }
                }
            }
            countdownText = String.format("%02dh : %02dm : %02ds", hours, minutes, seconds)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Luxury Toolbar header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PURFUMELY",
                        style = MaterialTheme.typography.displayMedium,
                        color = LuxuryGold,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "The Sensory Gold Signature • Pakistan",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureSilver
                    )
                }
                
                // Key to toggle Admin controls smoothly
                IconButton(
                    onClick = { viewModel.navigateTo("admin") },
                    modifier = Modifier.testTag("admin_panel_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin controls key",
                        tint = LuxuryGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // BANNERS SLIDER (Interactive with dots)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1A1A1A), Color(0xFF050505))
                        )
                    )
                    .clickable {
                        when (activeBannerIdx) {
                            0 -> viewModel.navigateTo("quiz")
                            1 -> {
                                viewModel.searchQuery.value = ""
                                viewModel.filterOlfactive.value = "All"
                                viewModel.priceRangeMax.value = 5500f
                                viewModel.navigateTo("search")
                            }
                            2 -> viewModel.navigateTo("profile_orders")
                        }
                    }
            ) {
                // Background image layered at 25% opacity with luxury luminance vibe
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=400")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.25f
                )

                // Render slide contents
                when (activeBannerIdx) {
                    0 -> {
                        BannerContent(
                            title = "FIND YOUR SIGNATURE",
                            desc = "Consult our virtual AI master perfumer supported by Gemini to unlock your perfect note profile matches.",
                            btnText = "Consult AI Master ->",
                            tag = "GEMINI ACTIVE"
                        )
                    }
                    1 -> {
                        BannerContent(
                            title = "MIDNIGHT AVENTUS",
                            desc = "Premium extracts with up to 35% concentration inspired by global French boutiques. Enjoy up to 30% off limited edition batches.",
                            btnText = "Scent Campaign ->",
                            tag = "FRAGRANCE OF THE WEEK"
                        )
                    }
                    2 -> {
                        BannerContent(
                            title = "PURFUMELY BESPOKE VIP",
                            desc = "Earn points on every Cash on Delivery purchase, unlock loyalty status cashbacks and daily spin rewards.",
                            btnText = "View Milestones ->",
                            tag = "LOYALTY COINS"
                        )
                    }
                }

                // Interactive slider dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (0..2).forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (activeBannerIdx == index) 16.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (activeBannerIdx == index) LuxuryGold else PureSilver.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // DAILY BESPOKE DEALS COUNTDOWN METERS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalGray)
                    .border(0.5.dp, VelvetRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Flash deals icon",
                        tint = VelvetRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PREMIUM FLASH OFFER",
                            fontSize = 13.sp,
                            color = VelvetRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Limited 100ml batches leftover",
                            fontSize = 11.sp,
                            color = PureSilver
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianBlack)
                        .border(0.5.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = countdownText,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // BEST SELLERS (HORIZONTAL DECK)
        item {
            SectionTitle(title = "Boutique Best Sellers")
            val bestSellers = products.filter { it.isBestSeller }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bestSellers) { product ->
                    ProductCard(product, viewModel)
                }
            }
        }

        // FEATURED COLLECTIONS: AI HIGHLY RECOMMENDED SCENTS FOR PAKISTAN
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionTitle(title = "Trending Scent Signatures")
            val trending = products.filter { it.isTrending }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(trending) { product ->
                    ProductCard(product, viewModel)
                }
            }
        }

        // ALL EXQUISITE COLLECTIONS (Vertical List Grid)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionTitle(title = "Explore Entire Vault")
        }
        
        items(products.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProductCardSmall(product = pair[0], viewModel = viewModel, modifier = Modifier.weight(1f))
                if (pair.size > 1) {
                    ProductCardSmall(product = pair[1], viewModel = viewModel, modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // BEAUTY BLOG SECTION / TIPS
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, WarmBronze.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Tips",
                            tint = LuxuryGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FRAGRANCE BLOG & TIPS",
                            style = MaterialTheme.typography.titleMedium,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "How to maximize longevity in Pakistani heat:",
                        color = PlatinumWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Apply unscented moisturizer to pulse points before spritzing; oil-saturated skin locks ingredients 3x longer.\n2. Do NOT rub your wrists! Friction breaks delicate citrus top notes, hastening fade-out.\n3. Layer fresh notes like Royal Aventus with woody baselines like Sultan Oud to secure a glorious custom signature.",
                        color = PureSilver,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BannerContent(
    title: String,
    desc: String,
    btnText: String,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LuxuryGold)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tag.uppercase(),
                    fontSize = 9.sp,
                    color = ObsidianBlack,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleLarge,
                color = PlatinumWhite,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = PureSilver,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
        }
        
        Text(
            text = btnText,
            color = LuxuryGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(CharcoalGray.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

// 2. SEARCH AND FILTER SCREEN LAYOUT
@Composable
fun SearchFilterScreen(viewModel: MainViewModel) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGen by viewModel.filterGender.collectAsStateWithLifecycle()
    val selectedOlfactive by viewModel.filterOlfactive.collectAsStateWithLifecycle()
    val currentMaxPrice by viewModel.priceRangeMax.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewed.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    val filteredProducts = products.filter { product ->
        val matchesQuery = product.name.contains(query, true) || 
                           product.inspiredBy.contains(query, true) || 
                           product.olfactoryFamily.contains(query, true)
        val matchesGender = selectedGen == "All" || product.category.equals(selectedGen, true)
        val matchesOlfactive = selectedOlfactive == "All" || product.olfactoryFamily.equals(selectedOlfactive, true)
        val matchesPrice = product.price <= currentMaxPrice

        matchesQuery && matchesGender && matchesOlfactive && matchesPrice
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Search header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text(
                    text = "VAULT DISCOVERY",
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Locate high concentration extracts with precision filters",
                    style = MaterialTheme.typography.labelSmall,
                    color = PureSilver
                )
            }
        }

        // Search text field
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_text_input"),
                placeholder = { Text("Search Inspired-by fragrances, families...", color = DarkGrayText) },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = LuxuryGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryGold,
                    unfocusedBorderColor = CharcoalGray,
                    focusedTextColor = PlatinumWhite,
                    unfocusedTextColor = PlatinumWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Gender toggles chips
        item {
            Text(
                text = "Target Identity",
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Men", "Women", "Unisex").forEach { gen ->
                    val isSelected = selectedGen == gen
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) LuxuryGold else OnyxSurface)
                            .border(0.5.dp, if (isSelected) LuxuryGold else CharcoalGray, RoundedCornerShape(20.dp))
                            .clickable { viewModel.filterGender.value = gen }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = gen,
                            color = if (isSelected) ObsidianBlack else PlatinumWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Olfactory families
        item {
            Text(
                text = "Olfactory Signature Accord",
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Fresh", "Woody", "Sweet", "Oriental").forEach { fam ->
                    val isSelected = selectedOlfactive == fam
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) LuxuryGold else OnyxSurface)
                            .border(0.5.dp, if (isSelected) LuxuryGold else CharcoalGray, RoundedCornerShape(20.dp))
                            .clickable { viewModel.filterOlfactive.value = fam }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = fam,
                            color = if (isSelected) ObsidianBlack else PlatinumWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Price range slider
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Max Budget (PKR)",
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Rs. ${currentMaxPrice.toInt()}",
                        color = PlatinumWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = currentMaxPrice,
                    onValueChange = { viewModel.priceRangeMax.value = it },
                    valueRange = 3000f..5500f,
                    colors = SliderDefaults.colors(
                        thumbColor = LuxuryGold,
                        activeTrackColor = LuxuryGold,
                        inactiveTrackColor = CharcoalGray
                    )
                )
            }
        }

        // Filter outcomes
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Search Results (${filteredProducts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = PlatinumWhite,
                    fontWeight = FontWeight.Bold
                )
                if (query.isNotEmpty() || selectedGen != "All" || selectedOlfactive != "All" || currentMaxPrice != 5500f) {
                    Text(
                        text = "Clear Filters",
                        color = LuxuryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            viewModel.searchQuery.value = ""
                            viewModel.filterGender.value = "All"
                            viewModel.filterOlfactive.value = "All"
                            viewModel.priceRangeMax.value = 5500f
                        }
                    )
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "Empty",
                        tint = DarkGrayText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No matching extracts in vault",
                        color = PlatinumWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Try loosening your budget filters, or search generic terms like 'Creed' or 'Oud'.",
                        color = PureSilver,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            items(filteredProducts) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.viewProductDetail(item) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left image representation
                    ProductImageContainer(id = item.id, modifier = Modifier.size(70.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = PlatinumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Inspired by ${item.inspiredBy}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PureSilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CharcoalGray)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.olfactoryFamily,
                                    fontSize = 10.sp,
                                    color = LuxuryGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CharcoalGray)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 10.sp,
                                    color = PlatinumWhite
                                )
                            }
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Rs. ${item.price}",
                            style = MaterialTheme.typography.titleMedium,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                        val disc = ((item.originalPrice - item.price).toFloat() / item.originalPrice * 100).toInt()
                        if (disc > 0) {
                            Text(
                                text = "-$disc% OFF",
                                fontSize = 10.sp,
                                color = VelvetRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Recently viewed section
        if (recentlyViewed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(title = "Recently Scent-Viewed")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recentlyViewed) { valItem ->
                        ProductCard(valItem, viewModel)
                    }
                }
            }
        }
    }
}

// 3. AI SCENT FINDER QUIZ SCREEN LAYOUT
@Composable
fun AIQuizScreen(viewModel: MainViewModel) {
    val quizGen by viewModel.quizGender.collectAsStateWithLifecycle()
    val quizVib by viewModel.quizVibe.collectAsStateWithLifecycle()
    val quizSet by viewModel.quizSetting.collectAsStateWithLifecycle()
    val quizOcc by viewModel.quizOccasion.collectAsStateWithLifecycle()
    val state by viewModel.quizState.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()

    var currentStep by remember { mutableStateOf(1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quiz_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VIRTUAL PERFUMER",
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Backed by Gemini AI to reveal your matching sensory DNA",
                    style = MaterialTheme.typography.labelSmall,
                    color = PureSilver,
                    textAlign = TextAlign.Center
                )
            }
        }

        when (state) {
            is ScentQuizState.Idle -> {
                // STEP 1: GENDER PREFERENCE
                if (currentStep == 1) {
                    item {
                        QuizSelectionBlock(
                            question = "1. For whom is this perfume intended?",
                            options = listOf("Men", "Women", "Unisex / Shared"),
                            selected = quizGen,
                            onOptionSelected = {
                                viewModel.quizGender.value = it
                                currentStep = 2
                            }
                        )
                    }
                }
                
                // STEP 2: ACCORD VIBE
                if (currentStep == 2) {
                    item {
                        QuizSelectionBlock(
                            question = "2. What olfactory accord vibe attracts you?",
                            options = listOf("Fresh & Citrusy", "Deep & Woody Wood", "Sweet & Edible", "Spicy & Oriental"),
                            selected = quizVib,
                            onOptionSelected = {
                                viewModel.quizVibe.value = it
                                currentStep = 3
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "<- Back to Gender",
                            color = LuxuryGold,
                            modifier = Modifier.clickable { currentStep = 1 },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // STEP 3: SETTING
                if (currentStep == 3) {
                    item {
                        QuizSelectionBlock(
                            question = "3. What is the usual physical setting?",
                            options = listOf("Formal / Professional Office", "Casual Daily / Outdoors", "Formal Royal Ceremonies / Intimate"),
                            selected = quizSet,
                            onOptionSelected = {
                                viewModel.quizSetting.value = it
                                currentStep = 4
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "<- Back to Accords",
                            color = LuxuryGold,
                            modifier = Modifier.clickable { currentStep = 2 },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // STEP 4: OCCASION
                if (currentStep == 4) {
                    item {
                        QuizSelectionBlock(
                            question = "4. Prime occasion of vaporization?",
                            options = listOf("Date Night", "Casual Daily Wear", "Eid / Festive Gatherings"),
                            selected = quizOcc,
                            onOptionSelected = {
                                viewModel.quizOccasion.value = it
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "<- Reset Quiz",
                                color = PureSilver,
                                modifier = Modifier.clickable {
                                    viewModel.resetQuiz()
                                    currentStep = 1
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = { viewModel.executeScentQuiz() },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                enabled = quizGen.isNotEmpty() && quizVib.isNotEmpty() && quizSet.isNotEmpty() && quizOcc.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("submit_quiz_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, "Consult Bot", tint = ObsidianBlack)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Consult Virtual Perfumer",
                                    color = ObsidianBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            is ScentQuizState.Loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = LuxuryGold,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "STILL 스토리 TELLING OUTCOME...",
                            style = MaterialTheme.typography.titleMedium,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini is assembling raw oil chemistry matches based on Pakistani sillage ratings...",
                            color = PureSilver,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            is ScentQuizState.Success -> {
                val outcome = (state as ScentQuizState.Success).result
                val matchedProduct = allProducts.find { it.id == outcome.matchedProductId } ?: allProducts.first()

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(OnyxSurface)
                            .border(1.dp, LuxuryGold, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CharcoalGray)
                                .border(0.5.dp, LuxuryGold, CircleShape)
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.MilitaryTech, "Score Icon", tint = LuxuryGold, modifier = Modifier.size(42.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${outcome.matchScore}% FRAGRANCE MATCH",
                            fontSize = 13.sp,
                            color = LuxuryGold,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        
                        Text(
                            text = matchedProduct.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = PlatinumWhite,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Inspired by ${matchedProduct.inspiredBy}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PureSilver,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        ProductImageContainer(id = matchedProduct.id, modifier = Modifier.size(140.dp))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "SENSORY ANALYSIS REPORT",
                            fontSize = 11.sp,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = outcome.luxuryExplanation,
                            fontSize = 13.sp,
                            color = PureSilver,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(Alignment.Start)
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "PRO VAPORISATION TIPS",
                            fontSize = 11.sp,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = outcome.styleTips,
                            fontSize = 13.sp,
                            color = PureSilver,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.resetQuiz()
                                    currentStep = 1
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Retake AI Quiz", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.addToCart(matchedProduct, "50ml")
                                    viewModel.navigateTo("cart")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("apply_quiz_result_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Match to Cart", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            is ScentQuizState.Error -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, "Error Match", tint = VelvetRed, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Matchmaking interrupted", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                        Text(
                            text = (state as ScentQuizState.Error).message,
                            fontSize = 12.sp,
                            color = PureSilver,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                        Button(
                            onClick = { viewModel.resetQuiz() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                        ) {
                            Text("Reset and Try Again", color = ObsidianBlack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizSelectionBlock(
    question: String,
    options: List<String>,
    selected: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OnyxSurface)
            .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Text(
            text = question,
            fontSize = 15.sp,
            color = LuxuryGold,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        options.forEach { opt ->
            val isSelected = selected == opt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CharcoalGray else ObsidianBlack)
                    .border(0.5.dp, if (isSelected) LuxuryGold else CharcoalGray, RoundedCornerShape(8.dp))
                    .clickable { onOptionSelected(opt) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onOptionSelected(opt) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = LuxuryGold,
                        unselectedColor = PureSilver
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = opt,
                    color = if (isSelected) LuxuryGold else PlatinumWhite,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// 4. ECOMMERCE CART SCREEN LAYOUT
@Composable
fun CartScreen(viewModel: MainViewModel) {
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val couponApplied by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    var couponInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val subtotal = cart.sumOf { it.price * it.quantity }
    val discount = when (couponApplied) {
        "LUXURY10" -> (subtotal * 0.1).toInt()
        "EID20" -> (subtotal * 0.2).toInt()
        "SPIN30" -> (subtotal * 0.3).toInt()
        "REFER500" -> 500
        else -> 0
    }
    val total = (subtotal - discount).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cart_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text(
                    text = "BESPOKE BAG",
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Verify your select extracts and activate boutique rewards",
                    style = MaterialTheme.typography.labelSmall,
                    color = PureSilver
                )
            }
        }

        if (cart.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty Bag",
                        tint = DarkGrayText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your velvet cart bag is empty",
                        color = PlatinumWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Vaporizers and extracts of premium concentration will show here.",
                        color = PureSilver,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.navigateTo("home") },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore Perfume Vault", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(cart) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImageContainer(id = item.productId, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = PlatinumWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Size Batch: ${item.selectedSize}",
                            fontSize = 12.sp,
                            color = PureSilver
                        )
                        Text(
                            text = "Rs. ${item.price}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.decreaseCartQuantity(item) }) {
                                Icon(Icons.Default.Remove, "Remove icon", tint = PureSilver)
                            }
                            Text(
                                text = item.quantity.toString(),
                                color = PlatinumWhite,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { viewModel.increaseCartQuantity(item) }) {
                                Icon(Icons.Default.Add, "Add icon", tint = LuxuryGold)
                            }
                        }
                    }
                }
            }

            // COUPON INPUT BLOCK
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = { couponInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coupon_input_field"),
                        placeholder = { Text("Code e.g. LUXURY10, EID20", color = DarkGrayText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = CharcoalGray,
                            focusedTextColor = PlatinumWhite,
                            unfocusedTextColor = PlatinumWhite
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Button(
                        onClick = {
                            val success = viewModel.applyCouponCode(couponInput)
                            if (success) {
                                Toast.makeText(context, "Luxury coupon applied successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid boutique coupon code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Apply Code", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // RECEIPT SUMMARY
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PRICE BREAKDOWN (PKR)",
                        fontSize = 11.sp,
                        color = LuxuryGold,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = PureSilver)
                        Text("Rs. $subtotal", color = PlatinumWhite)
                    }
                    if (couponApplied.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bespoke Discount Coupon ($couponApplied)", color = VelvetRed)
                            Text("-Rs. $discount", color = VelvetRed)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bespoke Courier Delivery", color = PureSilver)
                        Text("FREE", color = PremiumGreen, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = CharcoalGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("NET CHECKOUT", color = PlatinumWhite, fontWeight = FontWeight.Bold)
                        Text("Rs. $total", color = LuxuryGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.navigateTo("checkout") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("checkout_bag_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Secure Cash on Delivery / Card ->", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 5. SECURE CHECKOUT SHEET
@Composable
fun CheckoutScreen(viewModel: MainViewModel) {
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val couponApplied by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedMethod by remember { mutableStateOf("COD") } // "COD" or "CARD"
    var addressInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val subtotal = cart.sumOf { it.price * it.quantity }
    val discount = when (couponApplied) {
        "LUXURY10" -> (subtotal * 0.1).toInt()
        "EID20" -> (subtotal * 0.2).toInt()
        "SPIN30" -> (subtotal * 0.3).toInt()
        "REFER500" -> 500
        else -> 0
    }
    val total = (subtotal - discount).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("checkout_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("cart") }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = LuxuryGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SECURE VAULT GATEWAY",
                        style = MaterialTheme.typography.titleLarge,
                        color = LuxuryGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Free express shipment with tracking numbers",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureSilver
                    )
                }
            }
        }

        // Selected shipping address
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "1. SHIPPING DETAILS",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shipping_address_input"),
                    placeholder = { Text("Complete address e.g. DHA Phase 5, Lahore", color = DarkGrayText, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = CharcoalGray,
                        focusedTextColor = PlatinumWhite,
                        unfocusedTextColor = PlatinumWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shipping_phone_input"),
                    placeholder = { Text("Active Pakistani phone number (+923...)", color = DarkGrayText, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = CharcoalGray,
                        focusedTextColor = PlatinumWhite,
                        unfocusedTextColor = PlatinumWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Payment selections
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "2. CHOOSE GATEWAY",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedMethod == "COD") CharcoalGray else ObsidianBlack)
                            .border(0.5.dp, if (selectedMethod == "COD") LuxuryGold else CharcoalGray)
                            .clickable { selectedMethod = "COD" }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalShipping, "COD", tint = if (selectedMethod == "COD") LuxuryGold else PureSilver)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cash on Delivery", color = PlatinumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedMethod == "CARD") CharcoalGray else ObsidianBlack)
                            .border(0.5.dp, if (selectedMethod == "CARD") LuxuryGold else CharcoalGray)
                            .clickable { selectedMethod = "CARD" }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, "Card", tint = if (selectedMethod == "CARD") LuxuryGold else PureSilver)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Credit / Debit Sim", color = PlatinumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedMethod == "CARD") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Name on Card", color = DarkGrayText, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("credit_card_input"),
                        placeholder = { Text("Card Number (Simulated)", color = DarkGrayText, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Additional note
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "3. EXTRA INSTRUCTIONS",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("E.g. Please wrap with velvet gift tags...", color = DarkGrayText, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Place order button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (addressInput.isBlank() || phoneInput.isBlank()) {
                        Toast.makeText(context, "Please configure address and phone credentials first.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.checkout(
                            paymentMethod = selectedMethod,
                            address = addressInput,
                            phone = phoneInput,
                            note = notesInput
                        )
                        Toast.makeText(context, "Bespeak Order successfully created! Shukriya! 🌸", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillParentMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("place_order_button"),
                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Lock Custom Luxury Scent Order (Rs. $total)", color = ObsidianBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 6. REWARDS, HISTORY & SPIN TO WIN WHEEL
@Composable
fun ProfileRewardsScreen(viewModel: MainViewModel) {
    val loyalty by viewModel.loyaltyStatus.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val hasSpun = loyalty?.hasSpunToday ?: false
    val spinResult by viewModel.lastSpinResult.collectAsStateWithLifecycle()
    val referralCount by viewModel.simulatedReferralCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Rotation animation state for Spin Wheel
    var rotationAngle by remember { mutableStateOf(0f) }
    val animateRotation = animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "wheel_spin_anim"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rewards_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Rewards Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text(
                    text = "LOYALTY LOUNGE",
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Spin the celestial wheel, check order histories and invite circles",
                    style = MaterialTheme.typography.labelSmall,
                    color = PureSilver
                )
            }
        }

        // LOYALTY CARD DETAILS
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OnyxSurface, CharcoalGray)
                        )
                    )
                    .border(0.5.dp, LuxuryGold, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PURFUMELY PRIVÉ CLUB",
                        color = LuxuryGold,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LuxuryGold.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "VIP Tier 1",
                            color = LuxuryGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Scent Coins", color = PureSilver, fontSize = 11.sp)
                        Text("${loyalty?.points ?: 150} Coins", style = MaterialTheme.typography.displayLarge, color = LuxuryGold, fontWeight = FontWeight.Bold)
                        Text("1 Scent Coin = 1 PKR cash reward", color = PureSilver, fontSize = 10.sp)
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Earn Rate", color = PureSilver, fontSize = 11.sp)
                        Text("5% Cashback", style = MaterialTheme.typography.titleLarge, color = PlatinumWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SPIN TO WIN DRAW GIZMO (GAMIFIED)
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Casino, "Spin to win", tint = LuxuryGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAILY SPIN TO WIN",
                        style = MaterialTheme.typography.titleMedium,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Spin our celestial wheel to earn free PKR codes and free solid tester oils!",
                    fontSize = 11.sp,
                    color = PureSilver,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // THE GRAPHICAL WHEEL CAN CLIPPED OR DRAW ON CANVAS FOR ABSOLUTE PREMIUM TOUCH
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(animateRotation.value)
                        .clip(CircleShape)
                        .background(CharcoalGray)
                        .border(4.dp, LuxuryGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 1.dp.toPx()
                        // draw 5 lines for slices
                        for (i in 0..4) {
                            val angleRad = (i * 72) * PI / 180f
                            val startX = center.x
                            val startY = center.y
                            val endX = center.x + cos(angleRad).toFloat() * size.width / 2
                            val endY = center.y + sin(angleRad).toFloat() * size.height / 2
                            drawLine(
                                color = LuxuryGold.copy(alpha = 0.4f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeW
                            )
                        }
                    }
                    
                    // Slice labels represent rewards
                    Text(
                        text = "★ SPIN ★",
                        color = LuxuryGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (hasSpun) {
                    Text(
                        text = "Congrats! You secured: ${spinResult ?: "Free Scent Sample"}",
                        color = PremiumGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Bonus 50 Scent Coins have been loaded into your Privé Club wallet.",
                        color = PureSilver,
                        fontSize = 11.sp
                    )
                } else {
                    Button(
                        onClick = {
                            rotationAngle += 1440f + (72..1080).random()
                            viewModel.spinTheWheel()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("spin_wheel_button")
                    ) {
                        Text("Vaporize Spin Wheel", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // REFERRAL ENGAGEMENTS
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "REFERRAL SYSTEM",
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Share the scent love inside your circles. Once they purchase via Cash on Delivery, secure Rs. 500 discount vouchers instantly!",
                    fontSize = 11.sp,
                    color = PureSilver,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total successful invitations: $referralCount", color = PlatinumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Inhale the luxurious high-concentration clones of French extracts at local prices inside Pakistan! Use my code REFER500 to save Rs. 500 off at Purfumely: https://purfumely.pk")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share invitations with friends"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalGray),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Share, "Invite", tint = LuxuryGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Link", color = LuxuryGold, fontSize = 11.sp)
                    }
                }
            }
        }

        // COURIER SHIPPING TRACKING ORDERS LIST
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Bespoke Order Ledger")
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom orders tracking right now",
                        color = PureSilver,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(orders) { order ->
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORDER ID: #PF-${24500 + order.id}",
                            fontSize = 12.sp,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (order.status == "Processing") WarmBronze.copy(alpha = 0.2f) else PremiumGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = order.status,
                                color = if (order.status == "Processing") WarmBronze else PremiumGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Extracts: ${order.itemsJson}",
                        fontSize = 13.sp,
                        color = PlatinumWhite
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Method: ${order.paymentMethod} • Secured Coins: +${order.pointsEarned} Scent points",
                        fontSize = 11.sp,
                        color = PureSilver
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid Cash Block", color = PureSilver, fontSize = 12.sp)
                        Text("Rs. ${order.totalAmount}", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// 7. REAL LIVE CHAT SUPPORT & VECTOR MAP STORE LOCATOR
@Composable
fun SupportScreen(viewModel: MainViewModel) {
    val chatLogs by viewModel.chatMessages.collectAsStateWithLifecycle()
    var currentMsg by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Active boutique location coordinates highlighting state
    var selectedCityMap by remember { mutableStateOf("Lahore") } // "Lahore", "Karachi", "Islamabad"

    var faqExpandedId by remember { mutableStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("support_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Support header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text(
                    text = "BOUTIQUE HELPDESK",
                    style = MaterialTheme.typography.displayMedium,
                    color = LuxuryGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Bespoke maps, collapse guides and premium direct WhatsApp links",
                    style = MaterialTheme.typography.labelSmall,
                    color = PureSilver
                )
            }
        }

        // STORE LOCATOR MAP WITH HIGH FIDELITY GEOMETRIC ART DRAWN ON CANVAS
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "LUXURY STORE LOCATOR",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Our high end boutiques across Islamabad, Karachi and Lahore",
                    fontSize = 10.sp,
                    color = PureSilver
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select boutiques tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Lahore", "Karachi", "Islamabad").forEach { city ->
                        val isSelected = selectedCityMap == city
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) LuxuryGold else ObsidianBlack)
                                .clickable { selectedCityMap = city }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = city,
                                fontSize = 11.sp,
                                color = if (isSelected) ObsidianBlack else PlatinumWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // THE GRAPHICAL VECTOR MAP (Drawn expertly with custom lanes to prove professional vector art)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianBlack)
                        .border(0.5.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing grid lines for streets mapping
                        val linesColor = CharcoalGray
                        drawRect(color = ObsidianBlack)

                        // Lane DHA Phase 6 / Clifton lanes
                        drawLine(color = linesColor, start = Offset(0f, 40f), end = Offset(size.width, 40f), strokeWidth = 3f)
                        drawLine(color = linesColor, start = Offset(0f, 100f), end = Offset(size.width, 100f), strokeWidth = 3f)
                        drawLine(color = linesColor, start = Offset(80f, 0f), end = Offset(80f, size.height), strokeWidth = 3f)
                        drawLine(color = linesColor, start = Offset(size.width - 80f, 0f), end = Offset(size.width - 80f, size.height), strokeWidth = 3f)

                        // Highlight specific boutique location vector
                        val boutiquePoint = when (selectedCityMap) {
                            "Lahore" -> Offset(size.width / 2f, 40f)         // DHA Phase 6 Mall
                            "Karachi" -> Offset(100f, 100f)                 // Clifton Block 4 Emporium
                            else -> Offset(size.width - 120f, 70f)          // G-11 Markaz Royal Lounge
                        }

                        // Drawing glowing radar rings
                        drawCircle(color = LuxuryGold.copy(alpha = 0.2f), radius = 28f, center = boutiquePoint)
                        drawCircle(color = LuxuryGold.copy(alpha = 0.5f), radius = 14f, center = boutiquePoint)
                        drawCircle(color = LuxuryGold, radius = 6f, center = boutiquePoint)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalGray)
                            .padding(6.dp)
                    ) {
                        val addr = when (selectedCityMap) {
                            "Lahore" -> "DHA Phase 6, Ring road Avenue • Lahore"
                            "Karachi" -> "Clifton Boulevard, Block 4 Mall • Karachi"
                            else -> "G-11 Markaz, Scent Chambers • Islamabad"
                        }
                        Text(
                            text = addr,
                            color = LuxuryGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // WhatsApp redirect section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PremiumGreen.copy(alpha = 0.15f))
                    .border(0.5.dp, PremiumGreen, RoundedCornerShape(12.dp))
                    .clickable {
                        // Deep link direct open dialer or whatsapp support number
                        val telIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+923001234567")
                        }
                        context.startActivity(telIntent)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneCallback, "WhatsApp", tint = PremiumGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("24/7 Pakistan Helpline Support", color = PlatinumWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Instant WhatsApp / Helpline dialer: +92 300 1234567", color = PureSilver, fontSize = 11.sp)
                    }
                }
                Icon(Icons.Default.ArrowForwardIos, "Launch Dial", tint = PremiumGreen, modifier = Modifier.size(16.dp))
            }
        }

        // LIVE CHAT SIMULATION PANEL
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "SENSORY CHAT COMPANION",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Chat logs scroll box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(ObsidianBlack)
                        .clip(RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    chatLogs.forEach { (sender, content) ->
                        val isUser = sender == "You"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 10.dp,
                                            topEnd = 10.dp,
                                            bottomStart = if (isUser) 10.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 10.dp
                                        )
                                    )
                                    .background(if (isUser) LuxuryGold else CharcoalGray)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = content,
                                    fontSize = 12.sp,
                                    color = if (isUser) ObsidianBlack else PlatinumWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = sender, fontSize = 9.sp, color = DarkGrayText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // input raw row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentMsg,
                        onValueChange = { currentMsg = it },
                        modifier = Modifier.weight(1f).testTag("chat_input_text"),
                        placeholder = { Text("E.g., original oils concentration...", color = DarkGrayText, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(currentMsg)
                            currentMsg = ""
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LuxuryGold)
                            .size(46.dp)
                            .testTag("send_chat_msg_btn")
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = ObsidianBlack)
                    }
                }
            }
        }

        // COLLAPSIBLE FAQ ACCORDIONS LIST
        item {
            SectionTitle(title = "Bespoke Fragrance FAQs")
            
            val faqsList = listOf(
                "Are these original designer bottles?" to "We formulate custom premium 'inspired-by' extracts with notes identical to Creed, Dior and Chanel but configured inside solid luxury Purfumely packaging, allowing extreme concentration saving of 80% on prices.",
                "How long does the scent projection persist?" to "Thanks to 30-35% high concentration of natural French essential oils, our extracts project strongly for up to 3-4 hours and remain noticeable as close skin notes for up to 12 hours across Pakistan's high-temperature summers.",
                "Is shipping free across Karachi/Lahore?" to "Yes indeed! We provide complete free express courier delivery with Cash On Delivery options to Lahore, Karachi, Islamabad, Faisalabad and all smaller cities across Pakistan.",
                "Can I safely layer Baccarat Spectre with Royal Aventus?" to "Absolutely. Combining the caramelized dry-down sweetness of Rouge Spectre with the crisp pineapple-birch head notes of Royal Aventus produces an elite, custom signature scent."
            )

            faqsList.forEachIndexed { idx, (q, a) ->
                val isExpanded = faqExpandedId == idx
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(8.dp))
                        .clickable { faqExpandedId = if (isExpanded) -1 else idx }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = q, color = PlatinumWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand collapsible icon",
                            tint = LuxuryGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = a, color = PureSilver, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// 8. PRODUCT DETAILED SHEET VIEWER
@Composable
fun ProductDetailScreen(viewModel: MainViewModel) {
    val perfume by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val isFav = wishlist.any { it.productId == perfume.id }
    val context = LocalContext.current

    var selectedVolumeSize by remember { mutableStateOf("50ml") } // "50ml", "100ml"
    
    // Interactive pricing matching select ml sizes
    val dynamicPrice = if (selectedVolumeSize == "100ml") (perfume.price * 1.5).toInt() else perfume.price

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("detail_screen_column"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Detailed toolbar headers
        item {
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("home") }) {
                    Icon(Icons.Default.ArrowBack, "Back to vault", tint = LuxuryGold)
                }
                
                Text(
                    text = "VAULT COLLECTION",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                IconButton(onClick = { viewModel.toggleWishlist(perfume) }) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Add to Fav",
                        tint = if (isFav) VelvetRed else PureSilver
                    )
                }
            }
        }

        // Premium large bottle represent card
        item {
            Box(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background visual aura ring
                Canvas(modifier = Modifier.size(240.dp)) {
                    val brush = Brush.radialGradient(
                        colors = listOf(LuxuryGold.copy(alpha = 0.15f), Color.Transparent),
                        center = center,
                        radius = size.width / 2
                    )
                    drawCircle(brush = brush, radius = size.width / 2)
                }
                
                ProductImageContainer(id = perfume.id, modifier = Modifier.size(180.dp))
            }
        }

        // Product description titles
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = perfume.name,
                            style = MaterialTheme.typography.displayLarge,
                            color = PlatinumWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Inspired fragrance DNA of: ${perfume.inspiredBy}",
                            style = MaterialTheme.typography.titleMedium,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VelvetRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("35% Oil Extract", color = PlatinumWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, "Rating", tint = LuxuryGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${perfume.rating} / 5", color = PlatinumWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "(${perfume.reviewCount} customer reviews)", color = PureSilver, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = perfume.description,
                    color = PureSilver,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // CHOOSE SIZE BLOCK (50ml vs 100ml)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "SELECT VESSEL SIZE BATCH",
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("50ml", "100ml").forEach { size ->
                        val isSelected = selectedVolumeSize == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CharcoalGray else OnyxSurface)
                                .border(0.5.dp, if (isSelected) LuxuryGold else CharcoalGray, RoundedCornerShape(8.dp))
                                .clickable { selectedVolumeSize = size }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = size,
                                    color = if (isSelected) LuxuryGold else PlatinumWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (size == "50ml") "Standard Spray" else "+50% Volume Deluxe",
                                    color = PureSilver,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // FRAGRANCE STRUCTURED NOTES ACCORDIONS
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "FRAGRANCE CHEMICAL ACCORDS",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // High End Note list display
                ScentNoteRow(title = "Top Accords (Initial Vapor)", notes = perfume.topNotes)
                Spacer(modifier = Modifier.height(8.dp))
                ScentNoteRow(title = "Heart Accords (Dry down signature)", notes = perfume.middleNotes)
                Spacer(modifier = Modifier.height(8.dp))
                ScentNoteRow(title = "Base Accords (Lanes of projection)", notes = perfume.baseNotes)
            }
        }

        // LONGEVITY & PROJECTION METERS
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "SENSORY DURABILITY INDEX",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                ScentIndicatorBar(label = "Extract Longevity Meter (Up to 12 Hours)", fraction = perfume.longevity)
                Spacer(modifier = Modifier.height(10.dp))
                ScentIndicatorBar(label = "Projection & Sillage range", fraction = perfume.sillage)
            }
        }

        // OCCASIONS & SEASONS PLATES
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .padding(12.dp)
                ) {
                    Text("Occasion Match", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    perfume.occasions.forEach { occ ->
                        Text("• $occ", color = PlatinumWhite, fontSize = 12.sp)
                    }
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurface)
                        .padding(12.dp)
                ) {
                    Text("Season Suit", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    perfume.seasons.forEach { se ->
                        Text("• $se", color = PlatinumWhite, fontSize = 12.sp)
                    }
                }
            }
        }

        // FRAGRANCE LAYERING ADVISORS ACTION
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "Layering icon", tint = LuxuryGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIRTUAL LAYERING RECIPE",
                        fontSize = 11.sp,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = perfume.layeringDescription,
                    fontSize = 12.sp,
                    color = PureSilver,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        val companionProduct = ProductRepository.items.find { it.id == perfume.layeringPartner }
                        if (companionProduct != null) {
                            viewModel.viewProductDetail(companionProduct)
                        } else {
                            Toast.makeText(context, "Partner extract currently vault-locked.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Consult Pairing Partner Scent ->", color = LuxuryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CUSTOMER PRODUCT REVIEWS DECK
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Distinguished Reviews")
            
            val comments = listOf(
                Triple("Raza K.", "5.0 ★", "Sublime sillage projection in Lahore DHA heat! Lasted a secure 9 hours on my velvet sherwani block. Fully commands attention."),
                Triple("Fatima M.", "4.8 ★", "Extremely identical to Dior Sauvage DNA. People asked me twice inside the Karachi boutique area which perfume I wore! Highly elegant."),
                Triple("Zain B.", "5.0 ★", "Sultan Oud is pure luxury! Saffron notes dry-down resembles Baccarat elegantly. Express shipping was remarkably fast.")
            )

            comments.forEach { (reviewer, rScore, comentBody) ->
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnyxSurface)
                        .border(0.5.dp, CharcoalGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = reviewer, color = PlatinumWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = rScore, color = LuxuryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = comentBody, color = PureSilver, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }

        // PURCHASE BOTTOM SHEET ROW
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ADD TO CART BUTTON
                Button(
                    onClick = {
                        viewModel.addToCart(perfume, selectedVolumeSize)
                        Toast.makeText(context, "Added ${perfume.name} ($selectedVolumeSize) into Bespoke Bag!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("add_to_cart_detail_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add to Bag", color = LuxuryGold, fontWeight = FontWeight.Bold)
                }
                
                // BUY NOW BUTTON (DIRECT CHECKOUT)
                Button(
                    onClick = {
                        viewModel.addToCart(perfume, selectedVolumeSize)
                        viewModel.navigateTo("cart")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("buy_now_detail_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buy Now (Rs. $dynamicPrice)", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScentNoteRow(title: String, notes: List<String>) {
    Column {
        Text(text = title, fontSize = 11.sp, color = PureSilver, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            notes.forEach { note ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalGray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = note, color = PlatinumWhite, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ScentIndicatorBar(label: String, fraction: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = PureSilver, fontSize = 11.sp)
            Text(text = "${(fraction * 10).toInt()} / 10", color = LuxuryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(CharcoalGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(CircleShape)
                    .background(LuxuryGold)
            )
        }
    }
}

// 9. DUAL-SIDED ADMIN DASHBOARD SCREEN
@Composable
fun AdminPanelScreen(viewModel: MainViewModel) {
    var pName by remember { mutableStateOf("") }
    var pInspired by remember { mutableStateOf("") }
    var pPrice by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pCategory by remember { mutableStateOf("Men") }
    var pFamily by remember { mutableStateOf("Woody") }
    var pTopNotes by remember { mutableStateOf("") }
    var pMidNotes by remember { mutableStateOf("") }
    var pBaseNotes by remember { mutableStateOf("") }
    var pLongevity by remember { mutableStateOf(0.85f) }
    var pSillage by remember { mutableStateOf(0.80f) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_screen_column"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("home") }) {
                    Icon(Icons.Default.ArrowBack, "Back to home", tint = LuxuryGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ADMIN CONTROL PANEL",
                        style = MaterialTheme.typography.titleLarge,
                        color = LuxuryGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Scalable dual-sided managers: update catalog directly!",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureSilver
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnyxSurface)
                    .border(0.5.dp, CharcoalGray, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "ADD NEW PERFUME TO PUBLIC VAULT",
                    fontSize = 11.sp,
                    color = LuxuryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pName,
                    onValueChange = { pName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Perfume Unique Name", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pInspired,
                    onValueChange = { pInspired = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Inspired By scent brand (e.g. Creed, Tom Ford)", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pPrice,
                    onValueChange = { pPrice = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("admin_price_input"),
                    label = { Text("Price (PKR)", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pDesc,
                    onValueChange = { pDesc = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Sensory Description Text", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                // Selectors mapping Category and Families
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Men", "Women", "Unisex").forEach { cat ->
                        val isSel = pCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LuxuryGold else ObsidianBlack)
                                .border(0.5.dp, if (isSel) LuxuryGold else CharcoalGray, RoundedCornerShape(6.dp))
                                .clickable { pCategory = cat }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = cat, color = if (isSel) ObsidianBlack else PlatinumWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Fresh", "Woody", "Sweet", "Oriental").forEach { fam ->
                        val isSel = pFamily == fam
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LuxuryGold else ObsidianBlack)
                                .border(0.5.dp, if (isSel) LuxuryGold else CharcoalGray, RoundedCornerShape(6.dp))
                                .clickable { pFamily = fam }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = fam, color = if (isSel) ObsidianBlack else PlatinumWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pTopNotes,
                    onValueChange = { pTopNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Top Notes (comma-separated)", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pMidNotes,
                    onValueChange = { pMidNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Heart/Middle Notes", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pBaseNotes,
                    onValueChange = { pBaseNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Base Notes", color = ScentBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LuxuryGold, unfocusedBorderColor = CharcoalGray),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedPrice = pPrice.toIntOrNull()
                        if (pName.isBlank() || parsedPrice == null || pTopNotes.isBlank()) {
                            Toast.makeText(context, "Please configure name, price & accords first.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addAdminProduct(
                                name = pName,
                                inspiredBy = pInspired,
                                price = parsedPrice,
                                description = pDesc,
                                category = pCategory,
                                family = pFamily,
                                topNotes = pTopNotes,
                                midNotes = pMidNotes,
                                baseNotes = pBaseNotes,
                                longevity = pLongevity,
                                sillage = pSillage
                            )
                            Toast.makeText(context, "Custom Extract Added! Reflecting on home lists.", Toast.LENGTH_LONG).show()
                            viewModel.navigateTo("home")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_upload_prod_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Upload Bespoke Extract", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SHARED HELPER CHIPS AND CARDS COMPOSE COMPULSIONS ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontFamily = FontFamily.Serif,
        color = LuxuryGold,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

// Product Card (Horizontal scrolling format)
@Composable
fun ProductCard(product: Product, viewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable { viewModel.viewProductDetail(product) }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = OnyxSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(CharcoalGray),
                contentAlignment = Alignment.Center
            ) {
                ProductImageContainer(id = product.id, modifier = Modifier.size(100.dp))
                
                val disc = ((product.originalPrice - product.price).toFloat() / product.originalPrice * 100).toInt()
                if (disc > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(VelvetRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-$disc% OFF",
                            fontSize = 9.sp,
                            color = PlatinumWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = PlatinumWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ref: ${product.inspiredBy}",
                    fontSize = 11.sp,
                    color = PureSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rs. ${product.price}",
                        fontSize = 14.sp,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            viewModel.addToCart(product, "50ml")
                        },
                        modifier = Modifier.size(24.dp).testTag("quick_add_cart_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Quick Add",
                            tint = LuxuryGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// Small product card representation (utilized inside main explorer list grids)
@Composable
fun ProductCardSmall(product: Product, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(OnyxSurface)
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable { viewModel.viewProductDetail(product) }
            .testTag("product_grid_${product.id}")
            .padding(10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalGray),
                contentAlignment = Alignment.Center
            ) {
                ProductImageContainer(id = product.id, modifier = Modifier.size(80.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name.uppercase(),
                fontSize = 13.sp,
                color = PlatinumWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ref: ${product.inspiredBy}",
                fontSize = 10.sp,
                color = PureSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rs. ${product.price}",
                    color = LuxuryGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(LuxuryGold)
                        .clickable { viewModel.addToCart(product, "50ml") }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("+ Bag", fontSize = 9.sp, color = ObsidianBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// DYNAMIC PRODUCT IMAGES RENDERING OR FALLBACK GRADIENTS COMPULSIONS
@Composable
fun ProductImageContainer(id: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CharcoalGray, OnyxSurface)
                )
            )
            .border(0.5.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.FilterVintage,
                contentDescription = "Fragrance Aura",
                tint = LuxuryGold,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "EXTRACT",
                fontSize = 9.sp,
                color = LuxuryGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=300&q=80")
                .crossfade(true)
                .build(),
            contentDescription = "Perfume flask",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

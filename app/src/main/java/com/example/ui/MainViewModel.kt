package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ScentQuizState {
    object Idle : ScentQuizState
    object Loading : ScentQuizState
    data class Success(val result: ScentMatchResult) : ScentQuizState
    data class Error(val message: String) : ScentQuizState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.appDao())

    // --- Navigation and Focus State ---
    val currentScreen = MutableStateFlow("home") // "home", "search", "quiz", "cart", "profile_orders", "support", "detail", "admin"
    val selectedProduct = MutableStateFlow<Product>(ProductRepository.items[0])

    // --- Persistent DB states ---
    val cartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val wishlistItems = repository.wishlistItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val orders = repository.orders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val loyaltyStatus = repository.loyaltyStatus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val perfumeProfile = repository.perfumeProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Dynamic Scent Catalog ---
    private val _customProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = combine(
        _customProducts
    ) { custom ->
        ProductRepository.items + custom[0]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductRepository.items)

    // --- Search & Filters State ---
    val searchQuery = MutableStateFlow("")
    val filterGender = MutableStateFlow("All") // "All", "Men", "Women", "Unisex"
    val filterOlfactive = MutableStateFlow("All") // "All", "Fresh", "Woody", "Sweet", "Oriental"
    val filterOccasion = MutableStateFlow("All") // "All", "Office", "Date Night", "Casual Daily", "Eid/Festive"
    val priceRangeMax = MutableStateFlow(5500f)
    val recentlyViewed = MutableStateFlow<List<Product>>(emptyList())

    // --- AI Perfume Quiz States ---
    val quizGender = MutableStateFlow("")
    val quizVibe = MutableStateFlow("")
    val quizSetting = MutableStateFlow("")
    val quizOccasion = MutableStateFlow("")
    val quizState = MutableStateFlow<ScentQuizState>(ScentQuizState.Idle)

    // --- Checkout & Rewards System ---
    val appliedCoupon = MutableStateFlow("")
    val simulatedReferralCount = MutableStateFlow(2)
    val showSpinWin = MutableStateFlow(false)
    val lastSpinResult = MutableStateFlow<String?>(null)

    // --- Live Customer Chat Simulation ---
    val chatMessages = MutableStateFlow<List<Pair<String,String>>>(listOf(
        "Purfumely Bot" to "Asalam-o-Alaikum! Welcome to Purfumely. How can we elevate your sensory signature today? 🌸"
    ))

    init {
        // Initialize Default Loyalty Status if not present
        viewModelScope.launch {
            repository.loyaltyStatus.first()?.let { /* already exists */ } ?: run {
                repository.updateLoyalty(points = 150, referrals = 2, hasSpun = false)
            }
        }
    }

    // --- Navigation Actions ---
    fun navigateTo(screen: String) {
        currentScreen.value = screen
    }

    fun viewProductDetail(product: Product) {
        selectedProduct.value = product
        // Add to recently viewed
        val current = recentlyViewed.value.filter { it.id != product.id }
        recentlyViewed.value = (listOf(product) + current).take(5)
        navigateTo("detail")
    }

    // --- Cart Actions ---
    fun addToCart(product: Product, size: String) {
        viewModelScope.launch {
            val priceForSize = if (size == "100ml") (product.price * 1.5).toInt() else product.price
            // check if already in cart
            val existing = cartItems.value.find { it.productId == product.id && it.selectedSize == size }
            if (existing != null) {
                repository.updateCartQuantity(existing.id, existing.quantity + 1)
            } else {
                repository.addToCart(
                    productId = product.id,
                    name = product.name,
                    price = priceForSize,
                    quantity = 1,
                    imageUrl = product.id,
                    size = size
                )
            }
        }
    }

    fun increaseCartQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartQuantity(item.id, item.quantity + 1)
        }
    }

    fun decreaseCartQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartQuantity(item.id, item.quantity - 1)
        }
    }

    fun removeCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.removeCartItem(item.id)
        }
    }

    // --- Wishlist Actions ---
    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val isFav = wishlistItems.value.any { it.productId == product.id }
            if (isFav) {
                repository.removeWishlistItem(product.id)
            } else {
                repository.addWishlistItem(product.id, product.name, product.price, product.id)
            }
        }
    }

    // --- Coupon & Discount application ---
    fun applyCouponCode(code: String): Boolean {
        val uppercaseCode = code.uppercase().trim()
        return if (uppercaseCode == "LUXURY10" || uppercaseCode == "EID20" || uppercaseCode == "SPIN30" || uppercaseCode == "REFER500") {
            appliedCoupon.value = uppercaseCode
            true
        } else {
            false
        }
    }

    // --- Checkout order creation ---
    fun checkout(paymentMethod: String, address: String, phone: String, note: String) {
        viewModelScope.launch {
            if (cartItems.value.isEmpty()) return@launch

            val subtotal = cartItems.value.sumOf { it.price * it.quantity }
            val discount = when (appliedCoupon.value) {
                "LUXURY10" -> (subtotal * 0.1).toInt()
                "EID20" -> (subtotal * 0.2).toInt()
                "SPIN30" -> (subtotal * 0.3).toInt()
                "REFER500" -> 500
                else -> 0
            }
            val total = (subtotal - discount).coerceAtLeast(0)
            
            // Calculate loyalty points earned (10% of total cash spent)
            val pointsEarned = (total * 0.05).toInt()

            val itemsDescription = cartItems.value.joinToString(", ") { "${it.name} (${it.selectedSize}) x${it.quantity}" }
            
            // submit to DB
            repository.submitOrder(
                totalAmount = total,
                itemsJson = itemsDescription,
                coupon = appliedCoupon.value,
                payment = paymentMethod,
                points = pointsEarned
            )

            // Update loyalty points
            val currentLoyalty = loyaltyStatus.value ?: LoyaltyStatus()
            repository.updateLoyalty(
                points = currentLoyalty.points + pointsEarned,
                referrals = currentLoyalty.referrals,
                hasSpun = currentLoyalty.hasSpunToday
            )

            // clear cart and coupon
            repository.clearCart()
            appliedCoupon.value = ""
            navigateTo("profile_orders")
        }
    }

    // --- AI Scent Finder Execution ---
    fun executeScentQuiz() {
        quizState.value = ScentQuizState.Loading
        viewModelScope.launch {
            try {
                val result = ScentQuizResolver.findScent(
                    gender = quizGender.value,
                    vibe = quizVibe.value,
                    dailySetting = quizSetting.value,
                    occasion = quizOccasion.value
                )
                quizState.value = ScentQuizState.Success(result)
                // Persistence in room
                repository.savePerfumeProfile(
                    gender = quizGender.value,
                    vibe = quizVibe.value,
                    dailySetting = quizSetting.value,
                    occasion = quizOccasion.value,
                    matchedScentsJson = result.matchedProductId
                )
            } catch (e: Exception) {
                quizState.value = ScentQuizState.Error("Failed to trigger virtual perfume match: ${e.localizedMessage}")
            }
        }
    }

    fun resetQuiz() {
        quizGender.value = ""
        quizVibe.value = ""
        quizSetting.value = ""
        quizOccasion.value = ""
        quizState.value = ScentQuizState.Idle
    }

    // --- Gamified Spin to Win action ---
    fun spinTheWheel() {
        viewModelScope.launch {
            val discountCodes = listOf("EID20", "LUXURY10", "SPIN30", "Free Scent Sample", "REFER500")
            val chosen = discountCodes.random()
            
            val current = loyaltyStatus.value ?: LoyaltyStatus()
            repository.updateLoyalty(
                points = current.points + 50, // bonus 50 points from spinning
                referrals = current.referrals,
                hasSpun = true
            )
            lastSpinResult.value = chosen
            if (chosen != "Free Scent Sample") {
                appliedCoupon.value = chosen
            }
        }
    }

    // --- Shared Chat Actions ---
    fun sendMessage(userMsg: String) {
        if (userMsg.isBlank()) return
        val current = chatMessages.value.toMutableList()
        current.add("You" to userMsg)
        chatMessages.value = current

        // Auto luxurious reply generator
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val updated = chatMessages.value.toMutableList()
            val reply = when {
                userMsg.contains("order", true) || userMsg.contains("track", true) -> {
                    "Your Purfumely bespoke order is packed with velvet lining, wrapped in silk ribbon, and is dispatched via luxury express couriers to your doorstep. It typically takes 24-48 hours across major Pakistani cities! 🚚✨"
                }
                userMsg.contains("discount", true) || userMsg.contains("sale", true) || userMsg.contains("code", true) -> {
                    "To celebrate our prestigious community, use the coupon 'LUXURY10' for 10% off your collection instantly, or spin our daily luxury wheel inside your profile! 🎡👑"
                }
                userMsg.contains("original", true) || userMsg.contains("clone", true) || userMsg.contains("inspired", true) -> {
                    "Indeed! We formulate exact luxurious 'inspired-by' extracts capturing the exquisite high-end DNA of French houses (with up to 35% oil concentration for extreme 12-hour local longevity) but at a highly accessible Pakistani price point. 🪙🌹"
                }
                else -> {
                    "That sounds delightful. Our premium perfume advisors are always online. If you need immediate assistance, tab our WhatsApp call button below to chat with our Lahore boutique head! 🌿"
                }
            }
            updated.add("Purfumely Bot" to reply)
            chatMessages.value = updated
        }
    }

    // --- Admin Operations ---
    fun addAdminProduct(
        name: String,
        inspiredBy: String,
        price: Int,
        description: String,
        category: String,
        family: String,
        topNotes: String,
        midNotes: String,
        baseNotes: String,
        longevity: Float,
        sillage: Float
    ) {
        val newProd = Product(
            id = name.lowercase().replace(" ", "_"),
            name = name,
            inspiredBy = inspiredBy,
            price = price,
            originalPrice = (price * 1.25).toInt(),
            description = description,
            category = category,
            olfactoryFamily = family,
            topNotes = topNotes.split(",").map { it.trim() },
            middleNotes = midNotes.split(",").map { it.trim() },
            baseNotes = baseNotes.split(",").map { it.trim() },
            longevity = longevity,
            sillage = sillage,
            occasions = listOf("Office", "Casual Daily", "Date Night"),
            seasons = listOf("Summer", "Winter", "Spring", "Autumn"),
            rating = 4.8f,
            reviewCount = 1,
            ratingDetails = listOf(1, 0, 0, 0, 0),
            layeringPartner = "creed_aventus",
            layeringDescription = "Layers beautifully with Royal Aventus for custom freshness.",
            isNewArrival = true
        )
        _customProducts.value = _customProducts.value + newProd
    }
}

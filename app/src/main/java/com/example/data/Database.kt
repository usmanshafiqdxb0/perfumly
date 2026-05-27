package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val name: String,
    val price: Int, // PKR
    val quantity: Int,
    val imageUrl: String,
    val selectedSize: String
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Int,
    val imageUrl: String
)

@Entity(tableName = "user_orders")
data class UserOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderDate: Long = System.currentTimeMillis(),
    val totalAmount: Int,
    val itemsJson: String, // serialized list of buyable items
    val couponUsed: String = "",
    val status: String = "Processing", // Processing, Shipped, Delivered
    val paymentMethod: String = "COD", // COD, Card
    val pointsEarned: Int = 0
)

@Entity(tableName = "loyalty_status")
data class LoyaltyStatus(
    @PrimaryKey val userId: String = "default_user",
    val points: Int = 0,
    val referrals: Int = 0,
    val hasSpunToday: Boolean = false
)

@Entity(tableName = "perfume_profiles")
data class PerfumeProfile(
    @PrimaryKey val id: String = "user_profile",
    val gender: String,
    val vibe: String,
    val dailySetting: String,
    val occasion: String,
    val matchedScentsJson: String // Product list
)

// --- DAO Interface ---

@Dao
interface AppDao {
    // --- Cart Queries ---
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartItemQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // --- Wishlist Queries ---
    @Query("SELECT * FROM wishlist_items")
    fun getWishlist(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteWishlistItem(productId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId LIMIT 1)")
    fun isWishlisted(productId: String): Flow<Boolean>

    // --- Orders Queries ---
    @Query("SELECT * FROM user_orders ORDER BY orderDate DESC")
    fun getOrders(): Flow<List<UserOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: UserOrder)

    // --- Loyalty Queries ---
    @Query("SELECT * FROM loyalty_status WHERE userId = :userId LIMIT 1")
    fun getLoyalty(userId: String = "default_user"): Flow<LoyaltyStatus?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyalty(loyalty: LoyaltyStatus)

    // --- Scent Profile Queries ---
    @Query("SELECT * FROM perfume_profiles WHERE id = :id LIMIT 1")
    fun getPerfumeProfile(id: String = "user_profile"): Flow<PerfumeProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfumeProfile(profile: PerfumeProfile)
}

// --- App Database Holder ---

@Database(
    entities = [
        CartItem::class,
        WishlistItem::class,
        UserOrder::class,
        LoyaltyStatus::class,
        PerfumeProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "purfumely_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository abstraction ---

class AppRepository(private val appDao: AppDao) {
    val cartItems: Flow<List<CartItem>> = appDao.getCartItems()
    val wishlistItems: Flow<List<WishlistItem>> = appDao.getWishlist()
    val orders: Flow<List<UserOrder>> = appDao.getOrders()
    val loyaltyStatus: Flow<LoyaltyStatus?> = appDao.getLoyalty("default_user")
    val perfumeProfile: Flow<PerfumeProfile?> = appDao.getPerfumeProfile("user_profile")

    // Cart
    suspend fun addToCart(productId: String, name: String, price: Int, quantity: Int, imageUrl: String, size: String) {
        val item = CartItem(productId = productId, name = name, price = price, quantity = quantity, imageUrl = imageUrl, selectedSize = size)
        appDao.insertCartItem(item)
    }
    suspend fun updateCartQuantity(id: Int, quantity: Int) {
        if (quantity <= 0) {
            appDao.deleteCartItem(id)
        } else {
            appDao.updateCartItemQuantity(id, quantity)
        }
    }
    suspend fun removeCartItem(id: Int) = appDao.deleteCartItem(id)
    suspend fun clearCart() = appDao.clearCart()

    // Wishlist
    suspend fun toggleWishlist(productId: String, name: String, price: Int) {
        // Since we are running in non-blocking flow or background thread, we can check its state
        // To be secure, we can toggle or pass in explicit values. Let's provide standard add and remove.
    }
    suspend fun addWishlistItem(productId: String, name: String, price: Int, imageUrl: String) {
        appDao.insertWishlistItem(WishlistItem(productId = productId, name = name, price = price, imageUrl = imageUrl))
    }
    suspend fun removeWishlistItem(productId: String) = appDao.deleteWishlistItem(productId)
    fun isWishlisted(productId: String): Flow<Boolean> = appDao.isWishlisted(productId)

    // Orders
    suspend fun submitOrder(totalAmount: Int, itemsJson: String, coupon: String, payment: String, points: Int): UserOrder {
        val order = UserOrder(
            totalAmount = totalAmount,
            itemsJson = itemsJson,
            couponUsed = coupon,
            paymentMethod = payment,
            pointsEarned = points
        )
        appDao.insertOrder(order)
        return order
    }

    // Loyalty
    suspend fun updateLoyalty(points: Int, referrals: Int, hasSpun: Boolean) {
        appDao.insertLoyalty(LoyaltyStatus(points = points, referrals = referrals, hasSpunToday = hasSpun))
    }

    // Scent Profile
    suspend fun savePerfumeProfile(gender: String, vibe: String, dailySetting: String, occasion: String, matchedScentsJson: String) {
        appDao.insertPerfumeProfile(
            PerfumeProfile(
                gender = gender,
                vibe = vibe,
                dailySetting = dailySetting,
                occasion = occasion,
                matchedScentsJson = matchedScentsJson
            )
        )
    }
}

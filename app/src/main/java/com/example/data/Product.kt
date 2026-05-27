package com.example.data

data class Product(
    val id: String,
    val name: String,
    val inspiredBy: String,
    val price: Int, // PKR
    val originalPrice: Int, // for discounts
    val description: String,
    val category: String, // "Men", "Women", "Unisex"
    val olfactoryFamily: String, // "Fresh", "Woody", "Sweet", "Oriental"
    val topNotes: List<String>,
    val middleNotes: List<String>,
    val baseNotes: List<String>,
    val longevity: Float, // 0.0 to 1.0 (e.g. 0.85 for 8+ hours)
    val sillage: Float, // 0.0 to 1.0 (e.g. 0.75 for Strong)
    val occasions: List<String>, // "Office", "Date Night", "Casual Daily", "Eid/Festive"
    val seasons: List<String>, // "Summer", "Winter", "Spring", "Autumn"
    val rating: Float,
    val reviewCount: Int,
    val ratingDetails: List<Int>, // count of 5-star, 4-star, etc.
    val layeringPartner: String, // Product ID this layers beautifully with
    val layeringDescription: String,
    val isBestSeller: Boolean = false,
    val isTrending: Boolean = false,
    val isNewArrival: Boolean = false,
    val isFlashSale: Boolean = false,
    val stockCount: Int = 12
)

object ProductRepository {
    val items = listOf(
        Product(
            id = "creed_aventus",
            name = "Royal Aventus",
            inspiredBy = "Creed Aventus",
            price = 3900,
            originalPrice = 4800,
            description = "A bold, sophisticated fragrance opening with fresh pineapple and sparkling blackcurrant, transitioning into a robust birch and patchouli heart, supported by a rich base of oakmoss and smooth vanilla. Designed for the dynamic man who commands authority.",
            category = "Men",
            olfactoryFamily = "Fresh",
            topNotes = listOf("Pineapple", "Bergamot", "Blackcurrant", "Apple"),
            middleNotes = listOf("Birch", "Patchouli", "Moroccan Jasmine", "Rose"),
            baseNotes = listOf("Musk", "Oakmoss", "Ambergris", "Vanilla"),
            longevity = 0.85f, // 8 Hours
            sillage = 0.80f, // Strong
            occasions = listOf("Office", "Date Night", "Eid/Festive"),
            seasons = listOf("Summer", "Spring", "Autumn"),
            rating = 4.8f,
            reviewCount = 142,
            ratingDetails = listOf(120, 15, 5, 2, 0),
            layeringPartner = "tf_oud_wood",
            layeringDescription = "Layer Royal Aventus with Sultan Oud to add an exotic, smokey depth to the bright, sparkling pineapple opening.",
            isBestSeller = true,
            isTrending = true,
            isNewArrival = false,
            isFlashSale = false,
            stockCount = 18
        ),
        Product(
            id = "dior_sauvage",
            name = "Varan Desert",
            inspiredBy = "Dior Sauvage",
            price = 3800,
            originalPrice = 4500,
            description = "An ultra-fresh, raw and masculine composition. Spicy Sichuan pepper and citrusy calabrian bergamot pierce through a rich vetiver and patchouli heart, leaving a powerful trailing trail of amberwood and precious mineral ambroxan.",
            category = "Men",
            olfactoryFamily = "Fresh",
            topNotes = listOf("Calabrian Bergamot", "Sichuan Pepper"),
            middleNotes = listOf("Lavender", "Pink Pepper", "Vetiver", "Patchouli"),
            baseNotes = listOf("Ambroxan", "Cedarwood", "Labdanum"),
            longevity = 0.90f, // 10 Hours
            sillage = 0.85f, // Very Strong
            occasions = listOf("Casual Daily", "Date Night", "Office"),
            seasons = listOf("Summer", "Winter", "Spring", "Autumn"),
            rating = 4.9f,
            reviewCount = 289,
            ratingDetails = listOf(260, 20, 6, 2, 1),
            layeringPartner = "bleu_chanel",
            layeringDescription = "Layer with Bleu Elixir to amplify the sparkling citrus opening while adding deep, mysterious incense undertones.",
            isBestSeller = true,
            isTrending = false,
            isNewArrival = false,
            isFlashSale = true,
            stockCount = 25
        ),
        Product(
            id = "bleu_chanel",
            name = "Bleu Elixir",
            inspiredBy = "Bleu de Chanel EdP",
            price = 3950,
            originalPrice = 4900,
            description = "An ode to masculine freedom expressed in a woody aromatic fragrance with a captivating trail. A timeless scent housed in a bottle of deep and mysterious blue, with fresh grapefruit accents and deep warm amber-incense.",
            category = "Men",
            olfactoryFamily = "Woody",
            topNotes = listOf("Grapefruit", "Lemon", "Mint", "Pink Pepper"),
            middleNotes = listOf("Ginger", "Nutmeg", "Jasmine", "Iso E Super"),
            baseNotes = listOf("Incense", "Vetiver", "Cedarwood", "Sandalwood", "Patchouli", "Labdanum"),
            longevity = 0.80f, // 7-8 Hours
            sillage = 0.70f, // Moderate-Strong
            occasions = listOf("Office", "Casual Daily", "Date Night"),
            seasons = listOf("Spring", "Summer", "Autumn"),
            rating = 4.7f,
            reviewCount = 188,
            ratingDetails = listOf(150, 28, 8, 2, 0),
            layeringPartner = "dior_sauvage",
            layeringDescription = "Layer with Varan Desert to combine Dior's sharp peppery freshness with Chanel's rich, incense-heavy woody dry-down.",
            isBestSeller = false,
            isTrending = true,
            isNewArrival = true,
            isFlashSale = false,
            stockCount = 14
        ),
        Product(
            id = "baccarat_540",
            name = "Rouge Spectre 540",
            inspiredBy = "Maison Francis Kurkdjian Baccarat Rouge 540",
            price = 4500,
            originalPrice = 5500,
            description = "A poetic alchemy. A highly concentrated, signature scent. Luminous saffron and jasmine top notes merge with warm mineral ambergris and freshly cut cedarwood, projecting a sophisticated, airy sweetness like caramelized spun-sugar on driftwood.",
            category = "Unisex",
            olfactoryFamily = "Sweet",
            topNotes = listOf("Saffron", "Jasmine"),
            middleNotes = listOf("Amberwood", "Ambergris"),
            baseNotes = listOf("Fir Resin", "Cedarwood"),
            longevity = 0.95f, // 12+ Hours
            sillage = 0.90f, // Enormous
            occasions = listOf("Date Night", "Eid/Festive"),
            seasons = listOf("Winter", "Autumn", "Spring"),
            rating = 4.9f,
            reviewCount = 312,
            ratingDetails = listOf(285, 20, 5, 1, 1),
            layeringPartner = "creed_aventus",
            layeringDescription = "Layer with Royal Aventus to create a legendary smoky, sweet-pineapple combination favored by fragrance connoisseurs.",
            isBestSeller = true,
            isTrending = true,
            isNewArrival = false,
            isFlashSale = false,
            stockCount = 8
        ),
        Product(
            id = "tf_oud_wood",
            name = "Sultan Oud",
            inspiredBy = "Tom Ford Oud Wood",
            price = 4200,
            originalPrice = 5200,
            description = "One of the most rare, precious, and expensive ingredients in a perfumer's arsenal, oud wood is often burned in incense-filled temples. Woody, dark-warm blend of exotic rosewood, cardamom, rich agarwood (oud), sandalwood, and vetiver.",
            category = "Unisex",
            olfactoryFamily = "Oriental",
            topNotes = listOf("Exotic Rosewood", "Cardamom", "Chinese Pepper"),
            middleNotes = listOf("Agarwood (Oud)", "Sandalwood", "Vetiver"),
            baseNotes = listOf("Tonka Bean", "Vanilla", "Amber"),
            longevity = 0.85f, // 8 Hours
            sillage = 0.75f, // Moderate
            occasions = listOf("Office", "Eid/Festive", "Date Night"),
            seasons = listOf("Winter", "Autumn"),
            rating = 4.6f,
            reviewCount = 95,
            ratingDetails = listOf(68, 20, 5, 2, 0),
            layeringPartner = "creed_aventus",
            layeringDescription = "Layer with Royal Aventus to enrich the smokey birch-tar heart of Aventus with a dark, rich agarwood and incense presence.",
            isBestSeller = false,
            isTrending = true,
            isNewArrival = false,
            isFlashSale = false,
            stockCount = 10
        ),
        Product(
            id = "tf_soleil_blanc",
            name = "Soleil Coconut",
            inspiredBy = "Tom Ford Soleil Blanc",
            price = 4400,
            originalPrice = 5400,
            description = "An addictive solar floral blend that is alive with seductive cardamom and decadent ylang-ylang, taking you to a private luxury island. Infused with warm coconut and ambergris, this exudes cozy warmth and luxury.",
            category = "Women",
            olfactoryFamily = "Sweet",
            topNotes = listOf("Pistachio", "Bergamot", "Cardamom", "Pink Pepper"),
            middleNotes = listOf("Tuberose", "Ylang-Ylang", "Jasmine"),
            baseNotes = listOf("Coconut", "Amber", "Tonka Bean", "Benzoin"),
            longevity = 0.80f, // 7 Hours
            sillage = 0.65f, // Moderate
            occasions = listOf("Casual Daily", "Date Night"),
            seasons = listOf("Summer", "Spring"),
            rating = 4.8f,
            reviewCount = 104,
            ratingDetails = listOf(88, 12, 3, 1, 0),
            layeringPartner = "baccarat_540",
            layeringDescription = "Layer Soleil Coconut with Rouge Spectre 540 for a sweet, summery spun-sugar and warm coconut luxury beach vibe.",
            isBestSeller = false,
            isTrending = false,
            isNewArrival = true,
            isFlashSale = false,
            stockCount = 12
        ),
        Product(
            id = "dunhill_desire",
            name = "Imperial Desire",
            inspiredBy = "Dunhill Desire Red",
            price = 3400,
            originalPrice = 4200,
            description = "An orientation woody fragrance that stands out. It self-expresses confidence. Combines fresh apple, orange blossom, and sweet bergamot with a solid, sensual heart of rose, patchouli, teakwood, and a luxurious vanilla baseline.",
            category = "Men",
            olfactoryFamily = "Sweet",
            topNotes = listOf("Apple", "Neroli", "Bergamot", "Lemon"),
            middleNotes = listOf("Patchouli", "Teakwood", "Rose"),
            baseNotes = listOf("Musk", "Vanilla"),
            longevity = 0.90f, // 9 Hours
            sillage = 0.85f, // Very Strong
            occasions = listOf("Casual Daily", "Date Night"),
            seasons = listOf("Winter", "Autumn", "Spring"),
            rating = 4.7f,
            reviewCount = 160,
            ratingDetails = listOf(130, 20, 8, 2, 0),
            layeringPartner = "bleu_chanel",
            layeringDescription = "Layer with Bleu Elixir to balance the crisp, sweet apple top note of Desire with a refreshing, citrus and cedarwood incense base.",
            isBestSeller = false,
            isTrending = false,
            isNewArrival = false,
            isFlashSale = true,
            stockCount = 30
        )
    )

    fun getProductById(id: String): Product? {
        return items.find { it.id == id }
    }
}

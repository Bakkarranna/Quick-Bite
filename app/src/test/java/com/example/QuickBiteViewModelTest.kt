package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.viewmodel.QuickBiteViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuickBiteViewModelTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: QuickBiteRepository
    private lateinit var viewModel: QuickBiteViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        
        // Build an in-memory database for clean, isolated, reproducible test runs
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        repository = QuickBiteRepository(database, context)
        viewModel = QuickBiteViewModel(context, repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInitialState() = runTest {
        val count = viewModel.cartCount.first()
        val total = viewModel.cartSubtotal.first()
        val cartList = viewModel.cartItems.first()
        
        assertEquals(0, count)
        assertEquals(0.0, total, 0.001)
        assertTrue(cartList.isEmpty())
        assertFalse(viewModel.isDarkMode.value)
    }

    @Test
    fun testAddToCartAndCalculations() = runTest {
        // Add single item
        viewModel.addItemToCart(
            id = "test-item-1",
            name = "Chicken Biryani",
            price = 250.0,
            imageUrl = "https://example.com/item.png"
        )
        
        var count = viewModel.cartCount.first()
        var subtotal = viewModel.cartSubtotal.value
        var cartList = viewModel.cartItems.value
        
        assertEquals(1, count)
        assertEquals(250.0, subtotal, 0.001)
        assertEquals(1, cartList.size)
        assertEquals("Chicken Biryani", cartList[0].name)
        assertEquals(1, cartList[0].quantity)

        // Add matching item again (should increment quantity)
        viewModel.addItemToCart(
            id = "test-item-1",
            name = "Chicken Biryani",
            price = 250.0,
            imageUrl = "https://example.com/item.png"
        )
        
        count = viewModel.cartCount.first()
        subtotal = viewModel.cartSubtotal.value
        cartList = viewModel.cartItems.value
        
        assertEquals(2, count)
        assertEquals(500.0, subtotal, 0.001)
        assertEquals(1, cartList.size)
        assertEquals(2, cartList[0].quantity)
    }

    @Test
    fun testUpdateCartQuantityAndDeletion() = runTest {
        viewModel.addItemToCart(
            id = "test-item-1",
            name = "Margherita Pizza",
            price = 600.0,
            imageUrl = "https://example.com/pizza.png"
        )
        
        // Update quantity to 3
        viewModel.updateCartQuantity("test-item-1", 3)
        var count = viewModel.cartCount.first()
        var subtotal = viewModel.cartSubtotal.value
        assertEquals(3, count)
        assertEquals(1800.0, subtotal, 0.001)

        // Decrease quantity to 0 (should delete)
        viewModel.updateCartQuantity("test-item-1", 0)
        count = viewModel.cartCount.first()
        assertTrue(viewModel.cartItems.value.isEmpty())
        assertEquals(0, count)
    }

    @Test
    fun testClearCart() = runTest {
        viewModel.addItemToCart("id-1", "Fries", 120.0, "img")
        viewModel.addItemToCart("id-2", "Soda", 80.0, "img")
        
        assertEquals(2, viewModel.cartCount.first())
        
        viewModel.clearCart()
        assertEquals(0, viewModel.cartCount.first())
        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun testAddAndRemoveAddresses() = runTest {
        viewModel.addUserAddress("Gym", "456 Fitness Avenue, Faisalabad")
        
        val addresses = viewModel.addresses.first()
        // Wait, on initial database creation, seed address table contains "Home" and "Office"
        // Let's check we successfully appended "Gym"
        val gymAddress = addresses.find { it.label == "Gym" }
        assertNotNull(gymAddress)
        assertEquals("456 Fitness Avenue, Faisalabad", gymAddress?.detail)

        // Remove address
        viewModel.deleteUserAddress(gymAddress!!.id)
        val afterAddresses = viewModel.addresses.first()
        val deletedGym = afterAddresses.find { it.label == "Gym" }
        assertNull(deletedGym)
    }

    @Test
    fun testUserProfileUpdate() = runTest {
        viewModel.updateProfile("Ahmad Khan", "+923000000000")
        
        val profile = viewModel.userProfile.first()
        assertNotNull(profile)
        assertEquals("Ahmad Khan", profile?.name)
        assertEquals("+923000000000", profile?.phone)
    }

    @Test
    fun testSubmitReview() = runTest {
        viewModel.submitOrderReview(
            orderId = "QB-9999",
            restaurantName = "Naseeb Biryani Center",
            rating = 5.0f,
            comment = "Excellent spice level!",
            tags = "Spicy,Satisfying"
        )
        
        val reviews = viewModel.reviews.first()
        val matchingReview = reviews.find { it.comment == "Excellent spice level!" }
        assertNotNull(matchingReview)
        assertEquals("Naseeb Biryani Center", matchingReview?.restaurantName)
        assertEquals(5.0f, matchingReview?.rating)
    }

    @Test
    fun testCheckoutProcess() = runTest {
        viewModel.addItemToCart("item-id", "Single Patty Burger", 350.0, "url")
        
        viewModel.checkOutAndPlaceOrder { orderId ->
            assertNotNull(orderId)
            assertTrue(orderId.startsWith("QB-"))
        }
        
        // Cart should be cleared after checkout inside checkOutAndPlaceOrder
        val count = viewModel.cartCount.first()
        assertEquals(0, count)
    }
}

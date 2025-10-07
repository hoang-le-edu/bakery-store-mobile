package com.dev.thecodecup.model.db.order

import androidx.room.Room
import com.dev.thecodecup.model.data.coffeeList
import com.dev.thecodecup.model.db.AppDatabase
import com.dev.thecodecup.model.db.cart.CartItemEntity
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.rules.TestRule
import org.robolectric.RuntimeEnvironment

@RunWith(JUnit4::class)
class OrderDaoTest {
    @get:Rule
    val instantTaskExecutorRule: TestRule = TestRule { base, _ -> base }

    private lateinit var db: AppDatabase
    private lateinit var dao: OrderDao

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.orderDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun exchangeOrderFromOngoingToHistory_movesOrderCorrectly() = runBlocking {
        // Arrange: Insert an ongoing order (isHistory = false)
        val cartItem = CartItemEntity(
            coffee = coffeeList[0]
        )
        val order = OrderEntity(cartItem = cartItem, location = "Test", orderTime = "2024-06-01", isHistory = false)
        dao.insertOrder(order)

        // Act: Get the inserted order (should be the only one)
        val ongoingOrdersBefore = dao.getOngoingOrders()
        val insertedOrder = ongoingOrdersBefore.first()
        val updatedOrder = insertedOrder.copy(isHistory = true)
        dao.updateOrder(updatedOrder)

        // Assert: The order should not be in the ongoing list
        val ongoingOrders = dao.getOngoingOrders()
        Assert.assertFalse(ongoingOrders.any { it.id == insertedOrder.id })

        // Assert: The order should be in the history list
        val historyOrders = dao.getHistoryOrders()
        Assert.assertTrue(historyOrders.any { it.id == insertedOrder.id && it.isHistory })
    }
}

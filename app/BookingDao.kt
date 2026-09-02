package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getBookingsByCustomer(customerId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE providerId = :providerId ORDER BY createdAt DESC")
    fun getBookingsByProvider(providerId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: String): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<Booking>)

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status, updatedAt = :updatedAt WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bookings SET providerId = :providerId, providerName = :providerName, providerPhone = :providerPhone, status = :status, updatedAt = :updatedAt WHERE id = :bookingId")
    suspend fun assignProvider(
        bookingId: String,
        providerId: String,
        providerName: String,
        providerPhone: String,
        status: BookingStatus = BookingStatus.ACCEPTED,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bookings SET extraPartsCost = :extraParts, partsDescription = :partsDesc, totalAmount = :total, updatedAt = :updatedAt WHERE id = :bookingId")
    suspend fun updateJobPartsBill(
        bookingId: String,
        extraParts: Double,
        partsDesc: String,
        total: Double,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus, paymentMethod = :paymentMethod, updatedAt = :updatedAt WHERE id = :bookingId")
    suspend fun updatePayment(
        bookingId: String,
        paymentStatus: PaymentStatus,
        paymentMethod: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bookings SET rating = :rating, reviewText = :reviewText, feedbackTags = :tags, updatedAt = :updatedAt WHERE id = :bookingId")
    suspend fun submitRating(
        bookingId: String,
        rating: Float,
        reviewText: String,
        tags: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bookings SET status = :status, rejectionReason = :reason, updatedAt = :updatedAt WHERE id = :bookingId")
    suspend fun rejectBooking(
        bookingId: String,
        reason: String,
        status: BookingStatus = BookingStatus.REJECTED,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBooking(id: String)
}

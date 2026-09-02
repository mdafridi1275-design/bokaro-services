package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BookingStatus(val labelEn: String, val labelHi: String, val stepIndex: Int) {
    PENDING("Booking Placed", "बुकिंग दर्ज की गई", 0),
    ACCEPTED("Provider Accepted", "सेवा प्रदाता ने स्वीकार किया", 1),
    ON_THE_WAY("On The Way", "रास्ते में है", 2),
    WORK_STARTED("Work Started", "काम शुरू हुआ", 3),
    WORK_COMPLETED("Work Completed", "काम पूरा हुआ", 4),
    REJECTED("Rejected", "अस्वीकृत", -2),
    CANCELLED("Cancelled", "रद्द किया गया", -1)
}

enum class PaymentStatus(val labelEn: String, val labelHi: String) {
    PENDING("Payment Pending", "भुगतान बाकी है"),
    PAID_CASH("Paid via Cash", "नकद भुगतान संपन्न"),
    PAID_UPI("Paid via UPI / QR", "UPI द्वारा भुगतान संपन्न")
}

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val area: String,
    val serviceType: String, // ELECTRICIAN, PLUMBER, AC_APPLIANCE
    val subServiceName: String,
    val providerId: String? = null,
    val providerName: String? = null,
    val providerPhone: String? = null,
    val problemDescription: String = "",
    val bookingDate: String = "",
    val timeSlot: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val rejectionReason: String? = null,
    val basePrice: Double = 199.0,
    val extraPartsCost: Double = 0.0,
    val partsDescription: String = "",
    val totalAmount: Double = 199.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String = "Not Selected",
    val rating: Float? = null,
    val reviewText: String? = null,
    val feedbackTags: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "customerId" to customerId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "customerAddress" to customerAddress,
            "area" to area,
            "serviceType" to serviceType,
            "subServiceName" to subServiceName,
            "providerId" to providerId,
            "providerName" to providerName,
            "providerPhone" to providerPhone,
            "problemDescription" to problemDescription,
            "bookingDate" to bookingDate,
            "timeSlot" to timeSlot,
            "status" to status.name,
            "rejectionReason" to rejectionReason,
            "basePrice" to basePrice,
            "extraPartsCost" to extraPartsCost,
            "partsDescription" to partsDescription,
            "totalAmount" to totalAmount,
            "paymentStatus" to paymentStatus.name,
            "paymentMethod" to paymentMethod,
            "rating" to rating,
            "reviewText" to reviewText,
            "feedbackTags" to feedbackTags,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Booking {
            val statusString = map["status"] as? String ?: BookingStatus.PENDING.name
            val parsedStatus = try {
                BookingStatus.valueOf(statusString)
            } catch (e: Exception) {
                BookingStatus.PENDING
            }

            val paymentStatusString = map["paymentStatus"] as? String ?: PaymentStatus.PENDING.name
            val parsedPaymentStatus = try {
                PaymentStatus.valueOf(paymentStatusString)
            } catch (e: Exception) {
                PaymentStatus.PENDING
            }

            return Booking(
                id = map["id"] as? String ?: "",
                customerId = map["customerId"] as? String ?: "",
                customerName = map["customerName"] as? String ?: "",
                customerPhone = map["customerPhone"] as? String ?: "",
                customerAddress = map["customerAddress"] as? String ?: "",
                area = map["area"] as? String ?: "",
                serviceType = map["serviceType"] as? String ?: "ELECTRICIAN",
                subServiceName = map["subServiceName"] as? String ?: "General Service",
                providerId = map["providerId"] as? String,
                providerName = map["providerName"] as? String,
                providerPhone = map["providerPhone"] as? String,
                problemDescription = map["problemDescription"] as? String ?: "",
                bookingDate = map["bookingDate"] as? String ?: "",
                timeSlot = map["timeSlot"] as? String ?: "",
                status = parsedStatus,
                rejectionReason = map["rejectionReason"] as? String,
                basePrice = (map["basePrice"] as? Number)?.toDouble() ?: 199.0,
                extraPartsCost = (map["extraPartsCost"] as? Number)?.toDouble() ?: 0.0,
                partsDescription = map["partsDescription"] as? String ?: "",
                totalAmount = (map["totalAmount"] as? Number)?.toDouble() ?: 199.0,
                paymentStatus = parsedPaymentStatus,
                paymentMethod = map["paymentMethod"] as? String ?: "Not Selected",
                rating = (map["rating"] as? Number)?.toFloat(),
                reviewText = map["reviewText"] as? String,
                feedbackTags = map["feedbackTags"] as? String,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

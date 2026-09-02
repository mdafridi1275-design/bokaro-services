package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    PROVIDER,
    ADMIN
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val area: String = "Sector 4, Bokaro",
    val address: String = "",
    val serviceCategory: String? = null, // "ELECTRICIAN", "PLUMBER", "AC_APPLIANCE"
    val experienceYears: Int = 0,
    val rating: Double = 4.8,
    val reviewCount: Int = 0,
    val isAvailable: Boolean = true,
    val isVerified: Boolean = true,
    val hourlyRate: Double = 199.0,
    val bio: String = "",
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "role" to role.name,
            "area" to area,
            "address" to address,
            "serviceCategory" to serviceCategory,
            "experienceYears" to experienceYears,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "isAvailable" to isAvailable,
            "isVerified" to isVerified,
            "hourlyRate" to hourlyRate,
            "bio" to bio,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                role = UserRole.valueOf(map["role"] as? String ?: UserRole.CUSTOMER.name),
                area = map["area"] as? String ?: "Sector 4, Bokaro",
                address = map["address"] as? String ?: "",
                serviceCategory = map["serviceCategory"] as? String,
                experienceYears = (map["experienceYears"] as? Number)?.toInt() ?: 0,
                rating = (map["rating"] as? Number)?.toDouble() ?: 5.0,
                reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 0,
                isAvailable = map["isAvailable"] as? Boolean ?: true,
                isVerified = map["isVerified"] as? Boolean ?: true,
                hourlyRate = (map["hourlyRate"] as? Number)?.toDouble() ?: 199.0,
                bio = map["bio"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ServiceCatalog
import com.example.data.model.ServiceType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (continuation.isActive) continuation.cancel()
    }
}

class FirebaseManager private constructor(context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    companion object {
        private const val TAG = "FirebaseManager"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_BOOKINGS = "bookings"
        const val COLLECTION_SERVICES = "services"
        const val COLLECTION_RATINGS = "ratings"

        @Volatile
        private var instance: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return instance ?: synchronized(this) {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                val app = FirebaseApp.getInstance()
                Log.i(TAG, "Firebase initialized with project: ${app.options.projectId}")
                instance ?: FirebaseManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val currentAuthUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun testFirebaseConnection(): Result<String> {
        return try {
            val testDoc = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "projectId" to "bokaro-services",
                "status" to "SUCCESS",
                "authUid" to (auth.currentUser?.uid ?: "none"),
                "testedAt" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            )
            firestore.collection("debug")
                .document("firebase_test")
                .set(testDoc, SetOptions.merge())
                .awaitTask()
            Log.i(TAG, "Firestore write verified: successfully wrote to debug/firebase_test")
            Result.success("Firestore connected (Project: bokaro-services). Document 'debug/firebase_test' created successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Firestore connection test failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // AUTHENTICATION
    // ==========================================

    suspend fun signInWithEmailPassword(email: String, password: String): User {
        val trimmedEmail = email.trim().lowercase()
        return try {
            val authResult = try {
                auth.signInWithEmailAndPassword(trimmedEmail, password).awaitTask()
            } catch (authEx: Exception) {
                when (authEx) {
                    is FirebaseAuthInvalidUserException -> {
                        throw Exception("No account found with $trimmedEmail. Please register a new account.")
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        throw Exception("Invalid email or password. Please check your credentials.")
                    }
                    else -> throw authEx
                }
            }

            val uid = authResult.user?.uid ?: throw Exception("Authentication returned no user")
            val user = getUserFromFirestore(uid)
            if (user != null) {
                user
            } else {
                // Check by email in case record exists under initial profile seed
                val userByEmail = getUserByEmailFromFirestore(trimmedEmail)
                if (userByEmail != null) {
                    val syncedUser = userByEmail.copy(id = uid)
                    saveUserToFirestore(syncedUser)
                    syncedUser
                } else {
                    throw Exception("User profile not found for this account. Please register with your desired role.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmailPassword failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun registerWithEmailPassword(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole,
        area: String,
        address: String,
        serviceCategory: String?
    ): User {
        val trimmedEmail = email.trim().lowercase()
        return try {
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).awaitTask()
            val uid = authResult.user?.uid ?: throw Exception("User registration returned no UID")

            val newUser = User(
                id = uid,
                name = name.trim().ifEmpty { "User" },
                email = trimmedEmail,
                phone = phone.trim().ifEmpty { "9800000000" },
                role = role,
                area = area.ifEmpty { "Sector 4, Bokaro" },
                address = address.ifEmpty { "Bokaro Steel City" },
                serviceCategory = if (role == UserRole.PROVIDER) serviceCategory ?: ServiceType.ELECTRICIAN.name else null,
                experienceYears = if (role == UserRole.PROVIDER) 3 else 0,
                rating = 5.0,
                reviewCount = 0,
                hourlyRate = 199.0,
                isAvailable = true,
                isVerified = true,
                createdAt = System.currentTimeMillis()
            )
            saveUserToFirestore(newUser)
            newUser
        } catch (e: Exception) {
            Log.e(TAG, "registerWithEmailPassword failed: ${e.message}", e)
            throw e
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "signOut error: ${e.message}")
        }
    }

    // ==========================================
    // CLOUD FIRESTORE - REALTIME STREAMS
    // ==========================================

    fun observeUsers(): Flow<List<User>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COLLECTION_USERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "observeUsers snapshot note: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val users = snapshot.documents.mapNotNull { doc ->
                            try {
                                val map = doc.data ?: return@mapNotNull null
                                val userMap = map.toMutableMap()
                                if (!userMap.containsKey("id") || (userMap["id"] as? String).isNullOrEmpty()) {
                                    userMap["id"] = doc.id
                                }
                                User.fromMap(userMap)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing user doc ${doc.id}: ${e.message}")
                                null
                            }
                        }
                        trySend(users)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "observeUsers attach notice: ${e.message}")
        }
        awaitClose {
            listener?.remove()
        }
    }

    fun observeBookings(): Flow<List<Booking>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COLLECTION_BOOKINGS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "observeBookings snapshot note: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val bookings = snapshot.documents.mapNotNull { doc ->
                            try {
                                val map = doc.data ?: return@mapNotNull null
                                val bookingMap = map.toMutableMap()
                                if (!bookingMap.containsKey("id") || (bookingMap["id"] as? String).isNullOrEmpty()) {
                                    bookingMap["id"] = doc.id
                                }
                                Booking.fromMap(bookingMap)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing booking doc ${doc.id}: ${e.message}")
                                null
                            }
                        }.sortedByDescending { it.createdAt }
                        trySend(bookings)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "observeBookings attach notice: ${e.message}")
        }
        awaitClose {
            listener?.remove()
        }
    }

    // ==========================================
    // CLOUD FIRESTORE - CRUD OPERATIONS
    // ==========================================

    suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection(COLLECTION_USERS)
                .document(user.id)
                .set(user.toMap(), SetOptions.merge())
                .awaitTask()
            Log.i(TAG, "Successfully wrote user ${user.name} (${user.id}) to Firestore '$COLLECTION_USERS'")
        } catch (e: Exception) {
            Log.e(TAG, "saveUserToFirestore error for ${user.id}: ${e.message}", e)
            throw e
        }
    }

    suspend fun getUserFromFirestore(userId: String): User? {
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().awaitTask()
            if (doc.exists()) {
                val map = doc.data?.toMutableMap() ?: return null
                if (!map.containsKey("id")) map["id"] = doc.id
                User.fromMap(map)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "getUserFromFirestore notice: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmailFromFirestore(email: String): User? {
        return try {
            val query = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .awaitTask()
            val doc = query.documents.firstOrNull()
            if (doc != null && doc.exists()) {
                val map = doc.data?.toMutableMap() ?: return null
                if (!map.containsKey("id")) map["id"] = doc.id
                User.fromMap(map)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "getUserByEmailFromFirestore notice: ${e.message}")
            null
        }
    }

    suspend fun saveBookingToFirestore(booking: Booking) {
        val user = auth.currentUser
        if (user == null) {
            throw IllegalStateException("FirebaseAuth.currentUser is null. User must be signed in with Firebase Authentication before writing to Cloud Firestore.")
        }
        try {
            firestore.collection(COLLECTION_BOOKINGS)
                .document(booking.id)
                .set(booking.toMap(), SetOptions.merge())
                .awaitTask()
            Log.i(TAG, "Successfully committed booking #${booking.id} to Firestore collection '$COLLECTION_BOOKINGS' (project: ${firestore.app.options.projectId}, uid: ${user.uid})")
        } catch (e: Exception) {
            Log.e(TAG, "saveBookingToFirestore error for #${booking.id}: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateBookingStatusInFirestore(bookingId: String, status: BookingStatus) {
        try {
            val updates = mapOf(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .awaitTask()
            Log.i(TAG, "Successfully updated booking #$bookingId status to ${status.name} in Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "updateBookingStatusInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun assignProviderInFirestore(bookingId: String, provider: User) {
        try {
            val updates = mapOf(
                "providerId" to provider.id,
                "providerName" to provider.name,
                "providerPhone" to provider.phone,
                "status" to BookingStatus.ACCEPTED.name,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .awaitTask()
            Log.i(TAG, "Successfully assigned provider ${provider.name} (${provider.id}) to booking #$bookingId in Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "assignProviderInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun rejectBookingInFirestore(bookingId: String, reason: String) {
        try {
            val updates = mapOf(
                "status" to BookingStatus.REJECTED.name,
                "rejectionReason" to reason,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .awaitTask()
            Log.i(TAG, "Successfully rejected booking #$bookingId in Firestore with reason: $reason")
        } catch (e: Exception) {
            Log.e(TAG, "rejectBookingInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateJobPartsBillInFirestore(bookingId: String, extraParts: Double, partsDesc: String, total: Double) {
        try {
            val updates = mapOf(
                "extraPartsCost" to extraParts,
                "partsDescription" to partsDesc,
                "totalAmount" to total,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .awaitTask()
            Log.i(TAG, "Successfully updated parts bill for #$bookingId in Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "updateJobPartsBillInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun updatePaymentInFirestore(bookingId: String, paymentStatus: PaymentStatus, method: String) {
        try {
            val updates = mapOf(
                "paymentStatus" to paymentStatus.name,
                "paymentMethod" to method,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .awaitTask()
            Log.i(TAG, "Successfully updated payment for #$bookingId in Firestore: $paymentStatus ($method)")
        } catch (e: Exception) {
            Log.e(TAG, "updatePaymentInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun submitRatingInFirestore(bookingId: String, rating: Float, reviewText: String, tags: String, providerId: String?) {
        try {
            val updates = mapOf(
                "rating" to rating,
                "reviewText" to reviewText,
                "feedbackTags" to tags,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_BOOKINGS)
                .document(bookingId)
                .update(updates)
                .awaitTask()

            // Save to dedicated ratings collection
            val ratingDoc = mapOf(
                "bookingId" to bookingId,
                "providerId" to (providerId ?: ""),
                "rating" to rating,
                "reviewText" to reviewText,
                "tags" to tags,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_RATINGS).document(bookingId).set(ratingDoc, SetOptions.merge()).awaitTask()
            Log.i(TAG, "Successfully wrote rating for #$bookingId to Firestore '$COLLECTION_RATINGS'")

            // Update Provider rating in Firestore if providerId exists
            if (!providerId.isNullOrEmpty()) {
                val providerDoc = firestore.collection(COLLECTION_USERS).document(providerId).get().awaitTask()
                if (providerDoc.exists()) {
                    val currentRating = (providerDoc.getDouble("rating") ?: 4.8)
                    val currentCount = (providerDoc.getLong("reviewCount") ?: 10L).toInt()
                    val newCount = currentCount + 1
                    val newRating = ((currentRating * currentCount) + rating) / newCount
                    firestore.collection(COLLECTION_USERS).document(providerId).update(
                        mapOf(
                            "rating" to String.format(java.util.Locale.US, "%.1f", newRating).toDouble(),
                            "reviewCount" to newCount
                        )
                    ).awaitTask()
                    Log.i(TAG, "Successfully updated rating for provider $providerId in Firestore")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitRatingInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun setProviderAvailabilityInFirestore(providerId: String, isAvailable: Boolean) {
        try {
            firestore.collection(COLLECTION_USERS)
                .document(providerId)
                .update("isAvailable", isAvailable)
                .awaitTask()
            Log.i(TAG, "Successfully updated availability for provider $providerId to $isAvailable")
        } catch (e: Exception) {
            Log.e(TAG, "setProviderAvailabilityInFirestore error: ${e.message}", e)
            throw e
        }
    }

    suspend fun seedProductionFirestoreIfEmpty(initialUsers: List<User>, initialBookings: List<Booking>) {
        try {
            val userSnapshot = firestore.collection(COLLECTION_USERS).limit(1).get().awaitTask()
            if (userSnapshot.isEmpty) {
                Log.d(TAG, "Seeding Firestore with verified Bokaro providers and services...")
                for (user in initialUsers) {
                    firestore.collection(COLLECTION_USERS).document(user.id).set(user.toMap(), SetOptions.merge())
                }
                for (booking in initialBookings) {
                    firestore.collection(COLLECTION_BOOKINGS).document(booking.id).set(booking.toMap(), SetOptions.merge())
                }

                // Also seed Service Catalog
                for (cat in ServiceCatalog.categories) {
                    val catMap = mapOf(
                        "type" to cat.type.name,
                        "titleEn" to cat.titleEn,
                        "titleHi" to cat.titleHi,
                        "shortDescEn" to cat.shortDescEn,
                        "startingPrice" to cat.startingPrice,
                        "subServices" to cat.subServices.map { sub ->
                            mapOf(
                                "id" to sub.id,
                                "nameEn" to sub.nameEn,
                                "nameHi" to sub.nameHi,
                                "basePrice" to sub.basePrice,
                                "estimatedDuration" to sub.estimatedDuration,
                                "descriptionEn" to sub.descriptionEn
                            )
                        }
                    )
                    firestore.collection(COLLECTION_SERVICES).document(cat.type.name).set(catMap, SetOptions.merge())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "seedProductionFirestoreIfEmpty non-blocking notice: ${e.message}")
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

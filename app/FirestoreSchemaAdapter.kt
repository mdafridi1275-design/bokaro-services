package com.example.data.firebase

import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ServiceCatalog
import com.example.data.model.User
import com.example.data.model.UserRole

/**
 * Cloud Firestore & Firebase Auth Connection Adapter
 * Project: "bokaro-services"
 *
 * Provides typed mapping, collections and security rules for:
 * - /users/{userId}
 * - /bookings/{bookingId}
 * - /services/{serviceType}
 * - /ratings/{ratingId}
 */
object FirestoreSchemaAdapter {

    const val PROJECT_ID = "bokaro-services"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_BOOKINGS = "bookings"
    const val COLLECTION_SERVICES = "services"
    const val COLLECTION_RATINGS = "ratings"

    fun userToFirestoreDocument(user: User): Map<String, Any?> = user.toMap()
    fun firestoreDocumentToUser(doc: Map<String, Any?>): User = User.fromMap(doc)

    fun bookingToFirestoreDocument(booking: Booking): Map<String, Any?> = booking.toMap()
    fun firestoreDocumentToBooking(doc: Map<String, Any?>): Booking = Booking.fromMap(doc)

    /**
     * Production Security Rules for "bokaro-services" Firestore
     */
    val FIRESTORE_SECURITY_RULES = """
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            // Helper functions
            function isSignedIn() {
              return request.auth != null;
            }
            function isOwner(userId) {
              return isSignedIn() && request.auth.uid == userId;
            }
            function isAdmin() {
              return isSignedIn() && (
                request.auth.token.role == 'ADMIN' ||
                get(/databases/${'$'}(database)/documents/users/${'$'}(request.auth.uid)).data.role == 'ADMIN'
              );
            }
            function isProvider() {
              return isSignedIn() && (
                request.auth.token.role == 'PROVIDER' ||
                get(/databases/${'$'}(database)/documents/users/${'$'}(request.auth.uid)).data.role == 'PROVIDER'
              );
            }

            // User Profiles
            match /users/{userId} {
              allow read: if isSignedIn();
              allow create: if isSignedIn();
              allow update: if isOwner(userId) || isAdmin() || isProvider();
              allow delete: if isAdmin();
            }

            // Bookings Collection
            match /bookings/{bookingId} {
              allow read: if isSignedIn();
              allow create: if isSignedIn();
              allow update: if isSignedIn() && (
                resource.data.customerId == request.auth.uid ||
                resource.data.providerId == request.auth.uid ||
                isAdmin()
              );
              allow delete: if isAdmin();
            }

            // Services & Pricing Directory
            match /services/{serviceId} {
              allow read: if true;
              allow write: if isAdmin();
            }

            // Ratings & Reviews
            match /ratings/{ratingId} {
              allow read: if true;
              allow create, update: if isSignedIn();
              allow delete: if isAdmin();
            }
          }
        }
    """.trimIndent()
}


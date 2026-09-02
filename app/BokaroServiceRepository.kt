package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.data.local.BookingDao
import com.example.data.local.UserDao
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ServiceType
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BokaroServiceRepository(
    private val userDao: UserDao,
    private val bookingDao: BookingDao,
    private val firebaseManager: FirebaseManager
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()

    fun getCustomers(): Flow<List<User>> = userDao.getUsersByRole(UserRole.CUSTOMER)
    fun getProviders(): Flow<List<User>> = userDao.getUsersByRole(UserRole.PROVIDER)
    fun getProvidersByCategory(category: String): Flow<List<User>> = userDao.getProvidersByCategory(category)

    fun getBookingsByCustomer(customerId: String): Flow<List<Booking>> =
        bookingDao.getBookingsByCustomer(customerId)

    fun getBookingsByProvider(providerId: String): Flow<List<Booking>> =
        bookingDao.getBookingsByProvider(providerId)

    /**
     * Starts background real-time synchronization between Cloud Firestore and local Room cache
     */
    fun startFirestoreSync(scope: CoroutineScope) {
        // Synchronize Users from Firestore
        scope.launch(Dispatchers.IO) {
            try {
                firebaseManager.observeUsers().collectLatest { firestoreUsers ->
                    if (firestoreUsers.isNotEmpty()) {
                        userDao.insertUsers(firestoreUsers)
                    }
                }
            } catch (e: Exception) {
                // Background sync resilience
            }
        }

        // Synchronize Bookings from Firestore
        scope.launch(Dispatchers.IO) {
            try {
                firebaseManager.observeBookings().collectLatest { firestoreBookings ->
                    if (firestoreBookings.isNotEmpty()) {
                        bookingDao.insertBookings(firestoreBookings)
                    }
                }
            } catch (e: Exception) {
                // Background sync resilience
            }
        }
    }

    suspend fun loginWithEmailPassword(email: String, password: String): User = withContext(Dispatchers.IO) {
        val user = firebaseManager.signInWithEmailPassword(email, password)
        userDao.insertUser(user)
        user
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
    ): User = withContext(Dispatchers.IO) {
        val newUser = firebaseManager.registerWithEmailPassword(
            name = name,
            email = email,
            password = password,
            phone = phone,
            role = role,
            area = area,
            address = address,
            serviceCategory = serviceCategory
        )
        userDao.insertUser(newUser)
        newUser
    }

    fun signOut() {
        firebaseManager.signOut()
    }

    suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        val local = userDao.getUserById(id)
        if (local != null) return@withContext local
        val cloudUser = firebaseManager.getUserFromFirestore(id)
        if (cloudUser != null) {
            userDao.insertUser(cloudUser)
        }
        cloudUser
    }

    suspend fun getUserByEmailOrPhone(query: String): User? = withContext(Dispatchers.IO) {
        val local = userDao.getUserByEmail(query) ?: userDao.getUserByPhone(query)
        if (local != null) return@withContext local
        val cloudUser = firebaseManager.getUserByEmailFromFirestore(query)
        if (cloudUser != null) {
            userDao.insertUser(cloudUser)
        }
        cloudUser
    }

    suspend fun registerUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
        firebaseManager.saveUserToFirestore(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        firebaseManager.saveUserToFirestore(user)
    }

    suspend fun setProviderAvailability(providerId: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        userDao.updateAvailability(providerId, isAvailable)
        firebaseManager.setProviderAvailabilityInFirestore(providerId, isAvailable)
    }

    suspend fun createBooking(booking: Booking) = withContext(Dispatchers.IO) {
        // Direct Cloud Firestore write to bokaro-services (no fallback)
        firebaseManager.saveBookingToFirestore(booking)
        // Cache in local database only after Firestore write succeeds
        bookingDao.insertBooking(booking)
    }

    suspend fun testFirebaseConnection(): Result<String> = withContext(Dispatchers.IO) {
        firebaseManager.testFirebaseConnection()
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) = withContext(Dispatchers.IO) {
        bookingDao.updateBookingStatus(bookingId, status)
        firebaseManager.updateBookingStatusInFirestore(bookingId, status)
    }

    suspend fun assignProvider(bookingId: String, provider: User) = withContext(Dispatchers.IO) {
        bookingDao.assignProvider(
            bookingId = bookingId,
            providerId = provider.id,
            providerName = provider.name,
            providerPhone = provider.phone,
            status = BookingStatus.ACCEPTED
        )
        firebaseManager.assignProviderInFirestore(bookingId, provider)
    }

    suspend fun rejectBooking(bookingId: String, reason: String) = withContext(Dispatchers.IO) {
        bookingDao.rejectBooking(bookingId, reason)
        firebaseManager.rejectBookingInFirestore(bookingId, reason)
    }

    suspend fun updateJobPartsBill(bookingId: String, extraParts: Double, partsDesc: String, basePrice: Double) = withContext(Dispatchers.IO) {
        val total = basePrice + extraParts
        bookingDao.updateJobPartsBill(bookingId, extraParts, partsDesc, total)
        firebaseManager.updateJobPartsBillInFirestore(bookingId, extraParts, partsDesc, total)
    }

    suspend fun updatePayment(bookingId: String, paymentStatus: PaymentStatus, method: String) = withContext(Dispatchers.IO) {
        bookingDao.updatePayment(bookingId, paymentStatus, method)
        firebaseManager.updatePaymentInFirestore(bookingId, paymentStatus, method)
    }

    suspend fun submitRating(bookingId: String, rating: Float, reviewText: String, tags: String, providerId: String?) = withContext(Dispatchers.IO) {
        bookingDao.submitRating(bookingId, rating, reviewText, tags)
        firebaseManager.submitRatingInFirestore(bookingId, rating, reviewText, tags, providerId)
    }

    suspend fun getBookingById(bookingId: String): Booking? = withContext(Dispatchers.IO) {
        bookingDao.getBookingById(bookingId)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingAdmin = userDao.getUserById("admin_1")
        if (existingAdmin == null) {
            // Seed Initial Verified Bokaro Providers, Customers, and Services
            val initialUsers = getInitialVerifiedUsers()
            userDao.insertUsers(initialUsers)

            // Seed verified providers to Firestore if database is empty
            firebaseManager.seedProductionFirestoreIfEmpty(initialUsers, emptyList())
        }
    }

    fun getInitialVerifiedUsers(): List<User> {
        return listOf(
            User(
                id = "cust_1",
                name = "Rahul Kumar",
                email = "rahul.bokaro@gmail.com",
                phone = "9876543210",
                role = UserRole.CUSTOMER,
                area = "Sector 4 (City Centre), Bokaro",
                address = "Quarter 2045, Sector 4-D, Bokaro",
                createdAt = System.currentTimeMillis() - 86400000 * 3
            ),
            User(
                id = "cust_2",
                name = "Priya Sharma",
                email = "priya.bokaro@gmail.com",
                phone = "9876543211",
                role = UserRole.CUSTOMER,
                area = "Cooperative Colony, Bokaro",
                address = "Plot 12, Cooperative Colony, Bokaro",
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ),
            // Electricians
            User(
                id = "prov_elec_1",
                name = "Vikram Singh",
                email = "vikram.elec@bokaroservices.in",
                phone = "9835112233",
                role = UserRole.PROVIDER,
                serviceCategory = ServiceType.ELECTRICIAN.name,
                area = "Sector 4, Sector 6, Bokaro",
                address = "Sector 4-B Market Shop #14",
                experienceYears = 8,
                rating = 4.9,
                reviewCount = 142,
                hourlyRate = 199.0,
                bio = "Certified Industrial & Domestic Electrician. Specialized in full house wiring, tripping fixes & inverter setup.",
                isAvailable = true,
                isVerified = true
            ),
            User(
                id = "prov_elec_2",
                name = "Amit Pandey",
                email = "amit.elec@bokaroservices.in",
                phone = "9835223344",
                role = UserRole.PROVIDER,
                serviceCategory = ServiceType.ELECTRICIAN.name,
                area = "Chas Main Road, Bokaro",
                address = "Near ITI More, Chas",
                experienceYears = 5,
                rating = 4.7,
                reviewCount = 88,
                hourlyRate = 179.0,
                bio = "Fast response electrician for switchboard replacements, ceiling fan fittings, and MCB issues.",
                isAvailable = true,
                isVerified = true
            ),
            // Plumbers
            User(
                id = "prov_plumb_1",
                name = "Ramesh Soren",
                email = "ramesh.plumber@bokaroservices.in",
                phone = "9835334455",
                role = UserRole.PROVIDER,
                serviceCategory = ServiceType.PLUMBER.name,
                area = "Sector 1, Sector 2, Sector 3, Bokaro",
                address = "Sector 2-C, Bokaro Steel City",
                experienceYears = 10,
                rating = 4.85,
                reviewCount = 165,
                hourlyRate = 249.0,
                bio = "Expert in plumbing pipe blockages, underground pipeline leakage detection, and sanitary fittings.",
                isAvailable = true,
                isVerified = true
            ),
            User(
                id = "prov_plumb_2",
                name = "Deepak Mahto",
                email = "deepak.plumber@bokaroservices.in",
                phone = "9835445566",
                role = UserRole.PROVIDER,
                serviceCategory = ServiceType.PLUMBER.name,
                area = "Cooperative Colony & Sector 12",
                address = "Bypass Road, Chas",
                experienceYears = 4,
                rating = 4.65,
                reviewCount = 54,
                hourlyRate = 199.0,
                bio = "Water tank cleaning, tap repair, and PVC pipeline fitting specialist.",
                isAvailable = true,
                isVerified = true
            ),
            // AC & Appliance
            User(
                id = "prov_ac_1",
                name = "Md. Aslam Ansari",
                email = "aslam.ac@bokaroservices.in",
                phone = "9835556677",
                role = UserRole.PROVIDER,
                serviceCategory = ServiceType.AC_APPLIANCE.name,
                area = "Entire Bokaro & Chas",
                address = "Shop 5, Sector 4 Main Commercial Hub",
                experienceYears = 12,
                rating = 4.95,
                reviewCount = 210,
                hourlyRate = 349.0,
                bio = "Expert in Inverter Split AC jet cleaning, gas charging, LG/Samsung refrigerator & front-load washing machine repairs.",
                isAvailable = true,
                isVerified = true
            ),
            User(
                id = "prov_ac_2",
                name = "Sunil Verma",
                email = "sunil.ac@bokaroservices.in",
                phone = "9835667788",
                role = UserRole.PROVIDER,
                serviceCategory = ServiceType.AC_APPLIANCE.name,
                area = "Sector 5, Sector 8, Sector 11",
                address = "Sector 8 Market, Bokaro",
                experienceYears = 7,
                rating = 4.8,
                reviewCount = 96,
                hourlyRate = 299.0,
                bio = "AC compressor servicing, gas leak test, and instant geyser coil repair.",
                isAvailable = true,
                isVerified = true
            ),
            // Admin
            User(
                id = "admin_1",
                name = "Sanjay Mishra",
                email = "admin@bokaroservices.in",
                phone = "9835000000",
                role = UserRole.ADMIN,
                area = "City Centre Bokaro HQ",
                address = "Bokaro Services Ops Office, Sector 4 Commercial Complex",
                bio = "Regional Service Operations Manager for Bokaro & Dhanbad zone."
            )
        )
    }

    fun getInitialBookings(): List<Booking> {
        return listOf(
            Booking(
                id = "BK-1001",
                customerId = "cust_1",
                customerName = "Rahul Kumar",
                customerPhone = "9876543210",
                customerAddress = "Quarter 2045, Sector 4-D, Bokaro",
                area = "Sector 4 (City Centre), Bokaro",
                serviceType = ServiceType.AC_APPLIANCE.name,
                subServiceName = "AC Deep Jet Cleaning & Service",
                providerId = "prov_ac_1",
                providerName = "Md. Aslam Ansari",
                providerPhone = "9835556677",
                problemDescription = "Split AC cooling is very low, filters need pressure jet cleaning before summer peak.",
                bookingDate = "Today",
                timeSlot = "Morning (09:00 AM - 12:00 PM)",
                status = BookingStatus.ON_THE_WAY,
                basePrice = 499.0,
                extraPartsCost = 0.0,
                totalAmount = 499.0,
                paymentStatus = PaymentStatus.PENDING,
                createdAt = System.currentTimeMillis() - 3600000 * 2,
                updatedAt = System.currentTimeMillis() - 1800000
            ),
            Booking(
                id = "BK-1002",
                customerId = "cust_2",
                customerName = "Priya Sharma",
                customerPhone = "9876543211",
                customerAddress = "Plot 12, Cooperative Colony, Bokaro",
                area = "Cooperative Colony, Bokaro",
                serviceType = ServiceType.ELECTRICIAN.name,
                subServiceName = "Switchboard & Socket Repair",
                providerId = null,
                providerName = null,
                providerPhone = null,
                problemDescription = "Main drawing room switchboard sparking when turning on the heavy geyser line.",
                bookingDate = "Tomorrow",
                timeSlot = "Afternoon (12:00 PM - 03:00 PM)",
                status = BookingStatus.PENDING,
                basePrice = 149.0,
                extraPartsCost = 0.0,
                totalAmount = 149.0,
                paymentStatus = PaymentStatus.PENDING,
                createdAt = System.currentTimeMillis() - 3600000 * 4,
                updatedAt = System.currentTimeMillis() - 3600000 * 4
            ),
            Booking(
                id = "BK-1003",
                customerId = "cust_1",
                customerName = "Rahul Kumar",
                customerPhone = "9876543210",
                customerAddress = "Quarter 2045, Sector 4-D, Bokaro",
                area = "Sector 4 (City Centre), Bokaro",
                serviceType = ServiceType.PLUMBER.name,
                subServiceName = "Tap / Faucet Leakage Repair",
                providerId = "prov_plumb_1",
                providerName = "Ramesh Soren",
                providerPhone = "9835334455",
                problemDescription = "Kitchen sink tap continuously dripping water. Gasket replacement needed.",
                bookingDate = "Yesterday",
                timeSlot = "Evening (03:00 PM - 06:00 PM)",
                status = BookingStatus.WORK_COMPLETED,
                basePrice = 179.0,
                extraPartsCost = 120.0,
                partsDescription = "Heavy brass ceramic disk washer & Teflon tape",
                totalAmount = 299.0,
                paymentStatus = PaymentStatus.PAID_UPI,
                paymentMethod = "PhonePe / UPI",
                rating = 5.0f,
                reviewText = "Ramesh ji arrived promptly on time with all tools. Fixed the kitchen leak in 20 minutes cleanly!",
                feedbackTags = "On Time, Professional, Fair Pricing",
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis() - 86400000 + 7200000
            )
        )
    }
}


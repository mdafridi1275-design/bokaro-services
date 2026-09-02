package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseManager
import com.example.data.local.AppDatabase
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ServiceCatalog
import com.example.data.model.ServiceCategoryItem
import com.example.data.model.SubService
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.BokaroServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    AUTH,
    CUSTOMER_HOME,
    CATEGORY_DETAIL,
    BOOKING_FLOW,
    CUSTOMER_BOOKINGS,
    PROVIDER_DASHBOARD,
    ADMIN_DASHBOARD
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseManager = FirebaseManager.getInstance(application)
    private val repository: BokaroServiceRepository

    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val allBookings: StateFlow<List<Booking>>
    val allUsers: StateFlow<List<User>>

    // Booking Creation Draft State
    private val _selectedCategory = MutableStateFlow<ServiceCategoryItem?>(null)
    val selectedCategory: StateFlow<ServiceCategoryItem?> = _selectedCategory.asStateFlow()

    private val _selectedSubService = MutableStateFlow<SubService?>(null)
    val selectedSubService: StateFlow<SubService?> = _selectedSubService.asStateFlow()

    private val _selectedProvider = MutableStateFlow<User?>(null)
    val selectedProvider: StateFlow<User?> = _selectedProvider.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activeBookingDetail = MutableStateFlow<Booking?>(null)
    val activeBookingDetail: StateFlow<Booking?> = _activeBookingDetail.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isSubmittingBooking = MutableStateFlow(false)
    val isSubmittingBooking: StateFlow<Boolean> = _isSubmittingBooking.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _firestoreStatus = MutableStateFlow<String?>(null)
    val firestoreStatus: StateFlow<String?> = _firestoreStatus.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BokaroServiceRepository(db.userDao(), db.bookingDao(), firebaseManager)

        allBookings = repository.allBookings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allUsers = repository.allUsers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Launch background synchronization with Cloud Firestore
        repository.startFirestoreSync(viewModelScope)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            val firebaseUser = firebaseManager.currentAuthUser
            if (firebaseUser != null) {
                // Restore active Firebase authenticated user profile
                val user = repository.getUserById(firebaseUser.uid)
                if (user != null) {
                    _currentUser.value = user
                    _currentScreen.value = when (user.role) {
                        UserRole.CUSTOMER -> AppScreen.CUSTOMER_HOME
                        UserRole.PROVIDER -> AppScreen.PROVIDER_DASHBOARD
                        UserRole.ADMIN -> AppScreen.ADMIN_DASHBOARD
                    }
                } else {
                    _currentUser.value = null
                    _currentScreen.value = AppScreen.AUTH
                }
            } else {
                _currentUser.value = null
                _currentScreen.value = AppScreen.AUTH
            }
        }
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.EN) AppLanguage.HI else AppLanguage.EN
    }

    fun selectCategory(category: ServiceCategoryItem) {
        _selectedCategory.value = category
        _selectedSubService.value = category.subServices.firstOrNull()
        _selectedProvider.value = null
        _currentScreen.value = AppScreen.CATEGORY_DETAIL
    }

    fun selectSubService(subService: SubService) {
        _selectedSubService.value = subService
    }

    fun selectProvider(provider: User?) {
        _selectedProvider.value = provider
    }

    fun proceedToBookingFlow(category: ServiceCategoryItem, subService: SubService, provider: User?) {
        _selectedCategory.value = category
        _selectedSubService.value = subService
        _selectedProvider.value = provider
        _currentScreen.value = AppScreen.BOOKING_FLOW
    }

    fun viewBookingDetail(booking: Booking) {
        _activeBookingDetail.value = booking
        _currentScreen.value = AppScreen.CUSTOMER_BOOKINGS
    }

    fun navigateToBookings() {
        _currentScreen.value = AppScreen.CUSTOMER_BOOKINGS
    }

    fun navigateToHome() {
        val user = _currentUser.value
        if (user == null) {
            _currentScreen.value = AppScreen.AUTH
            return
        }
        _currentScreen.value = when (user.role) {
            UserRole.PROVIDER -> AppScreen.PROVIDER_DASHBOARD
            UserRole.ADMIN -> AppScreen.ADMIN_DASHBOARD
            UserRole.CUSTOMER -> AppScreen.CUSTOMER_HOME
        }
    }

    fun navigateBack() {
        when (_currentScreen.value) {
            AppScreen.BOOKING_FLOW -> _currentScreen.value = AppScreen.CATEGORY_DETAIL
            AppScreen.CATEGORY_DETAIL -> _currentScreen.value = AppScreen.CUSTOMER_HOME
            AppScreen.CUSTOMER_BOOKINGS -> _currentScreen.value = AppScreen.CUSTOMER_HOME
            else -> navigateToHome()
        }
    }

    fun loginWithEmailPassword(email: String, passwordInput: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim().lowercase()
            val password = passwordInput.trim()

            if (trimmedEmail.isEmpty()) {
                showMessage("Please enter your email address")
                return@launch
            }
            if (!trimmedEmail.contains("@")) {
                showMessage("Please enter a valid email address")
                return@launch
            }
            if (password.isEmpty()) {
                showMessage("Please enter your password")
                return@launch
            }

            _isAuthLoading.value = true
            try {
                val user = repository.loginWithEmailPassword(trimmedEmail, password)
                val authUser = firebaseManager.currentAuthUser
                if (authUser == null) {
                    throw IllegalStateException("Firebase Authentication failed to establish active session.")
                }
                _currentUser.value = user
                _currentScreen.value = when (user.role) {
                    UserRole.CUSTOMER -> AppScreen.CUSTOMER_HOME
                    UserRole.PROVIDER -> AppScreen.PROVIDER_DASHBOARD
                    UserRole.ADMIN -> AppScreen.ADMIN_DASHBOARD
                }
                showMessage("Welcome, ${user.name}! Firebase session verified.")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "Invalid email or password"
                android.util.Log.e("AppViewModel", "Firebase Login error: $errorMsg", e)
                showMessage("Login Error: $errorMsg")
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun register(
        name: String,
        phone: String,
        email: String,
        passwordInput: String,
        role: UserRole,
        area: String,
        address: String,
        serviceCategory: String? = null
    ) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val password = passwordInput.trim()

            if (cleanEmail.isEmpty()) {
                showMessage("Please enter an email address")
                return@launch
            }
            if (!cleanEmail.contains("@")) {
                showMessage("Please enter a valid email address (e.g. user@gmail.com)")
                return@launch
            }
            if (password.length < 6) {
                showMessage("Password must be at least 6 characters long")
                return@launch
            }

            _isAuthLoading.value = true
            try {
                val assignedRole = if (role == UserRole.ADMIN) UserRole.CUSTOMER else role
                val registeredUser = repository.registerWithEmailPassword(
                    name = name.trim().ifEmpty { "Bokaro Resident" },
                    email = cleanEmail,
                    password = password,
                    phone = phone.trim().ifEmpty { "9800000000" },
                    role = assignedRole,
                    area = area,
                    address = address,
                    serviceCategory = serviceCategory
                )

                val authUser = firebaseManager.currentAuthUser
                if (authUser == null) {
                    throw IllegalStateException("Firebase Authentication failed to establish active session.")
                }

                _currentUser.value = registeredUser
                _currentScreen.value = when (assignedRole) {
                    UserRole.CUSTOMER -> AppScreen.CUSTOMER_HOME
                    UserRole.PROVIDER -> AppScreen.PROVIDER_DASHBOARD
                    UserRole.ADMIN -> AppScreen.CUSTOMER_HOME
                }
                showMessage("Registered with Firebase Auth! Welcome, ${registeredUser.name}")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "Registration failed"
                android.util.Log.e("AppViewModel", "Firebase Registration error: $errorMsg", e)
                showMessage("Registration Error: $errorMsg")
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun logout() {
        repository.signOut()
        _currentUser.value = null
        _currentScreen.value = AppScreen.AUTH
    }

    fun testFirestoreConnection() {
        viewModelScope.launch {
            _userMessage.value = "Testing Cloud Firestore connection (bokaro-services)..."
            val result = repository.testFirebaseConnection()
            result.onSuccess { msg ->
                _firestoreStatus.value = msg
                _userMessage.value = msg
            }.onFailure { err ->
                val errMsg = "Firestore Connection Error: ${err.localizedMessage ?: err.message}"
                _firestoreStatus.value = errMsg
                _userMessage.value = errMsg
            }
        }
    }

    fun createBooking(
        area: String,
        address: String,
        problemDescription: String,
        date: String,
        timeSlot: String
    ) {
        val authUser = firebaseManager.currentAuthUser
        val user = _currentUser.value
        if (authUser == null || user == null) {
            _userMessage.value = "You must be signed in with Firebase Authentication before booking a service."
            _currentScreen.value = AppScreen.AUTH
            return
        }

        val category = _selectedCategory.value ?: ServiceCatalog.categories.first()
        val subService = _selectedSubService.value ?: category.subServices.first()
        val provider = _selectedProvider.value

        val uniqueSuffix = (System.currentTimeMillis() % 1000000).toString().padStart(6, '0')
        val newBooking = Booking(
            id = "BK-$uniqueSuffix",
            customerId = authUser.uid,
            customerName = user.name,
            customerPhone = user.phone,
            customerAddress = address.ifEmpty { user.address.ifEmpty { "Bokaro Steel City" } },
            area = area,
            serviceType = category.type.name,
            subServiceName = subService.nameEn,
            providerId = provider?.id,
            providerName = provider?.name,
            providerPhone = provider?.phone,
            problemDescription = problemDescription,
            bookingDate = date,
            timeSlot = timeSlot,
            status = if (provider != null) BookingStatus.ACCEPTED else BookingStatus.PENDING,
            basePrice = subService.basePrice,
            extraPartsCost = 0.0,
            totalAmount = subService.basePrice,
            paymentStatus = PaymentStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _isSubmittingBooking.value = true
            try {
                // Perform real write to Cloud Firestore bookings/{bookingId}
                repository.createBooking(newBooking)
                _activeBookingDetail.value = newBooking
                _currentScreen.value = AppScreen.CUSTOMER_BOOKINGS
                showMessage("Booking #${newBooking.id} created & saved to Cloud Firestore!")
            } catch (e: Exception) {
                val errorMsg = "Firestore Write Error: ${e.localizedMessage ?: e.message ?: e.toString()}"
                android.util.Log.e("AppViewModel", "Booking write to Cloud Firestore failed: $errorMsg", e)
                showMessage(errorMsg)
                // DO NOT proceed to Customer Bookings on error — user must see error
            } finally {
                _isSubmittingBooking.value = false
            }
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        viewModelScope.launch {
            try {
                repository.updateBookingStatus(bookingId, newStatus)
                showMessage("Status updated in Firestore: ${newStatus.labelEn}")
            } catch (e: Exception) {
                showMessage("Status update error: ${e.localizedMessage}")
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun assignProvider(bookingId: String, provider: User) {
        viewModelScope.launch {
            try {
                repository.assignProvider(bookingId, provider)
                showMessage("Assigned ${provider.name} in Firestore (#$bookingId)")
            } catch (e: Exception) {
                showMessage("Assign error: ${e.localizedMessage}")
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun acceptBookingAsProvider(bookingId: String) {
        val user = _currentUser.value ?: return
        val authUser = firebaseManager.currentAuthUser
        val providerUser = if (authUser != null && user.id != authUser.uid) {
            user.copy(id = authUser.uid)
        } else {
            user
        }
        viewModelScope.launch {
            try {
                repository.assignProvider(bookingId, providerUser)
                showMessage("Booking #$bookingId accepted & assigned in Firestore!")
            } catch (e: Exception) {
                val errorMsg = "Accept Booking Error: ${e.localizedMessage ?: e.message}"
                android.util.Log.e("AppViewModel", errorMsg, e)
                showMessage(errorMsg)
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun rejectBookingAsProvider(bookingId: String, reason: String = "Provider unavailable / slots full") {
        viewModelScope.launch {
            try {
                repository.rejectBooking(bookingId, reason)
                showMessage("Booking #$bookingId rejected: $reason")
            } catch (e: Exception) {
                val errorMsg = "Reject Booking Error: ${e.localizedMessage ?: e.message}"
                android.util.Log.e("AppViewModel", errorMsg, e)
                showMessage(errorMsg)
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun addPartsBill(bookingId: String, extraParts: Double, partsDesc: String, basePrice: Double) {
        viewModelScope.launch {
            try {
                repository.updateJobPartsBill(bookingId, extraParts, partsDesc, basePrice)
                showMessage("Bill updated in Firestore: ₹${extraParts.toInt()} extra parts")
            } catch (e: Exception) {
                showMessage("Parts bill update error: ${e.localizedMessage}")
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun processPayment(bookingId: String, paymentStatus: PaymentStatus, method: String) {
        viewModelScope.launch {
            try {
                repository.updatePayment(bookingId, paymentStatus, method)
                showMessage("Payment synced: $method ($paymentStatus)")
            } catch (e: Exception) {
                showMessage("Payment sync error: ${e.localizedMessage}")
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun submitRating(bookingId: String, rating: Float, reviewText: String, tags: String) {
        viewModelScope.launch {
            try {
                val booking = repository.getBookingById(bookingId)
                repository.submitRating(bookingId, rating, reviewText, tags, booking?.providerId)
                showMessage("Thank you! Rating $rating ★ recorded in Firestore")
            } catch (e: Exception) {
                showMessage("Rating sync error: ${e.localizedMessage}")
            }
            if (_activeBookingDetail.value?.id == bookingId) {
                _activeBookingDetail.value = repository.getBookingById(bookingId)
            }
        }
    }

    fun toggleProviderAvailability(providerId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.setProviderAvailability(providerId, isAvailable)
            val updatedUser = repository.getUserById(providerId)
            if (_currentUser.value?.id == providerId) {
                _currentUser.value = updatedUser
            }
            showMessage("Status: ${if (isAvailable) "ONLINE" else "OFFLINE"}")
        }
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}


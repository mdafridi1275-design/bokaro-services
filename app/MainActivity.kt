package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookingFlowScreen
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.CustomerBookingsScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.ProviderDashboardScreen
import com.example.ui.state.AppScreen
import com.example.ui.state.AppViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BokaroServicesApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BokaroServicesApp(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSubService by viewModel.selectedSubService.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val activeBookingDetail by viewModel.activeBookingDetail.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val firestoreStatus by viewModel.firestoreStatus.collectAsStateWithLifecycle()
    val isSubmittingBooking by viewModel.isSubmittingBooking.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenNavigation"
            ) { screen ->
                // Navigation Role Guard
                if (currentUser == null && screen != AppScreen.AUTH) {
                    LaunchedEffect(Unit) {
                        viewModel.navigateToHome()
                    }
                }

                when (screen) {
                    AppScreen.AUTH -> {
                        AuthScreen(
                            language = language,
                            isLoading = isAuthLoading,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onLogin = { email, password -> viewModel.loginWithEmailPassword(email, password) },
                            onRegister = { name, phone, email, password, role, area, address, category ->
                                viewModel.register(name, phone, email, password, role, area, address, category)
                            }
                        )
                    }

                    AppScreen.CUSTOMER_HOME -> {
                        if (currentUser?.role != com.example.data.model.UserRole.CUSTOMER) {
                            LaunchedEffect(currentUser) {
                                viewModel.navigateToHome()
                            }
                        } else {
                            CustomerHomeScreen(
                                currentUser = currentUser,
                                language = language,
                                bookings = allBookings,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onLogout = { viewModel.logout() },
                                onSelectCategory = { cat -> viewModel.selectCategory(cat) },
                                onNavigateToBookings = { viewModel.navigateToBookings() },
                                onTrackBooking = { booking -> viewModel.viewBookingDetail(booking) }
                            )
                        }
                    }

                    AppScreen.CATEGORY_DETAIL -> {
                        if (currentUser?.role != com.example.data.model.UserRole.CUSTOMER) {
                            LaunchedEffect(currentUser) {
                                viewModel.navigateToHome()
                            }
                        } else {
                            selectedCategory?.let { category ->
                                CategoryDetailScreen(
                                    category = category,
                                    subService = selectedSubService,
                                    selectedProvider = selectedProvider,
                                    allProviders = allUsers,
                                    currentUser = currentUser,
                                    language = language,
                                    onToggleLanguage = { viewModel.toggleLanguage() },
                                    onLogout = { viewModel.logout() },
                                    onSelectSubService = { sub -> viewModel.selectSubService(sub) },
                                    onSelectProvider = { prov -> viewModel.selectProvider(prov) },
                                    onProceedToBooking = { cat, sub, prov ->
                                        viewModel.proceedToBookingFlow(cat, sub, prov)
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            } ?: run {
                                viewModel.navigateToHome()
                            }
                        }
                    }

                    AppScreen.BOOKING_FLOW -> {
                        if (currentUser?.role != com.example.data.model.UserRole.CUSTOMER) {
                            LaunchedEffect(currentUser) {
                                viewModel.navigateToHome()
                            }
                        } else if (selectedCategory != null && selectedSubService != null) {
                            BookingFlowScreen(
                                category = selectedCategory!!,
                                subService = selectedSubService!!,
                                provider = selectedProvider,
                                currentUser = currentUser,
                                language = language,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onLogout = { viewModel.logout() },
                                onConfirmBooking = { area, address, problem, date, slot ->
                                    viewModel.createBooking(area, address, problem, date, slot)
                                },
                                onBack = { viewModel.navigateBack() },
                                isSubmitting = isSubmittingBooking
                            )
                        } else {
                            viewModel.navigateToHome()
                        }
                    }

                    AppScreen.CUSTOMER_BOOKINGS -> {
                        if (currentUser?.role != com.example.data.model.UserRole.CUSTOMER) {
                            LaunchedEffect(currentUser) {
                                viewModel.navigateToHome()
                            }
                        } else {
                            CustomerBookingsScreen(
                                bookings = allBookings,
                                activeBookingDetail = activeBookingDetail,
                                currentUser = currentUser,
                                language = language,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onLogout = { viewModel.logout() },
                                onSelectBooking = { booking -> viewModel.viewBookingDetail(booking) },
                                onProcessPayment = { bookingId, status, method ->
                                    viewModel.processPayment(bookingId, status, method)
                                },
                                onSubmitRating = { bookingId, rating, review, tags ->
                                    viewModel.submitRating(bookingId, rating, review, tags)
                                },
                                onBackToHome = { viewModel.navigateToHome() }
                            )
                        }
                    }

                    AppScreen.PROVIDER_DASHBOARD -> {
                        if (currentUser?.role != com.example.data.model.UserRole.PROVIDER) {
                            LaunchedEffect(currentUser) {
                                viewModel.navigateToHome()
                            }
                        } else {
                            ProviderDashboardScreen(
                                currentUser = currentUser,
                                bookings = allBookings,
                                language = language,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onLogout = { viewModel.logout() },
                                onAcceptBooking = { bookingId ->
                                    viewModel.acceptBookingAsProvider(bookingId)
                                },
                                onRejectBooking = { bookingId, reason ->
                                    viewModel.rejectBookingAsProvider(bookingId, reason)
                                },
                                onUpdateStatus = { bookingId, status ->
                                    viewModel.updateBookingStatus(bookingId, status)
                                },
                                onSavePartsBill = { bookingId, partsCost, desc, basePrice ->
                                    viewModel.addPartsBill(bookingId, partsCost, desc, basePrice)
                                },
                                onToggleAvailability = { userId, isAvailable ->
                                    viewModel.toggleProviderAvailability(userId, isAvailable)
                                }
                            )
                        }
                    }

                    AppScreen.ADMIN_DASHBOARD -> {
                        if (currentUser?.role != com.example.data.model.UserRole.ADMIN) {
                            LaunchedEffect(currentUser) {
                                viewModel.navigateToHome()
                            }
                        } else {
                            AdminDashboardScreen(
                                currentUser = currentUser,
                                allBookings = allBookings,
                                allUsers = allUsers,
                                language = language,
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onLogout = { viewModel.logout() },
                                onAssignProvider = { bookingId, provider ->
                                    viewModel.assignProvider(bookingId, provider)
                                },
                                onUpdateBookingStatus = { bookingId, status ->
                                    viewModel.updateBookingStatus(bookingId, status)
                                },
                                onTestFirestore = { viewModel.testFirestoreConnection() },
                                firestoreStatus = firestoreStatus
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.BokaroTopBar
import com.example.ui.components.BookingTimelineTracker
import com.example.ui.components.PaymentBadge
import com.example.ui.components.PaymentDialog
import com.example.ui.components.RatingReviewDialog
import com.example.ui.components.StarRatingBar
import com.example.ui.components.StatusBadge
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BokaroBlueDark
import com.example.ui.theme.BokaroBlueLight
import com.example.ui.theme.BokaroBluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CanvasBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CustomerBookingsScreen(
    bookings: List<Booking>,
    activeBookingDetail: Booking?,
    currentUser: User?,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    onSelectBooking: (Booking) -> Unit,
    onProcessPayment: (String, PaymentStatus, String) -> Unit,
    onSubmitRating: (String, Float, String, String) -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Completed

    var paymentDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var ratingDialogBooking by remember { mutableStateOf<Booking?>(null) }

    val userBookings = bookings.filter { it.customerId == currentUser?.id }

    val filteredBookings = when (selectedTab) {
        1 -> userBookings.filter { it.status != BookingStatus.WORK_COMPLETED && it.status != BookingStatus.CANCELLED }
        2 -> userBookings.filter { it.status == BookingStatus.WORK_COMPLETED || it.status == BookingStatus.CANCELLED }
        else -> userBookings
    }

    if (paymentDialogBooking != null) {
        PaymentDialog(
            booking = paymentDialogBooking!!,
            onDismiss = { paymentDialogBooking = null },
            onConfirmPayment = { status, method ->
                onProcessPayment(paymentDialogBooking!!.id, status, method)
                paymentDialogBooking = null
            }
        )
    }

    if (ratingDialogBooking != null) {
        RatingReviewDialog(
            booking = ratingDialogBooking!!,
            onDismiss = { ratingDialogBooking = null },
            onSubmitRating = { rating, review, tags ->
                onSubmitRating(ratingDialogBooking!!.id, rating, review, tags)
                ratingDialogBooking = null
            }
        )
    }

    Scaffold(
        topBar = {
            BokaroTopBar(
                title = AppStrings.get("my_bookings", language),
                currentUser = currentUser,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onLogout = onLogout,
                showBack = true,
                onBack = onBackToHome
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBg)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BokaroBluePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${userBookings.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val activeCount = userBookings.count { it.status != BookingStatus.WORK_COMPLETED && it.status != BookingStatus.CANCELLED }
                        Text("Active ($activeCount)", fontWeight = FontWeight.SemiBold)
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        val completedCount = userBookings.count { it.status == BookingStatus.WORK_COMPLETED }
                        Text("Completed ($completedCount)", fontWeight = FontWeight.SemiBold)
                    }
                )
            }

            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Bookings Found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "You don't have any bookings in this section.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBackToHome,
                            colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary)
                        ) {
                            Text("Book a Home Service")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredBookings) { booking ->
                        CustomerBookingCard(
                            booking = booking,
                            language = language,
                            onPayClick = { paymentDialogBooking = booking },
                            onRateClick = { ratingDialogBooking = booking },
                            onCallProvider = { phone ->
                                Toast.makeText(context, "Dialing Provider: $phone", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBookingCard(
    booking: Booking,
    language: AppLanguage,
    onPayClick: () -> Unit,
    onRateClick: () -> Unit,
    onCallProvider: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .testTag("customer_booking_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Booking #${booking.id}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BokaroBlueDark)
                    )
                    Text(
                        text = booking.subServiceName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Booking Timeline
            BookingTimelineTracker(status = booking.status, rejectionReason = booking.rejectionReason)

            Spacer(modifier = Modifier.height(12.dp))

            // Details Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BokaroBluePrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${booking.area} • ${booking.customerAddress}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.5.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${booking.bookingDate} (${booking.timeSlot})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }

                    if (booking.problemDescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Problem: \"${booking.problemDescription}\"",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                }
            }

            // Assigned Provider Info Card
            if (booking.providerName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BokaroBlueLight.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BokaroBluePrimary,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Technician: ${booking.providerName}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Ph: ${booking.providerPhone ?: "Verified Pro"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                        if (booking.providerPhone != null) {
                            Button(
                                onClick = { onCallProvider(booking.providerPhone) },
                                colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Bill & Payment Breakdown
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total: ₹${booking.totalAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        if (booking.extraPartsCost > 0) {
                            Text(
                                text = " (Incl. ₹${booking.extraPartsCost.toInt()} parts)",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                        }
                    }
                    PaymentBadge(status = booking.paymentStatus)
                }

                if (booking.paymentStatus == PaymentStatus.PENDING) {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Pay Bill / भुगतान", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Rating & Review Section
            if (booking.status == BookingStatus.WORK_COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                if (booking.rating != null) {
                    // Already rated
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFFBEB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StarRatingBar(rating = booking.rating, starSize = 16)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rated ${booking.rating} / 5.0",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                )
                            }
                            if (!booking.feedbackTags.isNullOrEmpty()) {
                                Text(
                                    text = "Tags: ${booking.feedbackTags}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFB45309), fontSize = 10.sp)
                                )
                            }
                            if (!booking.reviewText.isNullOrEmpty()) {
                                Text(
                                    text = "\"${booking.reviewText}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F), fontSize = 11.5.sp)
                                )
                            }
                        }
                    }
                } else {
                    // Not rated yet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "How was the service?",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Rate technician & leave review",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp)
                            )
                        }
                        OutlinedButton(
                            onClick = onRateClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.StarRate, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rate Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

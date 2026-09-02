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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.components.AddPartsBillDialog
import com.example.ui.components.BokaroTopBar
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PaymentBadge
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
fun ProviderDashboardScreen(
    currentUser: User?,
    bookings: List<Booking>,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    onAcceptBooking: (String) -> Unit = {},
    onRejectBooking: (String, String) -> Unit = { _, _ -> },
    onUpdateStatus: (String, BookingStatus) -> Unit,
    onSavePartsBill: (String, Double, String, Double) -> Unit,
    onToggleAvailability: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active Jobs, 1: Pending Requests, 2: Completed Jobs

    var addPartsDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var rejectDialogBooking by remember { mutableStateOf<Booking?>(null) }

    // Filter bookings for provider:
    // 1. Assigned to this provider
    // 2. OR unassigned pending booking matching provider's trade (or all pending if general provider)
    val myBookings = bookings.filter { booking ->
        booking.providerId == currentUser?.id ||
        (booking.providerId.isNullOrEmpty() && (
            currentUser?.serviceCategory.isNullOrEmpty() ||
            booking.serviceType.equals(currentUser?.serviceCategory, ignoreCase = true)
        ))
    }

    val activeJobs = myBookings.filter {
        it.providerId == currentUser?.id &&
        it.status != BookingStatus.WORK_COMPLETED &&
        it.status != BookingStatus.CANCELLED &&
        it.status != BookingStatus.REJECTED
    }

    val pendingRequests = myBookings.filter {
        it.status == BookingStatus.PENDING
    }

    val completedJobs = myBookings.filter {
        it.status == BookingStatus.WORK_COMPLETED
    }

    val totalEarnings = completedJobs.sumOf { it.totalAmount }

    if (addPartsDialogBooking != null) {
        AddPartsBillDialog(
            booking = addPartsDialogBooking!!,
            onDismiss = { addPartsDialogBooking = null },
            onSaveBill = { extraParts, desc ->
                onSavePartsBill(addPartsDialogBooking!!.id, extraParts, desc, addPartsDialogBooking!!.basePrice)
                addPartsDialogBooking = null
            }
        )
    }

    if (rejectDialogBooking != null) {
        RejectBookingDialog(
            booking = rejectDialogBooking!!,
            onDismiss = { rejectDialogBooking = null },
            onConfirmReject = { reason ->
                onRejectBooking(rejectDialogBooking!!.id, reason)
                rejectDialogBooking = null
            }
        )
    }

    Scaffold(
        topBar = {
            BokaroTopBar(
                title = AppStrings.get("provider_dashboard", language),
                currentUser = currentUser,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBg)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Profile & Online/Offline Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BokaroBluePrimary,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.name ?: "Mechanic",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Specialist: ${currentUser?.serviceCategory ?: "General"} • ${currentUser?.area ?: "Bokaro"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.5.sp)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${currentUser?.rating ?: 4.9} (${currentUser?.reviewCount ?: 80}+ reviews)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }

                            // Online / Offline Switch
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (currentUser?.isAvailable != false) "ONLINE" else "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUser?.isAvailable != false) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        fontSize = 11.sp
                                    )
                                )
                                Switch(
                                    checked = currentUser?.isAvailable != false,
                                    onCheckedChange = { isChecked ->
                                        if (currentUser != null) {
                                            onToggleAvailability(currentUser.id, isChecked)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF16A34A)
                                    ),
                                    modifier = Modifier.testTag("provider_availability_switch")
                                )
                            }
                        }
                    }
                }
            }

            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Active Jobs",
                        value = "${activeJobs.size}",
                        subValue = "In progress",
                        icon = Icons.Default.Handyman,
                        iconBgColor = BokaroBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Pending Leads",
                        value = "${pendingRequests.size}",
                        subValue = "Ready to accept",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Earnings",
                        value = "₹${totalEarnings.toInt()}",
                        subValue = "${completedJobs.size} done",
                        icon = Icons.Default.MonetizationOn,
                        iconBgColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tabs for Jobs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = BokaroBluePrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active (${activeJobs.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Pending (${pendingRequests.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("History (${completedJobs.size})", fontWeight = FontWeight.SemiBold) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tab Content
            val currentList = when (selectedTab) {
                0 -> activeJobs
                1 -> pendingRequests
                else -> completedJobs
            }

            if (currentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "No Active Jobs Right Now"
                                    1 -> "No Pending Requests"
                                    else -> "No Completed Jobs Yet"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "New bookings created in Bokaro will appear here from Cloud Firestore in real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(currentList, key = { it.id }) { booking ->
                    ProviderJobCard(
                        booking = booking,
                        currentUser = currentUser,
                        language = language,
                        onAccept = { onAcceptBooking(booking.id) },
                        onReject = { rejectDialogBooking = booking },
                        onUpdateStatus = { status -> onUpdateStatus(booking.id, status) },
                        onAddParts = { addPartsDialogBooking = booking },
                        onCallCustomer = { phone ->
                            Toast.makeText(context, "Calling Customer: $phone", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderJobCard(
    booking: Booking,
    currentUser: User?,
    language: AppLanguage,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onUpdateStatus: (BookingStatus) -> Unit,
    onAddParts: () -> Unit,
    onCallCustomer: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .testTag("provider_job_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BokaroBluePrimary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = booking.serviceType,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BokaroBluePrimary,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Job #${booking.id}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BokaroBlueDark)
                        )
                    }
                    Text(
                        text = booking.subServiceName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer details
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Customer: ${booking.customerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (booking.customerPhone.isNotEmpty()) {
                            Button(
                                onClick = { onCallCustomer(booking.customerPhone) },
                                colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call ${booking.customerPhone}", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BokaroBluePrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${booking.area} • ${booking.customerAddress}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                        )
                    }

                    if (booking.bookingDate.isNotEmpty() || booking.timeSlot.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Slot: ${booking.bookingDate} (${booking.timeSlot})",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    if (booking.problemDescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Problem: \"${booking.problemDescription}\"",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bill Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Amount: ₹${booking.totalAmount.toInt()}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    if (booking.extraPartsCost > 0) {
                        Text(
                            text = "Parts: ₹${booking.extraPartsCost.toInt()} (${booking.partsDescription})",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp)
                        )
                    }
                }

                PaymentBadge(status = booking.paymentStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Status Control Actions (Provider Work Flow)
            when (booking.status) {
                BookingStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("provider_reject_job_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject / अस्वीकार", color = Color(0xFFDC2626), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("provider_accept_job_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Accept Job / स्वीकारें", fontSize = 12.sp)
                        }
                    }
                }

                BookingStatus.ACCEPTED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onUpdateStatus(BookingStatus.ON_THE_WAY) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("provider_on_the_way_button")
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("I am On The Way / रास्ते में")
                        }
                    }
                }

                BookingStatus.ON_THE_WAY -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onUpdateStatus(BookingStatus.WORK_STARTED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E7490)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("provider_work_started_button")
                        ) {
                            Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reached & Start Work / काम शुरू")
                        }
                    }
                }

                BookingStatus.WORK_STARTED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onAddParts,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Extra Parts Bill", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onUpdateStatus(BookingStatus.WORK_COMPLETED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("provider_work_completed_button")
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Work Done", fontSize = 12.sp)
                        }
                    }
                }

                BookingStatus.WORK_COMPLETED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ Job Successfully Completed",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        )
                        if (booking.rating != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Customer Rated ${booking.rating}★", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                BookingStatus.REJECTED -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Booking Rejected",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            )
                            if (!booking.rejectionReason.isNullOrEmpty()) {
                                Text(
                                    text = "Reason: ${booking.rejectionReason}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B), fontSize = 11.5.sp)
                                )
                            }
                        }
                    }
                }

                BookingStatus.CANCELLED -> {
                    Text(
                        text = "Booking Cancelled",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun RejectBookingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirmReject: (String) -> Unit
) {
    val predefinedReasons = listOf(
        "Currently busy in another repair in Bokaro",
        "Out of service coverage area / too far",
        "Slot timing conflict with another appointment",
        "Required tools / spare parts unavailable today",
        "Other reason"
    )

    var selectedReason by remember { mutableStateOf(predefinedReasons[0]) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Booking #${booking.id}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Please select a reason for rejecting this service request:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                predefinedReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = BokaroBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, fontSize = 12.sp)
                    }
                }

                if (selectedReason == "Other reason") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Specify reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Other reason") {
                        customReason.ifEmpty { "Provider unavailable" }
                    } else {
                        selectedReason
                    }
                    onConfirmReject(finalReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Confirm Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

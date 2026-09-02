package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.User
import com.example.data.model.UserRole
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
import com.example.ui.theme.bokaroTextFieldColors

@Composable
fun AdminDashboardScreen(
    currentUser: User?,
    allBookings: List<Booking>,
    allUsers: List<User>,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    onAssignProvider: (String, User) -> Unit,
    onUpdateBookingStatus: (String, BookingStatus) -> Unit,
    onTestFirestore: () -> Unit = {},
    firestoreStatus: String? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Bookings, 2: Customers, 3: Providers, 4: Payments
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<BookingStatus?>(null) }

    var selectedBookingForDetail by remember { mutableStateOf<Booking?>(null) }
    var assignDialogBooking by remember { mutableStateOf<Booking?>(null) }

    val customers = allUsers.filter { it.role == UserRole.CUSTOMER }
    val providers = allUsers.filter { it.role == UserRole.PROVIDER }

    // Summary Calculations
    val totalBookingsCount = allBookings.size
    val pendingCount = allBookings.count { it.status == BookingStatus.PENDING }
    val acceptedCount = allBookings.count { it.status == BookingStatus.ACCEPTED }
    val onTheWayCount = allBookings.count { it.status == BookingStatus.ON_THE_WAY }
    val workStartedCount = allBookings.count { it.status == BookingStatus.WORK_STARTED }
    val completedCount = allBookings.count { it.status == BookingStatus.WORK_COMPLETED }
    val rejectedCount = allBookings.count { it.status == BookingStatus.REJECTED }
    val cancelledCount = allBookings.count { it.status == BookingStatus.CANCELLED }

    val totalGrossVolume = allBookings.sumOf { it.totalAmount }
    val activeInFlightCount = pendingCount + acceptedCount + onTheWayCount + workStartedCount

    // Dialogs
    if (selectedBookingForDetail != null) {
        // Keep updated with live booking list if snapshot changes
        val latestBooking = allBookings.find { it.id == selectedBookingForDetail!!.id } ?: selectedBookingForDetail!!
        AdminBookingDetailDialog(
            booking = latestBooking,
            allProviders = providers,
            onDismiss = { selectedBookingForDetail = null },
            onAssignProvider = { provider ->
                onAssignProvider(latestBooking.id, provider)
            },
            onUpdateStatus = { newStatus ->
                onUpdateBookingStatus(latestBooking.id, newStatus)
            }
        )
    }

    if (assignDialogBooking != null) {
        val currentBooking = assignDialogBooking!!
        AssignProviderDialog(
            booking = currentBooking,
            availableProviders = providers.filter { it.serviceCategory.equals(currentBooking.serviceType, ignoreCase = true) || it.serviceCategory == null },
            onDismiss = { assignDialogBooking = null },
            onAssign = { provider ->
                onAssignProvider(currentBooking.id, provider)
                assignDialogBooking = null
            }
        )
    }

    Scaffold(
        topBar = {
            BokaroTopBar(
                title = AppStrings.get("admin_dashboard", language),
                currentUser = currentUser,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBg)
                .padding(paddingValues)
        ) {
            // Live Status Banner
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Portal • Live Firestore Sync",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Text(
                        text = "Admin: ${currentUser?.name ?: "Owner"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Top Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BokaroBluePrimary,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📊 Overview", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📋 Bookings ($totalBookingsCount)", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("👥 Customers (${customers.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("🔧 Providers (${providers.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("💳 Payments", fontWeight = FontWeight.SemiBold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // ==========================================
                    // 1. DASHBOARD OVERVIEW & SUMMARY
                    // ==========================================
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Column {
                                Text(
                                    text = "Bokaro Services Operations Summary",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Real-time dispatch, customer activity & service provider monitoring",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Core Metric Cards
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetricStatCard(
                                        title = "Total Customers",
                                        value = "${customers.size}",
                                        subValue = "Registered users",
                                        icon = Icons.Default.Group,
                                        iconBgColor = BokaroBluePrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricStatCard(
                                        title = "Total Providers",
                                        value = "${providers.size}",
                                        subValue = "${providers.count { it.isAvailable }} online now",
                                        icon = Icons.Default.Engineering,
                                        iconBgColor = Color(0xFF0284C7),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetricStatCard(
                                        title = "Total Bookings",
                                        value = "$totalBookingsCount",
                                        subValue = "$activeInFlightCount in progress",
                                        icon = Icons.Default.Assignment,
                                        iconBgColor = Color(0xFF6366F1),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricStatCard(
                                        title = "Gross Revenue",
                                        value = "₹${totalGrossVolume.toInt()}",
                                        subValue = "$completedCount completed",
                                        icon = Icons.Default.MonetizationOn,
                                        iconBgColor = Color(0xFF16A34A),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Detailed Lifecycle Breakdown (Requested Summary Metrics)
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Booking Status Lifecycle Funnel",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatusPillCard(
                                            title = "Pending",
                                            count = pendingCount,
                                            color = Color(0xFFF59E0B),
                                            bgColor = Color(0xFFFEF3C7),
                                            icon = Icons.Default.HourglassTop,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPillCard(
                                            title = "Accepted",
                                            count = acceptedCount,
                                            color = Color(0xFF3B82F6),
                                            bgColor = Color(0xFFEFF6FF),
                                            icon = Icons.Default.CheckCircle,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPillCard(
                                            title = "On The Way",
                                            count = onTheWayCount,
                                            color = Color(0xFF8B5CF6),
                                            bgColor = Color(0xFFF5F3FF),
                                            icon = Icons.Default.DirectionsRun,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatusPillCard(
                                            title = "Work Started",
                                            count = workStartedCount,
                                            color = Color(0xFF0284C7),
                                            bgColor = Color(0xFFF0F9FF),
                                            icon = Icons.Default.Build,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPillCard(
                                            title = "Completed",
                                            count = completedCount,
                                            color = Color(0xFF10B981),
                                            bgColor = Color(0xFFECFDF5),
                                            icon = Icons.Default.Verified,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPillCard(
                                            title = "Rejected",
                                            count = rejectedCount,
                                            color = Color(0xFFEF4444),
                                            bgColor = Color(0xFFFEF2F2),
                                            icon = Icons.Default.Cancel,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Firestore Connectivity / Diagnostic Box
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Cloud Firestore Connection",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                                )
                                                Text(
                                                    text = "Real-time sync: users, bookings, ratings",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = onTestFirestore,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("test_firestore_connection_button")
                                        ) {
                                            Text("Test Write", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (firestoreStatus != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = Color.White,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = firestoreStatus,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (firestoreStatus.contains("Error") || firestoreStatus.contains("Failed")) Color(0xFFDC2626) else Color(0xFF166534),
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Unassigned Pending Bookings Quick Alert Box
                        val unassignedPending = allBookings.filter { it.status == BookingStatus.PENDING && it.providerId == null }
                        if (unassignedPending.isNotEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = Color(0xFFB45309))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${unassignedPending.size} Booking(s) Require Provider Assignment",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        unassignedPending.forEach { booking ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${booking.subServiceName} • #${booking.id}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = "${booking.customerName} (${booking.area})",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                                    )
                                                }
                                                Button(
                                                    onClick = { assignDialogBooking = booking },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Assign Provider", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recent System Bookings
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent System Bookings",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "View All (${allBookings.size}) →",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = BokaroBluePrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.clickable { selectedTab = 1 }
                                )
                            }
                        }

                        items(allBookings.take(5)) { booking ->
                            AdminBookingCard(
                                booking = booking,
                                onClick = { selectedBookingForDetail = booking },
                                onAssignClick = { assignDialogBooking = booking }
                            )
                        }
                    }
                }

                1 -> {
                    // ==========================================
                    // 2. BOOKING MANAGEMENT TAB
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by ID, customer, provider, area, trade...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            colors = bokaroTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_booking_search"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = filterStatus == null,
                                    onClick = { filterStatus = null },
                                    label = { Text("All (${allBookings.size})", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BokaroBluePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            BookingStatus.entries.forEach { status ->
                                val count = allBookings.count { it.status == status }
                                item {
                                    FilterChip(
                                        selected = filterStatus == status,
                                        onClick = { filterStatus = if (filterStatus == status) null else status },
                                        label = { Text("${status.labelEn} ($count)", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BokaroBluePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredBookings = allBookings.filter { b ->
                            val query = searchQuery.trim()
                            (query.isEmpty() ||
                                    b.id.contains(query, ignoreCase = true) ||
                                    b.customerName.contains(query, ignoreCase = true) ||
                                    b.customerPhone.contains(query, ignoreCase = true) ||
                                    (b.providerName?.contains(query, ignoreCase = true) == true) ||
                                    (b.providerPhone?.contains(query, ignoreCase = true) == true) ||
                                    b.area.contains(query, ignoreCase = true) ||
                                    b.subServiceName.contains(query, ignoreCase = true) ||
                                    b.serviceType.contains(query, ignoreCase = true)) &&
                                    (filterStatus == null || b.status == filterStatus)
                        }

                        if (filteredBookings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No bookings match your filter criteria", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                items(filteredBookings) { booking ->
                                    AdminBookingCard(
                                        booking = booking,
                                        onClick = { selectedBookingForDetail = booking },
                                        onAssignClick = { assignDialogBooking = booking }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ==========================================
                    // 3. CUSTOMER MANAGEMENT TAB
                    // ==========================================
                    var customerSearch by remember { mutableStateOf("") }
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = customerSearch,
                            onValueChange = { customerSearch = it },
                            placeholder = { Text("Search customers by name, phone, area...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            colors = bokaroTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredCustomers = customers.filter { c ->
                            val q = customerSearch.trim()
                            q.isEmpty() ||
                                    c.name.contains(q, ignoreCase = true) ||
                                    c.phone.contains(q, ignoreCase = true) ||
                                    c.email.contains(q, ignoreCase = true) ||
                                    c.area.contains(q, ignoreCase = true) ||
                                    c.address.contains(q, ignoreCase = true)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Registered Bokaro Customers (${filteredCustomers.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            items(filteredCustomers) { customer ->
                                val userBookings = allBookings.filter { it.customerId == customer.id }
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = BokaroBlueLight,
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = BokaroBluePrimary, modifier = Modifier.size(24.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = customer.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = customer.email,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFEFF6FF)
                                            ) {
                                                Text(
                                                    text = "${userBookings.size} Bookings",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = BokaroBluePrimary,
                                                        fontSize = 11.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = BorderSubtle)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(customer.phone, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${customer.area}, ${customer.address}",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                                        context.startActivity(callIntent)
                                                    } catch (_: Exception) {}
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call Customer", tint = Color(0xFF16A34A))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // ==========================================
                    // 4. PROVIDER MANAGEMENT TAB
                    // ==========================================
                    var providerSearch by remember { mutableStateOf("") }
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = providerSearch,
                            onValueChange = { providerSearch = it },
                            placeholder = { Text("Search providers by name, trade, area, phone...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            colors = bokaroTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredProviders = providers.filter { p ->
                            val q = providerSearch.trim()
                            q.isEmpty() ||
                                    p.name.contains(q, ignoreCase = true) ||
                                    p.phone.contains(q, ignoreCase = true) ||
                                    p.email.contains(q, ignoreCase = true) ||
                                    (p.serviceCategory?.contains(q, ignoreCase = true) == true) ||
                                    p.area.contains(q, ignoreCase = true)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Verified Service Providers (${filteredProviders.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            items(filteredProviders) { provider ->
                                val assignedBookings = allBookings.filter { it.providerId == provider.id }
                                val completedByPro = assignedBookings.count { it.status == BookingStatus.WORK_COMPLETED }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFF0FDF4),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = provider.name,
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                                                }
                                                Text(
                                                    text = "Trade: ${provider.serviceCategory ?: "General Specialist"}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = BokaroBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                                                )
                                                Text(
                                                    text = provider.email,
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (provider.isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = if (provider.isAvailable) "ONLINE" else "OFFLINE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (provider.isAvailable) Color(0xFF15803D) else Color(0xFFB91C1C),
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = BorderSubtle)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(provider.phone, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Coverage: ${provider.area}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                                                    Text(" ${provider.rating} Rating", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                    Text(" • $completedByPro completed jobs", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                                        context.startActivity(callIntent)
                                                    } catch (_: Exception) {}
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call Provider", tint = Color(0xFF16A34A))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // ==========================================
                    // 5. PAYMENTS & REVENUE TAB
                    // ==========================================
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Payments & Revenue Settlements",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Track customer cash payments, online UPI transfers & provider parts billing",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        val paidUPI = allBookings.filter { it.paymentStatus == PaymentStatus.PAID_UPI }
                        val paidCash = allBookings.filter { it.paymentStatus == PaymentStatus.PAID_CASH }
                        val pendingPay = allBookings.filter { it.paymentStatus == PaymentStatus.PENDING }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE0E7FF),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("UPI Online", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3730A3))
                                        Text("₹${paidUPI.sumOf { it.totalAmount }.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF312E81))
                                        Text("${paidUPI.size} jobs", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4338CA))
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Cash on Site", style = MaterialTheme.typography.labelSmall, color = Color(0xFF166534))
                                        Text("₹${paidCash.sumOf { it.totalAmount }.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                        Text("${paidCash.size} jobs", style = MaterialTheme.typography.labelSmall, color = Color(0xFF15803D))
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Pending", style = MaterialTheme.typography.labelSmall, color = Color(0xFF92400E))
                                        Text("₹${pendingPay.sumOf { it.totalAmount }.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                        Text("${pendingPay.size} jobs", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                                    }
                                }
                            }
                        }

                        items(allBookings) { booking ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable { selectedBookingForDetail = booking }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "#${booking.id} • ${booking.subServiceName}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Customer: ${booking.customerName} (${booking.customerPhone})",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                                        )
                                        Text(
                                            text = "Provider: ${booking.providerName ?: "Unassigned"} • Base: ₹${booking.basePrice.toInt()} + Parts: ₹${booking.extraPartsCost.toInt()}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.5.sp)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${booking.totalAmount.toInt()}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                        )
                                        PaymentBadge(status = booking.paymentStatus)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STATUS PILL COMPONENT FOR SUMMARY
// ==========================================
@Composable
fun StatusPillCard(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// ADMIN BOOKING CARD
// ==========================================
@Composable
fun AdminBookingCard(
    booking: Booking,
    onClick: () -> Unit,
    onAssignClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: ID, Service & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Booking #${booking.id}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BokaroBlueDark
                        )
                    )
                    Text(
                        text = "${booking.serviceType} • ${booking.bookingDate} (${booking.timeSlot})",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            // Subservice & Problem
            Text(
                text = booking.subServiceName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            if (booking.problemDescription.isNotBlank()) {
                Text(
                    text = "Problem: \"${booking.problemDescription}\"",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.5.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer & Provider Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Customer", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                        Text(booking.customerName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                        Text(booking.customerPhone, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp))
                        Text(booking.area, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp), maxLines = 1)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (booking.providerId != null) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Provider", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                        Text(
                            text = booking.providerName ?: "Unassigned ⚠️",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (booking.providerId != null) TextPrimary else Color(0xFFB45309)
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = booking.providerPhone ?: "Tap to assign",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp)
                        )
                    }
                }
            }

            // Rejection Note (If Available)
            if (!booking.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rejection Reason: ${booking.rejectionReason}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFB91C1C), fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Rating / Review (If available)
            if (booking.rating != null && booking.rating!! > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                    Text(
                        text = " Rating: ${booking.rating}★ ${booking.reviewText?.let { "- \"$it\"" } ?: ""}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, color = TextPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Financials & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total: ₹${booking.totalAmount.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PaymentBadge(status = booking.paymentStatus)
                    }
                    Text(
                        text = "Base: ₹${booking.basePrice.toInt()} | Extra Parts: ₹${booking.extraPartsCost.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (booking.status == BookingStatus.PENDING || booking.providerId == null) {
                        Button(
                            onClick = onAssignClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (booking.providerId != null) "Reassign" else "Assign", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Details", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. COMPREHENSIVE BOOKING DETAIL DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingDetailDialog(
    booking: Booking,
    allProviders: List<User>,
    onDismiss: () -> Unit,
    onAssignProvider: (User) -> Unit,
    onUpdateStatus: (BookingStatus) -> Unit
) {
    val context = LocalContext.current
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showAssignProSubDialog by remember { mutableStateOf(false) }

    if (showAssignProSubDialog) {
        AssignProviderDialog(
            booking = booking,
            availableProviders = allProviders.filter { it.serviceCategory.equals(booking.serviceType, ignoreCase = true) || it.serviceCategory == null },
            onDismiss = { showAssignProSubDialog = false },
            onAssign = { provider ->
                onAssignProvider(provider)
                showAssignProSubDialog = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(640.dp)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Booking #${booking.id}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BokaroBlueDark)
                        )
                        Text(
                            text = "Created: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(booking.createdAt))}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Status & Payment Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = booking.status)
                    PaymentBadge(status = booking.paymentStatus)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                // Service & Problem Info
                Text("Service & Requirement", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "${booking.serviceType} • ${booking.subServiceName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Schedule: ${booking.bookingDate} (${booking.timeSlot})", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Problem Note:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted))
                        Text(
                            text = if (booking.problemDescription.isNotBlank()) booking.problemDescription else "No specific problem notes entered by customer.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Details Card
                Text("Customer Information", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(booking.customerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Phone: ${booking.customerPhone}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("Address: ${booking.area}, ${booking.customerAddress}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.customerPhone}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Customer", tint = Color(0xFF16A34A))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Provider Details Card
                Text("Assigned Service Specialist", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = if (booking.providerId != null) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = booking.providerName ?: "No Provider Assigned Yet",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (booking.providerId != null) TextPrimary else Color(0xFFB45309)
                                )
                            )
                            Text(
                                text = booking.providerPhone?.let { "Phone: $it" } ?: "Tap Assign Pro button to dispatch",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Row {
                            if (!booking.providerPhone.isNullOrEmpty()) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.providerPhone}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Provider", tint = Color(0xFF16A34A))
                                }
                            }
                            Button(
                                onClick = { showAssignProSubDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(if (booking.providerId != null) "Change" else "Assign", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pricing & Financial Breakdown
                Text("Financial & Parts Billing Breakdown", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Inspection / Labor Charge", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("₹${booking.basePrice.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                        if (booking.extraPartsCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Extra Parts / Material: ${booking.partsDescription}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("₹${booking.extraPartsCost.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("₹${booking.totalAmount.toInt()}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = BokaroBluePrimary))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payment Method: ${booking.paymentMethod} • Status: ${booking.paymentStatus.labelEn}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                }

                // Rejection Reason Details (If Available)
                if (!booking.rejectionReason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Rejection Information", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = booking.rejectionReason ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB91C1C)),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Customer Rating & Review (If Available)
                if (booking.rating != null && booking.rating!! > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Customer Rating & Feedback", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < ((booking.rating ?: 0f).toInt())) AccentGold else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${booking.rating} / 5", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            if (!booking.reviewText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${booking.reviewText}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                // Admin Manual Status Control
                Text("Admin Status Override", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = showStatusDropdown,
                    onExpandedChange = { showStatusDropdown = !showStatusDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = booking.status.labelEn,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Update Current Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown) },
                        colors = bokaroTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        BookingStatus.entries.forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text("${statusOption.labelEn} (${statusOption.labelHi})") },
                                onClick = {
                                    onUpdateStatus(statusOption)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

// ==========================================
// ASSIGN PROVIDER MODAL DIALOG
// ==========================================
@Composable
fun AssignProviderDialog(
    booking: Booking,
    availableProviders: List<User>,
    onDismiss: () -> Unit,
    onAssign: (User) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Assign Specialist for #${booking.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${booking.serviceType} • ${booking.subServiceName} in ${booking.area}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (availableProviders.isEmpty()) {
                    Text(
                        text = "No verified providers found matching category: ${booking.serviceType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDC2626)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableProviders) { provider ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable { onAssign(provider) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = BokaroBlueLight,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Engineering, contentDescription = null, tint = BokaroBluePrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = provider.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "${provider.area} • Ph: ${provider.phone}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                                            Text(text = "${provider.rating} (${provider.reviewCount} jobs)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                        }
                                    }
                                    Button(
                                        onClick = { onAssign(provider) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Assign", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BokaroLocations
import com.example.data.model.ServiceCategoryItem
import com.example.data.model.SubService
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.BokaroTopBar
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.theme.BokaroBlueDark
import com.example.ui.theme.BokaroBlueLight
import com.example.ui.theme.BokaroBluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CanvasBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.bokaroTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    category: ServiceCategoryItem,
    subService: SubService,
    provider: User?,
    currentUser: User?,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    onConfirmBooking: (String, String, String, String, String) -> Unit,
    onBack: () -> Unit,
    isSubmitting: Boolean = false
) {
    var selectedArea by remember { mutableStateOf(currentUser?.area ?: BokaroLocations.areas.first()) }
    var areaDropdownExpanded by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf(currentUser?.address ?: "Quarter 2045, Sector 4-D") }
    var problemDescription by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Today") }
    var selectedTimeSlot by remember { mutableStateOf(BokaroLocations.timeSlots.first()) }

    Scaffold(
        topBar = {
            BokaroTopBar(
                title = if (language == AppLanguage.EN) "Confirm Booking" else "बुकिंग विवरण",
                currentUser = currentUser,
                language = language,
                onToggleLanguage = onToggleLanguage,
                onLogout = onLogout,
                showBack = true,
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pay after service",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "₹${subService.basePrice.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BokaroBluePrimary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            if (!isSubmitting) {
                                onConfirmBooking(
                                    selectedArea,
                                    address,
                                    problemDescription.ifEmpty { "Inspection & service for ${subService.nameEn}" },
                                    selectedDate,
                                    selectedTimeSlot
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("submit_final_booking_button")
                    ) {
                        if (isSubmitting) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.EN) "Writing to Firestore..." else "क्लाउड में सेव हो रहा है...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = if (language == AppLanguage.EN) "Book Service Now" else "बुकिंग कन्फर्म करें",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBg)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Selected Service Summary Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subService.nameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "₹${subService.basePrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BokaroBluePrimary)
                        )
                    }
                    Text(
                        text = "Category: ${category.titleEn} • ${subService.estimatedDuration}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    if (provider != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = BokaroBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Assigned Specialist: ${provider.name} (⭐ ${provider.rating})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ Auto-Assigning Nearest Available Bokaro Specialist",
                            style = MaterialTheme.typography.labelSmall.copy(color = BokaroBlueDark, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bokaro Location & Address Section
            Text(
                text = if (language == AppLanguage.EN) "Location in Bokaro" else "बोकारो का पता",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = areaDropdownExpanded,
                onExpandedChange = { areaDropdownExpanded = !areaDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedArea,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Sector / Area in Bokaro") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BokaroBluePrimary) },
                    colors = bokaroTextFieldColors(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = areaDropdownExpanded,
                    onDismissRequest = { areaDropdownExpanded = false }
                ) {
                    BokaroLocations.areas.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area) },
                            onClick = {
                                selectedArea = area
                                areaDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("House / Quarter / Flat Address") },
                placeholder = { Text("e.g. Qr No. 2045, Street 18, Sector 4-D") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BokaroBluePrimary) },
                colors = bokaroTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_address_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Problem Description
            Text(
                text = if (language == AppLanguage.EN) "Describe Problem" else "समस्या का विवरण",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = problemDescription,
                onValueChange = { problemDescription = it },
                label = { Text("Issue Details / समस्या") },
                placeholder = { Text("e.g. Switchboard fuse blown, water pipe dripping, AC not cooling...") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                colors = bokaroTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_problem_input"),
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Preferred Date
            Text(
                text = if (language == AppLanguage.EN) "Preferred Date" else "तारीख चुनें",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Tomorrow", "Day After").forEach { day ->
                    val isSelected = selectedDate == day
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BokaroBlueLight else Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (isSelected) BokaroBluePrimary else BorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedDate = day }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = day,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BokaroBluePrimary else TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Slots
            Text(
                text = if (language == AppLanguage.EN) "Select Time Slot" else "समय चुनें",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BokaroLocations.timeSlots.forEach { slot ->
                    val isSelected = selectedTimeSlot == slot
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BokaroBlueLight.copy(alpha = 0.5f) else Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isSelected) BokaroBluePrimary else BorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTimeSlot = slot }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isSelected) BokaroBluePrimary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = slot,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BokaroBluePrimary else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trust Card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF0FDF4),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay securely via Cash or UPI after service completion. Free revisits within 30 days.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D), fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

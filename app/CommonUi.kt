package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ServiceType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BokaroBlueDark
import com.example.ui.theme.BokaroBlueLight
import com.example.ui.theme.BokaroBluePrimary
import com.example.ui.theme.BokaroNavy
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.StatusAcceptedBg
import com.example.ui.theme.StatusAcceptedText
import com.example.ui.theme.bokaroTextFieldColors
import com.example.ui.theme.StatusCancelledBg
import com.example.ui.theme.StatusCancelledText
import com.example.ui.theme.StatusCompletedBg
import com.example.ui.theme.StatusCompletedText
import com.example.ui.theme.StatusOnTheWayBg
import com.example.ui.theme.StatusOnTheWayText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText
import com.example.ui.theme.StatusWorkStartedBg
import com.example.ui.theme.StatusWorkStartedText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BokaroTopBar(
    title: String,
    currentUser: User?,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = AppStrings.get("bokaro_steel_city", language),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            // Language switch pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("language_switch_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.EN) "हिन्दी" else "English",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Role / Profile Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("role_menu_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val roleLetter = when (currentUser?.role) {
                                UserRole.CUSTOMER -> "C"
                                UserRole.PROVIDER -> "P"
                                UserRole.ADMIN -> "A"
                                null -> "U"
                            }
                            Text(
                                text = roleLetter,
                                color = BokaroBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    if (currentUser != null) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                text = currentUser.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (currentUser.role) {
                                    UserRole.CUSTOMER -> BokaroBlueLight
                                    UserRole.PROVIDER -> Color(0xFFDCFCE7)
                                    UserRole.ADMIN -> Color(0xFFF3E8FF)
                                }
                            ) {
                                Text(
                                    text = when (currentUser.role) {
                                        UserRole.CUSTOMER -> "CUSTOMER ACCOUNT"
                                        UserRole.PROVIDER -> "SERVICE PROVIDER (${currentUser.serviceCategory ?: "General"})"
                                        UserRole.ADMIN -> "ADMIN / OPERATIONS"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (currentUser.role) {
                                            UserRole.CUSTOMER -> BokaroBluePrimary
                                            UserRole.PROVIDER -> Color(0xFF15803D)
                                            UserRole.ADMIN -> Color(0xFF7C3AED)
                                        },
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (currentUser.area.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 ${currentUser.area}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        androidx.compose.material3.HorizontalDivider()
                    }

                    DropdownMenuItem(
                        text = { Text("Logout / Sign Out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626)) },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BokaroBluePrimary
        )
    )
}

@Composable
fun StatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status) {
        BookingStatus.PENDING -> Triple(StatusPendingBg, StatusPendingText, "Pending / नई बुकिंग")
        BookingStatus.ACCEPTED -> Triple(StatusAcceptedBg, StatusAcceptedText, "Accepted / स्वीकृत")
        BookingStatus.ON_THE_WAY -> Triple(StatusOnTheWayBg, StatusOnTheWayText, "On The Way / रास्ते में")
        BookingStatus.WORK_STARTED -> Triple(StatusWorkStartedBg, StatusWorkStartedText, "In Progress / काम चालू")
        BookingStatus.WORK_COMPLETED -> Triple(StatusCompletedBg, StatusCompletedText, "Completed / पूरा हुआ")
        BookingStatus.REJECTED -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), "Rejected / अस्वीकृत")
        BookingStatus.CANCELLED -> Triple(StatusCancelledBg, StatusCancelledText, "Cancelled / रद्द")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun PaymentBadge(status: PaymentStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status) {
        PaymentStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Payment Pending")
        PaymentStatus.PAID_CASH -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Paid (Cash)")
        PaymentStatus.PAID_UPI -> Triple(Color(0xFFE0E7FF), Color(0xFF4338CA), "Paid (UPI / Online)")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = textColor
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun BookingTimelineTracker(status: BookingStatus, rejectionReason: String? = null, modifier: Modifier = Modifier) {
    if (status == BookingStatus.REJECTED) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFEF2F2),
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Booking Rejected by Provider / सेवा अस्वीकृत",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontSize = 12.sp)
                    )
                }
                if (!rejectionReason.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reason: $rejectionReason",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B), fontSize = 11.5.sp)
                    )
                }
            }
        }
        return
    }

    val steps = listOf(
        Triple(BookingStatus.PENDING, "Placed", Icons.Default.HourglassTop),
        Triple(BookingStatus.ACCEPTED, "Accepted", Icons.Default.CheckCircle),
        Triple(BookingStatus.ON_THE_WAY, "On The Way", Icons.Default.DirectionsCar),
        Triple(BookingStatus.WORK_STARTED, "Working", Icons.Default.Handyman),
        Triple(BookingStatus.WORK_COMPLETED, "Done", Icons.Default.Verified)
    )

    val currentStep = status.stepIndex

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Service Progress Timeline",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, (stepStatus, label, icon) ->
                    val isDone = currentStep >= index
                    val isCurrent = currentStep == index

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isCurrent -> BokaroBluePrimary
                                isDone -> Color(0xFF10B981)
                                else -> Color(0xFFCBD5E1)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isDone && !isCurrent) Icons.Default.Check else icon,
                                    contentDescription = label,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) BokaroBluePrimary else if (isDone) TextPrimary else TextMuted,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(3.dp)
                                .background(
                                    if (currentStep > index) Color(0xFF10B981) else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Float,
    maxStars: Int = 5,
    onRatingChanged: ((Float) -> Unit)? = null,
    starSize: Int = 20,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            val icon = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder
            val tint = if (isFilled) AccentGold else Color(0xFFCBD5E1)

            Icon(
                imageVector = icon,
                contentDescription = "$i Stars",
                tint = tint,
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable(enabled = onRatingChanged != null) {
                        onRatingChanged?.invoke(i.toFloat())
                    }
                    .padding(horizontal = 1.dp)
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBgColor.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconBgColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }
        }
    }
}

@Composable
fun PaymentDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirmPayment: (PaymentStatus, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("UPI_QR") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Complete Payment for #${booking.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bokaro Services Safe Pay Guarantee",
                    style = MaterialTheme.typography.labelSmall.copy(color = BokaroBluePrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base Service Charge", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("₹${booking.basePrice.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        if (booking.extraPartsCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Extra Parts & Material", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("+₹${booking.extraPartsCost.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payable", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("₹${booking.totalAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BokaroBluePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Payment Method", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(8.dp))

                // Payment options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (selectedMethod == "UPI_QR") BokaroBluePrimary else BorderSubtle,
                            RoundedCornerShape(8.dp)
                        )
                        .background(if (selectedMethod == "UPI_QR") BokaroBlueLight.copy(alpha = 0.3f) else Color.White)
                        .clickable { selectedMethod = "UPI_QR" }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡ UPI / PhonePe / GPay / QR Code", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (selectedMethod == "UPI_QR") {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BokaroBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (selectedMethod == "CASH") BokaroBluePrimary else BorderSubtle,
                            RoundedCornerShape(8.dp)
                        )
                        .background(if (selectedMethod == "CASH") BokaroBlueLight.copy(alpha = 0.3f) else Color.White)
                        .clickable { selectedMethod = "CASH" }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💵 Pay Cash to Service Provider", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (selectedMethod == "CASH") {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BokaroBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedMethod == "UPI_QR") {
                                onConfirmPayment(PaymentStatus.PAID_UPI, "UPI / Online Pay")
                            } else {
                                onConfirmPayment(PaymentStatus.PAID_CASH, "Cash on Service")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                        modifier = Modifier.testTag("confirm_payment_button")
                    ) {
                        Text("Confirm Paid ₹${booking.totalAmount.toInt()}")
                    }
                }
            }
        }
    }
}

@Composable
fun RatingReviewDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmitRating: (Float, String, String) -> Unit
) {
    var rating by remember { mutableFloatStateOf(5.0f) }
    var reviewText by remember { mutableStateOf("") }
    val tags = listOf("On Time", "Courteous & Polite", "Expert Work", "Reasonable Price", "Clean Workspace")
    val selectedTags = remember { mutableStateOf(setOf("On Time", "Expert Work")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Rate Your Service Experience",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Technician: ${booking.providerName ?: "Bokaro Specialist"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    StarRatingBar(
                        rating = rating,
                        starSize = 34,
                        onRatingChanged = { rating = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("What did you like?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(6.dp))

                // Tag chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.take(3).forEach { tag ->
                        val isSelected = selectedTags.value.contains(tag)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) BokaroBlueLight else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clickable {
                                    val current = selectedTags.value.toMutableSet()
                                    if (isSelected) current.remove(tag) else current.add(tag)
                                    selectedTags.value = current
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) BokaroBluePrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Write your feedback (Optional)") },
                    placeholder = { Text("e.g. Prompt service, fixed the issue quickly...") },
                    colors = bokaroTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Skip")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmitRating(
                                rating,
                                reviewText.ifEmpty { "Great service!" },
                                selectedTags.value.joinToString(", ")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                        modifier = Modifier.testTag("submit_rating_button")
                    ) {
                        Text("Submit Rating")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPartsBillDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSaveBill: (Double, String) -> Unit
) {
    var partsDescription by remember { mutableStateOf(booking.partsDescription) }
    var partsCostText by remember { mutableStateOf(if (booking.extraPartsCost > 0) booking.extraPartsCost.toInt().toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Add Extra Parts / Materials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Add any replacement components used for ${booking.customerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = partsCostText,
                    onValueChange = { partsCostText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Extra Parts Cost (₹)") },
                    placeholder = { Text("e.g. 250") },
                    colors = bokaroTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partsDescription,
                    onValueChange = { partsDescription = it },
                    label = { Text("Parts Description") },
                    placeholder = { Text("e.g. Havells 16A Switch & PVC Tape") },
                    colors = bokaroTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                val cost = partsCostText.toDoubleOrNull() ?: 0.0
                val totalNew = booking.basePrice + cost

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("New Total Bill:", fontWeight = FontWeight.Bold)
                        Text("₹${totalNew.toInt()}", fontWeight = FontWeight.Bold, color = BokaroBluePrimary, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveBill(cost, partsDescription)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary)
                    ) {
                        Text("Save & Update Bill")
                    }
                }
            }
        }
    }
}

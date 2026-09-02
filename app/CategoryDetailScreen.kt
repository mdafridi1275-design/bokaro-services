package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceCategoryItem
import com.example.data.model.SubService
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.BokaroTopBar
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
fun CategoryDetailScreen(
    category: ServiceCategoryItem,
    subService: SubService?,
    selectedProvider: User?,
    allProviders: List<User>,
    currentUser: User?,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    onSelectSubService: (SubService) -> Unit,
    onSelectProvider: (User?) -> Unit,
    onProceedToBooking: (ServiceCategoryItem, SubService, User?) -> Unit,
    onBack: () -> Unit
) {
    val categoryProviders = allProviders.filter {
        it.role == UserRole.PROVIDER && it.serviceCategory == category.type.name
    }

    val activeSubService = subService ?: category.subServices.first()

    Scaffold(
        topBar = {
            BokaroTopBar(
                title = if (language == AppLanguage.EN) category.titleEn else category.titleHi,
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
                            text = "Estimated Price",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "₹${activeSubService.basePrice.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BokaroBluePrimary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onProceedToBooking(category, activeSubService, selectedProvider)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BokaroBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("proceed_to_booking_button")
                    ) {
                        Text(
                            text = if (language == AppLanguage.EN) "Select Slot & Address ➔" else "पता व समय चुनें ➔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBg)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Image(
                                painter = painterResource(id = category.imageRes),
                                contentDescription = category.titleEn,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.EN) category.titleEn else category.titleHi,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (language == AppLanguage.EN) category.shortDescEn else category.shortDescHi,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.5.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Certified Bokaro Steel City Technicians",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }

            // Step 1: Select Sub-Service
            item {
                Text(
                    text = "1. " + if (language == AppLanguage.EN) "Select Specific Service" else "विशिष्ट सेवा का चयन करें",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(category.subServices) { sub ->
                val isSelected = sub.id == activeSubService.id
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BokaroBlueLight.copy(alpha = 0.4f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .border(
                            1.5.dp,
                            if (isSelected) BokaroBluePrimary else BorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectSubService(sub) }
                        .testTag("sub_service_item_${sub.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) BokaroBluePrimary else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.EN) sub.nameEn else sub.nameHi,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BokaroBluePrimary else TextPrimary
                                )
                            )
                            Text(
                                text = if (language == AppLanguage.EN) sub.descriptionEn else sub.descriptionHi,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "⏱ Est. Time: ${sub.estimatedDuration}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                        }
                        Text(
                            text = "₹${sub.basePrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BokaroBluePrimary else TextPrimary
                            )
                        )
                    }
                }
            }

            // Step 2: Choose Technician / Specialist
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "2. " + if (language == AppLanguage.EN) "Choose Service Provider (Optional)" else "कारीगर चुनें (वैकल्पिक)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Option: Auto Assign
                val isAutoSelected = selectedProvider == null
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAutoSelected) BokaroBlueLight.copy(alpha = 0.4f) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .border(
                            1.5.dp,
                            if (isAutoSelected) BokaroBluePrimary else BorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectProvider(null) }
                        .testTag("provider_auto_assign_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BokaroBluePrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("auto_assign", language),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Fastest dispatch based on your Bokaro sector location",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                        if (isAutoSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BokaroBluePrimary)
                        }
                    }
                }
            }

            // List of specific verified providers
            items(categoryProviders) { provider ->
                val isSelected = selectedProvider?.id == provider.id
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BokaroBlueLight.copy(alpha = 0.4f) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .border(
                            1.5.dp,
                            if (isSelected) BokaroBluePrimary else BorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectProvider(provider) }
                        .testTag("provider_card_${provider.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = BokaroBlueDark, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = provider.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "${provider.rating}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = " (${provider.reviewCount})",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                    )
                                }
                            }
                            Text(
                                text = "${provider.experienceYears} yrs experience • ${provider.area}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                            if (provider.bio.isNotEmpty()) {
                                Text(
                                    text = provider.bio,
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.5.sp),
                                    maxLines = 1
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BokaroBluePrimary)
                        }
                    }
                }
            }
        }
    }
}

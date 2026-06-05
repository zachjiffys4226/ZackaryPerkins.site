package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.data.JobEntity
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(viewModel: JiffyViewModel, onLogout: () -> Unit, onJobClick: (Int) -> Unit) {
    val jobs by viewModel.driverJobs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Portal", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (jobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No assigned jobs currently.")
                }
            } else {
                JobCard(jobs.first(), onClick = { onJobClick(jobs.first().id) })
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MapWidgetCard()
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DailySplitCard(jobs)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MessagesCard(modifier = Modifier.weight(1f))
                NextUpCard(jobs.drop(1).firstOrNull(), modifier = Modifier.weight(1f))
            }
            
            AdminQuickAccessCard(onLogout)
        }
    }
}

@Composable
fun MapWidgetCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("📍", fontSize = 16.sp)
                }
                Text("LIVE MAP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                // Mock Map graphic
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF3B82F6), RoundedCornerShape(50)).border(2.dp, Color.White, RoundedCornerShape(50)))
            }
            
            Text("7 mins to destination", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun DailySplitCard(jobs: List<JobEntity>) {
    val totalRevenue = jobs.filter { it.status == "Completed" }.sumOf { it.finalPrice ?: it.basePrice }
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val ownerShare = totalRevenue * 0.60
    val taxShare = totalRevenue * 0.25
    val saveShare = totalRevenue * 0.15

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text("DAILY SPLIT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SplitRow("Owner (60%)", formatter.format(ownerShare))
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                    Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
                }
                SplitRow("Tax (25%)", formatter.format(taxShare))
                SplitRow("Save (15%)", formatter.format(saveShare))
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(formatter.format(totalRevenue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("TOTAL EARNED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f), fontSize = 8.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun SplitRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun MessagesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("💬", fontSize = 12.sp)
                Text("MESSAGES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(Color.Gray, RoundedCornerShape(50)))
                Text("\"Is there space...\"", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun NextUpCard(job: JobEntity?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("📅", fontSize = 12.sp)
                Text("NEXT UP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            if (job != null) {
                Text("14:30 PM", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                Text("${job.description} - ${job.customerName}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp)
            } else {
                Text("No upcoming jobs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AdminQuickAccessCard(onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1F))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.Center) {
                Text("ADMIN QUICK ACCESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp, fontSize = 10.sp)
                Spacer(Modifier.height(4.dp))
                Text("Pardonmetodeath@gmail.com", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = Color.White)
            }
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Owner Portal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun JobCard(job: JobEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        when (job.status) {
                            "Pending" -> "PENDING"
                            "In Progress" -> "ACTIVE JOB"
                            "Completed" -> "COMPLETED"
                            else -> job.status.uppercase()
                        }, 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text("Synced: ${job.driverId ?: "Unassigned"}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(12.dp))
            Text(job.description.takeIf { it.isNotBlank() } ?: "Service Request", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, lineHeight = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text("${job.customerName} • ${job.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                val priceStr = if (job.finalPrice != null) formatter.format(job.finalPrice) else formatter.format(job.basePrice) + " (Est)"
                Text(priceStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("VIEW DETAILS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverJobDetailScreen(
    jobId: Int,
    viewModel: JiffyViewModel,
    onBack: () -> Unit
) {
    val jobs by viewModel.allJobs.collectAsState()
    val job = jobs.find { it.id == jobId } ?: return
    
    val context = LocalContext.current
    var beforePhotoUri by remember { mutableStateOf<Uri?>(job.beforePhotoUri?.let { Uri.parse(it) }) }
    var afterPhotoUri by remember { mutableStateOf<Uri?>(job.afterPhotoUri?.let { Uri.parse(it) }) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val beforePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            beforePhotoUri = uri
            viewModel.updateJobStatus(jobId, job.status, beforePhoto = uri.toString())
        }
    }

    val afterPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            afterPhotoUri = uri
            viewModel.updateJobStatus(jobId, job.status, afterPhoto = uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(job.customerName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(job.address, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Description:", style = MaterialTheme.typography.titleSmall)
            Text(job.description, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(Modifier.height(24.dp))
            
            // Navigate Action
            OutlinedButton(
                onClick = {
                    val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(job.address)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        val genericIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(genericIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Navigation")
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Job Documentation", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PhotoBox(
                    label = "Before Photo", 
                    uri = beforePhotoUri, 
                    onClick = { beforePhotoLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
                PhotoBox(
                    label = "After Photo", 
                    uri = afterPhotoUri, 
                    onClick = { afterPhotoLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            if (job.status == "Pending") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateJobStatus(jobId, "Declined")
                            onBack()
                        },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Decline")
                    }
                    Button(
                        onClick = {
                            viewModel.updateJobStatus(jobId, "In Progress")
                        },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Accept Job")
                    }
                }
            } else if (job.status != "Completed") {
                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = beforePhotoUri != null && afterPhotoUri != null
                ) {
                    Text("Complete Job & Collect Payment")
                }
                if (beforePhotoUri == null || afterPhotoUri == null) {
                    Text("Before and after photos are required.", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Job Completed - Paid: ${NumberFormat.getCurrencyInstance().format(job.finalPrice)}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }

    if (showCompleteDialog) {
        var finalPriceInput by remember { mutableStateOf(job.basePrice.toString()) }
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Job") },
            text = {
                Column {
                    Text("Mechanics can adjust the final price based on actual work performed.")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = finalPriceInput,
                        onValueChange = { finalPriceInput = it },
                        label = { Text("Final Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val finalPrice = finalPriceInput.toDoubleOrNull() ?: job.basePrice
                    viewModel.updateJobStatus(jobId, "Completed", finalPrice = finalPrice)
                    showCompleteDialog = false
                    onBack()
                }) {
                    Text("Charge & Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PhotoBox(label: String, uri: Uri?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.aspectRatio(1f)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                ) {
                    Text(label, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

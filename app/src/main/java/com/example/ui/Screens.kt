package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.JobEntity
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke

@Composable
fun LoginScreen(onLogin: (String) -> Unit, onNavigateToSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "App Icon",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Jiffy's Portal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (email.isNotBlank() && password.isNotBlank()) onLogin(email) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Sign In")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateToSignup) {
                Text("Need an account? Sign up to drive")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Login with pardonmetodeath@gmail.com for Owner Portal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(viewModel: JiffyViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Owner Portal") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.List, "Jobs") },
                    label = { Text("Jobs") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, "Dispatch") },
                    label = { Text("Dispatch") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.People, "Drivers") },
                    label = { Text("Drivers") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, "Finances") },
                    label = { Text("Finances") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    val jobs by viewModel.allJobs.collectAsState()
                    OwnerJobsTab(jobs)
                }
                1 -> OwnerDispatchTab(viewModel)
                2 -> OwnerDriversTab(viewModel)
                3 -> {
                    val completedJobs by viewModel.completedJobs.collectAsState()
                    OwnerFinancesTab(completedJobs)
                }
            }
        }
    }
}

@Composable
fun OwnerDriversTab(viewModel: JiffyViewModel) {
    val pendingDrivers by viewModel.pendingDrivers.collectAsState()
    var selectedDriver by remember { mutableStateOf<com.example.data.UserEntity?>(null) }
    
    if (pendingDrivers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending driver applications.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(pendingDrivers) { driver ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(driver.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${driver.city}, ${driver.state}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Button(onClick = { viewModel.approveDriver(driver.email) }, modifier = Modifier.weight(1f)) { Text("Approve") }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.denyDriver(driver.email) }, modifier = Modifier.weight(1f)) { Text("Deny") }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { selectedDriver = driver }) { Text("Details") }
                        }
                    }
                }
            }
        }
        
        selectedDriver?.let { driver ->
            AlertDialog(
                onDismissRequest = { selectedDriver = null },
                title = { Text("Driver Details") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Email: ${driver.email}")
                        Text("Phone: ${driver.phoneNumber}")
                        Text("Address: ${driver.address}, ${driver.city}, ${driver.state} ${driver.zipCode}")
                        Text("DL Number: ${driver.dlNumber}")
                        Text("DOB: ${driver.dateOfBirth}")
                        Text("Photos have been submitted.", color = MaterialTheme.colorScheme.primary)
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedDriver = null }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun OwnerJobsTab(jobs: List<JobEntity>) {
    if (jobs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scheduled jobs.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(jobs) { job ->
                JobCard(job, onClick = {})
            }
        }
    }
}

@Composable
fun OwnerDispatchTab(viewModel: JiffyViewModel) {
    var customerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Manual Dispatch", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address/Location") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Job Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Estimated Base Price ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (customerName.isNotBlank() && address.isNotBlank()) {
                    viewModel.addNewJob(customerName, address, desc, price.toDoubleOrNull() ?: 0.0)
                    customerName = ""
                    address = ""
                    desc = ""
                    price = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Dispatch Job")
        }
        
        Spacer(Modifier.height(32.dp))
        Divider()
        Spacer(Modifier.height(32.dp))
        Text("Auto Dispatch", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Sync with Jiffysmobile@gmail.com calendar to automatically dispatch jobs from events.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { /* OAuth trigger placeholder */ }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Icon(Icons.Default.CalendarToday, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Connect Google Calendar")
        }
    }
}

@Composable
fun OwnerFinancesTab(completedJobs: List<JobEntity>) {
    val totalRevenue = completedJobs.sumOf { it.finalPrice ?: it.basePrice }
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    val savingsFund = totalRevenue * 0.15
    val ownerShare = totalRevenue * 0.60
    val taxEmergencyFund = totalRevenue * 0.25

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Revenue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(formatter.format(totalRevenue), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("${completedJobs.size} completed jobs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Fund Distributions:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        
        FundRow("Owner Bank Account (60%)", ownerShare, MaterialTheme.colorScheme.primary)
        FundRow("Tax & Emergency Fund (25%)", taxEmergencyFund, MaterialTheme.colorScheme.error)
        FundRow("Company Savings (15%)", savingsFund, MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun FundRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(formatter.format(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

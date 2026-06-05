package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(viewModel: JiffyViewModel, onBack: () -> Unit, onSignupComplete: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var dlNumber by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    var dlPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }

    val dlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) dlPhotoUri = uri
    }
    
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selfieUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Sign Up", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Date of Birth (MM/DD/YYYY)") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))
            Text("Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Street Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = zipCode, onValueChange = { zipCode = it }, label = { Text("Zip Code") }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Text("Driver Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(value = dlNumber, onValueChange = { dlNumber = it }, label = { Text("Driver's License Number") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PhotoBox("Upload ID", dlPhotoUri, { dlLauncher.launch("image/*") }, Modifier.weight(1f))
                PhotoBox("Selfie with ID", selfieUri, { selfieLauncher.launch("image/*") }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val user = UserEntity(
                        email = email.trim().lowercase(),
                        passwordHash = password,
                        phoneNumber = phone,
                        address = address,
                        city = city,
                        state = state,
                        zipCode = zipCode,
                        dlNumber = dlNumber,
                        dateOfBirth = dob,
                        dlPhotoUri = dlPhotoUri?.toString(),
                        selfieUri = selfieUri?.toString()
                    )
                    viewModel.signupUser(user, onComplete = onSignupComplete)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && dlPhotoUri != null && selfieUri != null,
                shape = RoundedCornerShape(50)
            ) {
                Text("Submit Application")
            }
        }
    }
}

@Composable
fun StatusScreen(title: String, message: String, onBackToIntro: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
         Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
             Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
             Spacer(modifier = Modifier.height(16.dp))
             Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
             Spacer(modifier = Modifier.height(32.dp))
             Button(onClick = onBackToIntro) {
                 Text("Back to Login")
             }
         }
    }
}

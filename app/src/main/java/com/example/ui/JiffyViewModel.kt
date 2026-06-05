package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.JobDatabase
import com.example.data.JobEntity
import com.example.data.JobRepository
import com.example.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JiffyViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        JobDatabase::class.java, "jiffy-jobs-db"
    ).fallbackToDestructiveMigration().build()

    private val repository = JobRepository(db)

    // Auth state - hardcoded mock auth
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    // Owner email check
    val isOwner = combine(_currentUserEmail) { (email) ->
        email == "pardonmetodeath@gmail.com"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Data streams
    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedJobs: StateFlow<List<JobEntity>> = repository.completedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDrivers: StateFlow<List<UserEntity>> = repository.pendingDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // To prevent recomposition loops or UI blocking, expose driver jobs dynamically based on logged in user
    private val _driverJobs = MutableStateFlow<List<JobEntity>>(emptyList())
    val driverJobs: StateFlow<List<JobEntity>> = _driverJobs
    
    init {
        // Prepopulate with a sample job if empty
        viewModelScope.launch {
            repository.allJobs.collect { jobs ->
                if (jobs.isEmpty()) {
                    repository.addJob(
                        JobEntity(
                            customerName = "John Doe",
                            address = "123 Main St, Anytown",
                            description = "Brake pad replacement and rotor check",
                            scheduledTime = System.currentTimeMillis() + 86400000, // tomorrow
                            driverId = "driver@gmail.com",
                            basePrice = 150.0
                        )
                    )
                }
            }
        }
    }

    fun loginWithValidation(email: String, onResult: (String) -> Unit) {
        val emailNormalized = email.trim().lowercase()
        if (emailNormalized == "pardonmetodeath@gmail.com") {
            login(emailNormalized)
            onResult("Success_Owner")
        } else {
            viewModelScope.launch {
                val user = repository.getUser(emailNormalized)
                if (user == null) {
                    onResult("Not Found")
                } else {
                    when (user.status) {
                        "Approved" -> {
                            login(emailNormalized)
                            onResult("Success_Driver")
                        }
                        "Pending" -> onResult("Pending")
                        "Denied" -> onResult("Denied")
                        else -> onResult("Not Found")
                    }
                }
            }
        }
    }

    fun login(email: String) {
        val emailNormalized = email.trim().lowercase()
        _currentUserEmail.value = emailNormalized
        if (emailNormalized != "pardonmetodeath@gmail.com") {
            viewModelScope.launch {
                repository.getDriverJobs(emailNormalized).collect { jobs ->
                    _driverJobs.value = jobs
                }
            }
        }
    }

    fun logout() {
        _currentUserEmail.value = null
        _driverJobs.value = emptyList()
    }
    
    fun signupUser(user: UserEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertUser(user)
            onComplete()
        }
    }

    fun approveDriver(email: String) {
        viewModelScope.launch {
            val user = repository.getUser(email)
            if (user != null) {
                repository.updateUser(user.copy(status = "Approved"))
            }
        }
    }

    fun denyDriver(email: String) {
        viewModelScope.launch {
            val user = repository.getUser(email)
            if (user != null) {
                repository.updateUser(user.copy(status = "Denied"))
            }
        }
    }

    fun updateJobStatus(jobId: Int, newStatus: String, finalPrice: Double? = null, beforePhoto: String? = null, afterPhoto: String? = null) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                repository.updateJob(job.copy(
                    status = newStatus,
                    finalPrice = finalPrice ?: job.finalPrice,
                    beforePhotoUri = beforePhoto ?: job.beforePhotoUri,
                    afterPhotoUri = afterPhoto ?: job.afterPhotoUri
                ))
            }
        }
    }
    
    fun addNewJob(customerName: String, address: String, description: String, basePrice: Double) {
        viewModelScope.launch {
            repository.addJob(
                JobEntity(
                    customerName = customerName,
                    address = address,
                    description = description,
                    scheduledTime = System.currentTimeMillis() + 7200000, 
                    driverId = "driver@gmail.com", // default assign to dummy driver
                    basePrice = basePrice
                )
            )
        }
    }
}

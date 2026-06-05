package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val phoneNumber: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val dlNumber: String,
    val dateOfBirth: String,
    val dlPhotoUri: String?,
    val selfieUri: String?,
    val role: String = "Driver", // 'Owner' or 'Driver'
    val status: String = "Pending" // 'Pending', 'Approved', 'Denied'
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val address: String,
    val description: String,
    val status: String = "Pending", // Pending, Accepted, In Progress, Completed, Declined
    val scheduledTime: Long,
    val driverId: String? = null,
    val basePrice: Double = 0.0,
    val finalPrice: Double? = null, // Set at completion
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val notes: String = ""
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'Driver' AND status = 'Pending'")
    fun getPendingDrivers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY scheduledTime DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE driverId = :driverId AND status != 'Declined' ORDER BY scheduledTime ASC")
    fun getDriverJobs(driverId: String): Flow<List<JobEntity>>
    
    @Query("SELECT * FROM jobs WHERE status = 'Completed'")
    fun getCompletedJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: Int): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)
}

@Database(entities = [JobEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class JobDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
}

class JobRepository(private val db: JobDatabase) {
    val allJobs: Flow<List<JobEntity>> = db.jobDao().getAllJobs()
    val completedJobs: Flow<List<JobEntity>> = db.jobDao().getCompletedJobs()
    val pendingDrivers: Flow<List<UserEntity>> = db.userDao().getPendingDrivers()
    
    fun getDriverJobs(driverEmail: String) = db.jobDao().getDriverJobs(driverEmail)
    suspend fun getJobById(id: Int) = db.jobDao().getJobById(id)

    suspend fun addJob(job: JobEntity) {
        db.jobDao().insertJob(job)
    }

    suspend fun updateJob(job: JobEntity) {
        db.jobDao().updateJob(job)
    }
    
    suspend fun getUser(email: String) = db.userDao().getUser(email)
    
    suspend fun insertUser(user: UserEntity) = db.userDao().insertUser(user)
    
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
}

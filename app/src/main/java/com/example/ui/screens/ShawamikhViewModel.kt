package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

class ShawamikhViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CompanyRepository(db)

    // User configurations
    val isDarkMode = MutableStateFlow(true)
    val currentLanguage = MutableStateFlow("ar") // "ar" or "en"
    val currentUserRole = MutableStateFlow("Admin") // "Admin" or "Employee"

    // Active simulated Employee (Default is Yasser SH-1002)
    val activeEmployeeId = MutableStateFlow("SH-1002")
    val activeEmployeeState = MutableStateFlow<EmployeeEntity?>(null)

    // Simulated Phone GPS state
    val simLatitude = MutableStateFlow(24.7622) // Defaults to KAFD Riyadh coordinates
    val simLongitude = MutableStateFlow(46.6410)
    val selectedGeoOffice = MutableStateFlow("KAFD") // "KAFD", "RiyadhFront", "FarAway"

    // AI Advisor States
    val aiMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val isAiLoading = MutableStateFlow(false)

    // Database reactive streams
    val employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val attendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val leaves = repository.allLeaves.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.recentLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed database at first run
            repository.seedDatabaseAll()
            
            // Load user profile
            loadActiveUserProfile()
        }

        // Add welcome message from AI
        aiMessages.value = listOf(
            ChatMessage(
                isUser = false,
                text = "مرحباً بك في نظام **شوامخ** للتشغيل والموارد البشرية الذكي. يمكنك الرقابة والتفاعل بكفاءة. كيف يمكنني مساعدتك في تطبيق لوائح وزارة الموارد البشرية السعودية اليوم؟"
            )
        )
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun toggleLanguage() {
        currentLanguage.value = if (currentLanguage.value == "ar") "en" else "ar"
    }

    fun switchUserRole(role: String, empId: String = "SH-1002") {
        currentUserRole.value = role
        activeEmployeeId.value = empId
        viewModelScope.launch {
            loadActiveUserProfile()
            repository.insertLog(
                action = "ROLE_SWITCH",
                performedBy = empId,
                details = "Switched terminal simulation session role to $role for Employee $empId."
            )
        }
    }

    private suspend fun loadActiveUserProfile() {
        val emp = repository.getEmployeeById(activeEmployeeId.value)
        activeEmployeeState.value = emp
    }

    // Geofencing distances formula
    // Office Centers:
    // KAFD: Lat 24.7622, Lng 46.6410
    // Riyadh Front: Lat 24.8143, Lng 46.7261
    fun setSimulatedLocation(office: String) {
        selectedGeoOffice.value = office
        when (office) {
            "KAFD" -> {
                simLatitude.value = 24.7622
                simLongitude.value = 46.6410
            }
            "RiyadhFront" -> {
                simLatitude.value = 24.8143
                simLongitude.value = 46.7261
            }
            "FarAway" -> {
                // Far from geofence centers (Diriyah or desert)
                simLatitude.value = 24.6300
                simLongitude.value = 46.5100
            }
        }
    }

    // Haversine formula calculation for exact metric distance to KAFD or Riyadh Front
    fun calculateDistanceToOffice(office: String): Double {
        val officeLat = if (office == "KAFD") 24.7622 else 24.8143
        val officeLng = if (office == "KAFD") 46.6410 else 46.7261

        val r = 6371e3 // Earth's radius in meters
        val lat1Rad = simLatitude.value * PI / 180.0
        val lat2Rad = officeLat * PI / 180.0
        val dLat = (officeLat - simLatitude.value) * PI / 180.0
        val dLng = (officeLng - simLongitude.value) * PI / 180.0

        val a = sin(dLat / 2).pow(2) + dLng / 2 * cos(lat1Rad) * cos(lat2Rad) * sin(dLng / 2).pow(2)
        // Ensure bounds to avoid NaN from float precisions
        val aClamped = a.coerceIn(0.0, 1.0)
        val c = 2 * atan2(sqrt(aClamped), sqrt(1 - aClamped))

        return r * c // returns distance in meters
    }

    fun isWithinGeofence(office: String): Boolean {
        val maxAllowableDistanceMeters = 200.0 // 200 Meters diameter radius boundary
        return calculateDistanceToOffice(office) < maxAllowableDistanceMeters
    }

    // Clock In Action
    fun performClockIn(office: String, selfieMockB64: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val empId = activeEmployeeId.value
            val todayDate = SimpleDateFormat("YYYY-MM-DD", Locale.US).format(Date())
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            
            val isInside = isWithinGeofence(office)
            val statusStr = if (isInside) "Present" else "Unverified-GPS"

            val currAtt = repository.getTodayAttendance(empId, todayDate)
            if (currAtt == null) {
                // New record
                val newAtt = AttendanceEntity(
                    employeeId = empId,
                    date = todayDate,
                    clockInTime = timeString,
                    latitudeIn = simLatitude.value,
                    longitudeIn = simLongitude.value,
                    status = if (isInside) "Present" else "Late",
                    photoInBase64 = selfieMockB64,
                    note = "Checked in from simulated GPS boundary at $office."
                )
                repository.insertAttendance(newAtt)
                repository.insertLog("CHECK_IN", empId, "Successfully recorded attendance check-in ($statusStr) via geofenced office: $office at $timeString.")
                onSuccess()
            }
        }
    }

    // Clock Out Action
    fun performClockOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val empId = activeEmployeeId.value
            val todayDate = SimpleDateFormat("YYYY-MM-DD", Locale.US).format(Date())
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val currAtt = repository.getTodayAttendance(empId, todayDate)
            if (currAtt != null && currAtt.clockOutTime == null) {
                val updated = currAtt.copy(
                    clockOutTime = timeString,
                    latitudeOut = simLatitude.value,
                    longitudeOut = simLongitude.value,
                    workedHours = 8.5,
                    note = currAtt.note + " | Checked out at $timeString."
                )
                repository.updateAttendance(updated)
                repository.insertLog("CHECK_OUT", empId, "Successfully checked out at $timeString.")
                onSuccess()
            }
        }
    }

    // HR additions
    fun addEmployee(emp: EmployeeEntity) {
        viewModelScope.launch {
            repository.insertEmployee(emp)
            repository.insertLog("EMPLOYEE_ADD", "HR Administrator", "Registered new staff entry ${emp.nameEn} (${emp.employeeId}) with GOSI contribution metrics.")
        }
    }

    fun insertTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
            repository.insertLog("TASK_ADD", "HR Administrator", "Assigned new geofenced operations task '${task.titleEn}' to employee ${task.assignedToId}.")
        }
    }

    // Task Reporting and Status
    fun updateTaskStatus(task: TaskEntity, newStatus: String, beforePic: String? = null, afterPic: String? = null, reportNote: String? = null) {
        viewModelScope.launch {
            val updated = task.copy(
                status = newStatus,
                beforePhotoBase64 = beforePic ?: task.beforePhotoBase64,
                afterPhotoBase64 = afterPic ?: task.afterPhotoBase64,
                reportedNotes = reportNote ?: task.reportedNotes,
                completionTime = if (newStatus == "Completed") "Completed just now" else task.completionTime
            )
            repository.updateTask(updated)
            repository.insertLog("TASK_UPDATE", activeEmployeeId.value, "Updated status of task #${task.id} to $newStatus.")
        }
    }

    fun submitTaskResult(taskId: Int, status: String, notes: String, beforeImg: String?, afterImg: String?) {
        viewModelScope.launch {
            val taskList = tasks.value
            val match = taskList.firstOrNull { it.id == taskId }
            if (match != null) {
                val updated = match.copy(
                    status = status,
                    reportedNotes = notes,
                    beforePhotoBase64 = beforeImg ?: match.beforePhotoBase64,
                    afterPhotoBase64 = afterImg ?: match.afterPhotoBase64,
                    completionTime = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                )
                repository.updateTask(updated)
                repository.insertLog("TASK_SUBMIT", activeEmployeeId.value, "Uploaded operational logs for task ID: $taskId.")
            }
        }
    }

    // Leaves management
    fun requestLeave(typeAr: String, typeEn: String, start: String, end: String, reason: String, days: Int) {
        viewModelScope.launch {
            val emp = activeEmployeeState.value ?: return@launch
            val leave = LeaveEntity(
                employeeId = emp.employeeId,
                employeeName = emp.nameEn,
                leaveTypeAr = typeAr,
                leaveTypeEn = typeEn,
                startDate = start,
                endDate = end,
                reason = reason,
                status = "Pending",
                totalDays = days
            )
            repository.insertLeave(leave)
            repository.insertLog("LEAVE_REQUEST", emp.employeeId, "Logged leave application for: ${typeEn} ($days days) stating: $reason.")
        }
    }

    fun approveLeave(leave: LeaveEntity) {
        viewModelScope.launch {
            val updated = leave.copy(status = "Approved")
            repository.updateLeave(updated)
            
            // Deduct from leave balance
            val emp = repository.getEmployeeById(leave.employeeId)
            if (emp != null) {
                val updatedEmp = emp.copy(
                    leaveBalance = (emp.leaveBalance - leave.totalDays).coerceAtLeast(0)
                )
                repository.updateEmployee(updatedEmp)
                if (updatedEmp.employeeId == activeEmployeeId.value) {
                    activeEmployeeState.value = updatedEmp
                }
            }
            repository.insertLog("LEAVE_APPROVE", "HR Administrator", "Approved leave payload ID #${leave.id} for employee ${leave.employeeId}.")
        }
    }

    fun rejectLeave(leave: LeaveEntity) {
        viewModelScope.launch {
            val updated = leave.copy(status = "Rejected")
            repository.updateLeave(updated)
            repository.insertLog("LEAVE_REJECT", "HR Administrator", "Rejected leave application ID #${leave.id} for employee ${leave.employeeId}.")
        }
    }

    // AI advisor interaction
    fun sendAiQuestion(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            val current = aiMessages.value.toMutableList()
            current.add(ChatMessage(isUser = true, text = message))
            aiMessages.value = current
            
            isAiLoading.value = true
            
            val response = GeminiService.getSaudiHRAdvice(message)
            
            val updated = aiMessages.value.toMutableList()
            updated.add(ChatMessage(isUser = false, text = response))
            aiMessages.value = updated
            
            isAiLoading.value = false
            repository.insertLog("AI_CHAT", activeEmployeeId.value, "Queried smart advisory with prompt snippet: ${message.take(30)}...")
        }
    }
}

data class ChatMessage(
    val isUser: Boolean,
    val text: String
)

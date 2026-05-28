package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String, // e.g. "SH-1001"
    val nameAr: String,
    val nameEn: String,
    val role: String, // "Admin" or "Employee"
    val saudiId: String, // Hijri/National ID/Iqama
    val isSaudi: Boolean,
    val iqamaExpiry: String, // YYYY-MM-DD or Hijri
    val email: String,
    val phone: String,
    val department: String, // Operations, HR, Sales, Field, Engineering
    val designationAr: String,
    val designationEn: String,
    val salary: Double, // Base Salary in SAR
    val housingAllowance: Double, // SAR
    val transportAllowance: Double, // SAR
    val otherAllowances: Double = 0.0,
    val gosiContribution: Double, // Pre-calculated GOSI social security SAR
    val leaveBalance: Int = 30, // Default 30 days yearly as per Saudi law Ar 109
    val contractType: String = "Sovereign Contract", // "Definite" or "Indefinite"
    val contractStart: String,
    val profilePhoto: String = "",
    val activeCompany: String = "Shawamikh Corp (شوامخ)"
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val date: String, // YYYY-MM-DD
    val clockInTime: String, // HH:MM
    val clockOutTime: String? = null, // HH:MM
    val latitudeIn: Double,
    val longitudeIn: Double,
    val latitudeOut: Double? = null,
    val longitudeOut: Double? = null,
    val photoInBase64: String? = null, // Image capture for verify authentication
    val photoOutBase64: String? = null,
    val status: String, // "Present", "Late", "Excused", "Absent"
    val delayMinutes: Int = 0,
    val overtimeHours: Double = 0.0,
    val workedHours: Double = 0.0,
    val note: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val assignedToId: String, // Custom Employee ID
    val assignedToName: String, // Cache employee name
    val latitude: Double, // Task geolocalized position
    val longitude: Double,
    val status: String, // "Pending", "InProgress", "Completed", "OnHold"
    val beforePhotoBase64: String? = null,
    val afterPhotoBase64: String? = null,
    val reportedNotes: String? = null,
    val completionTime: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaves")
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val leaveTypeAr: String,
    val leaveTypeEn: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val reason: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val totalDays: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String, // "AUTH_LOGIN", "CHECK_IN", "PAYROLL_APPROVE", "LEAVE_REJECT"
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class CompanyRepository(private val db: AppDatabase) {

    val allEmployees: Flow<List<EmployeeEntity>> = db.employeeDao().getAllEmployees()
    val allAttendance: Flow<List<AttendanceEntity>> = db.attendanceDao().getAllAttendance()
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    val allLeaves: Flow<List<LeaveEntity>> = db.leaveDao().getAllLeaves()
    val recentLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getRecentLogs()

    suspend fun getEmployeeById(empId: String): EmployeeEntity? = db.employeeDao().getEmployeeById(empId)
    suspend fun getEmployeeByEmail(email: String): EmployeeEntity? = db.employeeDao().getEmployeeByEmail(email)

    suspend fun insertEmployee(employee: EmployeeEntity) = db.employeeDao().insertEmployee(employee)
    suspend fun updateEmployee(employee: EmployeeEntity) = db.employeeDao().updateEmployee(employee)
    suspend fun deleteEmployee(employee: EmployeeEntity) = db.employeeDao().deleteEmployee(employee)

    fun getAttendanceForEmployee(empId: String): Flow<List<AttendanceEntity>> = db.attendanceDao().getAttendanceForEmployee(empId)
    suspend fun getTodayAttendance(empId: String, date: String): AttendanceEntity? = db.attendanceDao().getAttendanceForEmployeeAndDate(empId, date)
    suspend fun insertAttendance(attendance: AttendanceEntity) = db.attendanceDao().insertAttendance(attendance)
    suspend fun updateAttendance(attendance: AttendanceEntity) = db.attendanceDao().updateAttendance(attendance)

    fun getTasksForEmployee(empId: String): Flow<List<TaskEntity>> = db.taskDao().getTasksForEmployee(empId)
    suspend fun insertTask(task: TaskEntity) = db.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().updateTask(task)
    suspend fun deleteTaskById(id: Int) = db.taskDao().deleteTaskById(id)

    fun getLeavesForEmployee(empId: String): Flow<List<LeaveEntity>> = db.leaveDao().getLeavesForEmployee(empId)
    suspend fun insertLeave(leave: LeaveEntity) = db.leaveDao().insertLeave(leave)
    suspend fun updateLeave(leave: LeaveEntity) = db.leaveDao().updateLeave(leave)

    suspend fun insertLog(action: String, performedBy: String, details: String) {
        db.auditLogDao().insertLog(
            AuditLogEntity(
                action = action,
                performedBy = performedBy,
                details = details
            )
        )
    }

    // Saudi Labor and Payroll Calculator Helper:
    // Saudi citizens: 9% GOSI deduction from (Base + Housing allowance) up to 45,000 SAR max
    // Non-Saudis: No worker-paid GOSI deduction (GOSI is paid entirely by employer at 2% for work injury)
    fun calculatePayroll(emp: EmployeeEntity): PayrollSlip {
        val base = emp.salary
        val housing = emp.housingAllowance
        val transport = emp.transportAllowance
        val other = emp.otherAllowances
        val grossEarnings = base + housing + transport + other

        val gosiSubject = base + housing
        val gosiEmployeeDeduction = if (emp.isSaudi) {
            val subjectBounded = gosiSubject.coerceAtMost(45000.0)
            subjectBounded * 0.09
        } else {
            0.0 // Non-Saudis do not have GOSI deducted from salary under normal conditions
        }

        // Standard deduction template (e.g. Absences / Late deductions could go here if state was fully calculated)
        val grossDeductions = gosiEmployeeDeduction
        val netSalary = grossEarnings - grossDeductions

        return PayrollSlip(
            employeeId = emp.employeeId,
            nameAr = emp.nameAr,
            nameEn = emp.nameEn,
            isSaudi = emp.isSaudi,
            baseSalary = base,
            housingAllowance = housing,
            transportAllowance = transport,
            otherAllowances = other,
            grossEarnings = grossEarnings,
            gosiDeduction = gosiEmployeeDeduction,
            totalDeductions = grossDeductions,
            netSalary = netSalary
        )
    }

    suspend fun seedDatabaseAll() {
        // Seeding database if the Employee Table is empty
        val list = db.employeeDao().getAllEmployees().first()
        if (list.isNotEmpty()) return

        // 1. Seed Strategic Saudi and Expat Employees
        val seedEmployees = listOf(
            EmployeeEntity(
                employeeId = "SH-1001",
                nameAr = "عبدالرحمن الشمري",
                nameEn = "Abdulrahman Al-Shammari",
                role = "Admin",
                saudiId = "1098273645",
                isSaudi = true,
                iqamaExpiry = "2029-08-30",
                email = "admin@shawamikh.com",
                phone = "0501234567",
                department = "HR & Administration",
                designationAr = "مدير الموارد البشرية",
                designationEn = "Chief HR Officer",
                salary = 16000.0,
                housingAllowance = 4000.0,
                transportAllowance = 1500.0,
                gosiContribution = 1800.0, // calculated later
                leaveBalance = 30,
                contractStart = "2020-03-01",
                profilePhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=250"
            ),
            EmployeeEntity(
                employeeId = "SH-1002",
                nameAr = "ياسر قحطاني",
                nameEn = "Yasser Qahtani",
                role = "Employee",
                saudiId = "1104839201",
                isSaudi = true,
                iqamaExpiry = "2030-05-12",
                email = "yasser@shawamikh.com",
                phone = "0539876543",
                department = "Operations",
                designationAr = "مشرف عمليات ميدانية",
                designationEn = "Field Operations Supervisor",
                salary = 9500.0,
                housingAllowance = 2375.0,
                transportAllowance = 1000.0,
                gosiContribution = 1068.75,
                leaveBalance = 24,
                contractStart = "2022-07-15",
                profilePhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=250"
            ),
            EmployeeEntity(
                employeeId = "SH-1003",
                nameAr = "سارة العتيبي",
                nameEn = "Sara Al-Otaibi",
                role = "Employee",
                saudiId = "1087462839",
                isSaudi = true,
                iqamaExpiry = "2028-11-04",
                email = "sara@shawamikh.com",
                phone = "0543219876",
                department = "Engineering",
                designationAr = "مهندس نظم برمجية",
                designationEn = "Systems Software Engineer",
                salary = 12000.0,
                housingAllowance = 3000.0,
                transportAllowance = 1200.0,
                gosiContribution = 1350.0,
                leaveBalance = 28,
                contractStart = "2023-01-10",
                profilePhoto = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=250"
            ),
            EmployeeEntity(
                employeeId = "SH-1004",
                nameAr = "أحمد رضوان",
                nameEn = "Ahmed Radwan",
                role = "Employee",
                saudiId = "2394871029", // Expatriate ID starts with 2
                isSaudi = false,
                iqamaExpiry = "2026-10-14",
                email = "ahmed.r@shawamikh.com",
                phone = "0554433221",
                department = "Operations",
                designationAr = "فني صيانة أنظمة أول",
                designationEn = "Senior Systems Technician",
                salary = 6500.0,
                housingAllowance = 1500.0,
                transportAllowance = 800.0,
                gosiContribution = 0.0, // Expatriate, paid entirely by company at 2% for hazard
                leaveBalance = 21,
                contractStart = "2021-11-20",
                profilePhoto = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?q=80&w=250"
            ),
            EmployeeEntity(
                employeeId = "SH-1005",
                nameAr = "رياض محمد",
                nameEn = "Riyadh Mohamed",
                role = "Employee",
                saudiId = "2493019283",
                isSaudi = false,
                iqamaExpiry = "2026-11-02",
                email = "riyadh@shawamikh.com",
                phone = "0563728192",
                department = "Sales",
                designationAr = "ممثل مبيعات إقليمي",
                designationEn = "Regional Sales Coordinator",
                salary = 7200.0,
                housingAllowance = 1800.0,
                transportAllowance = 1000.0,
                gosiContribution = 0.0,
                leaveBalance = 18,
                contractStart = "2024-02-05",
                profilePhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=250"
            )
        )

        db.employeeDao().insertEmployees(seedEmployees)

        // 2. Seed Realistic Geofenced Operations Tasks (Near Riyadh business landmarks)
        val seedTasks = listOf(
            TaskEntity(
                titleAr = "فحص أنظمة الاتصال - مركز الملك عبد الله المالي (KAFD)",
                titleEn = "Inspect Communication Systems - King Abdullah Financial District",
                descriptionAr = "القيام بالفحص الوقائي الدوري لغرف اتصالات الأبراج المركزية في المربع المفتوح رقم 4.",
                descriptionEn = "Conduct routine preventative technical audits of telecom hubs in tower sector 4 within KAFD coordinates.",
                assignedToId = "SH-1002",
                assignedToName = "Yasser Qahtani",
                latitude = 24.7622, // KAFD Riyadh coordinates
                longitude = 46.6410,
                status = "InProgress"
            ),
            TaskEntity(
                titleAr = "تركيب واجهات التحكم ومجسات الحرارة - واجهة الرياض",
                titleEn = "Install Controller Interfaces & Sensors - Riyadh Front",
                descriptionAr = "ربط وتأكيد مستشعرات إنترنت الأشياء الذكية مع لوحة المتابعة الرئيسية لنظام شوامخ لإرسال الإشعارات والتحذيرات الفورية لغرفة التشغيل المباشر.",
                descriptionEn = "Bind and calibrate dual-band IoT environmental monitors with the Shawamikh central gateway to feed real-time alert streams.",
                assignedToId = "SH-1004",
                assignedToName = "Ahmed Radwan",
                latitude = 24.8143, // Riyadh Front
                longitude = 46.7261,
                status = "Pending"
            ),
            TaskEntity(
                titleAr = "إعادة تقييم معايير السلامة المهنية والمخاطر الميدانية للموقع رقم 12",
                titleEn = "Perform Hazard & Occupational Safety Review on Site 12",
                descriptionAr = "مراجعة شاملة لبروتوكولات الأمان والسلامة في موقع العمليات وفقاً للائحة السلامة والصحة المهنية الصادرة عن وزارة الموارد البشرية السعودية.",
                descriptionEn = "In-situ inspection of operations scaffolding and warning layouts to comply with the latest KSA Ministry of Human Resources safety bulletins.",
                assignedToId = "SH-1002",
                assignedToName = "Yasser Qahtani",
                latitude = 24.7136, // Centered in Olaya, Riyadh
                longitude = 46.6753,
                status = "Completed",
                reportedNotes = "تم تنفيذ الكشف بالكامل والأمور متوافقة تماماً مع معايير الوزارة.",
                completionTime = "Today, 10:15 AM"
            )
        )

        for (task in seedTasks) {
            db.taskDao().insertTask(task)
        }

        // 3. Seed Leaves Approvals for Dashboard Interaction
        val seedLeaves = listOf(
            LeaveEntity(
                employeeId = "SH-1003",
                employeeName = "Sara Al-Otaibi",
                leaveTypeAr = "إجازة سنوية اعتيادية",
                leaveTypeEn = "Standard Annual Leave (Saudi Labor Art 109)",
                startDate = "2026-06-10",
                endDate = "2026-06-25",
                reason = "إجازة زواج ودراسية عائلية مجدولة مسبقاً بالتنسيق مع رئيس القسم.",
                status = "Pending",
                totalDays = 15
            ),
            LeaveEntity(
                employeeId = "SH-1002",
                employeeName = "Yasser Qahtani",
                leaveTypeAr = "إجازة مرضية معتمدة",
                leaveTypeEn = "Medical Leave (Fully Paid - Art 117)",
                startDate = "2026-05-01",
                endDate = "2026-05-04",
                reason = "وعكة صحية طارئة وتقديم تقرير طبي من مستشفى الحبيب بالرياض.",
                status = "Approved",
                totalDays = 3
            )
        )

        for (leave in seedLeaves) {
            db.leaveDao().insertLeave(leave)
        }

        // 4. Seed Attendance Records
        val seedAttendance = listOf(
            AttendanceEntity(
                employeeId = "SH-1002",
                date = "2026-05-27",
                clockInTime = "07:44",
                clockOutTime = "16:02",
                latitudeIn = 24.7621,
                longitudeIn = 46.6409,
                status = "Present",
                delayMinutes = 0,
                overtimeHours = 0.2,
                workedHours = 8.3
            ),
            AttendanceEntity(
                employeeId = "SH-1003",
                date = "2026-05-27",
                clockInTime = "08:15",
                clockOutTime = "17:00",
                latitudeIn = 24.7132,
                longitudeIn = 46.6751,
                status = "Late",
                delayMinutes = 15,
                workedHours = 8.75
            )
        )

        for (att in seedAttendance) {
            db.attendanceDao().insertAttendance(att)
        }

        // 5. Audit Log Seed
        db.auditLogDao().insertLog(
            AuditLogEntity(
                action = "SYSTEM_INIT",
                performedBy = "System Core Agent",
                details = "Initialized Shawamikh HR Database, uploaded compliant Saudi GOSI algorithms and initial operational assets."
            )
        )
    }
}

data class PayrollSlip(
    val employeeId: String,
    val nameAr: String,
    val nameEn: String,
    val isSaudi: Boolean,
    val baseSalary: Double,
    val housingAllowance: Double,
    val transportAllowance: Double,
    val otherAllowances: Double,
    val grossEarnings: Double,
    val gosiDeduction: Double,
    val totalDeductions: Double,
    val netSalary: Double
)

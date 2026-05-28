package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShawamikhAppScreen(
    viewModel: ShawamikhViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Base properties
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val role by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentEmp by viewModel.activeEmployeeState.collectAsStateWithLifecycle()

    // Flows
    val empList by viewModel.employees.collectAsStateWithLifecycle()
    val attendanceList by viewModel.attendance.collectAsStateWithLifecycle()
    val taskList by viewModel.tasks.collectAsStateWithLifecycle()
    val leaveList by viewModel.leaves.collectAsStateWithLifecycle()
    val auditList by viewModel.auditLogs.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Workstation/Registry, 1: Tasks, 2: AI Advisor, 3: Payroll

    // Text directionality
    val isRtl = lang == "ar"
    val textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
    val textAlignment = if (isRtl) TextAlign.Right else TextAlign.Left

    // Bilingual translation helper
    fun t(ar: String, en: String): String = if (isRtl) ar else en

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ش",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = t("شوامخ للتشغيل والـ HR", "Shawamikh HMS"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = t("المطابقة والتتبع الذكي", "Compliance & Tracking"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("lang_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Theme Switcher
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Theme Toggle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Session Role Selector Switcher
                    Button(
                        onClick = {
                            if (role == "Admin") {
                                viewModel.switchUserRole("Employee", "SH-1002")
                            } else {
                                viewModel.switchUserRole("Admin", "SH-1001")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("role_switcher")
                    ) {
                        Text(
                            text = if (role == "Admin") t("الجزيرة للموظفين 🧑‍💻", "Employee UI") else t("لوحة الإدارة 📊", "Admin UI"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(t("الرئيسية", "Dashboard"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                    label = { Text(t("المهام", "Tasks"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tasks")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI HR Advisor") },
                    label = { Text(t("مستشار الذكاء", "Smart AI"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_ai")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Payroll") },
                    label = { Text(t("الرواتب والأجور", "Payroll"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_payroll")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Interactive header showing active simulated entity role session and Quick Stats
            ActiveSessionBanner(role = role, currentEmp = currentEmp, isRtl = isRtl, t = ::t)

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        if (role == "Admin") {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                employees = empList,
                                leaves = leaveList,
                                logs = auditList,
                                attendanceList = attendanceList,
                                isRtl = isRtl,
                                t = ::t
                            )
                        } else {
                            EmployeeWorkCenterScreen(
                                viewModel = viewModel,
                                currentEmp = currentEmp,
                                attendanceList = attendanceList,
                                isRtl = isRtl,
                                t = ::t
                            )
                        }
                    }
                    1 -> {
                        TasksManagerPanel(
                            viewModel = viewModel,
                            taskList = taskList,
                            currentEmp = currentEmp,
                            role = role,
                            isRtl = isRtl,
                            t = ::t
                        )
                    }
                    2 -> {
                        SaudiAIAdvisorPanel(
                            viewModel = viewModel,
                            isRtl = isRtl,
                            t = ::t
                        )
                    }
                    3 -> {
                        PayrollCompliancePanel(
                            viewModel = viewModel,
                            employees = empList,
                            currentEmp = currentEmp,
                            role = role,
                            isRtl = isRtl,
                            t = ::t
                        )
                    }
                }
            }
        }
    }
}

// BANNERS: Illustrating current simulated environment values
@Composable
fun ActiveSessionBanner(
    role: String,
    currentEmp: EmployeeEntity?,
    isRtl: Boolean,
    t: (String, String) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (role == "Admin") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = "Session Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (role == "Admin") {
                            t("جلسة الإدارة العامة: عبدالرحمن الشمري", "General Admin Session: Abdulrahman")
                        } else {
                            t("المستخدم الميداني: ${currentEmp?.nameAr ?: ""}", "Field Engineer: ${currentEmp?.nameEn ?: ""}")
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (role == "Admin") {
                            t("صلاحيات كاملة | نظام موارد شوامخ الموحد", "Full Authority | Shawamikh Consolidated HRMS")
                        } else {
                            t("الرقم الوظيفي: ${currentEmp?.employeeId ?: ""} · قسم ${currentEmp?.department ?: ""}", "Staff ID: ${currentEmp?.employeeId ?: ""} · Dept: ${currentEmp?.department ?: ""}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (role == "Admin") t("المشرف العام", "SUPER ADMIN") else t("مستخدم ميدان", "FIELD WORKER"),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 1. ADMIN DASHBOARD VIEW
// ==========================================
@Composable
fun AdminDashboardScreen(
    viewModel: ShawamikhViewModel,
    employees: List<EmployeeEntity>,
    leaves: List<LeaveEntity>,
    logs: List<AuditLogEntity>,
    attendanceList: List<AttendanceEntity>,
    isRtl: Boolean,
    t: (String, String) -> String
) {
    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("admin_dashboard_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Corporate KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = t("التحليلات والمؤشرات الوطنية لوزارة الموارد البشرية", "National & Corporate KPI Analytics"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard(
                        title = t("إجمالي القوة العاملة", "Total Workforce"),
                        value = employees.size.toString(),
                        subText = t("موظفين نشطين", "Active Employees"),
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = t("طلبات الإجازة المعلقة", "Pending Leaves"),
                        value = leaves.count { it.status == "Pending" }.toString(),
                        subText = t("بانتظار موافقتك", "Awaiting HR approval"),
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard(
                        title = t("حضور اليوم الميداني", "Today Attendance"),
                        value = "${attendanceList.count { it.date == "2026-05-27" }}", // Simulated today
                        subText = t("عمليات تحضير ناجحة", "Successful Geo Checkins"),
                        icon = Icons.Default.GpsFixed,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = t("نسبة توطين السعودة", "Saudization Code"),
                        value = "60%",
                        subText = t("نطاق أخضر مرتفع 🇸🇦", "High Green Nitaqat"),
                        icon = Icons.Default.VerifiedUser,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Beautiful Live Graphics: Nitaqat Speedometer Gauge via Custom Canvas!
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = t("مقياس السعودة الموحد (نطاقات وزارة الموارد)", "Unified Saudization Nitaqat Indicator"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t(
                            "يقع نطاق شركة شوامخ ضمن النطاق الأخضر المرتفع (سعودة بلغت 60.0% متوافقة مع الأوزان والأبعاد الهيكلية قوى)",
                            "Shawamikh Saudization rests stable within High Green (60.0% compliances matched with Qiwa rules)."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    NitaqatMeterGauge(saudizationPercent = 60.0)
                }
            }
        }

        // Active Leaves Verification Queue
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("طلبات الإجازات تحت الإجراء (المادة 109)", "Awaiting HR Approvals (KSA Art 109)"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = t("متوافق", "Compliant"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val pendingList = leaves.filter { it.status == "Pending" }
                    if (pendingList.isEmpty()) {
                        Text(
                            text = t("لا توجد طلبات إجازة معلقة حالياً.", "All leave requests cleared. 0 pending."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            pendingList.forEach { leave ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = leave.employeeName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${leave.totalDays} " + t("أيام", "days"),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Text(
                                            text = "${leave.startDate} " + t("إلى", "to") + " ${leave.endDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = t("النوع: ${leave.leaveTypeAr}", "Type: ${leave.leaveTypeEn}"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = t("السبب: ${leave.reason}", "Reason: ${leave.reason}"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.rejectLeave(leave) },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.sizeIn(minWidth = 80.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(t("رفض", "Reject"), fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { viewModel.approveLeave(leave) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.sizeIn(minWidth = 80.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(t("اعتماد وموافقة", "Approve"), fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Corporate Employee List with Add Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("سجل الموظفين والمستندات وعقود قوى", "Staff Roster & Qiwa Contracts"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showAddEmployeeDialog = true },
                            modifier = Modifier.testTag("admin_add_employee_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add employee",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        employees.forEach { emp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emp.nameEn.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = t(emp.nameAr, emp.nameEn),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${emp.employeeId} · " + t(emp.designationAr, emp.designationEn),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${emp.salary.roundToInt()} SAR",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (emp.isSaudi) "سعودي 🇸🇦" else "مقيم  🛂",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (emp.isSaudi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // PDPL Compliance Logs
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("سجل تدقيق الأمان والامتثال (PDPL Audit)", "Security & PDPL Audit Logs"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = t("مشفر محلياً", "Locally Encrypted"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = t(
                            "يضمن هذا السجل تتبع استخدام الصلاحيات ورفع المستندات ومطابقة لوائح حماية البيانات الشخصية السعودية.",
                            "Maintains verified traceability of user permissions according to KSA personal data protection regulations."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        logs.take(5).forEach { log ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[${log.action}]",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Done by: ${log.performedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal popup to register new employee
    if (showAddEmployeeDialog) {
        var empId by remember { mutableStateOf("SH-100" + (employees.size + 1)) }
        var nameAr by remember { mutableStateOf("") }
        var nameEn by remember { mutableStateOf("") }
        var nationalId by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var isSaudiCitizen by remember { mutableStateOf(true) }
        var department by remember { mutableStateOf("Operations") }
        var desAr by remember { mutableStateOf("") }
        var desEn by remember { mutableStateOf("") }
        var baseSalary by remember { mutableStateOf("") }
        var hAllowance by remember { mutableStateOf("") }
        var tAllowance by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddEmployeeDialog = false },
            title = { Text(t("إضافة موظف وعقد جديد لقوى", "Add Employee & Register Contract")) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = empId,
                        onValueChange = { empId = it },
                        label = { Text(t("الرقم الوظيفي", "Employee ID")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_empid_input")
                    )
                    TextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(t("الاسم الكامل (عربي)", "Full Name (Arabic)")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_namear_input")
                    )
                    TextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(t("الاسم الكامل (إنجليزي)", "Full Name (English)")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_nameen_input")
                    )
                    TextField(
                        value = nationalId,
                        onValueChange = { nationalId = it },
                        label = { Text(t("رقم الهوية / الإقامة", "Saudi National ID / Iqama")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSaudiCitizen,
                            onCheckedChange = { isSaudiCitizen = it }
                        )
                        Text(t("الموظف مواطن سعودي", "Employee is a Saudi Citizen"))
                    }
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(t("البريد الإلكتروني", "Email Address")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(t("رقم الجوال", "Mobile Phone")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text(t("القسم", "Department")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = desAr,
                        onValueChange = { desAr = it },
                        label = { Text(t("المسمى الوظيفي (عربي)", "Job Title (Arabic)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = desEn,
                        onValueChange = { desEn = it },
                        label = { Text(t("المسمى الوظيفي (إنجليزي)", "Job Title (English)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = baseSalary,
                        onValueChange = { baseSalary = it },
                        label = { Text(t("الراتب الأساسي (SAR)", "Base Salary (SAR)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = hAllowance,
                        onValueChange = { hAllowance = it },
                        label = { Text(t("بدل السكن (SAR)", "Housing Allowance (SAR)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = tAllowance,
                        onValueChange = { tAllowance = it },
                        label = { Text(t("بدل النقل (SAR)", "Transport Allowance (SAR)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val base = baseSalary.toDoubleOrNull() ?: 5000.0
                        val housing = hAllowance.toDoubleOrNull() ?: (base * 0.25)
                        val transport = tAllowance.toDoubleOrNull() ?: 500.0
                        
                        // Prefind GOSI
                        val gosiVal = if (isSaudiCitizen) (base + housing) * 0.09 else 0.0

                        val newEmp = EmployeeEntity(
                            employeeId = empId,
                            nameAr = nameAr.ifBlank { "موظف جديد" },
                            nameEn = nameEn.ifBlank { "New Employee" },
                            role = "Employee",
                            saudiId = nationalId.ifBlank { "1000000000" },
                            isSaudi = isSaudiCitizen,
                            iqamaExpiry = "2029-12-30",
                            email = email.ifBlank { "new@shawamikh.com" },
                            phone = phone.ifBlank { "0500000000" },
                            department = department,
                            designationAr = desAr.ifBlank { "أخصائي عمليات" },
                            designationEn = desEn.ifBlank { "Operations Specialist" },
                            salary = base,
                            housingAllowance = housing,
                            transportAllowance = transport,
                            gosiContribution = gosiVal,
                            contractStart = "2026-05-28"
                        )
                        viewModel.addEmployee(newEmp)
                        showAddEmployeeDialog = false
                    },
                    modifier = Modifier.testTag("admin_submit_employee_btn")
                ) {
                    Text(t("حفظ وتسجيل", "Save & Register"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEmployeeDialog = false }) {
                    Text(t("إلغاء", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subText,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

// Nitaqat meter drawing
@Composable
fun NitaqatMeterGauge(saudizationPercent: Double) {
    val meterColorMap = listOf(
        Color(0xFFEF4444), // Red
        Color(0xFFFBBF24), // Yellow
        Color(0xFF10B981), // Green
        Color(0xFF047857), // Platinum / High Green
        Color(0xFF1E3A8A)  // Platinum Luxury
    )

    Box(
        modifier = Modifier
            .size(170.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Scale segment arcs
            // Semi-circle starts from 180 degrees down to 0 (top hemisphere)
            // Draw segment 1 Red (180 to 216 - 36deg)
            drawArc(
                color = Color(0xFFEF4444),
                startAngle = 180f,
                sweepAngle = 36f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            // Segment 2 Yellow (216 to 252 - 36deg)
            drawArc(
                color = Color(0xFFFBBF24),
                startAngle = 216f,
                sweepAngle = 36f,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            // Segment 3 Green (252 to 288 - 36deg)
            drawArc(
                color = Color(0xFF10B981),
                startAngle = 252f,
                sweepAngle = 36f,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            // Segment 4 High Green (288 to 324 - 36deg)
            drawArc(
                color = Color(0xFF047857),
                startAngle = 288f,
                sweepAngle = 36f,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            // Segment 5 Platinum (324 to 360 - 36deg)
            drawArc(
                color = Color(0xFF1E3A8A),
                startAngle = 324f,
                sweepAngle = 40f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Draw speedometer pointer needle based on percentage (0 to 100 becomes 180 to 360 angle)
            val angle = 180f + (saudizationPercent.toFloat() / 100f) * 180f
            val rad = angle * Math.PI / 180.0
            val rLength = size.width * 0.42f
            val needleEnd = Offset(
                (size.width / 2) + (rLength * kotlin.math.cos(rad)).toFloat(),
                (size.height / 2) + (rLength * kotlin.math.sin(rad)).toFloat()
            )

            // Needle Base Center
            drawCircle(
                color = Color(0xFF334155),
                radius = 12f
            )

            drawLine(
                color = Color(0xFF334155),
                start = Offset(size.width / 2, size.height / 2),
                end = needleEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Column(
            modifier = Modifier.offset(y = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "60.0%",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "سعودة مستهدفة",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// ==========================================
// 2. EMPLOYEE PROFILE WORKSTATION SCREEN (Holographic and Interactive)
// ==========================================
@Composable
fun EmployeeWorkCenterScreen(
    viewModel: ShawamikhViewModel,
    currentEmp: EmployeeEntity?,
    attendanceList: List<AttendanceEntity>,
    isRtl: Boolean,
    t: (String, String) -> String
) {
    val simLat by viewModel.simLatitude.collectAsStateWithLifecycle()
    val simLng by viewModel.simLongitude.collectAsStateWithLifecycle()
    val activeOffice by viewModel.selectedGeoOffice.collectAsStateWithLifecycle()

    var cameraSimState by remember { mutableStateOf(false) }
    var mockCapturedBase64 by remember { mutableStateOf<String?>(null) }
    var attendanceNoticeMsg by remember { mutableStateOf("") }
    var applyLeaveDialogState by remember { mutableStateOf(false) }

    // Distance computations for geofence authorization checking
    val distToKafd = viewModel.calculateDistanceToOffice("KAFD")
    val distToRiyadhFront = viewModel.calculateDistanceToOffice("RiyadhFront")

    val selectedDistance = if (activeOffice == "KAFD") distToKafd else distToRiyadhFront
    val withInGeofenceBound = viewModel.isWithinGeofence(activeOffice)

    val todayDateStr = SimpleDateFormat("YYYY-MM-DD", Locale.US).format(Date())
    val myAttendanceForToday = attendanceList.firstOrNull { it.employeeId == currentEmp?.employeeId && it.date == todayDateStr }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic location selector to check Geofencing live logic
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t("محاكي التموضع ونظام الإبعاد الجغرافي (Geofence)", "Geofence Controller & GPS Simulator"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.setSimulatedLocation(activeOffice) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Text(
                    text = t("اختر تموضع الهاتف لتجربة قوانين التحضير الذكي للشركة:", "Select phone positioning to test smart lock boundaries:"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.setSimulatedLocation("KAFD") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeOffice == "KAFD") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeOffice == "KAFD") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("gps_kafd")
                    ) {
                        Text(text = "KAFD (Riyadh)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setSimulatedLocation("RiyadhFront") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeOffice == "RiyadhFront") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeOffice == "RiyadhFront") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("gps_front")
                    ) {
                        Text(text = "Riyadh Front", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setSimulatedLocation("FarAway") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeOffice == "FarAway") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeOffice == "FarAway") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).testTag("gps_desert")
                    ) {
                        Text(text = t("البر / خارج النطاق", "Desert Out range"), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS status block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("المكتب المستهدف للتحضير:", "Target Clock-in Hub:"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (activeOffice == "KAFD") "مركز كافد المالي (KAFD)" else if (activeOffice == "RiyadhFront") "واجهة الرياض" else "خارج المركز المخطط",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("عنونة الإحداثيات النشطة:", "Current GPS Fix:"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Lat: ${String.format("%.4f", simLat)} , Lng: ${String.format("%.4f", simLng)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("الارتداد الجغرافي للمكتب:", "Hub Geodistance offset:"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${selectedDistance.roundToInt()} " + t("أمتار", "meters"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (withInGeofenceBound) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (withInGeofenceBound) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, "Inside", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = t("أنت بداخل النطاق المعتمد (أقل من 200م) - التحضير متاح 🟢", "Inside certified hub boundary (<200m). Clock-in UNLOCKED."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, "Outside", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = t("أنت خارج النطاق المعتمد للتحضير - الزر مقفل 🔴", "Outside corporate geo-ranges. Clock-in LOCKED."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Attendance Actions card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("التحضير الذكي اليومي (حفظ الصورة وصحة GPS)", "Smart Attendance & Bio Check-in"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = t("يتطلب النظام التقاط صورة شخصية ومطابقة التموضع الجغرافي المسموح به لمنع تزييف المواقع.", "System requires selfie bio-authentication and GPS matching to prevent site spoofings under Saudi PDPL."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (myAttendanceForToday == null) {
                    // Checkin flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { cameraSimState = true },
                            enabled = withInGeofenceBound,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(52.dp)
                                .testTag("clockin_btn")
                        ) {
                            Icon(Icons.Default.Fingerprint, "Fingerprint", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(t("التحضير الذكي الفوري (سيلفي)", "Instantly Clock-In (Selfie)"), fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    // Current Status Card & Checkout Action
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = t("تم تسجيل حضورك لليوم بنجاح 🎉", "You are successfully Clocked-In for today! 🎉"),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t("وقت الدخول", "Clock-in"), fontSize = 11.sp)
                                Text(myAttendanceForToday.clockInTime, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t("وقت الخروج", "Clock-out"), fontSize = 11.sp)
                                Text(myAttendanceForToday.clockOutTime ?: t("قيد العمل", "Active"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (myAttendanceForToday.clockOutTime == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        if (myAttendanceForToday.clockOutTime == null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.performClockOut {
                                        attendanceNoticeMsg = "Checked out for today!"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(42.dp)
                                    .testTag("clockout_btn")
                            ) {
                                Icon(Icons.Default.ExitToApp, "Exit")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(t("تسجيل خروج", "Clock-Out Now"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick leave actions & Payslip button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("لوحة التحكم بالطلبات والإجازات والأجور", "Requests & Leave Management Center"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = t("رصيد إجازاتك المتوفر (المادة 109):", "Available leave balance (Art 109):"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${currentEmp?.leaveBalance ?: 30} " + t("يوماً سنوية مدفوعة", "days annual paid"),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = { applyLeaveDialogState = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("request_leave_btn")
                    ) {
                        Icon(Icons.Default.CalendarToday, "Leave")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(t("تقديم إجازة", "Apply Leave"))
                    }
                }
            }
        }
    }

    // Selfie Camera Check Simulator
    if (cameraSimState) {
        var cameraStatusText by remember { mutableStateOf("Ready to scan face...") }
        var hasSnappedImage by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { cameraSimState = false },
            title = { Text(t("التحقق من الصورة الذاتية (Bio-Authentication)", "Biometric Facial Authentication")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t("جاري التحقق الجيو-مكاني لموقعك: " + activeOffice, "Verifying geospatial alignment with office hub: " + activeOffice),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Camera Iris Circle: Draws are visual mockup
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(3.dp, if (hasSnappedImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!hasSnappedImage) {
                            // Radar scanner line animation representation
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color(0x3300C4A7),
                                    radius = size.width * 0.45f
                                )
                                drawLine(
                                    color = Color(0xFF00C4A7),
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, "Camera Scan", tint = Color.LightGray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("CAMERA ACTIVE", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Switched user avatar representing capture
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Check, "Snapped", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("FACIAL SIGNATURE PINNED", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (!hasSnappedImage) t("الرجاء وضع وجهك داخل الإطار الدائري", "Align corporate device camera within circular boundaries") else t("سلسلة المعايير متطابقة والموقع مؤمّن. التحضير متاح.", "Locational boundaries validated. Token keys matching."),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                if (!hasSnappedImage) {
                    Button(onClick = {
                        hasSnappedImage = true
                        cameraStatusText = "Face structure matched!"
                    }) {
                        Text(t("التقاط ومطابقة", "Capture Face Map"))
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.performClockIn(activeOffice, "MOCK_SELFIE_BASE64_SHAWAMIKH") {
                                cameraSimState = false
                            }
                        },
                        modifier = Modifier.testTag("camera_submit_checkin")
                    ) {
                        Text(t("تسجيل الحضور النهائي", "Complete Verified Clock-In"))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { cameraSimState = false }) {
                    Text(t("إلغاء", "Close"))
                }
            }
        )
    }

    // Modal to request a new leave of absence
    if (applyLeaveDialogState) {
        var leaveArType by remember { mutableStateOf("إجازة سنوية اعتيادية") }
        var leaveEnType by remember { mutableStateOf("Annual Paid Leave - Art 109") }
        var leaveDays by remember { mutableStateOf("5") }
        var leaveReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { applyLeaveDialogState = false },
            title = { Text(t("تقديم طلب إجازة رسمي عمالي", "Submit Official Leave Application")) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = t("يتم تقديم طلبات الإجازة لتتم مراجعتها من قبل شؤون الموظفين وفقاً لقانون العمل السعودي.", "All leaves are channeled to direct administration for review against KSA employment criteria."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    TextField(
                        value = leaveArType,
                        onValueChange = { leaveArType = it },
                        label = { Text(t("نوع الإجازة (عربي)", "Leave Type (Arabic)")) },
                        modifier = Modifier.fillMaxWidth().testTag("leave_type_input")
                    )

                    TextField(
                        value = leaveEnType,
                        onValueChange = { leaveEnType = it },
                        label = { Text(t("نوع الإجازة (إنجليزي)", "Leave Type (English)")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = leaveDays,
                        onValueChange = { leaveDays = it },
                        label = { Text(t("عدد الأيام المطلوبة", "Requested days")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("leave_days_input")
                    )

                    TextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = { Text(t("السبب / التوضيح للشؤون", "HR Justification notes")) },
                        modifier = Modifier.fillMaxWidth().testTag("leave_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = leaveDays.toIntOrNull() ?: 3
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val start = formatter.format(Date(System.currentTimeMillis() + 86400000 * 2)) // starts in 2 days
                        val end = formatter.format(Date(System.currentTimeMillis() + 86400000 * (2 + days)))

                        viewModel.requestLeave(
                            typeAr = leaveArType,
                            typeEn = leaveEnType,
                            start = start,
                            end = end,
                            reason = leaveReason,
                            days = days
                        )
                        applyLeaveDialogState = false
                    },
                    modifier = Modifier.testTag("submit_leave_req_btn")
                ) {
                    Text(t("إرسال الطلب", "Transmit Application"))
                }
            },
            dismissButton = {
                TextButton(onClick = { applyLeaveDialogState = false }) {
                    Text(t("إلغاء", "Dismiss"))
                }
            }
        )
    }
}

// ==========================================
// 3. TASKS AND FIELD OPERATIONS PANEL
// ==========================================
@Composable
fun TasksManagerPanel(
    viewModel: ShawamikhViewModel,
    taskList: List<TaskEntity>,
    currentEmp: EmployeeEntity?,
    role: String,
    isRtl: Boolean,
    t: (String, String) -> String
) {
    var showCreateTaskDialog by remember { mutableStateOf(false) }

    val myTasks = if (role == "Admin") taskList else taskList.filter { it.assignedToId == currentEmp?.employeeId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (role == "Admin") t("التحكم والمتابعة بالتشغيل الميداني", "Operations & Field Controls") else t("مهامي الميدانية النشطة", "My Active Deployments"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = t("التتبع الجغرافي وحفظ تقارير قبل وبعد", "Slight site trackings with visual logging support"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            if (role == "Admin") {
                Button(
                    onClick = { showCreateTaskDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_add_task_btn")
                ) {
                    Icon(Icons.Default.Add, "AddTask")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(t("مهمة جديدة", "New task"), fontSize = 11.sp)
                }
            }
        }

        if (myTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = t("لا توجد مهام نشطة مسجلة لك حالياً.", "No active field tasks mapped to this account."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("tasks_list_scroll"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(myTasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (task.status) {
                                                "Completed" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                "InProgress" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (task.status) {
                                            "Completed" -> t("مكتملة ✅", "Completed")
                                            "InProgress" -> t("قيد التنفيذ 🛠️", "InProgress")
                                            else -> t("قيد الانتظار ⏳", "Pending")
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (task.status) {
                                            "Completed" -> MaterialTheme.colorScheme.primary
                                            "InProgress" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                                }

                                Text(
                                    text = "Assignee: ${task.assignedToName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = t(task.titleAr, task.titleEn),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = t(task.descriptionAr, task.descriptionEn),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // Geolocational indicators
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, "Coords", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "KSA Hub Coordinates: Lat: ${task.latitude} · Lng: ${task.longitude}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!task.reportedNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = t("الملاحظات المرفوعة:", "Reported Operational Notes:"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = task.reportedNotes,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            // Actions block inside Task Entity
                            if (role == "Employee" && task.status != "Completed") {
                                Spacer(modifier = Modifier.height(12.dp))
                                var reportingNoteInput by remember { mutableStateOf("") }
                                var simulationAttachedByImg by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = reportingNoteInput,
                                        onValueChange = { reportingNoteInput = it },
                                        label = { Text(t("تقرير الإنجاز وصور قبل وبعد", "Report progress & upload photo")) },
                                        placeholder = { Text("أي مشكلات أو ملاحظات فنية...") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("task_note_input_${task.id}"),
                                        textStyle = TextStyle(fontSize = 12.sp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { simulationAttachedByImg = true },
                                        modifier = Modifier.testTag("attach_pic_task_${task.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (simulationAttachedByImg) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                            contentDescription = "Attach Site Photo",
                                            tint = if (simulationAttachedByImg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (task.status == "Pending") {
                                        Button(
                                            onClick = {
                                                viewModel.updateTaskStatus(task, "InProgress")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("start_task_btn_${task.id}")
                                        ) {
                                            Text(t("بدء المهمة قيد العمل", "Start Active Work"), fontSize = 11.sp)
                                        }
                                    } else if (task.status == "InProgress") {
                                        Button(
                                            onClick = {
                                                viewModel.updateTaskStatus(
                                                    task,
                                                    "Completed",
                                                    beforePic = "ATTACHED",
                                                    afterPic = "CONFIRMED_COMPLETED",
                                                    reportNote = reportingNoteInput.ifBlank { "Completed operations safely on location coordinates." }
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("complete_task_btn_${task.id}")
                                        ) {
                                            Text(t("إنهاء واعتماد الميدان", "Mark Completed"), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Task Creation Dialog: Allows admins to assign geofenced sites to Yasser or Ahmed
    if (showCreateTaskDialog) {
        var tAr by remember { mutableStateOf("") }
        var tEn by remember { mutableStateOf("") }
        var dAr by remember { mutableStateOf("") }
        var dEn by remember { mutableStateOf("") }
        var assignedToId by remember { mutableStateOf("SH-1002") }

        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            title = { Text(t("إسناد مهمة تشغيل جيو-مكانية جديدة", "Assign New Geospatial Operations Task")) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = tAr,
                        onValueChange = { tAr = it },
                        label = { Text(t("العنوان (عربي)", "Task Title (Arabic)")) },
                        modifier = Modifier.fillMaxWidth().testTag("task_title_ar")
                    )
                    TextField(
                        value = tEn,
                        onValueChange = { tEn = it },
                        label = { Text(t("العنوان (إنجليزي)", "Task Title (English)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = dAr,
                        onValueChange = { dAr = it },
                        label = { Text(t("الوصف والضوابط (عربي)", "Description (Arabic)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = dEn,
                        onValueChange = { dEn = it },
                        label = { Text(t("الوصف والضوابط (إنجليزي)", "Description (English)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = assignedToId,
                        onValueChange = { assignedToId = it },
                        label = { Text(t("الرقم الوظيفي للموظف المسند إليه", "Assignee Staff ID")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taskObj = TaskEntity(
                            titleAr = tAr.ifBlank { "مهمة تشغيل عاجلة" },
                            titleEn = tEn.ifBlank { "Urgent Field Operation" },
                            descriptionAr = dAr.ifBlank { "القيام بالتدقيق الفني بالموقع" },
                            descriptionEn = dEn.ifBlank { "Execute site technical validation" },
                            assignedToId = assignedToId,
                            assignedToName = if (assignedToId == "SH-1002") "Yasser Qahtani" else "Ahmed Radwan",
                            latitude = 24.7622, // defaults to KAFD Riyadh coordinates
                            longitude = 46.6410,
                            status = "Pending"
                        )
                        viewModel.insertTask(taskObj)
                        showCreateTaskDialog = false
                    },
                    modifier = Modifier.testTag("admin_save_task_btn")
                ) {
                    Text(t("إرسال المهمة وتفعيل التتبع", "Dispatch & Track"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false }) {
                    Text(t("إلغاء", "Close"))
                }
            }
        )
    }
}

// ==========================================
// 4. SAUDI CO-OPERATION AI ADVISOR & CHAT PANEL
// ==========================================
@Composable
fun SaudiAIAdvisorPanel(
    viewModel: ShawamikhViewModel,
    isRtl: Boolean,
    t: (String, String) -> String
) {
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    var userQueryText by remember { mutableStateOf("") }

    val questionChips = listOf(
        "حساب مكافأة نهاية الخدمة بالاستقالة؟",
        "شروط المادة (109) للإجازات السنوية؟",
        "كم نسبة اشتراك تأمينات GOSI للسعودي؟",
        "الحالات التي تجيز الفصل بموجب المادة 80"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // AI Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, "AI Core", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = t("مستشار شوامخ الخبير بنظام العمل السعودي 🇸🇦", "Saudi Labor Advisor - Gemini AI"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = t("توليد تحليلات، حسابات الـ EOS والمادة 109/80", "Asynchronous legal advisory based on King's labor acts"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Quick Suggestion Chips
        Text(
            text = t("أسئلة عمالية شائعة متبعة بالمملكة العربية السعودية:", "Common Saudi labor rules queries:"),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            questionChips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .clickable { viewModel.sendAiQuestion(chip) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chip,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Chat Conversation Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp, start = 12.dp, end = 12.dp, top = 12.dp)
                        .testTag("ai_chat_scroll"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                            bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .background(
                                        if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = LocalTextStyle.current.copy(textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr)
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = t("جاري الاستبيان العمالي الذكي...", "Consulting Saudi laws via Gemini..."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Chat Input Panel at lower bounds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userQueryText,
                        onValueChange = { userQueryText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_prompt_input"),
                        placeholder = { Text(t("مثال: كيف أحسب مستحقات نهاية خدمة لموظف استقال؟", "Ask about GOSI, Leaves, or EOS rewards..."), fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 13.sp),
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.sendAiQuestion(userQueryText)
                            userQueryText = ""
                        },
                        enabled = userQueryText.isNotBlank(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (userQueryText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                            .testTag("ai_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send prompt",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PAYROLL COMPLIANCE & MOCK BANK PANEL
// ==========================================
@Composable
fun PayrollCompliancePanel(
    viewModel: ShawamikhViewModel,
    employees: List<EmployeeEntity>,
    currentEmp: EmployeeEntity?,
    role: String,
    isRtl: Boolean,
    t: (String, String) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Payments, "Pay Core", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = t("نظام مدد لحماية الأجور ورواتب قوى", "Madd Wage Protection & Payroll"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = t("حساب وخصم اشتراكات التأمينات GOSI وبدل السكن", "Automated KSA social securities assessment modules"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        if (role == "Admin") {
            // General Corporate registry of all payslips
            Text(
                text = t("مسيرات رواتب الموظفين للشهر الحالي (مسيرة حماية الأجور):", "Approved Corporate Wage Registries (Madd WPS XML format):"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Export Actions Mock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Simulated Madd API/WPS */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.UploadFile, "Madd Upload")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(t("رفع ملف مدد WPS XML", "Sync Madd WPS XML"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { /* Clean file generation excel */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, "Report")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(t("تصدير مسير Excel / PDF", "Export Payroll Ledger"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            employees.forEach { emp ->
                val slip = viewModel.repository.calculatePayroll(emp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = t(slip.nameAr, slip.nameEn), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${slip.employeeId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = t("الراتب الأساسي", "Base Salary"), fontSize = 11.sp, color = Color.Gray)
                                Text(text = "${slip.baseSalary.roundToInt()} SAR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text(text = t("بدل السكن", "Housing Allow"), fontSize = 11.sp, color = Color.Gray)
                                Text(text = "${slip.housingAllowance.roundToInt()} SAR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text(text = t("بدل النقل", "Transport"), fontSize = 11.sp, color = Color.Gray)
                                Text(text = "${slip.transportAllowance.roundToInt()} SAR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = t("الاستقطاع (التأمينات التأمينات GOSI):", "Deduction (GOSI Worker Pension):"), fontSize = 10.sp, color = Color.DarkGray)
                                Text(text = "${slip.gosiDeduction.roundToInt()} SAR (${if (slip.isSaudi) "9%" else "0%"})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = t("صافي الحساب البنكي:", "Net Bank Payback:"), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${slip.netSalary.roundToInt()} SAR", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

        } else {
            // Employee specific personal slip details
            if (currentEmp != null) {
                val mySlip = viewModel.repository.calculatePayroll(currentEmp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = t("تفاصيل كشف راتبي ومسيري المعتمد", "Summary of My Payroll Slip"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = t(
                                "يتم صرف رواتب موظفي شوامخ عبر الربط الآلي بمسارات وزارة الموارد مع إيداع بنكي فوري.",
                                "All payments are routed through ministerial security channels to your IBAN."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Visual ledger items
                        LedgerRow(title = t("الراتب الأساسي", "Base Salary"), value = "${mySlip.baseSalary.roundToInt()} SAR")
                        LedgerRow(title = t("بدل السكن والمبيت", "Housing Subsidy"), value = "${mySlip.housingAllowance.roundToInt()} SAR")
                        LedgerRow(title = t("بدل النقل والانتقالات الميدانية", "Transport Allowances"), value = "${mySlip.transportAllowance.roundToInt()} SAR")
                        LedgerRow(title = t("بدلات إضافية ومؤشرات KPI أخرى", "Performance KPIs"), value = "${mySlip.otherAllowances.roundToInt()} SAR")

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        LedgerRow(
                            title = t("حسم اشتراك التقاعد (التأمينات GOSI):", "Social security pension deductions (GOSI):"),
                            value = "-${mySlip.gosiDeduction.roundToInt()} SAR",
                            isNegative = true
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = t("صافي الراتب المربوط بالبنك المودع:", "Net Disbursed Cash to IBAN:"),
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${mySlip.netSalary.roundToInt()} SAR",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR compliance seal mock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, "Verified Qiwa", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = t("مستند معتمد من نظام حماية الأجور وصرف المستحقات", "Compliant with KSA MHRSD Wage protection regulations"),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerRow(
    title: String,
    value: String,
    isNegative: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

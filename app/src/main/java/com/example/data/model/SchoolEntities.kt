package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentIdNumber: String, // e.g. "ET-2017-001"
    val fullName: String, // e.g. "አበበ ታደሰ ከበደ"
    val gender: String, // "ወንድ" (Male) / "ሴት" (Female)
    val age: Int,
    val grade: String, // "9ኛ ክፍል", "10ኛ ክፍል", "11ኛ ክፍል", "12ኛ ክፍል", etc.
    val section: String, // "A", "B", "C", "D"
    val academicYear: String = "2017 ዓ.ም",
    val parentName: String,
    val parentPhone: String,
    val address: String,
    val status: String = "ንቁ", // "ንቁ" (Active), "ያቋረጠ" (Dropped), "የተዛወረ" (Transferred)
    val specialNeeds: String = "",
    val photoUri: String = "",
    val registeredDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherCode: String, // e.g. "T-101"
    val fullName: String, // e.g. "አቶ ሙሉጌታ ታከለ"
    val title: String = "አቶ", // "አቶ", "ወ/ሮ", "ወ/ሪት", "ዶ/ር"
    val gender: String, // "ወንድ", "ሴት"
    val phone: String,
    val email: String = "",
    val qualification: String, // "ዲፕሎማ", "ባችለር ዲግሪ (BA/BSc)", "ማስተርስ (MA/MSc)"
    val specialization: String, // "ሂሳብ", "ፊዚክስ", "እንግሊዝኛ", "አማርኛ", etc.
    val department: String = "አጠቃላይ የትምህርት ክፍል", // Mathematics, Languages, Natural Sciences, Social Sciences
    val isHomeroomTeacher: Boolean = false,
    val homeroomClass: String = "", // e.g. "10ኛ-A"
    val joinedYear: String = "2015 ዓ.ም",
    val maxWeeklyPeriods: Int = 20, // Standard weekly teaching capacity
    val photoUri: String = "",
    val status: String = "ንቁ" // "ንቁ" (Active), "በእረፍት ላይ" (On Leave)
)

data class TeacherWithAssignments(
    val teacher: Teacher,
    val assignments: List<TeacherWorkload> = emptyList()
) {
    val totalAssignedPeriods: Int get() = assignments.sumOf { it.periodsPerWeek }
    val uniqueSubjects: List<String> get() = assignments.map { it.subjectName }.distinct()
    val assignedClasses: List<String> get() = assignments.map { "${it.grade} - ${it.section}" }.distinct()
    val workloadPercentage: Float get() = if (teacher.maxWeeklyPeriods > 0) {
        (totalAssignedPeriods.toFloat() / teacher.maxWeeklyPeriods.toFloat()).coerceIn(0f, 1.5f)
    } else 0f
    val isWorkloadOverCapacity: Boolean get() = totalAssignedPeriods > teacher.maxWeeklyPeriods
}

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // "MATH-09", "ENG-09", "PHY-10"
    val name: String, // "ሂሳብ (Mathematics)", "እንግሊዝኛ (English)"
    val gradeLevel: String, // "9ኛ ክፍል", "10ኛ ክፍል", "ሁሉም"
    val periodsPerWeek: Int = 4, // ሳምንታዊ የክፍለ-ጊዜ ብዛት
    val passingScore: Int = 50,
    val category: String = "ዋና የትምህርት ዓይነት" // Core, Elective, Vocational
)

@Entity(tableName = "teacher_workloads")
data class TeacherWorkload(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: Long,
    val teacherName: String,
    val subjectId: Long,
    val subjectName: String,
    val grade: String, // "9ኛ ክፍል"
    val section: String, // "A"
    val periodsPerWeek: Int = 4,
    val daysAllotted: String = "ሰኞ፣ ረቡዕ፣ አርብ",
    val academicYear: String = "2017 ዓ.ም"
)

@Entity(tableName = "student_grades")
data class StudentGrade(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val grade: String,
    val section: String,
    val subjectName: String,
    val semester: String = "1ኛ ሴሚስተር", // "1ኛ ሴሚስተር", "2ኛ ሴሚስተር"
    val academicYear: String = "2017 ዓ.ም",
    val quizScore: Float = 0f, // /10
    val assignmentScore: Float = 0f, // /10
    val midExamScore: Float = 0f, // /30
    val finalExamScore: Float = 0f, // /50
    val totalScore: Float = 0f, // /100
    val letterGrade: String = "B",
    val remarks: String = ""
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val grade: String,
    val section: String,
    val date: String, // YYYY-MM-DD or Ethiopian date
    val status: String = "ተገኝቷል" // "ተገኝቷል" (Present), "ቀርቷል" (Absent), "በፍቃድ" (Excused), "አርፍዷል" (Late)
)

@Entity(tableName = "staff_members")
data class Staff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffCode: String, // e.g. "ADM-101"
    val fullName: String, // e.g. "ዶ/ር ተስፋዬ ገብረመድህን"
    val title: String = "አቶ", // "አቶ", "ወ/ሮ", "ወ/ሪት", "ዶ/ር"
    val role: String, // "ርዕሰ መምህር", "ምክትል ርዕሰ መምህር", "ዋና ጸሐፊ", "ፋይናንስ / ሂሳብ ሹም", "ንብረት ክፍል", "ፅዳት ሰራተኛ", "ጥበቃ", "መረጃ ቴክኖሎጂ (IT)", "ላይብረሪያን", "ሌላ"
    val department: String = "አስተዳደር", // "አስተዳደር", "ፋይናንስ እና ንብረት", "አገልግሎትና ደህንነት", "አካዳሚክ ድጋፍ"
    val phone: String, // e.g. "0911000000"
    val email: String = "",
    val gender: String = "ወንድ", // "ወንድ", "ሴት"
    val qualification: String = "ዲፕሎማ", // "10/12ኛ ያጠናቀቀ", "ሰርተፍኬት", "ዲፕሎማ", "የመጀመሪያ ዲግሪ", "ማስተርስ", "ዶክትሬት"
    val employmentType: String = "ቋሚ", // "ቋሚ", "ኮንትራት", "ጊዜያዊ"
    val salary: String = "", // ደመወዝ
    val hiringDate: String = "2015 ዓ.ም",
    val responsibilities: String = "", // የስራ ድርሻ ዝርዝር መግለጫ
    val emergencyContact: String = "", // የአደጋ ጊዜ ተጠሪ ስልክ
    val photoUri: String = "",
    val status: String = "ንቁ", // "ንቁ", "በእረፍት ላይ", "የተሰናበተ"
    val teachingSubject: String = "", // የሚያስተምረው የትምህርት አይነት (ለምሳሌ ሒሳብ፣ እንግሊዝኛ)
    val assignedGrade: String = "", // የሚያስተምርበት ወይም ረዳት የሆነበት ክፍል
    val assignedSection: String = "", // ሴክሽን
    val isHomeroomTeacher: Boolean = false, // የክፍል ኃላፊ ነው? (አዎ / አይደለም)
    val homeroomClass: String = "", // የስንተኛ ክፍል እና ሴክሽን ኃላፊ (ለምሳሌ 7ኛ ክፍል - A)
    val dutyLocation: String = "", // የተመደበበት የስራ ግቢ/ህንጻ/ቦታ
    val shift: String = "" // ፈረቃ (ጧት፣ ከሰዓት፣ ሙሉ ቀን፣ የሌሊት ጥበቃ)
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String = "", // e.g. "0911000000" (Used by teachers, students, staff)
    val email: String = "", // e.g. "admin@dilijent.edu.et" (Used exclusively by System Admin)
    val password: String, // Password
    val role: String = "ሲስተም አድሚን (Admin)", // Role
    val isActive: Boolean = true,
    val createdDate: Long = System.currentTimeMillis()
)

data class GradeSectionOption(
    val grade: String,
    val sections: List<String>
)

data class SchoolStats(
    val totalStudents: Int = 0,
    val maleStudents: Int = 0,
    val femaleStudents: Int = 0,
    val totalTeachers: Int = 0,
    val totalStaff: Int = 0,
    val totalSubjects: Int = 0,
    val totalAllocatedPeriods: Int = 0,
    val activeGradesCount: Int = 0
)

enum class StaffAttendanceSession(val titleAm: String, val titleEn: String, val typeKey: String) {
    CHECK_IN("የመግቢያ አቴንዳንስ (Check-In)", "Check-In (Arrival)", "entry"),
    CHECK_OUT("የመውጫ አቴንዳንስ (Check-Out)", "Check-Out (Departure)", "exit");

    companion object {
        val MORNING = CHECK_IN
        val AFTERNOON = CHECK_OUT
    }
}

data class StaffRoleSchedule(
    val roleCategory: String, // "መምህራን", "ፅዳት ሰራተኞች", "ደህንነትና ጥበቃ", "አስተዳደርና ፋይናንስ", "ሌሎች ሰራተኞች"
    val entryStart: String = "01:30 ጧት",
    val entryEnd: String = "01:51 ጧት",
    val exitStart: String = "09:15 ከሰዓት",
    val exitEnd: String = "09:40 ከሰዓት",
    val description: String = ""
)

data class StaffAttendanceItem(
    val id: Long,
    val name: String,
    val role: String,
    val department: String,
    val phone: String,
    val isTeacher: Boolean,
    val roleCategory: String = "መምህራን",
    // Check-in (የመግቢያ)
    val entryStatus: String = "ተገኝቷል", // "ተገኝቷል", "አርፍዷል", "ቀርቷል", "በፍቃድ"
    val entryTime: String = "01:42 ጧት",
    val entryRemarks: String = "በሰዓቱ ገብተዋል",
    // Check-out (የመውጫ)
    val exitStatus: String = "ተገኝቷል", // "ተገኝቷል", "ቀድሞ ወጥቷል", "በሰዓቱ ወጥቷል", "ዘግይቶ ወጥቷል", "ቀርቷል", "በፍቃድ"
    val exitTime: String = "09:25 ከሰዓት",
    val exitRemarks: String = "በሰዓቱ ወጥተዋል",
    // Compatibility fields
    val morningStatus: String = entryStatus,
    val morningTime: String = entryTime,
    val morningRemarks: String = entryRemarks,
    val afternoonStatus: String = exitStatus,
    val afternoonTime: String = exitTime,
    val afternoonRemarks: String = exitRemarks,
    val status: String = entryStatus,
    val checkInTime: String = entryTime,
    val remarks: String = entryRemarks
)


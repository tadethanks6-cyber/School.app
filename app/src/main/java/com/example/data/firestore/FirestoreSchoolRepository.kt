package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreSchoolRepo"

enum class CloudStatus {
    CONNECTED,   // አስተማማኝ ክላውድ ተገናኝቷል
    SYNCING,     // መረጃ በማመሳሰል ላይ...
    OFFLINE_PERSISTENT // ያለ ኢንተርኔት በውስጥ ማህደረ-ትውስታ የተያዘ
}

class FirestoreSchoolRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val _syncStatus = MutableStateFlow(CloudStatus.CONNECTED)
    val syncStatus = _syncStatus.asStateFlow()

    init {
        // Enable automatic Firestore offline persistence
        try {
            db.firestoreSettings = com.google.firebase.firestore.firestoreSettings {
                isPersistenceEnabled = true
            }
        } catch (e: Exception) {
            Log.d(TAG, "Persistence settings notice: ${e.message}")
        }
    }

    // --- STUDENT CLOUD PERSISTENCE ---
    suspend fun saveStudent(userId: String, student: Student, docId: String = "student_${student.studentIdNumber.replace(Regex("[^a-zA-Z0-9_]"), "_")}") {
        _syncStatus.value = CloudStatus.SYNCING
        val docRef = db.collection("students").document(docId)
        val data = hashMapOf<String, Any>(
            "studentIdNumber" to student.studentIdNumber,
            "fullName" to student.fullName,
            "gender" to student.gender,
            "age" to student.age,
            "grade" to student.grade,
            "section" to student.section,
            "parentName" to student.parentName,
            "parentPhone" to student.parentPhone,
            "address" to student.address,
            "status" to student.status,
            "specialNeeds" to student.specialNeeds,
            "academicYear" to student.academicYear,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            _syncStatus.value = CloudStatus.CONNECTED
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save student in cloud", e)
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
        }
    }

    suspend fun deleteStudent(docId: String) {
        try {
            db.collection("students").document(docId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting student $docId", e)
        }
    }

    // --- TEACHER CLOUD PERSISTENCE ---
    suspend fun saveTeacher(userId: String, teacher: Teacher, docId: String = "teacher_${teacher.teacherCode.replace(Regex("[^a-zA-Z0-9_]"), "_")}") {
        _syncStatus.value = CloudStatus.SYNCING
        val docRef = db.collection("teachers").document(docId)
        val data = hashMapOf<String, Any>(
            "teacherCode" to teacher.teacherCode,
            "fullName" to teacher.fullName,
            "title" to teacher.title,
            "gender" to teacher.gender,
            "phone" to teacher.phone,
            "email" to teacher.email,
            "qualification" to teacher.qualification,
            "specialization" to teacher.specialization,
            "department" to teacher.department,
            "isHomeroomTeacher" to teacher.isHomeroomTeacher,
            "homeroomClass" to teacher.homeroomClass,
            "joinedYear" to teacher.joinedYear,
            "maxWeeklyPeriods" to teacher.maxWeeklyPeriods,
            "status" to teacher.status,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            _syncStatus.value = CloudStatus.CONNECTED
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save teacher in cloud", e)
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
        }
    }

    // --- STAFF CLOUD PERSISTENCE ---
    suspend fun saveStaff(userId: String, staff: Staff, docId: String = "staff_${staff.staffCode.replace(Regex("[^a-zA-Z0-9_]"), "_")}") {
        _syncStatus.value = CloudStatus.SYNCING
        val docRef = db.collection("staff").document(docId)
        val data = hashMapOf<String, Any>(
            "staffCode" to staff.staffCode,
            "fullName" to staff.fullName,
            "title" to staff.title,
            "role" to staff.role,
            "department" to staff.department,
            "phone" to staff.phone,
            "email" to staff.email,
            "gender" to staff.gender,
            "qualification" to staff.qualification,
            "employmentType" to staff.employmentType,
            "salary" to staff.salary,
            "hiringDate" to staff.hiringDate,
            "responsibilities" to staff.responsibilities,
            "emergencyContact" to staff.emergencyContact,
            "status" to staff.status,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            _syncStatus.value = CloudStatus.CONNECTED
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save staff in cloud", e)
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
        }
    }

    // --- SUBJECT CLOUD PERSISTENCE ---
    suspend fun saveSubject(userId: String, subject: Subject, docId: String = "sub_${subject.code.replace(Regex("[^a-zA-Z0-9_]"), "_")}") {
        val docRef = db.collection("subjects").document(docId)
        val data = hashMapOf<String, Any>(
            "code" to subject.code,
            "name" to subject.name,
            "gradeLevel" to subject.gradeLevel,
            "periodsPerWeek" to subject.periodsPerWeek,
            "passingScore" to subject.passingScore,
            "category" to subject.category,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save subject in cloud", e)
        }
    }

    // --- WORKLOAD CLOUD PERSISTENCE ---
    suspend fun saveWorkload(userId: String, workload: TeacherWorkload, docId: String = "workload_${workload.teacherId}_${workload.subjectId}_${workload.grade.replace(" ", "_")}_${workload.section}") {
        val docRef = db.collection("workloads").document(docId)
        val data = hashMapOf<String, Any>(
            "teacherName" to workload.teacherName,
            "subjectName" to workload.subjectName,
            "grade" to workload.grade,
            "section" to workload.section,
            "periodsPerWeek" to workload.periodsPerWeek,
            "daysAllotted" to workload.daysAllotted,
            "academicYear" to workload.academicYear,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save workload in cloud", e)
        }
    }

    // --- GRADE CLOUD PERSISTENCE ---
    suspend fun saveGrade(userId: String, grade: StudentGrade, docId: String = "grade_${grade.studentId}_${grade.subjectName.replace(Regex("[^a-zA-Z0-9_]"), "_")}_${grade.semester.replace(" ", "_")}") {
        val docRef = db.collection("grades").document(docId)
        val data = hashMapOf<String, Any>(
            "studentName" to grade.studentName,
            "grade" to grade.grade,
            "section" to grade.section,
            "subjectName" to grade.subjectName,
            "semester" to grade.semester,
            "academicYear" to grade.academicYear,
            "quizScore" to grade.quizScore,
            "assignmentScore" to grade.assignmentScore,
            "midExamScore" to grade.midExamScore,
            "finalExamScore" to grade.finalExamScore,
            "totalScore" to grade.totalScore,
            "letterGrade" to grade.letterGrade,
            "remarks" to grade.remarks,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save grade in cloud", e)
        }
    }

    // --- ATTENDANCE CLOUD PERSISTENCE ---
    suspend fun saveAttendance(userId: String, record: AttendanceRecord, docId: String = "att_${record.studentId}_${record.date.replace("-", "_")}") {
        val docRef = db.collection("attendance").document(docId)
        val data = hashMapOf<String, Any>(
            "studentName" to record.studentName,
            "grade" to record.grade,
            "section" to record.section,
            "date" to record.date,
            "status" to record.status,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save attendance in cloud", e)
        }
    }
}

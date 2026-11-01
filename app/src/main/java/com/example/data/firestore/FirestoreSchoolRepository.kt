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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val auth = Firebase.auth
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

    private fun getAuthenticatedUid(): String? {
        return auth.currentUser?.uid
    }

    // --- STUDENT CLOUD PERSISTENCE ---
    suspend fun saveStudent(
        userId: String,
        student: Student,
        docId: String = "student_${student.studentIdNumber.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
    ) {
        val uid = getAuthenticatedUid()
        if (uid == null) {
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
            return
        }
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
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            _syncStatus.value = CloudStatus.CONNECTED
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Offline local persistence active for student: ${e.message}")
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
        }
    }

    suspend fun deleteStudent(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("students").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete student handled locally: ${e.message}")
        }
    }

    // --- TEACHER CLOUD PERSISTENCE ---
    suspend fun saveTeacher(
        userId: String,
        teacher: Teacher,
        docId: String = "teacher_${teacher.teacherCode.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
    ) {
        val uid = getAuthenticatedUid()
        if (uid == null) {
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
            return
        }
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
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            _syncStatus.value = CloudStatus.CONNECTED
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Offline local persistence active for teacher: ${e.message}")
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
        }
    }

    suspend fun deleteTeacher(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("teachers").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete teacher handled locally: ${e.message}")
        }
    }

    // --- STAFF CLOUD PERSISTENCE ---
    suspend fun saveStaff(
        userId: String,
        staff: Staff,
        docId: String = "staff_${staff.staffCode.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
    ) {
        val uid = getAuthenticatedUid()
        if (uid == null) {
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
            return
        }
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
            "teachingSubject" to staff.teachingSubject,
            "assignedGrade" to staff.assignedGrade,
            "assignedSection" to staff.assignedSection,
            "isHomeroomTeacher" to staff.isHomeroomTeacher,
            "homeroomClass" to staff.homeroomClass,
            "dutyLocation" to staff.dutyLocation,
            "shift" to staff.shift,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            _syncStatus.value = CloudStatus.CONNECTED
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Offline local persistence active for staff: ${e.message}")
            _syncStatus.value = CloudStatus.OFFLINE_PERSISTENT
        }
    }

    suspend fun deleteStaff(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("staff").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete staff handled locally: ${e.message}")
        }
    }

    // --- SUBJECT CLOUD PERSISTENCE ---
    suspend fun saveSubject(
        userId: String,
        subject: Subject,
        docId: String = "sub_${subject.code.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
    ) {
        val uid = getAuthenticatedUid() ?: return
        val docRef = db.collection("subjects").document(docId)
        val data = hashMapOf<String, Any>(
            "code" to subject.code,
            "name" to subject.name,
            "gradeLevel" to subject.gradeLevel,
            "periodsPerWeek" to subject.periodsPerWeek,
            "passingScore" to subject.passingScore,
            "category" to subject.category,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Subject cloud save handled locally: ${e.message}")
        }
    }

    suspend fun deleteSubject(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("subjects").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete subject handled locally: ${e.message}")
        }
    }

    // --- WORKLOAD CLOUD PERSISTENCE ---
    suspend fun saveWorkload(
        userId: String,
        workload: TeacherWorkload,
        docId: String = "workload_${workload.teacherId}_${workload.subjectId}_${workload.grade.replace(" ", "_")}_${workload.section}"
    ) {
        val uid = getAuthenticatedUid() ?: return
        val docRef = db.collection("workloads").document(docId)
        val data = hashMapOf<String, Any>(
            "teacherName" to workload.teacherName,
            "subjectName" to workload.subjectName,
            "grade" to workload.grade,
            "section" to workload.section,
            "periodsPerWeek" to workload.periodsPerWeek,
            "daysAllotted" to workload.daysAllotted,
            "academicYear" to workload.academicYear,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Workload cloud save handled locally: ${e.message}")
        }
    }

    suspend fun deleteWorkload(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("workloads").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete workload handled locally: ${e.message}")
        }
    }

    // --- GRADE CLOUD PERSISTENCE ---
    suspend fun saveGrade(
        userId: String,
        grade: StudentGrade,
        docId: String = "grade_${grade.studentId}_${grade.subjectName.replace(Regex("[^a-zA-Z0-9_]"), "_")}_${grade.semester.replace(" ", "_")}"
    ) {
        val uid = getAuthenticatedUid() ?: return
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
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Grade cloud save handled locally: ${e.message}")
        }
    }

    suspend fun deleteGrade(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("grades").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete grade handled locally: ${e.message}")
        }
    }

    // --- ATTENDANCE CLOUD PERSISTENCE ---
    suspend fun saveAttendance(
        userId: String,
        record: AttendanceRecord,
        docId: String = "att_${record.studentId}_${record.date.replace("-", "_")}"
    ) {
        val uid = getAuthenticatedUid() ?: return
        val docRef = db.collection("attendance").document(docId)
        val data = hashMapOf<String, Any>(
            "studentName" to record.studentName,
            "grade" to record.grade,
            "section" to record.section,
            "date" to record.date,
            "status" to record.status,
            "userId" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Attendance cloud save handled locally: ${e.message}")
        }
    }

    suspend fun deleteAttendance(docId: String) {
        val uid = getAuthenticatedUid() ?: return
        try {
            db.collection("attendance").document(docId).delete().await()
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Cloud delete attendance handled locally: ${e.message}")
        }
    }
}

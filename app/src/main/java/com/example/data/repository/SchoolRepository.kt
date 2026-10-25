package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.StaffDao
import com.example.data.dao.StudentDao
import com.example.data.dao.StudentGradeDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TeacherDao
import com.example.data.dao.TeacherWorkloadDao
import com.example.data.dao.UserAccountDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

class SchoolRepository(
    private val studentDao: StudentDao,
    private val teacherDao: TeacherDao,
    private val subjectDao: SubjectDao,
    private val workloadDao: TeacherWorkloadDao,
    private val gradeDao: StudentGradeDao,
    private val attendanceDao: AttendanceDao,
    private val staffDao: StaffDao,
    private val userAccountDao: UserAccountDao
) {
    // Students
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    fun getStudentsByGradeAndSection(grade: String, section: String): Flow<List<Student>> =
        studentDao.getStudentsByGradeAndSection(grade, section)

    fun searchStudents(query: String): Flow<List<Student>> =
        studentDao.searchStudents(query)

    suspend fun getStudentById(id: Long): Student? =
        studentDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long =
        studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) =
        studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) =
        studentDao.deleteStudent(student)

    suspend fun deleteStudentById(id: Long) =
        studentDao.deleteStudentById(id)

    // Teachers
    val allTeachers: Flow<List<Teacher>> = teacherDao.getAllTeachers()
    val teacherCount: Flow<Int> = teacherDao.getTeacherCount()

    fun searchTeachers(query: String): Flow<List<Teacher>> =
        teacherDao.searchTeachers(query)

    suspend fun getTeacherById(id: Long): Teacher? =
        teacherDao.getTeacherById(id)

    suspend fun insertTeacher(teacher: Teacher): Long =
        teacherDao.insertTeacher(teacher)

    suspend fun updateTeacher(teacher: Teacher) =
        teacherDao.updateTeacher(teacher)

    suspend fun deleteTeacher(teacher: Teacher) =
        teacherDao.deleteTeacher(teacher)

    suspend fun deleteTeacherById(id: Long) =
        teacherDao.deleteTeacherById(id)

    // Subjects
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val subjectCount: Flow<Int> = subjectDao.getSubjectCount()

    fun getSubjectsByGrade(gradeLevel: String): Flow<List<Subject>> =
        subjectDao.getSubjectsByGrade(gradeLevel)

    suspend fun insertSubject(subject: Subject): Long =
        subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: Subject) =
        subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subject: Subject) =
        subjectDao.deleteSubject(subject)

    suspend fun deleteSubjectById(id: Long) =
        subjectDao.deleteSubjectById(id)

    // Workloads
    val allWorkloads: Flow<List<TeacherWorkload>> = workloadDao.getAllWorkloads()

    fun getWorkloadsByTeacher(teacherId: Long): Flow<List<TeacherWorkload>> =
        workloadDao.getWorkloadsByTeacher(teacherId)

    fun getWorkloadsByClass(grade: String, section: String): Flow<List<TeacherWorkload>> =
        workloadDao.getWorkloadsByClass(grade, section)

    suspend fun insertWorkload(workload: TeacherWorkload): Long =
        workloadDao.insertWorkload(workload)

    suspend fun updateWorkload(workload: TeacherWorkload) =
        workloadDao.updateWorkload(workload)

    suspend fun deleteWorkload(workload: TeacherWorkload) =
        workloadDao.deleteWorkload(workload)

    suspend fun deleteWorkloadById(id: Long) =
        workloadDao.deleteWorkloadById(id)

    // Grades
    val allGrades: Flow<List<StudentGrade>> = gradeDao.getAllGrades()

    fun getGradesForStudent(studentId: Long): Flow<List<StudentGrade>> =
        gradeDao.getGradesForStudent(studentId)

    fun getGradesForClass(grade: String, section: String, semester: String): Flow<List<StudentGrade>> =
        gradeDao.getGradesForClass(grade, section, semester)

    suspend fun insertGrade(grade: StudentGrade): Long =
        gradeDao.insertGrade(grade)

    suspend fun updateGrade(grade: StudentGrade) =
        gradeDao.updateGrade(grade)

    suspend fun deleteGrade(grade: StudentGrade) =
        gradeDao.deleteGrade(grade)

    // Attendance
    fun getAttendanceForDate(grade: String, section: String, date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDate(grade, section, date)

    suspend fun insertAttendance(record: AttendanceRecord): Long =
        attendanceDao.insertAttendance(record)

    suspend fun insertAttendanceList(records: List<AttendanceRecord>) =
        attendanceDao.insertAttendanceList(records)

    // Staff Members (የአስተዳደር ሰራተኞች)
    val allStaff: Flow<List<Staff>> = staffDao.getAllStaff()
    val staffCount: Flow<Int> = staffDao.getStaffCount()

    fun getStaffByRole(role: String): Flow<List<Staff>> =
        staffDao.getStaffByRole(role)

    fun getStaffByDepartment(dept: String): Flow<List<Staff>> =
        staffDao.getStaffByDepartment(dept)

    fun searchStaff(query: String): Flow<List<Staff>> =
        staffDao.searchStaff(query)

    suspend fun getStaffById(id: Long): Staff? =
        staffDao.getStaffById(id)

    suspend fun insertStaff(staff: Staff): Long =
        staffDao.insertStaff(staff)

    suspend fun updateStaff(staff: Staff) =
        staffDao.updateStaff(staff)

    suspend fun deleteStaff(staff: Staff) =
        staffDao.deleteStaff(staff)

    suspend fun deleteStaffById(id: Long) =
        staffDao.deleteStaffById(id)

    // User Authentication
    suspend fun authenticate(phone: String, password: String): UserAccount? =
        userAccountDao.authenticate(phone, password)

    suspend fun getUserByPhone(phone: String): UserAccount? =
        userAccountDao.getUserByPhone(phone)

    suspend fun registerUser(user: UserAccount): Long =
        userAccountDao.insertUser(user)

    val allUsers: Flow<List<UserAccount>> = userAccountDao.getAllUsers()

    suspend fun deleteUser(user: UserAccount) =
        userAccountDao.deleteUser(user)
}


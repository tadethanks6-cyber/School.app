package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY grade ASC, section ASC, fullName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE grade = :grade AND section = :section ORDER BY fullName ASC")
    fun getStudentsByGradeAndSection(grade: String, section: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :query || '%' OR studentIdNumber LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY fullName ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: Long): Teacher?

    @Query("SELECT * FROM teachers WHERE fullName LIKE '%' || :query || '%' OR specialization LIKE '%' || :query || '%'")
    fun searchTeachers(query: String): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteTeacherById(id: Long)

    @Query("SELECT COUNT(*) FROM teachers")
    fun getTeacherCount(): Flow<Int>
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE gradeLevel = :gradeLevel OR gradeLevel = 'ሁሉም' ORDER BY name ASC")
    fun getSubjectsByGrade(gradeLevel: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)

    @Query("SELECT COUNT(*) FROM subjects")
    fun getSubjectCount(): Flow<Int>
}

@Dao
interface TeacherWorkloadDao {
    @Query("SELECT * FROM teacher_workloads ORDER BY teacherName ASC, grade ASC, section ASC")
    fun getAllWorkloads(): Flow<List<TeacherWorkload>>

    @Query("SELECT * FROM teacher_workloads WHERE teacherId = :teacherId")
    fun getWorkloadsByTeacher(teacherId: Long): Flow<List<TeacherWorkload>>

    @Query("SELECT * FROM teacher_workloads WHERE grade = :grade AND section = :section")
    fun getWorkloadsByClass(grade: String, section: String): Flow<List<TeacherWorkload>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkload(workload: TeacherWorkload): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkloads(workloads: List<TeacherWorkload>)

    @Update
    suspend fun updateWorkload(workload: TeacherWorkload)

    @Delete
    suspend fun deleteWorkload(workload: TeacherWorkload)

    @Query("DELETE FROM teacher_workloads WHERE id = :id")
    suspend fun deleteWorkloadById(id: Long)
}

@Dao
interface StudentGradeDao {
    @Query("SELECT * FROM student_grades")
    fun getAllGrades(): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE studentId = :studentId ORDER BY semester ASC, subjectName ASC")
    fun getGradesForStudent(studentId: Long): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE grade = :grade AND section = :section AND semester = :semester")
    fun getGradesForClass(grade: String, section: String, semester: String): Flow<List<StudentGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: StudentGrade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<StudentGrade>)

    @Update
    suspend fun updateGrade(grade: StudentGrade)

    @Delete
    suspend fun deleteGrade(grade: StudentGrade)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE grade = :grade AND section = :section AND date = :date")
    fun getAttendanceForDate(grade: String, section: String, date: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members ORDER BY fullName ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff_members WHERE role = :role ORDER BY fullName ASC")
    fun getStaffByRole(role: String): Flow<List<Staff>>

    @Query("SELECT * FROM staff_members WHERE department = :dept ORDER BY fullName ASC")
    fun getStaffByDepartment(dept: String): Flow<List<Staff>>

    @Query("SELECT * FROM staff_members WHERE id = :id")
    suspend fun getStaffById(id: Long): Staff?

    @Query("SELECT * FROM staff_members WHERE fullName LIKE '%' || :query || '%' OR role LIKE '%' || :query || '%' OR staffCode LIKE '%' || :query || '%'")
    fun searchStaff(query: String): Flow<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(staffList: List<Staff>)

    @Update
    suspend fun updateStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: Long)

    @Query("SELECT COUNT(*) FROM staff_members")
    fun getStaffCount(): Flow<Int>
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun authenticate(phone: String, password: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) AND password = :password LIMIT 1")
    suspend fun authenticateByEmail(email: String, password: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("UPDATE user_accounts SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Delete
    suspend fun deleteUser(user: UserAccount)
}


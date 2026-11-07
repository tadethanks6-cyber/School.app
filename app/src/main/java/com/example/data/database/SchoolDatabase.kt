package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class,
        Teacher::class,
        Subject::class,
        TeacherWorkload::class,
        StudentGrade::class,
        AttendanceRecord::class,
        Staff::class,
        UserAccount::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun subjectDao(): SubjectDao
    abstract fun workloadDao(): TeacherWorkloadDao
    abstract fun gradeDao(): StudentGradeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun staffDao(): StaffDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "ethiopian_school_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SchoolDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SchoolDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: SchoolDatabase) {
            // 1. Initial Subjects (Ethiopian Standard Curriculum)
            val subjects = listOf(
                Subject(name = "አማርኛ ቋንቋ (Amharic)", code = "AMH-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                Subject(name = "እንግሊዝኛ (English)", code = "ENG-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 5, passingScore = 50),
                Subject(name = "ሂሳብ (Mathematics)", code = "MTH-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 5, passingScore = 50),
                Subject(name = "ፊዚክስ (Physics)", code = "PHY-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                Subject(name = "ኬሚስትሪ (Chemistry)", code = "CHM-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                Subject(name = "ባዮሎጂ (Biology)", code = "BIO-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                Subject(name = "ዜግነት ትምህርት (Citizenship)", code = "CTZ-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 2, passingScore = 50),
                Subject(name = "ህብረተሰብ ጥናት (Social Studies)", code = "SOC-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 3, passingScore = 50),
                Subject(name = "ኢንፎርሜሽን ቴክኖሎጂ (IT)", code = "ICT-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 2, passingScore = 50),
                Subject(name = "የሰውነት ማጎልመሻ (HPE)", code = "HPE-09", gradeLevel = "9ኛ ክፍል", periodsPerWeek = 2, passingScore = 50)
            )
            database.subjectDao().insertSubjects(subjects)

            // 2. Initial Teachers (Empty by default - exclusively user-registered)
            val teachers = emptyList<Teacher>()
            database.teacherDao().insertTeachers(teachers)

            // 3. Initial Students
            val students = listOf(
                Student(
                    studentIdNumber = "ET-2017-001",
                    fullName = "ዮናስ አለሙ ከበደ",
                    gender = "ወንድ",
                    age = 15,
                    grade = "9ኛ ክፍል",
                    section = "A",
                    academicYear = "2017 ዓ.ም",
                    parentName = "አለሙ ከበደ",
                    parentPhone = "0911002233",
                    address = "አዲስ አበባ፣ የካ ክፍለ ከተማ ወረዳ 07",
                    status = "ንቁ"
                ),
                Student(
                    studentIdNumber = "ET-2017-002",
                    fullName = "ሄለን ታደሰ ብርሃኑ",
                    gender = "ሴት",
                    age = 14,
                    grade = "9ኛ ክፍል",
                    section = "A",
                    academicYear = "2017 ዓ.ም",
                    parentName = "ታደሰ ብርሃኑ",
                    parentPhone = "0912112233",
                    address = "አዲስ አበባ፣ ቦሌ ክፍለ ከተማ ወረዳ 03",
                    status = "ንቁ"
                ),
                Student(
                    studentIdNumber = "ET-2017-003",
                    fullName = "ዳዊት ካሳሁን ሃይሌ",
                    gender = "ወንድ",
                    age = 15,
                    grade = "9ኛ ክፍል",
                    section = "B",
                    academicYear = "2017 ዓ.ም",
                    parentName = "ካሳሁን ሃይሌ",
                    parentPhone = "0913223344",
                    address = "አዲስ አበባ፣ ቂርቆስ ክፍለ ከተማ ወረዳ 02",
                    status = "ንቁ"
                ),
                Student(
                    studentIdNumber = "ET-2017-004",
                    fullName = "ሳሮን ኤርሚያስ ዳንኤል",
                    gender = "ሴት",
                    age = 15,
                    grade = "9ኛ ክፍል",
                    section = "B",
                    academicYear = "2017 ዓ.ም",
                    parentName = "ኤርሚያስ ዳንኤል",
                    parentPhone = "0914334455",
                    address = "አዲስ አበባ፣ አራዳ ክፍለ ከተማ ወረዳ 05",
                    status = "ንቁ"
                ),
                Student(
                    studentIdNumber = "ET-2017-005",
                    fullName = "በረከት ዘውዱ ሽፈራው",
                    gender = "ወንድ",
                    age = 16,
                    grade = "10ኛ ክፍል",
                    section = "A",
                    academicYear = "2017 ዓ.ም",
                    parentName = "ዘውዱ ሽፈራው",
                    parentPhone = "0915445566",
                    address = "አዲስ አበባ፣ ጉለሌ ክፍለ ከተማ ወረዳ 04",
                    status = "ንቁ"
                ),
                Student(
                    studentIdNumber = "ET-2017-006",
                    fullName = "ማህሌት ግርማቸው ተፈራ",
                    gender = "ሴት",
                    age = 16,
                    grade = "10ኛ ክፍል",
                    section = "A",
                    academicYear = "2017 ዓ.ም",
                    parentName = "ግርማቸው ተፈራ",
                    parentPhone = "0916556677",
                    address = "አዲስ አበባ፣ ንፋስ ስልክ ክፍለ ከተማ ወረዳ 01",
                    status = "ንቁ"
                )
            )
            val allInitialStudents = students + com.example.data.model.OfficialStudentsData.getAllOfficialStudents()
            database.studentDao().insertStudents(allInitialStudents)

            // 4. Initial Teacher Workload Allotments (Empty by default)
            val workloads = emptyList<TeacherWorkload>()
            database.workloadDao().insertWorkloads(workloads)

            // 5. Initial Grades (Do not pre-fill estimated sample scores; marks are entered exclusively by subject teachers)
            val sampleGrades = emptyList<StudentGrade>()
            database.gradeDao().insertGrades(sampleGrades)

            // 6. Initial Administrative Staff Members (Empty by default - exclusively user-registered)
            val sampleStaff = emptyList<Staff>()
            database.staffDao().insertStaffList(sampleStaff)

            // 7. Initial Role-Based Users for Login Gate (መግቢያ በር)
            val defaultUsers = listOf(
                UserAccount(
                    fullName = "ሲስተም አድሚን (ታደሰ ምስጋናው)",
                    email = "tadessemisganaw4@gmail.com",
                    phone = "0911999999",
                    password = "tade@2026",
                    role = "ሲስተም አድሚን (Super Admin)"
                ),
                UserAccount(
                    fullName = "Sara Niguse",
                    email = "sara.niguse@dilijent.edu.et",
                    phone = "0960225925",
                    password = "sara@123",
                    role = "ዋና ጸሐፊ / ሬጅስትራር"
                )
            )
            defaultUsers.forEach { database.userAccountDao().insertUser(it) }
        }
    }
}

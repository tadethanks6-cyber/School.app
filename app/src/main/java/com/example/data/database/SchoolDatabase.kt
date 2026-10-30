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

            // 2. Initial Teachers (Ethiopian School Teachers)
            val teachers = listOf(
                Teacher(
                    teacherCode = "T-201",
                    fullName = "አቶ ሙሉጌታ ታከለ",
                    title = "አቶ",
                    gender = "ወንድ",
                    phone = "0911223344",
                    email = "mulugeta.t@ethioschool.edu.et",
                    qualification = "ባችለር ዲግሪ (BSc)",
                    specialization = "ሂሳብ (Mathematics)",
                    isHomeroomTeacher = true,
                    homeroomClass = "9ኛ ክፍል-A"
                ),
                Teacher(
                    teacherCode = "T-202",
                    fullName = "ወ/ሮ ትእግስት አስፋው",
                    title = "ወ/ሮ",
                    gender = "ሴት",
                    phone = "0922334455",
                    email = "tsega.a@ethioschool.edu.et",
                    qualification = "ማስተርስ (MA)",
                    specialization = "እንግሊዝኛ (English)",
                    isHomeroomTeacher = true,
                    homeroomClass = "9ኛ ክፍል-B"
                ),
                Teacher(
                    teacherCode = "T-203",
                    fullName = "አቶ ቴዎድሮስ ግርማ",
                    title = "አቶ",
                    gender = "ወንድ",
                    phone = "0933445566",
                    email = "tedros.g@ethioschool.edu.et",
                    qualification = "ባችለር ዲግሪ (BSc)",
                    specialization = "ፊዚክስ (Physics)",
                    isHomeroomTeacher = false,
                    homeroomClass = ""
                ),
                Teacher(
                    teacherCode = "T-204",
                    fullName = "ወ/ሪት ቤተልሔም ወርቁ",
                    title = "ወ/ሪት",
                    gender = "ሴት",
                    phone = "0944556677",
                    email = "betelhem.w@ethioschool.edu.et",
                    qualification = "ባችለር ዲግሪ (BSc)",
                    specialization = "ባዮሎጂ (Biology)",
                    isHomeroomTeacher = false,
                    homeroomClass = ""
                ),
                Teacher(
                    teacherCode = "T-205",
                    fullName = "አቶ ደረጀ በቀለ",
                    title = "አቶ",
                    gender = "ወንድ",
                    phone = "0955667788",
                    email = "dereje.b@ethioschool.edu.et",
                    qualification = "ባችለር ዲግሪ (BA)",
                    specialization = "አማርኛ ቋንቋ",
                    isHomeroomTeacher = true,
                    homeroomClass = "10ኛ ክፍል-A"
                )
            )
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
            database.studentDao().insertStudents(students)

            // 4. Initial Teacher Workload Allotments (የሥራ ክፍፍል)
            val workloads = listOf(
                TeacherWorkload(
                    teacherId = 1,
                    teacherName = "አቶ ሙሉጌታ ታከለ",
                    subjectId = 3,
                    subjectName = "ሂሳብ (Mathematics)",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    periodsPerWeek = 5,
                    daysAllotted = "ሰኞ፣ ማክሰኞ፣ ረቡዕ፣ ሐሙስ፣ አርብ"
                ),
                TeacherWorkload(
                    teacherId = 1,
                    teacherName = "አቶ ሙሉጌታ ታከለ",
                    subjectId = 3,
                    subjectName = "ሂሳብ (Mathematics)",
                    grade = "9ኛ ክፍል",
                    section = "B",
                    periodsPerWeek = 5,
                    daysAllotted = "ሰኞ፣ ማክሰኞ፣ ረቡዕ፣ ሐሙስ፣ አርብ"
                ),
                TeacherWorkload(
                    teacherId = 2,
                    teacherName = "ወ/ሮ ትእግስት አስፋው",
                    subjectId = 2,
                    subjectName = "እንግሊዝኛ (English)",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    periodsPerWeek = 5,
                    daysAllotted = "ሰኞ፣ ማክሰኞ፣ ረቡዕ፣ ሐሙስ፣ አርብ"
                ),
                TeacherWorkload(
                    teacherId = 3,
                    teacherName = "አቶ ቴዎድሮስ ግርማ",
                    subjectId = 4,
                    subjectName = "ፊዚክስ (Physics)",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    periodsPerWeek = 4,
                    daysAllotted = "ሰኞ፣ ረቡዕ፣ ሐሙስ፣ አርብ"
                ),
                TeacherWorkload(
                    teacherId = 4,
                    teacherName = "ወ/ሪት ቤተልሔም ወርቁ",
                    subjectId = 6,
                    subjectName = "ባዮሎጂ (Biology)",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    periodsPerWeek = 4,
                    daysAllotted = "ማክሰኞ፣ ረቡዕ፣ ሐሙስ፣ አርብ"
                )
            )
            database.workloadDao().insertWorkloads(workloads)

            // 5. Initial Sample Grades
            val sampleGrades = listOf(
                StudentGrade(
                    studentId = 1,
                    studentName = "ዮናስ አለሙ ከበደ",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    subjectName = "ሂሳብ (Mathematics)",
                    quizScore = 9f,
                    assignmentScore = 9.5f,
                    midExamScore = 27f,
                    finalExamScore = 46f,
                    totalScore = 91.5f,
                    letterGrade = "A",
                    remarks = "በጣም ከፍተኛ ውጤት"
                ),
                StudentGrade(
                    studentId = 1,
                    studentName = "ዮናስ አለሙ ከበደ",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    subjectName = "እንግሊዝኛ (English)",
                    quizScore = 8f,
                    assignmentScore = 8.5f,
                    midExamScore = 25f,
                    finalExamScore = 44f,
                    totalScore = 85.5f,
                    letterGrade = "A",
                    remarks = "ጥሩ ውጤት"
                ),
                StudentGrade(
                    studentId = 2,
                    studentName = "ሄለን ታደሰ ብርሃኑ",
                    grade = "9ኛ ክፍል",
                    section = "A",
                    subjectName = "ሂሳብ (Mathematics)",
                    quizScore = 8.5f,
                    assignmentScore = 9f,
                    midExamScore = 28f,
                    finalExamScore = 48f,
                    totalScore = 93.5f,
                    letterGrade = "A",
                    remarks = "የክፍሉ ቀዳሚ"
                )
            )
            database.gradeDao().insertGrades(sampleGrades)

            // 6. Initial Administrative Staff Members (የአስተዳደር ሰራተኞች)
            val sampleStaff = listOf(
                Staff(
                    staffCode = "ADM-001",
                    fullName = "ዶ/ር ተስፋዬ ገብረመድህን",
                    title = "ዶ/ር",
                    role = "ርዕሰ መምህር",
                    department = "አስተዳደር",
                    phone = "0911000000",
                    email = "principal@dilijentschool.edu.et",
                    gender = "ወንድ",
                    qualification = "ዶክትሬት (PhD)",
                    employmentType = "ቋሚ",
                    salary = "28,000 ብር",
                    hiringDate = "2012 ዓ.ም",
                    responsibilities = "የትምህርት ቤቱን አጠቃላይ የአስተዳደር፣ የአካዳሚክ እና የልማት ስራዎችን በበላይነት መምራት፤ ከወላጆችና ከትምህርት ቢሮ ጋር ግንኙነት ማድረግ።"
                ),
                Staff(
                    staffCode = "ADM-002",
                    fullName = "ወ/ሮ መስከረም በቀለ",
                    title = "ወ/ሮ",
                    role = "ምክትል ርዕሰ መምህር",
                    department = "አስተዳደር",
                    phone = "0911223311",
                    email = "v.principal@dilijentschool.edu.et",
                    gender = "ሴት",
                    qualification = "ማስተርስ (MA)",
                    employmentType = "ቋሚ",
                    salary = "22,000 ብር",
                    hiringDate = "2014 ዓ.ም",
                    responsibilities = "የመምህራን እና የተማሪዎች የዕለት ተዕለት ስነ-ስርዓት፣ የአካዳሚክ መርሃ-ግብር አፈፃፀም እና የመማር ማስተማር ክትትል ማድረግ።"
                ),
                Staff(
                    staffCode = "ADM-003",
                    fullName = "ወ/ሪት ትዕግስት ወርቁ",
                    title = "ወ/ሪት",
                    role = "ዋና ጸሐፊ",
                    department = "አስተዳደር",
                    phone = "0912334455",
                    email = "secretary@dilijentschool.edu.et",
                    gender = "ሴት",
                    qualification = "ዲፕሎማ",
                    employmentType = "ቋሚ",
                    salary = "11,500 ብር",
                    hiringDate = "2016 ዓ.ም",
                    responsibilities = "ደብዳቤዎችን መጻፍ፣ የተማሪዎች እና የመምህራን ፋይል አያያዝ፣ የጽህፈት መሳሪያዎች ጥያቄ መቀበል እና የስልክ ጥሪዎችን ማስተናገድ።"
                ),
                Staff(
                    staffCode = "ADM-004",
                    fullName = "አቶ ዳዊት ብርሃኑ",
                    title = "አቶ",
                    role = "ፋይናንስ / ሂሳብ ሹም",
                    department = "ፋይናንስ እና ንብረት",
                    phone = "0913445566",
                    email = "finance@dilijentschool.edu.et",
                    gender = "ወንድ",
                    qualification = "የመጀመሪያ ዲግሪ (BA)",
                    employmentType = "ቋሚ",
                    salary = "16,000 ብር",
                    hiringDate = "2015 ዓ.ም",
                    responsibilities = "የወርሃዊ የትምህርት ቤት ክፍያ መሰብሰብ፣ የደመወዝ ክፍያ ማዘጋጀት፣ የበጀት እቅድ እና የፋይናንስ ሪፖርቶችን ማዘጋጀት።"
                ),
                Staff(
                    staffCode = "ADM-005",
                    fullName = "አቶ ግርማ አየለ",
                    title = "አቶ",
                    role = "ንብረት ክፍል ኃላፊ",
                    department = "ፋይናንስ እና ንብረት",
                    phone = "0914556677",
                    email = "property@dilijentschool.edu.et",
                    gender = "ወንድ",
                    qualification = "ዲፕሎማ",
                    employmentType = "ቋሚ",
                    salary = "12,000 ብር",
                    hiringDate = "2016 ዓ.ም",
                    responsibilities = "የትምህርት ቤቱን ቋሚ እና አላቂ ንብረቶች መመዝገብ፣ ማከፋፈል፣ አመታዊ የንብረት ቆጠራ ማካሄድ እና የጥገና ፍላጎቶችን መከታተል።"
                ),
                Staff(
                    staffCode = "ADM-006",
                    fullName = "ወ/ሮ ዓለሚቱ ታደሰ",
                    title = "ወ/ሮ",
                    role = "የፅዳት ሰራተኛ",
                    department = "አገልግሎትና ደህንነት",
                    phone = "0915667788",
                    email = "",
                    gender = "ሴት",
                    qualification = "10ኛ ያጠናቀቀች",
                    employmentType = "ቋሚ",
                    salary = "7,500 ብር",
                    hiringDate = "2017 ዓ.ም",
                    responsibilities = "የመማሪያ ክፍሎችን፣ የቢሮዎችን፣ የመጸዳጃ ቤቶችን እና የግቢውን ንጽህና በየቀኑ መጠበቅ።"
                ),
                Staff(
                    staffCode = "ADM-007",
                    fullName = "አቶ በላይነህ ጥላሁን",
                    title = "አቶ",
                    role = "የጥበቃ ኃላፊ",
                    department = "አገልግሎትና ደህንነት",
                    phone = "0916778899",
                    email = "",
                    gender = "ወንድ",
                    qualification = "ሰርተፍኬት",
                    employmentType = "ቋሚ",
                    salary = "8,000 ብር",
                    hiringDate = "2015 ዓ.ም",
                    responsibilities = "የትምህርት ቤቱን ግቢ ደህንነት መጠበቅ፣ የተማሪዎችን እና የእንግዶችን መግቢያና መውጫ መቆጣጠር።"
                )
            )
            database.staffDao().insertStaffList(sampleStaff)

            // 7. Initial Role-Based Users for Login Gate (መግቢያ በር)
            val defaultUsers = listOf(
                UserAccount(
                    fullName = "ሲስተም አድሚን (እኔ)",
                    email = "admin@dilijent.edu.et",
                    phone = "0900000000",
                    password = "admin123",
                    role = "ሲስተም አድሚን (Super Admin)"
                ),
                UserAccount(
                    fullName = "ዶ/ር ተስፋዬ ገብረመድህን (ርዕሰ መምህር)",
                    email = "principal@dilijent.edu.et",
                    phone = "0911000000",
                    password = "1234",
                    role = "ርዕሰ መምህር (Principal)"
                ),
                UserAccount(
                    fullName = "አቶ ከበደ ለማ",
                    phone = "0918000000",
                    password = "1234",
                    role = "ምክትል ርዕሰ መምህር"
                ),
                UserAccount(
                    fullName = "አቶ ሙሉጌታ ታከለ",
                    phone = "0911223344",
                    password = "1234",
                    role = "መምህር (Teacher)"
                ),
                UserAccount(
                    fullName = "ወ/ሮ ትእግስት አስፋው",
                    phone = "0922334455",
                    password = "1234",
                    role = "መምህር (Teacher)"
                ),
                UserAccount(
                    fullName = "ዮናስ አለሙ (ተማሪ)",
                    phone = "0911002233",
                    password = "1234",
                    role = "ተማሪ (Student)"
                ),
                UserAccount(
                    fullName = "አቶ ዮናስ ብርሃኑ",
                    phone = "0913445566",
                    password = "1234",
                    role = "ፋይናንስ / ሂሳብ ሹም"
                ),
                UserAccount(
                    fullName = "ወ/ሪት ሰላማዊት አበበ",
                    phone = "0912334455",
                    password = "1234",
                    role = "ዋና ጸሐፊ"
                ),
                UserAccount(
                    fullName = "አቶ ግርማ አየለ",
                    phone = "0914556677",
                    password = "1234",
                    role = "ንብረት ክፍል ኃላፊ"
                ),
                UserAccount(
                    fullName = "ወ/ሮ ዓለሚቱ ታደሰ",
                    phone = "0915667788",
                    password = "1234",
                    role = "የፅዳት ሰራተኛ"
                ),
                UserAccount(
                    fullName = "አቶ በላይነህ ጥላሁን",
                    phone = "0916778899",
                    password = "1234",
                    role = "የጥበቃ ኃላፊ"
                )
            )
            defaultUsers.forEach { database.userAccountDao().insertUser(it) }
        }
    }
}

package com.example

import com.example.data.model.StudentGrade
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testScoreAndLetterGradeComputation() {
        val quiz = 9f
        val assignment = 9.5f
        val midExam = 27f
        val finalExam = 46f
        val total = quiz + assignment + midExam + finalExam
        assertEquals(91.5f, total, 0.01f)

        val letter = when {
            total >= 90 -> "A"
            total >= 80 -> "B"
            total >= 65 -> "C"
            total >= 50 -> "D"
            else -> "F"
        }
        assertEquals("A", letter)
    }

    @Test
    fun testTeacherWeeklyPeriodsSum() {
        val periodsList = listOf(5, 5, 4, 4, 2)
        val sum = periodsList.sum()
        assertEquals(20, sum)
    }
}

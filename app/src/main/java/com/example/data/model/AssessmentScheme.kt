package com.example.data.model

data class AssessmentComponent(
    val id: String = "c1",
    val label: String = "ምዘና",
    val weight: Float = 10f
)

data class AssessmentScheme(
    val id: String = "moe_standard",
    val titleAmharic: String = "መደበኛ የመንግስት ስርዓተ-ትምህርት",
    val titleEnglish: String = "Standard MOE Curriculum",
    val schoolType: String = "የመንግስት", // "የመንግስት", "የግል"
    val test1Label: String = "ጥያቄ/ፈተና 1",
    val test1Weight: Float = 10f,
    val assignmentLabel: String = "የቤት ስራ/ፕሮጀክት",
    val assignmentWeight: Float = 10f,
    val midExamLabel: String = "አጋማሽ ፈተና (Mid)",
    val midExamWeight: Float = 30f,
    val finalExamLabel: String = "ማጠቃለያ ፈተና (Final)",
    val finalExamWeight: Float = 50f,
    val components: List<AssessmentComponent> = listOf(
        AssessmentComponent("c1", test1Label, test1Weight),
        AssessmentComponent("c2", assignmentLabel, assignmentWeight),
        AssessmentComponent("c3", midExamLabel, midExamWeight),
        AssessmentComponent("c4", finalExamLabel, finalExamWeight)
    )
) {
    val totalWeight: Float
        get() = if (components.isNotEmpty()) components.sumOf { it.weight.toDouble() }.toFloat() else (test1Weight + assignmentWeight + midExamWeight + finalExamWeight)

    val isValid: Boolean
        get() = (totalWeight in 99.9f..100.1f)

    companion object {
        val MOE_STANDARD = AssessmentScheme(
            id = "moe_standard",
            titleAmharic = "መደበኛ የመንግስት ስርዓተ-ትምህርት (10% + 10% + 30% + 50%)",
            titleEnglish = "Standard Public School (10/10/30/50)",
            schoolType = "የመንግስት",
            test1Label = "ጥያቄ/ፈተና 1 (Quiz)",
            test1Weight = 10f,
            assignmentLabel = "ምደባ/የቤት ስራ (Assign)",
            assignmentWeight = 10f,
            midExamLabel = "አጋማሽ ፈተና (Mid)",
            midExamWeight = 30f,
            finalExamLabel = "ማጠቃለያ ፈተና (Final)",
            finalExamWeight = 50f,
            components = listOf(
                AssessmentComponent("c1", "ጥያቄ/ፈተና 1 (Quiz)", 10f),
                AssessmentComponent("c2", "ምደባ/የቤት ስራ (Assign)", 10f),
                AssessmentComponent("c3", "አጋማሽ ፈተና (Mid)", 30f),
                AssessmentComponent("c4", "ማጠቃለያ ፈተና (Final)", 50f)
            )
        )

        val MULTI_MODULAR = AssessmentScheme(
            id = "multi_modular",
            titleAmharic = "ባለ ብዙ ምዘና ሞዴል (Test 1, 2, 3, Groupw, CW, CP, Final)",
            titleEnglish = "Modular Continuous Model (Test 1-3, Groupw, CW, CP, Final)",
            schoolType = "የግል",
            test1Label = "ፈተና 1 (Test 1)",
            test1Weight = 10f,
            assignmentLabel = "የቡድን ስራ (Groupw)",
            assignmentWeight = 10f,
            midExamLabel = "የክፍል ስራ (CW)",
            midExamWeight = 10f,
            finalExamLabel = "ማጠቃለያ ፈተና (Final)",
            finalExamWeight = 40f,
            components = listOf(
                AssessmentComponent("c1", "ፈተና 1 (Test 1)", 10f),
                AssessmentComponent("c2", "ፈተና 2 (Test 2)", 10f),
                AssessmentComponent("c3", "ፈተና 3 (Test 3)", 10f),
                AssessmentComponent("c4", "የቡድን ስራ (Groupw)", 10f),
                AssessmentComponent("c5", "የክፍል ስራ (CW)", 10f),
                AssessmentComponent("c6", "ተሳትፎ (CP)", 10f),
                AssessmentComponent("c7", "ማጠቃለያ ፈተና (Final)", 40f)
            )
        )

        val PRIVATE_MODEL_A = AssessmentScheme(
            id = "private_model_a",
            titleAmharic = "የግል ት/ቤት አማራጭ 1 - Continuous (15% + 15% + 10% + 60%)",
            titleEnglish = "Private School Model A (Continuous 40% + Final 60%)",
            schoolType = "የግል",
            test1Label = "ፈተና/ኩዊዝ (Quiz)",
            test1Weight = 15f,
            assignmentLabel = "የክፍል ስራ/ሆምወርክ (HW)",
            assignmentWeight = 15f,
            midExamLabel = "የወር ምዘና (Mid)",
            midExamWeight = 10f,
            finalExamLabel = "ማጠቃለያ ፈተና (Final)",
            finalExamWeight = 60f,
            components = listOf(
                AssessmentComponent("c1", "ፈተና/ኩዊዝ (Quiz)", 15f),
                AssessmentComponent("c2", "የክፍል ስራ/ሆምወርክ (HW)", 15f),
                AssessmentComponent("c3", "የወር ምዘና (Mid)", 10f),
                AssessmentComponent("c4", "ማጠቃለያ ፈተና (Final)", 60f)
            )
        )

        val PRIVATE_MODEL_B = AssessmentScheme(
            id = "private_model_b",
            titleAmharic = "የግል ት/ቤት አማራጭ 2 - ሚዛናዊ (15% + 15% + 20% + 50%)",
            titleEnglish = "Private School Model B (Balanced 50/50)",
            schoolType = "የግል",
            test1Label = "ኩዊዝ/ፈተና (Quiz)",
            test1Weight = 15f,
            assignmentLabel = "ፕሮጀክት/የቤት ስራ (Project)",
            assignmentWeight = 15f,
            midExamLabel = "አጋማሽ ፈተና (Mid Exam)",
            midExamWeight = 20f,
            finalExamLabel = "ማጠቃለያ ፈተና (Final Exam)",
            finalExamWeight = 50f,
            components = listOf(
                AssessmentComponent("c1", "ኩዊዝ/ፈተና (Quiz)", 15f),
                AssessmentComponent("c2", "ፕሮጀክት/የቤት ስራ (Project)", 15f),
                AssessmentComponent("c3", "አጋማሽ ፈተና (Mid Exam)", 20f),
                AssessmentComponent("c4", "ማጠቃለያ ፈተና (Final Exam)", 50f)
            )
        )

        val PRIVATE_MODEL_C = AssessmentScheme(
            id = "private_model_c",
            titleAmharic = "የግል ት/ቤት አማራጭ 3 - ፈተና ተኮር (Mid 30% + Final 70%)",
            titleEnglish = "Private School Model C (Mid 30% + Final 70%)",
            schoolType = "የግል",
            test1Label = "ተከታታይ ምዘና 1",
            test1Weight = 0f,
            assignmentLabel = "ተከታታይ ምዘና 2",
            assignmentWeight = 0f,
            midExamLabel = "አጋማሽ ፈተና (Mid)",
            midExamWeight = 30f,
            finalExamLabel = "ማጠቃለያ ፈተና (Final)",
            finalExamWeight = 70f,
            components = listOf(
                AssessmentComponent("c1", "አጋማሽ ፈተና (Mid)", 30f),
                AssessmentComponent("c2", "ማጠቃለያ ፈተና (Final)", 70f)
            )
        )

        val PRESETS = listOf(MOE_STANDARD, MULTI_MODULAR, PRIVATE_MODEL_A, PRIVATE_MODEL_B, PRIVATE_MODEL_C)
    }
}

val TEACHERS = arrayListOf<String>(
    "Ведерников",
    "Корнеев",
    "Кравцов",
    "Меньшутин",
    "Юрченко"
)

val TEACHERS_FULL_NAMES = arrayListOf<String>(
    "Ведерников Николай Викторович",
    "Корнеев Георгий Александрович",
    "Кравцов Никита Олегович",
    "Меньшутин Алексей Сергеевич",
    "Юрченко Артем Олегович"
)
fun getTeacher(str : String) : Int {
    val index = str.toIntOrNull()
    return when (index) {
        null -> TEACHERS.indexOf(str)
        else -> if (index >= 0 && index < TEACHERS.size) index else -1
    }
}

fun getTeacherName(index: Int) : String {
    return TEACHERS_FULL_NAMES[index]
}

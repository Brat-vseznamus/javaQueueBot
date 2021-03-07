suspend fun main(vararg args: String) {
    val db = DB()
    db.addUser("@Quicksmart", "Вихнин Фёдор Алексеевич")
    println(db.getUsers().toString())
}
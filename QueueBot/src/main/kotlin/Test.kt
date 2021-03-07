suspend fun main(vararg args: String) {
    val db = DB()
//    db.addUser("a", "b")
    println(db.getUsers().toString())
}
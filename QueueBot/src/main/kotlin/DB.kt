import java.io.FileInputStream
import java.sql.*
import java.util.*

private const val USER_TABLE_NAME = "users"
private const val DB_PROPERTIES_PATH = "QueueBot/build/resources/main/db.properties"

class DB {
    private lateinit var connection: Connection
    private val path : String
    private val name : String

    init {
        val properties = Properties()
        properties.load(FileInputStream(DB_PROPERTIES_PATH))
        path = properties.getProperty("db.path")
        name = properties.getProperty("db.name")

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:$path")
            createUserTable(connection)
        } catch (e: SQLException) {
            println(e.message)
            e.printStackTrace()
        }
    }

    private fun createUserTable(connection: Connection) {
        val stmt = connection.createStatement()
        stmt.execute("""
        CREATE TABLE IF NOT EXISTS ${USER_TABLE_NAME} (
            usertag TEXT NOT NULL UNIQUE,
            userfullname TEXT NOT NULL UNIQUE
        )
        """.trimIndent())
    }


    fun containsUser(userTag : String) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE usertag = $userTag")
        return rs.next()
    }

    fun addUser(userTag : String, userName : String) {
        val stmt = connection.createStatement()
        if (!containsUser(userTag)) {
            stmt.execute("INSERT INTO $USER_TABLE_NAME (usertag, userfullname) VALUES ($userTag, $userName)")
        } else {
            stmt.executeUpdate("UPDATE $USER_TABLE_NAME SET usertag = $userTag WHERE userfullname = $userName")
        }
        stmt.close()
    }

    fun getUsers() : MutableList<Int> {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME")
        var list = mutableListOf<Int>()
        while (rs.next()) {
            list.add(rs.getInt("user"))
        }
        return list
    }

//    val tmpUserMap = mutableMapOf<String, String>(
//        "@Quicksmart" to "Будущев Матвей Ярославович")
//
//    fun getName(usertag : String) : String? {
//        //TODO
//        return tmpUserMap[usertag]
//    }

}

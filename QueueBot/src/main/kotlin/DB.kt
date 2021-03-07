import java.io.FileInputStream
import java.sql.*
import java.util.*

private const val USER_TABLE_NAME = "users"
private const val DB_PROPERTIES_PATH = "build/resources/main/db.properties"

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
            USERTAG TEXT NOT NULL UNIQUE,
            USERNAME TEXT NOT NULL UNIQUE
        )
        """.trimIndent())
    }


    private fun containsUser(userTag : String) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERTAG = \'$userTag\'")
        return rs.next()
    }

    fun addUser(userTag : String, userName : String) {
        val stmt = connection.createStatement()
        if (!containsUser(userTag)) {
            stmt.execute("INSERT INTO $USER_TABLE_NAME (USERTAG, USERNAME) VALUES (\'$userTag\', \'$userName\')")
        } else {
            stmt.executeUpdate("UPDATE $USER_TABLE_NAME SET USERNAME = \'$userName\' WHERE USERTAG = \'$userTag\'")
        }
        stmt.close()
    }

    fun getUserName(userTag: String) : String? {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERTAG = \'$userTag\'")
        if (rs.next()) {
            return rs.getString("USERNAME")
        }
        return null
    }

    fun getUsers() : MutableList<String> {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME")
        var list = mutableListOf<String>()
        while (rs.next()) {
            list.add(rs.getString("USERNAME"))
        }
        return list
    }

}

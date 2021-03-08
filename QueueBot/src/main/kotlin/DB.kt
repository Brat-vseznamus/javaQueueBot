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
            USERNAME TEXT NOT NULL UNIQUE,
            CHATID INTEGER NOT NULL,
            MUTE INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent())
    }


    private fun containsUser(userTag : String) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERTAG = \'$userTag\'")
        return rs.next()
    }

    fun containsUserName(userName: String) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERNAME = \'$userName\'")
        return rs.next()
    }

    fun addUser(userTag : String, userName : String) {
        addUser(userTag, userName, 0)
    }

    fun addUser(userTag : String, userName : String, userChatId : Int) {
        val stmt = connection.createStatement()
        if (!containsUser(userTag)) {
            stmt.execute("INSERT INTO $USER_TABLE_NAME (USERTAG, USERNAME, CHATID) VALUES (\'$userTag\', \'$userName\', $userChatId)")
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

    fun getUserTag(userName: String) : String? {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERNAME = \'$userName\'")
        if (rs.next()) {
            return rs.getString("USERTAG")
        }
        return null
    }

    fun getChatIdByTag(userTag: String) : Int {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERTAG = \'$userTag\'")
        if (rs.next()) {
            return rs.getInt("CHATID")
        }
        return -1
    }

    fun getChatIdByName(userName: String) : Int {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERNAME = \'$userName\'")
        if (rs.next()) {
            return rs.getInt("CHATID")
        }
        return -1
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

    fun getUserByProperty(propertyName : String, propertyValue : Any) : User? {
        val stmt = connection.createStatement()
        var request = "SELECT * FROM $USER_TABLE_NAME WHERE %s = %s"
        val value = when (propertyValue) {
            is String -> "\'$propertyValue\'"
            is Int -> "$propertyValue"
            else -> ""
        }
        request = String.format(request, propertyName, value)
        val rs : ResultSet = stmt.executeQuery(request)
        if (rs.next()) {
            return User(
                rs.getString("USERTAG"),
                rs.getString("USERNAME"),
                rs.getInt("CHATID"),
                rs.getInt("MUTE")
            )
        } else {
            return null
        }
    }

    fun updateMuteStatus(userTag: String, muteStatus : Boolean) : Boolean{
        val stmt = connection.createStatement()
        val newMuteStatus = if (muteStatus) 1 else 0
        var status = false
        if (containsUser(userTag)) {
            stmt.executeUpdate("UPDATE $USER_TABLE_NAME SET MUTE = $newMuteStatus WHERE USERTAG = \'$userTag\'")
            status = true
        }
        stmt.close()
        return status
    }

//    fun updateUserByProperty(propertyName : String, propertyValue : Any) : User? {
//        val stmt = connection.createStatement()
//        var request = "SELECT * FROM $USER_TABLE_NAME WHERE %s = %s"
//        val value = when (propertyValue) {
//            is String -> "\'$propertyValue\'"
//            is Int -> "$propertyValue"
//            else -> ""
//        }
//        request = String.format(request, propertyName, value)
//        val rs : ResultSet = stmt.executeQuery(request)
//        if (rs.next()) {
//            return User(
//                rs.getString("USERTAG"),
//                rs.getString("USERNAME"),
//                rs.getInt("CHATID"),
//                rs.getInt("MUTE")
//            )
//        } else {
//            return null
//        }
//    }

    fun getUserByName(userName : String) : User? {
        return getUserByProperty("USERNAME", userName)
    }

}

data class User(
    val tag : String,
    val name : String,
    val chatId : Int,
    val mute : Int)

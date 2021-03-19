import java.io.File
import java.io.FileInputStream
import java.sql.*
import java.util.*

private const val USER_TABLE_NAME = "users"
private const val NOTIFICATION_TABLE_NAME = "notifications"
private const val DB_PROPERTIES_PATH = "build/resources/main/db.properties"
//private const val DB_PROPERTIES_PATH = "QueueBot/src/main/resources/db.properties"

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
            createNotificationTable(connection)
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

    private fun createNotificationTable(connection: Connection) {
        val stmt = connection.createStatement()
        stmt.execute("""
        CREATE TABLE IF NOT EXISTS ${NOTIFICATION_TABLE_NAME} (
            USERTAG TEXT NOT NULL,
            NTIME INTEGER NOT NULL
        )
        """.trimIndent())
    }

    fun getTimesOf(usertag : String) : List<Int> {
        val times = mutableListOf<Int>()
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $NOTIFICATION_TABLE_NAME WHERE USERTAG = \'$usertag\'")
        while (rs.next()) {
            times.add(rs.getInt("NTIME"))
        }
        return times
    }

    fun addTime(userTag : String, time : Int) {
        val stmt = connection.createStatement()
        if (!containsUserAndTime(userTag, NOTIFICATION_TABLE_NAME, time)) {
            stmt.execute("INSERT INTO $NOTIFICATION_TABLE_NAME (USERTAG, NTIME) VALUES (\'$userTag\', $time)")
        }
        stmt.close()
    }

    fun deleteTime(userTag : String, time : Int) {
        val stmt = connection.createStatement()
        if (containsUserAndTime(userTag, NOTIFICATION_TABLE_NAME, time)) {
            stmt.execute("DELETE FROM $NOTIFICATION_TABLE_NAME WHERE USERTAG = \'$userTag\' AND NTIME = $time")
        }
        stmt.close()
    }

    fun deleteAllTimes(userTag : String) {
        val stmt = connection.createStatement()
        if (containsUser(userTag, NOTIFICATION_TABLE_NAME)) {
            stmt.execute("DELETE FROM $NOTIFICATION_TABLE_NAME WHERE USERTAG = \'$userTag\'")
        }
        stmt.close()
    }


    private fun containsUser(userTag : String) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME WHERE USERTAG = \'$userTag\'")
        return rs.next()
    }

    private fun containsUser(userTag : String, table : String) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $table WHERE USERTAG = \'$userTag\'")
        return rs.next()
    }

    private fun containsUserAndTime(userTag : String, table : String, time : Int) : Boolean {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $table WHERE USERTAG = \'$userTag\' AND NTIME = $time")
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

    fun getAllUsers() : MutableList<MutableList<String>> {
        val stmt = connection.createStatement()
        val rs : ResultSet = stmt.executeQuery("SELECT * FROM $USER_TABLE_NAME")
        var list = mutableListOf<MutableList<String>>()
        while (rs.next()) {
            list.add(
                mutableListOf(
                    rs.getString("USERTAG"),
                    rs.getString("USERNAME"),
                    rs.getInt("CHATID").toString(),
                    rs.getInt("MUTE").toString()
                    ));
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

    fun printTable(file : File) {
        val format = "| %-20s| %-40s| %-10s| %-4s |\n"
        var text = ""
        text += String.format("__%-20s__%-40s__%-10s__%-4s__\n", "_".repeat(20), "_".repeat(40), "_".repeat(10), "_".repeat(4))
        text += String.format(format, "USERTAG", "USERNAME", "CHATID", "MUTE")
        text += String.format("|-%-20s|-%-40s|-%-10s|-%4s-|\n", "-".repeat(20), "-".repeat(40), "-".repeat(10), "-".repeat(4))
        val tb = getAllUsers();
        for (row in tb) {
            text += String.format(format, row[0], row[1], row[2], row[3])
        }
        text += String.format("|_%-20s|_%-40s|_%-10s|_%-4s_|", "_".repeat(20), "_".repeat(40), "_".repeat(10), "_".repeat(4))
        file.writeText(text)
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

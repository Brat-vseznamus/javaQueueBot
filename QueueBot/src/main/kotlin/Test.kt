import kotlinx.coroutines.delay
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

suspend fun main(vararg args: String) {
    println("we are testing here")

//    val cmd = "gradle run"
//    val a = Runtime.getRuntime().exec(cmd)
////    a.exitValue()
////    a.destroy()
////    val input = a.inputStream
////    val inA = BufferedReader(InputStreamReader(input))
////    var line : String? = null
////    while (inA.readLine().also { line = it } != null) {
////        println(line)
////    }
//
////    if (a.isAlive) {
////        a.destroy()
////        println("!was alive")
////    } else {
////        println("!not alive")
////    }
//    delay(10000L)
//    a.destroy()
    val myObj = File("QueueBot/src/main/resources/filename.txt")
    if (myObj.createNewFile()) {
        println("File created: " + myObj.getName())
    } else {
        println("File already exists.")
    }
    val db2 = DB();
    val format = "| %-20s| %-40s| %-10s| %-4s |\n"
    var text = ""
    text += String.format("__%-20s__%-40s__%-10s__%-4s__\n", "_".repeat(20), "_".repeat(40), "_".repeat(10), "_".repeat(4))
//    println(String.format("__%-20s__%-40s__%-10s__%-4s__", "_".repeat(20), "_".repeat(40), "_".repeat(10), "_".repeat(4)))
    text += String.format(format, "USERTAG", "USERNAME", "CHATID", "MUTE")
//    println(String.format(format, "USERTAG", "USERNAME", "CHATID", "MUTE"));

//    println(String.format("| %-10s| %-10s| %-10s|", "Title1", "Title2", "Title3"))
//    println(String.format("|-%-10s|-%-10s|-%-10s|", "-".repeat(10), "-".repeat(10), "-".repeat(10)))
    text += String.format("|-%-20s|-%-40s|-%-10s|-%4s-|\n", "-".repeat(20), "-".repeat(40), "-".repeat(10), "-".repeat(4))
    val tb = db2.getAllUsers();
    for (row in tb) {
        text += String.format(format, row[0], row[1], row[2], row[3])
    }
    text += String.format("|_%-20s|_%-40s|_%-10s|_%-4s_|", "_".repeat(20), "_".repeat(40), "_".repeat(10), "_".repeat(4))
    myObj.writeText(text)

}
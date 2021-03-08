import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*


internal val db : DB = DB()
fun sendMessage(chatId : Int, text : String, botToken : String) {
    var urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s"
    urlString = String.format(urlString, botToken, chatId.toString(), text)
    val conn = URL(urlString).openConnection()
    val inputStream = BufferedInputStream(conn.getInputStream())
    val br = BufferedReader(InputStreamReader(inputStream))
    val response = br.readText()
}

@OptIn(PreviewFeature::class)
suspend fun main(vararg args: String) {
    val botproperties = Properties();
    botproperties.load(FileInputStream("build/resources/main/botInfo.properties"))
    val botToken = botproperties.getProperty("botInfo.token")
    val bot = telegramBot(botToken)
    println(bot.getMe())
    println(db.getUsers())
    val scope = CoroutineScope(Dispatchers.Default)
    //var javatb = Table.JAVATable()
    fun getTb() = SheetsQuickstart.getQueues("1EmM8619VtPPd5svGF-vuXNVDf6vImsucU3GTXwUi9NE", "Лист1", "A1:D6")
    var table = getTb()?.let { Table(it) }

    bot.buildBehaviour(scope) {
        startCommand()
        queueCommand()
        findMeCommand()
        setNameCommand()
        muteCommand()
        helpCommand()
    }

    GlobalScope.launch {
        while (true) {
            delay(2000L)
            val newtb = getTb()?.let { Table(it) }
            if (table != null && newtb != null) {
                val ind = Table.compare(table!!, newtb)
                if (ind != -1) {
                    val queue = newtb.getQueue(ind)
                    if (queue != null && queue.isNotEmpty()) {
                        if (queue[0] != table!!.getQueue(ind)!![0]) {
                            val newHeadName = queue[0]
                            if (db.containsUserName(newHeadName)) {
                                sendMessage(db.getChatIdByName(newHeadName),
                                        "${db.getUserTag(newHeadName)},%0A" +
                                                "You're next to ${getTeacherName(ind)}",
                                            botToken)
                            }
                            table = newtb
                        }
                    }
                }
            }
        }
    }
    scope.coroutineContext[Job]!!.join()
}






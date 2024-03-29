import dev.inmo.tgbotapi.CommonAbstracts.types.ChatRequest
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.requests.abstracts.*
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.requests.send.media.SendDocument
import dev.inmo.tgbotapi.requests.send.media.SendDocumentData
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.StorageFile
import kotlinx.coroutines.*
import java.io.*
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
//    fun getTb() = SheetsQuickstart.getQueues("1EmM8619VtPPd5svGF-vuXNVDf6vImsucU3GTXwUi9NE", "Лист1", "A1:D6")
//    var table = getTb()?.let { Table(it) }

//    db.printTable(File("build/resources/main/filename.txt"))
//    bot.execute(SendDocument(ChatId(0), MultipartFile(StorageFile(file))))


    val getTb : () -> Spreadsheet? = {
        Spreadsheet.JAVATable()
    }

    bot.buildBehaviour(scope) {
        startCommand()
        queueCommand()
        findMeCommand()
        setNameCommand()
        muteCommand()
        helpCommand()
        setNotificationCommand()
        deleteNotificationsCommand()
        linksCommand()
    }

    GlobalScope.launch {
        tableListing(botToken) { getTb() }
    }
    GlobalScope.launch {
        tableListing(botToken) { Spreadsheet.JAVATable(1) }
    }
    scope.coroutineContext[Job]!!.join()
}

suspend fun tableListing(botToken: String, updateTable: () -> Spreadsheet?) {
    var table = updateTable()
    while (true) {
        delay(2000L)
        val newtb = updateTable()
        if (table != null && newtb != null) {
            val difference = table.findDifferencesFrom(newtb)
            if (difference.isNotEmpty()) {
                for (entry in difference) {
                    val person = entry.first
                    val column = entry.second.first
                    val position = entry.second.second
                    val user = db.getUserByName(person)
                    if (user != null && user.mute != 1) {
                        if (position == 0) {
                            sendMessage(user.chatId,
                                "${user.tag},%0A" +
                                        "You're next to ${table.columnsName[column]}",
                                botToken)
                            println("@Bot send message to ${user.tag}")
                        } else {
                            val positions = db.getTimesOf(user.tag)
//                            println(positions)
                            if (positions.isNotEmpty()) {
                                var send = false
                                for (pos in positions) {
                                    if (position + 1 <= pos) {
                                        send = true
                                        break
                                    }
                                }
                                if (send) {
                                    sendMessage(user.chatId,
                                        "${user.tag},%0A" +
                                                "You're ${getNumeral(position + 1)} to ${table.columnsName[column]}",
                                        botToken)
                                    println("@Bot send message to ${user.tag}")
                                }
                            }
                        }
                    }
                }
            }
        }
        table = newtb
    }
}





